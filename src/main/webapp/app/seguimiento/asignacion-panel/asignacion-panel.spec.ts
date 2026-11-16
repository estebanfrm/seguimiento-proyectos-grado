import { beforeEach, describe, expect, it, vi } from 'vitest';
import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { TranslateService, provideTranslateService } from '@ngx-translate/core';
import { Subject, of, throwError } from 'rxjs';

import { AccountService } from 'app/core/auth';
import { RestProyectoGrado } from 'app/entities/proyecto-grado/service/proyecto-grado.service';
import seguimientoEs from '../../../i18n/es/seguimiento.json';

import { IUsuarioResumen } from '../seguimiento.model';
import { cuentaSeguimiento, seguimientoEjemplo } from '../seguimiento.test-samples';
import { SeguimientoService } from '../seguimiento.service';

import { AsignacionPanel } from './asignacion-panel';

describe('AsignacionPanel', () => {
  let fixture: ComponentFixture<AsignacionPanel>;
  let comp: AsignacionPanel;
  let root: HTMLElement;
  let account: AccountService;
  let proyecto: RestProyectoGrado;
  const usuarios: Record<string, IUsuarioResumen[]> = {
    ROLE_TUTOR: [{ id: 2, login: 'tutor', firstName: 'Ana', lastName: 'Pérez' }],
    ROLE_JURADO: [
      { id: 3, login: 'jurado', firstName: 'Juan', lastName: 'López' },
      { id: 4, login: 'jurado2', firstName: 'Luisa', lastName: 'Díaz' },
    ],
    ROLE_ESTUDIANTE: [{ id: 1, login: 'ana', firstName: 'Ana', lastName: 'Gómez' }],
  };
  const service = {
    usuariosPorRol: vi.fn<SeguimientoService['usuariosPorRol']>(),
    asignarProyecto: vi.fn<SeguimientoService['asignarProyecto']>(),
  };

  beforeEach(() => {
    vi.resetAllMocks();
    proyecto = seguimientoEjemplo().proyecto;
    service.usuariosPorRol.mockImplementation(rol => of(usuarios[rol]));
    service.asignarProyecto.mockReturnValue(of(proyecto));
    TestBed.configureTestingModule({
      imports: [AsignacionPanel],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideTranslateService(),
        provideRouter([]),
        { provide: SeguimientoService, useValue: service },
      ],
    });
    const translate = TestBed.inject(TranslateService);
    translate.setTranslation('es', seguimientoEs);
    translate.use('es');
    account = TestBed.inject(AccountService);
    fixture = TestBed.createComponent(AsignacionPanel);
    comp = fixture.componentInstance;
    root = fixture.nativeElement;
  });

  async function render(rol = 'ROLE_ADMIN'): Promise<void> {
    account.authenticate(cuentaSeguimiento(rol));
    fixture.componentRef.setInput('proyecto', proyecto);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  it('carga los tres directorios y renderiza nombres, logins y asignaciones actuales', async () => {
    await render();
    expect(service.usuariosPorRol.mock.calls).toEqual([['ROLE_TUTOR'], ['ROLE_JURADO'], ['ROLE_ESTUDIANTE']]);
    expect(root.querySelector('#asignacion-tutor')?.textContent).toContain('Ana Pérez (tutor)');
    expect(root.querySelector('#asignacion-jurados')?.textContent).toContain('Juan López (jurado)');
    expect(root.querySelector('#asignacion-estudiantes')?.textContent).toContain('Ana Gómez (ana)');
    expect(root.querySelectorAll('select[multiple]')).toHaveLength(2);
    expect(comp.form.getRawValue()).toEqual({ tutorId: 2, juradoIds: [3, 4], estudianteIds: [1] });
    expect(root.querySelector('[data-cy="aviso-tutor"]')).toBeNull();
    expect(root.querySelector('[data-cy="aviso-jurados"]')).toBeNull();
  });

  it('envía los ids de las asignaciones completas y notifica el refresco', async () => {
    await render();
    const actualizado = vi.fn();
    comp.actualizado.subscribe(actualizado);
    comp.form.setValue({ tutorId: 2, juradoIds: [4], estudianteIds: [1] });
    root.querySelector<HTMLButtonElement>('[data-cy="guardar-asignacion"]')!.click();
    expect(service.asignarProyecto).toHaveBeenCalledWith(7, {
      tutorId: 2,
      juradoIds: [4],
      estudianteIds: [1],
    });
    expect(actualizado).toHaveBeenCalledOnce();
  });

  it('permite vaciar las selecciones enviando null y arrays vacíos', async () => {
    await render();
    comp.form.setValue({ tutorId: null, juradoIds: [], estudianteIds: [] });
    comp.guardar();
    expect(service.asignarProyecto).toHaveBeenCalledWith(7, { tutorId: null, juradoIds: [], estudianteIds: [] });
  });

  it('actualiza los avisos del tutor y del mínimo de jurados al cambiar la selección', async () => {
    await render();
    comp.form.patchValue({ tutorId: null, juradoIds: [3] });
    fixture.detectChanges();
    expect(root.querySelector('[data-cy="aviso-tutor"]')?.textContent).toContain('TUTOR_ASIGNADO');
    expect(root.querySelector('[data-cy="aviso-jurados"]')?.textContent).toContain('JURADOS_ASIGNADOS');
    proyecto = { ...proyecto, etapaActual: 'DESARROLLO', tutor: null, jurados: [] };
    fixture.componentRef.setInput('proyecto', proyecto);
    fixture.detectChanges();
    expect(root.querySelector('[data-cy="aviso-tutor"]')).not.toBeNull();
    expect(root.querySelector('[data-cy="aviso-jurados"]')).toBeNull();
  });

  it('conserva las personas asignadas que ya no aparecen en el directorio', async () => {
    proyecto.jurados = [{ id: 99, login: 'jurado-anterior' }];
    await render();
    expect(root.querySelector('#asignacion-jurados')?.textContent).toContain('jurado-anterior');
    comp.guardar();
    expect(service.asignarProyecto.mock.calls[0][1].juradoIds).toEqual([99]);
  });

  it.each(['ROLE_ESTUDIANTE', 'ROLE_TUTOR', 'ROLE_JURADO', 'ROLE_USER'])('oculta el panel y no consulta ni guarda para %s', async rol => {
    await render(rol);
    comp.cargarUsuarios();
    comp.guardar();
    expect(root.querySelector('[data-cy="asignacion-panel"]')).toBeNull();
    expect(service.usuariosPorRol).not.toHaveBeenCalled();
    expect(service.asignarProyecto).not.toHaveBeenCalled();
  });

  it('oculta el panel y bloquea guardar al perder el rol admin', async () => {
    await render();
    account.authenticate(null);
    fixture.detectChanges();
    comp.guardar();
    expect(root.querySelector('form')).toBeNull();
    expect(service.asignarProyecto).not.toHaveBeenCalled();
  });

  it('muestra la alerta de carga 403, bloquea guardar y permite reintentar', async () => {
    service.usuariosPorRol.mockReturnValueOnce(throwError(() => new HttpErrorResponse({ status: 403 })));
    await render();
    expect(root.querySelector('[role="alert"]')?.textContent).toContain('No tiene permisos');
    comp.guardar();
    expect(service.asignarProyecto).not.toHaveBeenCalled();
    comp.cargarUsuarios();
    fixture.detectChanges();
    expect(comp.usuariosCargados()).toBe(true);
    expect(root.querySelector('[role="alert"]')).toBeNull();
  });

  it.each([403, 409])('muestra el error %s al guardar sin emitir un refresco', async status => {
    service.asignarProyecto.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status, error: { detail: 'La asignación cambió', pendientes: ['Recargue el proyecto'] } })),
    );
    await render();
    const actualizado = vi.fn();
    comp.actualizado.subscribe(actualizado);
    comp.guardar();
    fixture.detectChanges();
    expect(root.querySelector('[role="alert"]')?.textContent).toContain(status === 403 ? 'No tiene permisos' : 'La asignación cambió');
    expect(comp.guardando()).toBe(false);
    expect(actualizado).not.toHaveBeenCalled();
  });

  it('bloquea envíos duplicados y cancela la respuesta al destruir el panel', async () => {
    const respuesta = new Subject<RestProyectoGrado>();
    service.asignarProyecto.mockReturnValue(respuesta);
    await render();
    const actualizado = vi.fn();
    comp.actualizado.subscribe(actualizado);
    comp.guardar();
    comp.guardar();
    expect(service.asignarProyecto).toHaveBeenCalledOnce();
    fixture.destroy();
    respuesta.next(proyecto);
    expect(actualizado).not.toHaveBeenCalled();
  });
});
