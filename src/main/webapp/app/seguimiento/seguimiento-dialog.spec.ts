import { beforeEach, describe, expect, it, vi } from 'vitest';
import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap/modal';
import { provideTranslateService } from '@ngx-translate/core';
import { Subject, of, throwError } from 'rxjs';

import { Concepto } from 'app/entities/enumerations/concepto.model';

import { SeguimientoDialog } from './seguimiento-dialog';
import { SeguimientoService } from './seguimiento.service';

describe('SeguimientoDialog', () => {
  let comp: SeguimientoDialog;
  let fixture: ComponentFixture<SeguimientoDialog>;
  const service = {
    crearPropuesta: vi.fn<SeguimientoService['crearPropuesta']>(),
    crearEntrega: vi.fn<SeguimientoService['crearEntrega']>(),
    crearObservacion: vi.fn<SeguimientoService['crearObservacion']>(),
    crearEvaluacion: vi.fn<SeguimientoService['crearEvaluacion']>(),
    revisar: vi.fn<SeguimientoService['revisar']>(),
    avanzar: vi.fn<SeguimientoService['avanzar']>(),
    cancelar: vi.fn<SeguimientoService['cancelar']>(),
  };
  const modal = { close: vi.fn(), dismiss: vi.fn() };

  beforeEach(() => {
    vi.resetAllMocks();
    service.crearPropuesta.mockReturnValue(of({ id: 7 }));
    service.crearEntrega.mockReturnValue(of({ id: 9 }));
    service.crearObservacion.mockReturnValue(of({ id: 12 }));
    service.crearEvaluacion.mockReturnValue(of({ id: 15 }));
    service.revisar.mockReturnValue(of({ id: 9 }));
    service.avanzar.mockReturnValue(of({ id: 7 }));
    service.cancelar.mockReturnValue(of({ id: 7 }));
    TestBed.configureTestingModule({
      imports: [SeguimientoDialog],
      providers: [
        provideTranslateService(),
        { provide: SeguimientoService, useValue: service },
        { provide: NgbActiveModal, useValue: modal },
      ],
    });
    fixture = TestBed.createComponent(SeguimientoDialog);
    comp = fixture.componentInstance;
  });

  it('valida el título de propuesta después de quitar espacios y envía solo los datos editables', () => {
    comp.preparar('propuesta');
    comp.form.patchValue({ titulo: '  corto  ', resumen: ' Resumen ', programa: ' Sistemas ' });
    comp.guardar();
    expect(service.crearPropuesta).not.toHaveBeenCalled();
    comp.form.controls.titulo.setValue('  Proyecto de grado  ');
    comp.guardar();
    expect(service.crearPropuesta).toHaveBeenCalledWith({ titulo: 'Proyecto de grado', resumen: 'Resumen', programa: 'Sistemas' });
    expect(modal.close).toHaveBeenCalledWith(true);
  });

  it('valida la URL y crea una entrega sin etapa, estado, versión o autor', () => {
    comp.preparar('entrega', 7);
    comp.form.patchValue({ titulo: 'Documento', descripcion: 'Descripción', urlDocumento: 'javascript:alert(1)' });
    comp.guardar();
    expect(service.crearEntrega).not.toHaveBeenCalled();
    comp.form.controls.urlDocumento.setValue('https://example.org/doc');
    comp.guardar();
    expect(service.crearEntrega).toHaveBeenCalledWith({
      titulo: 'Documento',
      descripcion: 'Descripción',
      urlDocumento: 'https://example.org/doc',
      proyecto: { id: 7 },
    });
  });

  it('exige un comentario con contenido al cancelar', () => {
    comp.preparar('cancelar', 7);
    comp.form.controls.comentario.setValue('   ');
    comp.guardar();
    expect(service.cancelar).not.toHaveBeenCalled();
    comp.form.controls.comentario.setValue('Retiro solicitado');
    comp.guardar();
    expect(service.cancelar).toHaveBeenCalledWith(7, 'Retiro solicitado');
  });

  it('permite avanzar con un comentario opcional', () => {
    comp.preparar('avanzar', 7);
    comp.guardar();
    expect(service.avanzar).toHaveBeenCalledWith(7, '');
  });

  it('impide conceptos incoherentes y calificaciones fuera del rango o del paso 0.1', () => {
    comp.preparar('evaluacion', 7);
    for (const calificacion of [-0.1, 5.1, 3.25]) {
      comp.form.controls.calificacion.setValue(calificacion);
      comp.guardar();
      expect(service.crearEvaluacion).not.toHaveBeenCalled();
    }
    comp.form.patchValue({ calificacion: 2.9, concepto: Concepto.APROBADO });
    comp.guardar();
    expect(comp.evaluacionIncoherente).toBe(true);
    expect(service.crearEvaluacion).not.toHaveBeenCalled();
    comp.form.controls.concepto.setValue(Concepto.REPROBADO);
    comp.guardar();
    expect(service.crearEvaluacion).toHaveBeenCalledWith({
      calificacion: 2.9,
      concepto: 'REPROBADO',
      comentarios: '',
      proyecto: { id: 7 },
    });
  });

  it('rechaza REPROBADO a partir de 3.0 y acepta aprobado con modificaciones', () => {
    comp.preparar('evaluacion', 7);
    comp.form.patchValue({ calificacion: 3, concepto: Concepto.REPROBADO });
    comp.guardar();
    expect(service.crearEvaluacion).not.toHaveBeenCalled();
    comp.form.controls.concepto.setValue(Concepto.APROBADO_CON_MODIFICACIONES);
    comp.guardar();
    expect(service.crearEvaluacion).toHaveBeenCalledWith({
      calificacion: 3,
      concepto: 'APROBADO_CON_MODIFICACIONES',
      comentarios: '',
      proyecto: { id: 7 },
    });
  });

  it('conserva el formulario y muestra el detail y pendientes cuando el servidor rechaza la acción', () => {
    service.cancelar.mockReturnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 409,
            error: { detail: 'Proyecto terminado', pendientes: ['Actualice el seguimiento'] },
          }),
      ),
    );
    comp.preparar('cancelar', 7);
    comp.form.controls.comentario.setValue('Retiro');
    comp.guardar();
    fixture.detectChanges();
    const root: HTMLElement = fixture.nativeElement;
    expect(root.querySelector('[role="alert"]')?.textContent).toContain('Proyecto terminado');
    expect(root.querySelector('[role="alert"] li')?.textContent).toContain('Actualice el seguimiento');
    expect(comp.guardando()).toBe(false);
    expect(modal.close).not.toHaveBeenCalled();
  });

  it('bloquea un segundo envío mientras la solicitud está en curso', () => {
    const respuesta = new Subject<{ id: number }>();
    service.avanzar.mockReturnValue(respuesta);
    comp.preparar('avanzar', 7);
    comp.guardar();
    comp.guardar();
    expect(service.avanzar).toHaveBeenCalledTimes(1);
    expect(comp.guardando()).toBe(true);
    respuesta.next({ id: 7 });
    respuesta.complete();
    expect(comp.guardando()).toBe(false);
  });
});
