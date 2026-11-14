import { beforeEach, describe, expect, it, vi } from 'vitest';
import { HttpErrorResponse, HttpHeaders, HttpResponse, provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { NgbModal } from '@ng-bootstrap/ng-bootstrap/modal';
import { provideTranslateService } from '@ngx-translate/core';
import { Subject, of, throwError } from 'rxjs';

import { AccountService } from 'app/core/auth';

import MisProyectos from './mis-proyectos';
import { cuentaSeguimiento, seguimientoEjemplo } from './seguimiento.test-samples';
import { SeguimientoService } from './seguimiento.service';

describe('MisProyectos', () => {
  let fixture: ComponentFixture<MisProyectos>;
  let account: AccountService;
  let root: HTMLElement;
  const proyectos = vi.fn<SeguimientoService['proyectos']>();
  const modal = { open: vi.fn() };

  beforeEach(() => {
    vi.resetAllMocks();
    proyectos.mockReturnValue(
      of(
        new HttpResponse({
          body: [seguimientoEjemplo().proyecto],
          headers: new HttpHeaders({ 'X-Total-Count': '25' }),
        }),
      ),
    );
    TestBed.configureTestingModule({
      imports: [MisProyectos],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        provideTranslateService(),
        { provide: SeguimientoService, useValue: { proyectos } },
        { provide: NgbModal, useValue: modal },
      ],
    });
    account = TestBed.inject(AccountService);
    fixture = TestBed.createComponent(MisProyectos);
    root = fixture.nativeElement;
  });

  it('lista los proyectos visibles con avance, enlace y paginación del servidor', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    expect(proyectos).toHaveBeenCalledWith(0, 12);
    expect(root.textContent).toContain('Sistema de seguimiento académico');
    expect(root.querySelector('[role="progressbar"]')?.getAttribute('aria-valuenow')).toBe('35');
    expect(root.querySelector('a.btn')?.getAttribute('href')).toBe('/seguimiento/7');
    expect(root.querySelector('ngb-pagination')).not.toBeNull();
    fixture.componentInstance.cargar(2);
    expect(proyectos).toHaveBeenLastCalledWith(1, 12);
  });

  it.each(['ROLE_TUTOR', 'ROLE_JURADO', 'ROLE_ADMIN', 'ROLE_USER'])('oculta registrar propuesta para %s', async rol => {
    account.authenticate(cuentaSeguimiento(rol));
    fixture.detectChanges();
    await fixture.whenStable();
    expect(root.querySelector('[data-cy="registrar-propuesta"]')).toBeNull();
    fixture.componentInstance.registrar();
    expect(modal.open).not.toHaveBeenCalled();
  });

  it('permite registrar al estudiante y actualiza la primera página al guardar', async () => {
    const preparar = vi.fn();
    const closed = new Subject<boolean>();
    modal.open.mockReturnValue({ componentInstance: { preparar }, closed });
    account.authenticate(cuentaSeguimiento('ROLE_ESTUDIANTE'));
    fixture.detectChanges();
    await fixture.whenStable();
    root.querySelector<HTMLButtonElement>('[data-cy="registrar-propuesta"]')!.click();
    expect(preparar).toHaveBeenCalledWith('propuesta');
    closed.next(true);
    expect(proyectos).toHaveBeenCalledTimes(2);
    expect(proyectos).toHaveBeenLastCalledWith(0, 12);
  });

  it('muestra una alerta ante un error y recupera la lista al reintentar', () => {
    proyectos.mockReturnValueOnce(throwError(() => new HttpErrorResponse({ status: 409, error: { detail: 'Conflicto al consultar' } })));
    fixture.detectChanges();
    expect(root.querySelector('[role="alert"]')?.textContent).toContain('Conflicto al consultar');
    fixture.componentInstance.cargar();
    fixture.detectChanges();
    expect(root.querySelector('[role="alert"]')).toBeNull();
    expect(root.querySelectorAll('article')).toHaveLength(1);
  });
});
