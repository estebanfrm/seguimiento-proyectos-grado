import { beforeEach, describe, expect, it, vi } from 'vitest';
import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';

import { TranslateService, provideTranslateService } from '@ngx-translate/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap/modal';
import { BehaviorSubject, Subject, of, throwError } from 'rxjs';

import { AccountService } from 'app/core/auth';
import etapaEs from '../../i18n/es/etapa.json';
import seguimientoEs from '../../i18n/es/seguimiento.json';

import SeguimientoDetail from './seguimiento-detail';
import { ISeguimientoProyecto } from './seguimiento.model';
import { cuentaSeguimiento, seguimientoEjemplo } from './seguimiento.test-samples';
import { SeguimientoService } from './seguimiento.service';

describe('SeguimientoDetail', () => {
  let fixture: ComponentFixture<SeguimientoDetail>;
  let comp: SeguimientoDetail;
  let root: HTMLElement;
  let account: AccountService;
  let data: ISeguimientoProyecto;
  const params = new BehaviorSubject(convertToParamMap({ id: '7' }));
  const service = {
    seguimiento: vi.fn<SeguimientoService['seguimiento']>(),
    detalleEntrega: vi.fn<SeguimientoService['detalleEntrega']>(),
    resolver: vi.fn<SeguimientoService['resolver']>(),
  };
  const modal = { open: vi.fn() };

  beforeEach(() => {
    vi.resetAllMocks();
    params.next(convertToParamMap({ id: '7' }));
    data = seguimientoEjemplo();
    service.seguimiento.mockImplementation(() => of(data));
    service.detalleEntrega.mockReturnValue(
      of({
        entrega: { id: 9, descripcion: 'Documento completo', urlDocumento: 'https://example.org/doc' },
        observaciones: [{ id: 12, contenido: 'Ajustar objetivos', tipo: 'CORRECCION_REQUERIDA', resuelta: false }],
      }),
    );
    service.resolver.mockReturnValue(of({ id: 12, resuelta: true }));
    TestBed.configureTestingModule({
      imports: [SeguimientoDetail],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideTranslateService(),
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { paramMap: params.asObservable() } },
        { provide: SeguimientoService, useValue: service },
        { provide: NgbModal, useValue: modal },
      ],
    });
    const translate = TestBed.inject(TranslateService);
    translate.setTranslation('es', { ...seguimientoEs, ...etapaEs });
    translate.use('es');
    account = TestBed.inject(AccountService);
    fixture = TestBed.createComponent(SeguimientoDetail);
    comp = fixture.componentInstance;
    root = fixture.nativeElement;
  });

  async function render(rol = 'ROLE_ESTUDIANTE', login = 'ana'): Promise<void> {
    account.authenticate(cuentaSeguimiento(rol, login));
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  function boton(nombre: string): HTMLButtonElement | null {
    return root.querySelector<HTMLButtonElement>(`[data-cy="${nombre}"]`);
  }

  it('renderiza las cinco etapas, avance, checklist, fechas e historial', async () => {
    await render();
    expect(root.querySelectorAll('.etapas .etapa')).toHaveLength(5);
    expect(root.querySelector('.completada')?.textContent).toContain('Propuesta');
    expect(root.querySelector('[aria-current="step"]')?.textContent).toContain('Anteproyecto');
    expect(root.querySelectorAll('[data-estado="PENDIENTE"]')).toHaveLength(3);
    expect(root.querySelector('[role="progressbar"]')?.getAttribute('aria-valuenow')).toBe('35');
    expect(root.querySelector('[data-requisito="JURADOS_ASIGNADOS"]')?.textContent).toContain('✔');
    expect(root.querySelector('[data-requisito="ENTREGA_APROBADA"]')?.textContent).toContain('✘');
    expect(root.textContent).toContain('Una entrega aprobada');
    expect(root.textContent).toContain('vence en 18 días');
    expect(root.querySelector('.historial')?.textContent).toContain('Propuesta aprobada');
  });

  it('permite entregar solo al estudiante relacionado y bloquea entregas en curso o vencidas', async () => {
    await render();
    expect(boton('nueva-entrega')?.disabled).toBe(false);
    expect(boton('avanzar')).toBeNull();
    expect(boton('revisar-entrega')).toBeNull();
    expect(boton('evaluar')).toBeNull();
    expect(boton('cancelar-proyecto')).toBeNull();
    for (const estado of ['ENVIADA', 'EN_REVISION', 'APROBADA'] as const) {
      data.etapas[1].entregas[0].estado = estado;
      comp.seguimiento.set({ ...data });
      fixture.detectChanges();
      expect(boton('nueva-entrega')?.disabled).toBe(true);
    }
    data.etapas[1].entregas[0].estado = 'RECHAZADA';
    data.etapas[1].vencida = true;
    comp.seguimiento.set({ ...data });
    fixture.detectChanges();
    expect(boton('nueva-entrega')?.disabled).toBe(true);
    expect(root.textContent).toContain('VENCIDA');
    expect(root.querySelector('#fechas-title')?.parentElement?.querySelector('.text-danger')).not.toBeNull();
    account.authenticate(cuentaSeguimiento('ROLE_ESTUDIANTE', 'otro'));
    fixture.detectChanges();
    expect(boton('nueva-entrega')).toBeNull();
  });

  it('muestra revisión, observación y avance condicionado al tutor asignado', async () => {
    await render('ROLE_TUTOR', 'tutor');
    expect(boton('revisar-entrega')).not.toBeNull();
    expect(boton('agregar-observacion')).not.toBeNull();
    expect(boton('avanzar')?.disabled).toBe(true);
    expect(boton('cancelar-proyecto')).toBeNull();
    data.requisitos.puedeAvanzar = true;
    comp.seguimiento.set({ ...data });
    fixture.detectChanges();
    expect(boton('avanzar')?.disabled).toBe(false);
    account.authenticate(cuentaSeguimiento('ROLE_TUTOR', 'otro'));
    fixture.detectChanges();
    expect(boton('avanzar')).toBeNull();
    expect(boton('revisar-entrega')).toBeNull();
  });

  it('el admin ve asignaciones, cancelación y avance sujeto a requisitos', async () => {
    await render('ROLE_ADMIN', 'admin');
    expect(boton('avanzar')?.disabled).toBe(true);
    expect(boton('cancelar-proyecto')).not.toBeNull();
    expect(root.querySelector('[data-cy="editar-proyecto"]')?.getAttribute('href')).toBe('/proyecto-grado/7/edit');
    expect(boton('nueva-entrega')).toBeNull();
    expect(boton('evaluar')).toBeNull();
  });

  it.each(['ANTEPROYECTO', 'SUSTENTACION'] as const)('solo permite evaluar una vez al jurado asignado en %s', async etapa => {
    data.proyecto.etapaActual = etapa;
    await render('ROLE_JURADO', 'jurado');
    expect(boton('evaluar')).not.toBeNull();
    expect(boton('revisar-entrega')).toBeNull();
    expect(boton('avanzar')).toBeNull();
    data.etapas.find(item => item.etapa === etapa)!.evaluaciones.push({ id: 15, jurado: { id: 3, login: 'jurado' }, etapa });
    comp.seguimiento.set({ ...data });
    fixture.detectChanges();
    expect(boton('evaluar')).toBeNull();
  });

  it('oculta evaluar fuera de las etapas evaluables o cuando el jurado no está asignado', async () => {
    data.proyecto.etapaActual = 'DESARROLLO';
    await render('ROLE_JURADO', 'jurado');
    expect(boton('evaluar')).toBeNull();
    data.proyecto.etapaActual = 'ANTEPROYECTO';
    comp.seguimiento.set({ ...data });
    account.authenticate(cuentaSeguimiento('ROLE_JURADO', 'otro'));
    fixture.detectChanges();
    expect(boton('evaluar')).toBeNull();
  });

  it.each(['CANCELADO', 'APROBADO', 'REPROBADO'] as const)('oculta las mutaciones del proyecto %s', async estado => {
    data.proyecto.estado = estado;
    await render('ROLE_ADMIN', 'admin');
    expect(boton('avanzar')).toBeNull();
    expect(boton('cancelar-proyecto')).toBeNull();
    expect(boton('revisar-entrega')).toBeNull();
  });

  it('expande observaciones, muestra el documento y recarga tras resolver', async () => {
    await render('ROLE_TUTOR', 'tutor');
    root.querySelector<HTMLButtonElement>('[aria-controls="entrega-9"]')!.click();
    fixture.detectChanges();
    expect(root.textContent).toContain('Ajustar objetivos');
    expect(root.querySelector('#entrega-9 a')?.getAttribute('href')).toBe('https://example.org/doc');
    expect(boton('resolver-observacion')).not.toBeNull();
    boton('resolver-observacion')!.click();
    fixture.detectChanges();
    expect(service.resolver).toHaveBeenCalledWith(12);
    expect(service.seguimiento).toHaveBeenCalledTimes(2);
  });

  it('mantiene las observaciones en lectura para estudiantes', async () => {
    await render();
    comp.expandir(9);
    fixture.detectChanges();
    expect(root.textContent).toContain('Ajustar objetivos');
    expect(boton('resolver-observacion')).toBeNull();
  });

  it('muestra la alerta de permisos si no puede consultar las observaciones de la entrega', async () => {
    service.detalleEntrega.mockReturnValueOnce(throwError(() => new HttpErrorResponse({ status: 403 })));
    await render();
    comp.expandir(9);
    fixture.detectChanges();
    expect(service.detalleEntrega).toHaveBeenCalledWith(9);
    expect(root.querySelector('[role="alert"]')?.textContent).toContain('No tiene permisos');
    expect(comp.detalles()[9]).toBeUndefined();
    expect(comp.cargandoEntregas()).toEqual([]);
  });

  it('abre el modal con el proyecto y la entrega y actualiza tras guardar', async () => {
    const preparar = vi.fn();
    const closed = new Subject<boolean>();
    modal.open.mockReturnValue({ componentInstance: { preparar }, closed });
    await render('ROLE_TUTOR', 'tutor');
    boton('revisar-entrega')!.click();
    expect(preparar).toHaveBeenCalledWith('revision', 7, 9);
    closed.next(true);
    expect(service.seguimiento).toHaveBeenCalledTimes(2);
  });

  it('muestra 403 y permite recuperar una carga fallida', async () => {
    service.seguimiento.mockReturnValueOnce(throwError(() => new HttpErrorResponse({ status: 403 })));
    await render();
    expect(root.querySelector('[role="alert"]')?.textContent).toContain('No tiene permisos');
    expect(comp.seguimiento()).toBeNull();
    comp.recargar();
    fixture.detectChanges();
    expect(service.seguimiento).toHaveBeenCalledTimes(2);
    expect(root.querySelectorAll('.etapa')).toHaveLength(5);
  });

  it('muestra el detail y pendientes de un 409 al resolver', async () => {
    service.resolver.mockReturnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 409,
            error: { detail: 'La etapa cambió', pendientes: ['Recargue el proyecto'] },
          }),
      ),
    );
    await render('ROLE_TUTOR', 'tutor');
    comp.resolver(12);
    fixture.detectChanges();
    expect(root.querySelector('[role="alert"]')?.textContent).toContain('La etapa cambió');
    expect(root.querySelector('[role="alert"] li')?.textContent).toContain('Recargue el proyecto');
    expect(comp.guardando()).toBe(false);
  });

  it('cancela la carga anterior al navegar a otro proyecto', async () => {
    const primera = new Subject<ISeguimientoProyecto>();
    service.seguimiento.mockReturnValueOnce(primera).mockReturnValueOnce(of({ ...data, proyecto: { ...data.proyecto, id: 8 } }));
    await render();
    params.next(convertToParamMap({ id: '8' }));
    primera.next({ ...data, proyecto: { ...data.proyecto, id: 99 } });
    expect(service.seguimiento).toHaveBeenLastCalledWith(8);
    expect(comp.seguimiento()?.proyecto.id).toBe(8);
  });
});
