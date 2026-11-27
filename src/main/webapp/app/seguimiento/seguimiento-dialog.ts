import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap/modal';
import { TranslatePipe } from '@ngx-translate/core';
import { Observable, finalize } from 'rxjs';

import { Concepto } from 'app/entities/enumerations/concepto.model';
import { EstadoEntrega } from 'app/entities/enumerations/estado-entrega.model';
import { TipoObservacion } from 'app/entities/enumerations/tipo-observacion.model';
import { DataUtils } from 'app/core/util/data-util.service';
import { TranslateDirective } from 'app/shared/language';

import { SeguimientoAlert } from './seguimiento-alert';
import { problemaSeguimiento } from './seguimiento-error';
import { IProblemaSeguimiento } from './seguimiento.model';
import { SeguimientoService } from './seguimiento.service';

export type AccionSeguimiento = 'propuesta' | 'entrega' | 'revision' | 'observacion' | 'evaluacion' | 'avanzar' | 'cancelar';

@Component({
  selector: 'jhi-seguimiento-dialog',
  templateUrl: './seguimiento-dialog.html',
  imports: [ReactiveFormsModule, TranslateDirective, TranslatePipe, SeguimientoAlert],
})
export class SeguimientoDialog {
  readonly guardando = signal(false);
  readonly problema = signal<IProblemaSeguimiento | null>(null);
  readonly errorArchivo = signal<string | null>(null);
  readonly cargandoArchivo = signal(false);
  readonly tamanoMaximoArchivo = 10 * 1024 * 1024;
  readonly conceptos = Object.values(Concepto);
  readonly tipos = Object.values(TipoObservacion);
  readonly estadosRevision = [EstadoEntrega.EN_REVISION, EstadoEntrega.CON_OBSERVACIONES, EstadoEntrega.APROBADA, EstadoEntrega.RECHAZADA];
  readonly form = inject(FormBuilder).nonNullable.group({
    titulo: [''],
    resumen: [''],
    programa: [''],
    descripcion: [''],
    urlDocumento: [''],
    archivo: [''],
    archivoContentType: [''],
    archivoNombre: [''],
    estado: [EstadoEntrega.EN_REVISION],
    contenido: [''],
    tipo: [TipoObservacion.GENERAL],
    calificacion: [3],
    concepto: [Concepto.APROBADO],
    comentarios: [''],
    comentario: [''],
  });
  accion: AccionSeguimiento = 'propuesta';
  proyectoId = 0;
  entregaId = 0;

  private readonly service = inject(SeguimientoService);
  private readonly activeModal = inject(NgbActiveModal);
  private readonly dataUtils = inject(DataUtils);

  get evaluacionIncoherente(): boolean {
    const { calificacion, concepto } = this.form.getRawValue();
    return this.accion === 'evaluacion' && calificacion < 3 !== (concepto === Concepto.REPROBADO);
  }

  preparar(accion: AccionSeguimiento, proyectoId = 0, entregaId = 0): void {
    this.accion = accion;
    this.proyectoId = proyectoId;
    this.entregaId = entregaId;
    const controls = this.form.controls;
    const requerido = [Validators.required, Validators.pattern(/\S/)];
    if (accion === 'propuesta') {
      controls.titulo.setValidators([...requerido, Validators.minLength(10), Validators.maxLength(250)]);
      controls.resumen.setValidators(requerido);
      controls.programa.setValidators([...requerido, Validators.maxLength(120)]);
    }
    if (accion === 'entrega') {
      controls.titulo.setValidators([...requerido, Validators.maxLength(200)]);
      controls.urlDocumento.setValidators([Validators.maxLength(500), Validators.pattern(/^https?:\/\/[^\s]+$/)]);
      this.form.setValidators(() => (controls.archivo.value || controls.urlDocumento.value.trim() ? null : { documentoRequerido: true }));
    }
    if (accion === 'observacion') {
      controls.contenido.setValidators(requerido);
    }
    if (accion === 'evaluacion') {
      controls.calificacion.setValidators([Validators.required, Validators.min(0), Validators.max(5), Validators.pattern(/^\d(?:\.\d)?$/)]);
    }
    controls.comentario.setValidators(accion === 'cancelar' ? [...requerido, Validators.maxLength(500)] : Validators.maxLength(500));
    Object.values(controls).forEach(control => control.updateValueAndValidity());
  }

