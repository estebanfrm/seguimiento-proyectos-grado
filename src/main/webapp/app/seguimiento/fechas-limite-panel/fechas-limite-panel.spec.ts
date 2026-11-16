import { beforeEach, describe, expect, it, vi } from 'vitest';
import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { TranslateService, provideTranslateService } from '@ngx-translate/core';
import dayjs from 'dayjs/esm';
import { Subject, of, throwError } from 'rxjs';

import { AccountService } from 'app/core/auth';
import { RestFechaLimiteEtapa } from 'app/entities/fecha-limite-etapa/service/fecha-limite-etapa.service';
import etapaEs from '../../../i18n/es/etapa.json';
import seguimientoEs from '../../../i18n/es/seguimiento.json';

import { ISeguimientoProyecto } from '../seguimiento.model';
import { cuentaSeguimiento, seguimientoEjemplo } from '../seguimiento.test-samples';
import { SeguimientoService } from '../seguimiento.service';

import { FechasLimitePanel } from './fechas-limite-panel';

describe('FechasLimitePanel', () => {
  let fixture: ComponentFixture<FechasLimitePanel>;
  let comp: FechasLimitePanel;
  let root: HTMLElement;
  let account: AccountService;
  let data: ISeguimientoProyecto;
  let fecha: RestFechaLimiteEtapa;
  const service = {
    fechasLimite: vi.fn<SeguimientoService['fechasLimite']>(),
    crearFechaLimite: vi.fn<SeguimientoService['crearFechaLimite']>(),
    actualizarFechaLimite: vi.fn<SeguimientoService['actualizarFechaLimite']>(),
    eliminarFechaLimite: vi.fn<SeguimientoService['eliminarFechaLimite']>(),
  };

  beforeEach(() => {
    vi.resetAllMocks();
    data = seguimientoEjemplo();
    fecha = { id: 21, etapa: 'ANTEPROYECTO', fechaLimite: '2026-12-01T23:59:00Z', descripcion: 'Documento final', proyecto: { id: 7 } };
    service.fechasLimite.mockReturnValue(of([fecha]));
    service.crearFechaLimite.mockReturnValue(of({ ...fecha, id: 22, etapa: 'SUSTENTACION' }));
    service.actualizarFechaLimite.mockReturnValue(of(fecha));
    service.eliminarFechaLimite.mockReturnValue(of(undefined));
    TestBed.configureTestingModule({
      imports: [FechasLimitePanel],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideTranslateService(),
        provideRouter([]),
        { provide: SeguimientoService, useValue: service },
      ],
    });
    const translate = TestBed.inject(TranslateService);
    translate.setTranslation('es', { ...seguimientoEs, ...etapaEs });
    translate.use('es');
    account = TestBed.inject(AccountService);
    fixture = TestBed.createComponent(FechasLimitePanel);
    comp = fixture.componentInstance;
    root = fixture.nativeElement;
  });

  async function render(rol = 'ROLE_ADMIN'): Promise<void> {
    account.authenticate(cuentaSeguimiento(rol));
    fixture.componentRef.setInput('seguimiento', data);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  it('renderiza las cinco etapas con las fechas del seguimiento y obtiene los ids del proyecto', async () => {
    await render();
    expect(service.fechasLimite).toHaveBeenCalledWith(7);
    expect(root.querySelectorAll('tbody tr')).toHaveLength(5);
    expect(root.querySelector('[data-etapa="ANTEPROYECTO"] td time')?.getAttribute('datetime')).toBe(data.etapas[1].fechaLimite);
    expect(root.querySelector('[data-fecha-id="21"]')?.textContent).toContain('Documento final');
    expect(root.querySelector('[data-etapa="PROPUESTA"]')?.textContent).toContain('Sin fecha límite');
    expect(root.textContent).toContain('SUSTENTACION_PROGRAMADA');
    expect(root.textContent).toContain('DESARROLLO a SUSTENTACION');
  });

  it('crea un plazo de sustentación convirtiendo la hora local a ISO y solicita refrescar', async () => {
    await render();
    const actualizado = vi.fn();
    comp.actualizado.subscribe(actualizado);
    root.querySelector<HTMLButtonElement>('[data-etapa="SUSTENTACION"] [data-cy="crear-fecha"]')!.click();
    fixture.detectChanges();
    const valorLocal = '2027-02-03T14:30:00';
    const input = root.querySelector<HTMLInputElement>('#admin-fecha-limite')!;
    input.value = valorLocal;
    input.dispatchEvent(new Event('input'));
    comp.form.controls.descripcion.setValue('  Sustentación pública  ');
    fixture.detectChanges();
    root.querySelector<HTMLButtonElement>('[data-cy="guardar-fecha"]')!.click();
    expect(service.crearFechaLimite).toHaveBeenCalledWith({
      etapa: 'SUSTENTACION',
      fechaLimite: dayjs(valorLocal).toISOString(),
      descripcion: 'Sustentación pública',
      proyecto: { id: 7 },
    });
    expect(service.actualizarFechaLimite).not.toHaveBeenCalled();
    expect(actualizado).toHaveBeenCalledOnce();
    expect(comp.etapaEditada()).toBeNull();
  });

  it('edita el id del plazo y conserva el instante al convertir ISO a local y de vuelta', async () => {
    fecha.fechaLimite = '2026-12-01T23:59:15.123Z';
    await render();
    root.querySelector<HTMLButtonElement>('[data-cy="editar-fecha"]')!.click();
    fixture.detectChanges();
    expect(comp.idEditado()).toBe(21);
    expect(comp.form.controls.fechaLimite.value).toBe(dayjs(fecha.fechaLimite).format('YYYY-MM-DDTHH:mm:ss.SSS'));
    comp.form.controls.descripcion.setValue('Nuevo plazo');
    comp.guardar();
    expect(service.actualizarFechaLimite).toHaveBeenCalledWith(21, {
      etapa: 'ANTEPROYECTO',
      fechaLimite: fecha.fechaLimite,
      descripcion: 'Nuevo plazo',
      proyecto: { id: 7 },
    });
    expect(service.crearFechaLimite).not.toHaveBeenCalled();
  });

  it('muestra la confirmación con etapa y fecha y elimina por id tras confirmar', async () => {
    await render();
    const actualizado = vi.fn();
    comp.actualizado.subscribe(actualizado);
    root.querySelector<HTMLButtonElement>('[data-cy="eliminar-fecha"]')!.click();
    fixture.detectChanges();
    expect(service.eliminarFechaLimite).not.toHaveBeenCalled();
    expect(root.querySelector('[role="alert"]')?.textContent).toContain('Anteproyecto');
    expect(root.querySelector('[role="alert"]')?.textContent).toContain('Documento final');
    root.querySelector<HTMLButtonElement>('[data-cy="confirmar-eliminar-fecha"]')!.click();
    expect(service.eliminarFechaLimite).toHaveBeenCalledWith(21);
    expect(actualizado).toHaveBeenCalledOnce();
    expect(comp.porEliminar()).toBeNull();
  });

  it('permite cancelar la edición y la eliminación sin cambiar datos', async () => {
    await render();
    comp.editar('ANTEPROYECTO', fecha);
    comp.cancelarEdicion();
    comp.guardar();
    comp.eliminar(fecha);
    comp.porEliminar.set(null);
    comp.confirmarEliminacion();
    expect(service.crearFechaLimite).not.toHaveBeenCalled();
    expect(service.actualizarFechaLimite).not.toHaveBeenCalled();
    expect(service.eliminarFechaLimite).not.toHaveBeenCalled();
  });

  it('permite administrar todos los ids cuando hay varios plazos de la misma etapa', async () => {
    service.fechasLimite.mockReturnValue(of([fecha, { ...fecha, id: 23, descripcion: 'Prórroga' }]));
    await render();
    expect(root.querySelectorAll('[data-etapa="ANTEPROYECTO"] [data-cy="editar-fecha"]')).toHaveLength(2);
    root.querySelector<HTMLButtonElement>('[data-fecha-id="23"] [data-cy="editar-fecha"]')!.click();
    expect(comp.idEditado()).toBe(23);
    expect(comp.form.controls.descripcion.value).toBe('Prórroga');
  });

  it('impide guardar una fecha vacía, inválida o una descripción demasiado larga', async () => {
    await render();
    comp.editar('SUSTENTACION');
    comp.guardar();
    comp.form.controls.fechaLimite.setValue('sin fecha');
    expect(comp.form.controls.fechaLimite.invalid).toBe(true);
    comp.guardar();
    comp.form.setValue({ fechaLimite: '2027-02-03T14:30', descripcion: 'a'.repeat(251) });
    comp.guardar();
    fixture.detectChanges();
    expect(root.querySelector('.text-danger')?.textContent).toContain('Ingrese una fecha válida');
    expect(service.crearFechaLimite).not.toHaveBeenCalled();
  });

  it.each(['ROLE_ESTUDIANTE', 'ROLE_TUTOR', 'ROLE_JURADO', 'ROLE_USER'])('oculta el panel y no consulta ni muta para %s', async rol => {
    await render(rol);
    comp.editar('ANTEPROYECTO', fecha);
    comp.guardar();
    comp.eliminar(fecha);
    comp.confirmarEliminacion();
    expect(root.querySelector('[data-cy="fechas-limite-panel"]')).toBeNull();
    expect(service.fechasLimite).not.toHaveBeenCalled();
    expect(service.crearFechaLimite).not.toHaveBeenCalled();
    expect(service.actualizarFechaLimite).not.toHaveBeenCalled();
    expect(service.eliminarFechaLimite).not.toHaveBeenCalled();
  });

  it('bloquea editar y eliminar registros de otro proyecto', async () => {
    await render();
    const ajena = { ...fecha, proyecto: { id: 8 } };
    comp.editar('ANTEPROYECTO', ajena);
    comp.eliminar(ajena);
    expect(comp.etapaEditada()).toBeNull();
    expect(comp.porEliminar()).toBeNull();
  });

  it('muestra un 403 de carga, bloquea crear y permite reintentar', async () => {
    service.fechasLimite.mockReturnValueOnce(throwError(() => new HttpErrorResponse({ status: 403 })));
    await render();
    comp.editar('SUSTENTACION');
    expect(comp.etapaEditada()).toBeNull();
    expect(root.querySelector('[role="alert"]')?.textContent).toContain('No tiene permisos');
    comp.cargarFechas();
    fixture.detectChanges();
    expect(comp.cargadas()).toBe(true);
    expect(root.querySelector('[role="alert"]')).toBeNull();
  });

  it.each([403, 409])('presenta el error %s al guardar y permite conservar el formulario', async status => {
    service.actualizarFechaLimite.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status, error: { detail: 'El plazo cambió', pendientes: ['Recargue el proyecto'] } })),
    );
    await render();
    const actualizado = vi.fn();
    comp.actualizado.subscribe(actualizado);
    comp.editar('ANTEPROYECTO', fecha);
    comp.guardar();
    fixture.detectChanges();
    expect(root.querySelector('[role="alert"]')?.textContent).toContain(status === 403 ? 'No tiene permisos' : 'El plazo cambió');
    expect(comp.idEditado()).toBe(21);
    expect(comp.guardando()).toBe(false);
    expect(actualizado).not.toHaveBeenCalled();
  });

  it('muestra un 409 al eliminar sin anunciar éxito', async () => {
    service.eliminarFechaLimite.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 409, error: { detail: 'Plazo en uso' } })),
    );
    await render();
    const actualizado = vi.fn();
    comp.actualizado.subscribe(actualizado);
    comp.eliminar(fecha);
    comp.confirmarEliminacion();
    fixture.detectChanges();
    expect(comp.problema()?.mensaje).toBe('Plazo en uso');
    expect(actualizado).not.toHaveBeenCalled();
    expect(comp.guardando()).toBe(false);
  });

  it('bloquea envíos duplicados y cancela las cargas anteriores al cambiar de proyecto', async () => {
    const carga = new Subject<RestFechaLimiteEtapa[]>();
    service.fechasLimite.mockReturnValueOnce(carga);
    await render();
    fixture.componentRef.setInput('seguimiento', { ...data, proyecto: { ...data.proyecto, id: 8 } });
    service.fechasLimite.mockReturnValueOnce(of([]));
    fixture.detectChanges();
    carga.next([fecha]);
    expect(service.fechasLimite).toHaveBeenLastCalledWith(8);
    expect(comp.fechas()).toEqual([]);
    const respuesta = new Subject<RestFechaLimiteEtapa>();
    service.crearFechaLimite.mockReturnValue(respuesta);
    comp.editar('SUSTENTACION');
    comp.form.controls.fechaLimite.setValue('2027-02-03T14:30');
    comp.guardar();
    comp.guardar();
    expect(service.crearFechaLimite).toHaveBeenCalledOnce();
    expect(service.crearFechaLimite.mock.calls[0][0].proyecto).toEqual({ id: 8 });
  });
});
