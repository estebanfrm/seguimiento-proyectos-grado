import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';

import { FaIconLibrary } from '@fortawesome/angular-fontawesome';
import { faArrowLeft, faPencilAlt } from '@fortawesome/free-solid-svg-icons';
import { provideTranslateService } from '@ngx-translate/core';
import { of } from 'rxjs';

import { FechaLimiteEtapaDetail } from './fecha-limite-etapa-detail';

describe('FechaLimiteEtapa Management Detail Component', () => {
  let comp: FechaLimiteEtapaDetail;
  let fixture: ComponentFixture<FechaLimiteEtapaDetail>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideTranslateService(),
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./fecha-limite-etapa-detail').then(m => m.FechaLimiteEtapaDetail),
              resolve: { fechaLimiteEtapa: () => of({ id: 31350 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    });
    const library = TestBed.inject(FaIconLibrary);
    library.addIcons(faArrowLeft);
    library.addIcons(faPencilAlt);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FechaLimiteEtapaDetail);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load fechaLimiteEtapa on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', FechaLimiteEtapaDetail);

      // THEN
      expect(instance.fechaLimiteEtapa()).toEqual(expect.objectContaining({ id: 31350 }));
    });
  });

  describe('PreviousState', () => {
    it('should navigate to previous state', () => {
      vi.spyOn(globalThis.history, 'back');
      comp.previousState();
      expect(globalThis.history.back).toHaveBeenCalled();
    });
  });
});
