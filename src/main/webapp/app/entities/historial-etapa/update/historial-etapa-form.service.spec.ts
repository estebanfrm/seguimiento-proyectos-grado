import { beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../historial-etapa.test-samples';

import { HistorialEtapaFormService } from './historial-etapa-form.service';

describe('HistorialEtapa Form Service', () => {
  let service: HistorialEtapaFormService;

  beforeEach(() => {
    service = TestBed.inject(HistorialEtapaFormService);
  });

  describe('Service methods', () => {
    describe('createHistorialEtapaFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createHistorialEtapaFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            etapaOrigen: expect.any(Object),
            etapaDestino: expect.any(Object),
            fecha: expect.any(Object),
            comentario: expect.any(Object),
            usuario: expect.any(Object),
            proyecto: expect.any(Object),
          }),
        );
      });

      it('passing IHistorialEtapa should create a new form with FormGroup', () => {
        const formGroup = service.createHistorialEtapaFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            etapaOrigen: expect.any(Object),
            etapaDestino: expect.any(Object),
            fecha: expect.any(Object),
            comentario: expect.any(Object),
            usuario: expect.any(Object),
            proyecto: expect.any(Object),
          }),
        );
      });
    });

    describe('getHistorialEtapa', () => {
      it('should return NewHistorialEtapa for default HistorialEtapa initial value', () => {
        const formGroup = service.createHistorialEtapaFormGroup(sampleWithNewData);

        const historialEtapa = service.getHistorialEtapa(formGroup);

        expect(historialEtapa).toMatchObject(sampleWithNewData);
      });

      it('should return NewHistorialEtapa for empty HistorialEtapa initial value', () => {
        const formGroup = service.createHistorialEtapaFormGroup();

        const historialEtapa = service.getHistorialEtapa(formGroup);

        expect(historialEtapa).toMatchObject({});
      });

      it('should return IHistorialEtapa', () => {
        const formGroup = service.createHistorialEtapaFormGroup(sampleWithRequiredData);

        const historialEtapa = service.getHistorialEtapa(formGroup);

        expect(historialEtapa).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IHistorialEtapa should not enable id FormControl', () => {
        const formGroup = service.createHistorialEtapaFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewHistorialEtapa should disable id FormControl', () => {
        const formGroup = service.createHistorialEtapaFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
