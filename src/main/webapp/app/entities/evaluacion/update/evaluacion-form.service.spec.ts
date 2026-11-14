import { beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../evaluacion.test-samples';

import { EvaluacionFormService } from './evaluacion-form.service';

describe('Evaluacion Form Service', () => {
  let service: EvaluacionFormService;

  beforeEach(() => {
    service = TestBed.inject(EvaluacionFormService);
  });

  describe('Service methods', () => {
    describe('createEvaluacionFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createEvaluacionFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            etapa: expect.any(Object),
            calificacion: expect.any(Object),
            concepto: expect.any(Object),
            comentarios: expect.any(Object),
            fechaEvaluacion: expect.any(Object),
            jurado: expect.any(Object),
            proyecto: expect.any(Object),
          }),
        );
      });

      it('passing IEvaluacion should create a new form with FormGroup', () => {
        const formGroup = service.createEvaluacionFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            etapa: expect.any(Object),
            calificacion: expect.any(Object),
            concepto: expect.any(Object),
            comentarios: expect.any(Object),
            fechaEvaluacion: expect.any(Object),
            jurado: expect.any(Object),
            proyecto: expect.any(Object),
          }),
        );
      });
    });

    describe('getEvaluacion', () => {
      it('should return NewEvaluacion for default Evaluacion initial value', () => {
        const formGroup = service.createEvaluacionFormGroup(sampleWithNewData);

        const evaluacion = service.getEvaluacion(formGroup);

        expect(evaluacion).toMatchObject(sampleWithNewData);
      });

      it('should return NewEvaluacion for empty Evaluacion initial value', () => {
        const formGroup = service.createEvaluacionFormGroup();

        const evaluacion = service.getEvaluacion(formGroup);

        expect(evaluacion).toMatchObject({});
      });

      it('should return IEvaluacion', () => {
        const formGroup = service.createEvaluacionFormGroup(sampleWithRequiredData);

        const evaluacion = service.getEvaluacion(formGroup);

        expect(evaluacion).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IEvaluacion should not enable id FormControl', () => {
        const formGroup = service.createEvaluacionFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewEvaluacion should disable id FormControl', () => {
        const formGroup = service.createEvaluacionFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
