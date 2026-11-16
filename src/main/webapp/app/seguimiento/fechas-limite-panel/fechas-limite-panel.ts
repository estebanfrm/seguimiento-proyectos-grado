import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnChanges, computed, inject, input, output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AbstractControl, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { TranslatePipe } from '@ngx-translate/core';
import dayjs from 'dayjs/esm';
import { Observable, Subject, finalize, takeUntil } from 'rxjs';

import { AccountService } from 'app/core/auth';
import { Etapa } from 'app/entities/enumerations/etapa.model';
import { RestFechaLimiteEtapa } from 'app/entities/fecha-limite-etapa/service/fecha-limite-etapa.service';
import { TranslateDirective } from 'app/shared/language';

import { SeguimientoAlert } from '../seguimiento-alert';
import { problemaSeguimiento } from '../seguimiento-error';
import { IProblemaSeguimiento, ISeguimientoProyecto } from '../seguimiento.model';
import { SeguimientoService } from '../seguimiento.service';

@Component({
  selector: 'jhi-fechas-limite-panel',
  templateUrl: './fechas-limite-panel.html',
  imports: [DatePipe, ReactiveFormsModule, TranslatePipe, TranslateDirective, SeguimientoAlert],
})
export class FechasLimitePanel implements OnChanges {
  readonly seguimiento = input.required<ISeguimientoProyecto>();
  readonly actualizado = output();
  readonly fechas = signal<RestFechaLimiteEtapa[]>([]);
  readonly cargando = signal(false);
  readonly cargadas = signal(false);
  readonly guardando = signal(false);
  readonly problema = signal<IProblemaSeguimiento | null>(null);
  readonly etapaEditada = signal<keyof typeof Etapa | null>(null);
  readonly idEditado = signal<number | null>(null);
  readonly porEliminar = signal<RestFechaLimiteEtapa | null>(null);
  readonly esAdmin = computed(() => this.accountService.hasAnyAuthority('ROLE_ADMIN'));
  readonly form = inject(FormBuilder).nonNullable.group({
    fechaLimite: [
      '',
      [Validators.required, (control: AbstractControl<string>) => (dayjs(control.value).isValid() ? null : { fechaInvalida: true })],
    ],
    descripcion: ['', Validators.maxLength(250)],
  });

  private readonly accountService = inject(AccountService);
  private readonly service = inject(SeguimientoService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cambioProyecto = new Subject<void>();

  ngOnChanges(): void {
    this.cambioProyecto.next();
    this.cancelarEdicion();
    this.porEliminar.set(null);
    this.cargarFechas();
  }

  cargarFechas(): void {
    if (!this.esAdmin() || this.cargando()) {
      return;
    }
    this.cargando.set(true);
    this.cargadas.set(false);
    this.fechas.set([]);
    this.problema.set(null);
    this.service
      .fechasLimite(this.seguimiento().proyecto.id)
      .pipe(
        takeUntil(this.cambioProyecto),
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.cargando.set(false)),
      )
      .subscribe({
        next: fechas => {
          this.fechas.set(fechas);
          this.cargadas.set(true);
        },
        error: (error: HttpErrorResponse) => this.problema.set(problemaSeguimiento(error)),
      });
  }

  fechasPorEtapa(etapa: keyof typeof Etapa): RestFechaLimiteEtapa[] {
    return this.fechas().filter(fecha => fecha.etapa === etapa);
  }

  editar(etapa: keyof typeof Etapa, fecha?: RestFechaLimiteEtapa): void {
    if (!this.esAdmin() || !this.cargadas() || this.guardando() || (fecha && !this.perteneceAlProyecto(fecha))) {
      return;
    }
    this.porEliminar.set(null);
    this.etapaEditada.set(etapa);
    this.idEditado.set(fecha?.id ?? null);
    this.form.reset({
      fechaLimite: fecha?.fechaLimite ? dayjs(fecha.fechaLimite).format('YYYY-MM-DDTHH:mm:ss.SSS') : '',
      descripcion: fecha?.descripcion ?? '',
    });
  }

  cancelarEdicion(): void {
    this.etapaEditada.set(null);
    this.idEditado.set(null);
    this.form.reset();
  }

  guardar(): void {
    this.form.markAllAsTouched();
    const etapa = this.etapaEditada();
    const value = this.form.getRawValue();
    const fecha = dayjs(value.fechaLimite);
    if (!this.esAdmin() || !this.cargadas() || this.guardando() || !etapa || this.form.invalid || !fecha.isValid()) {
      return;
    }
    const payload = {
      etapa,
      fechaLimite: fecha.toISOString(),
      descripcion: value.descripcion.trim(),
      proyecto: { id: this.seguimiento().proyecto.id },
    };
    const id = this.idEditado();
    this.mutar(id === null ? this.service.crearFechaLimite(payload) : this.service.actualizarFechaLimite(id, payload));
  }

  eliminar(fecha: RestFechaLimiteEtapa): void {
    if (!this.esAdmin() || !this.cargadas() || this.guardando() || !this.perteneceAlProyecto(fecha)) {
      return;
    }
    this.cancelarEdicion();
    this.porEliminar.set(fecha);
  }

  confirmarEliminacion(): void {
    const fecha = this.porEliminar();
    if (!this.esAdmin() || !this.cargadas() || this.guardando() || !fecha || !this.perteneceAlProyecto(fecha)) {
      return;
    }
    this.mutar(this.service.eliminarFechaLimite(fecha.id));
  }

  private perteneceAlProyecto(fecha: RestFechaLimiteEtapa): boolean {
    return fecha.proyecto?.id === this.seguimiento().proyecto.id && this.fechas().some(item => item.id === fecha.id);
  }

  private mutar(request: Observable<unknown>): void {
    this.guardando.set(true);
    this.problema.set(null);
    request
      .pipe(
        takeUntil(this.cambioProyecto),
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.guardando.set(false)),
      )
      .subscribe({
        next: () => {
          this.cancelarEdicion();
          this.porEliminar.set(null);
          this.actualizado.emit();
        },
        error: (error: HttpErrorResponse) => this.problema.set(problemaSeguimiento(error)),
      });
  }
}
