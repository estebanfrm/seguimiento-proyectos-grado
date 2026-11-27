import { beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../fecha-limite-etapa.test-samples';

import { FechaLimiteEtapaFormService } from './fecha-limite-etapa-form.service';

describe('FechaLimiteEtapa Form Service', () => {
  let service: FechaLimiteEtapaFormService;

  beforeEach(() => {
    service = TestBed.inject(FechaLimiteEtapaFormService);
  });

  describe('Service methods', () => {
    describe('createFechaLimiteEtapaFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createFechaLimiteEtapaFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            etapa: expect.any(Object),
            fechaLimite: expect.any(Object),
            descripcion: expect.any(Object),
            proyecto: expect.any(Object),
          }),
        );
      });

      it('passing IFechaLimiteEtapa should create a new form with FormGroup', () => {
        const formGroup = service.createFechaLimiteEtapaFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            etapa: expect.any(Object),
            fechaLimite: expect.any(Object),
            descripcion: expect.any(Object),
            proyecto: expect.any(Object),
          }),
        );
      });
    });

    describe('getFechaLimiteEtapa', () => {
      it('should return NewFechaLimiteEtapa for default FechaLimiteEtapa initial value', () => {
        const formGroup = service.createFechaLimiteEtapaFormGroup(sampleWithNewData);

        const fechaLimiteEtapa = service.getFechaLimiteEtapa(formGroup);

        expect(fechaLimiteEtapa).toMatchObject(sampleWithNewData);
      });

      it('should return NewFechaLimiteEtapa for empty FechaLimiteEtapa initial value', () => {
        const formGroup = service.createFechaLimiteEtapaFormGroup();

        const fechaLimiteEtapa = service.getFechaLimiteEtapa(formGroup);

        expect(fechaLimiteEtapa).toMatchObject({});
      });

      it('should return IFechaLimiteEtapa', () => {
        const formGroup = service.createFechaLimiteEtapaFormGroup(sampleWithRequiredData);

        const fechaLimiteEtapa = service.getFechaLimiteEtapa(formGroup);

        expect(fechaLimiteEtapa).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IFechaLimiteEtapa should not enable id FormControl', () => {
        const formGroup = service.createFechaLimiteEtapaFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewFechaLimiteEtapa should disable id FormControl', () => {
        const formGroup = service.createFechaLimiteEtapaFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