  cerrar(): void {
    this.activeModal.dismiss();
  }

  seleccionarArchivo(event: Event): void {
    const input = event.target as HTMLInputElement;
    const archivo = input.files?.[0];
    if (!archivo || this.cargandoArchivo()) return;
    this.eliminarArchivo();
    if (archivo.type !== 'application/pdf') {
      this.errorArchivo.set('seguimiento.pdf.tipoInvalido');
    } else if (archivo.size > this.tamanoMaximoArchivo) {
      this.errorArchivo.set('seguimiento.pdf.tamanoExcedido');
    } else if (archivo.name.length > 255) {
      this.errorArchivo.set('seguimiento.pdf.nombreLargo');
    } else {
      this.cargandoArchivo.set(true);
      this.dataUtils
        .loadFileToForm(event, this.form, 'archivo', false)
        .pipe(finalize(() => this.cargandoArchivo.set(false)))
        .subscribe({
          next: () => this.form.controls.archivoNombre.setValue(archivo.name),
          error: () => this.errorArchivo.set('seguimiento.pdf.lecturaFallida'),
        });
    }
    if (this.errorArchivo()) input.value = '';
  }

  eliminarArchivo(): void {
    this.form.patchValue({ archivo: '', archivoContentType: '', archivoNombre: '' });
    this.errorArchivo.set(null);
  }

  guardar(): void {
    const controls = this.form.controls;
    [controls.titulo, controls.resumen, controls.programa, controls.urlDocumento, controls.contenido, controls.comentario].forEach(
      control => control.setValue(control.value.trim()),
    );
    this.form.markAllAsTouched();
    if (this.form.invalid || this.evaluacionIncoherente || this.guardando() || this.cargandoArchivo() || this.errorArchivo()) {
      return;
    }
    const value = this.form.getRawValue();
    let request: Observable<unknown>;
    switch (this.accion) {
      case 'propuesta':
        request = this.service.crearPropuesta({
          titulo: value.titulo.trim(),
          resumen: value.resumen.trim(),
          programa: value.programa.trim(),
        });
        break;
      case 'entrega':
        request = this.service.crearEntrega({
          titulo: value.titulo.trim(),
          descripcion: value.descripcion.trim(),
          urlDocumento: value.urlDocumento.trim(),
          ...(value.archivo
            ? {
                archivo: value.archivo,
                archivoContentType: value.archivoContentType,
                archivoNombre: value.archivoNombre,
              }
            : {}),
          proyecto: { id: this.proyectoId },
        });
        break;
      case 'revision':
        request = this.service.revisar(this.entregaId, value.estado as Exclude<EstadoEntrega, EstadoEntrega.ENVIADA>);
        break;
      case 'observacion':
        request = this.service.crearObservacion({ contenido: value.contenido.trim(), tipo: value.tipo, entrega: { id: this.entregaId } });
        break;
      case 'evaluacion':
        request = this.service.crearEvaluacion({
          calificacion: value.calificacion,
          concepto: value.concepto,
          comentarios: value.comentarios.trim(),
          proyecto: { id: this.proyectoId },
        });
        break;
      case 'avanzar':
        request = this.service.avanzar(this.proyectoId, value.comentario.trim());
        break;
      case 'cancelar':
        request = this.service.cancelar(this.proyectoId, value.comentario.trim());
    }
    this.guardando.set(true);
    this.problema.set(null);
    request.pipe(finalize(() => this.guardando.set(false))).subscribe({
      next: () => this.activeModal.close(true),
      error: (error: HttpErrorResponse) => this.problema.set(problemaSeguimiento(error)),
    });
  }
}
