import { beforeEach, describe, expect, it, vi } from 'vitest';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';

import { provideTranslateService } from '@ngx-translate/core';

import { AccountService } from 'app/core/auth';
import { cuentaSeguimiento } from 'app/seguimiento/seguimiento.test-samples';

import Home from './home';

describe('Home Component', () => {
  let comp: Home;
  let fixture: ComponentFixture<Home>;
  let mockRouter: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        provideTranslateService(),
        {
          provide: AccountService,
        },
      ],
    });
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(Home);
    comp = fixture.componentInstance;

    mockRouter = TestBed.inject(Router);
    vi.spyOn(mockRouter, 'navigate');
  });

  describe('login', () => {
    it('should navigate to /login on login', () => {
      // WHEN
      comp.login();

      // THEN
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
    });
  });

  it.each(['ADMIN', 'ESTUDIANTE', 'TUTOR', 'JURADO', 'USER'])('ofrece acceso y texto de seguimiento para %s', async rol => {
    TestBed.inject(AccountService).authenticate(cuentaSeguimiento(`ROLE_${rol}`));
    fixture.detectChanges();
    await fixture.whenStable();
    const root: HTMLElement = fixture.nativeElement;
    expect(comp.textoSeguimiento()).toBe(`seguimiento.home.${rol}`);
    expect(root.querySelector('[data-cy="home-seguimiento"]')?.getAttribute('href')).toBe('/seguimiento');
  });

  it('oculta el acceso de seguimiento a visitantes', () => {
    fixture.detectChanges();
    const root: HTMLElement = fixture.nativeElement;
    expect(root.querySelector('[data-cy="home-seguimiento"]')).toBeNull();
  });
});
