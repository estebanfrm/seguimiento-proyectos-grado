import { beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../observacion.test-samples';

import { ObservacionFormService } from './observacion-form.service';

describe('Observacion Form Service', () => {
  let service: ObservacionFormService;

  beforeEach(() => {
    service = TestBed.inject(ObservacionFormService);
  });

  describe('Service methods', () => {
    describe('createObservacionFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createObservacionFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            contenido: expect.any(Object),
            tipo: expect.any(Object),
            resuelta: expect.any(Object),
            fechaCreacion: expect.any(Object),
            autor: expect.any(Object),
            entrega: expect.any(Object),
          }),
        );
      });

      it('passing IObservacion should create a new form with FormGroup', () => {
        const formGroup = service.createObservacionFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            contenido: expect.any(Object),
            tipo: expect.any(Object),
            resuelta: expect.any(Object),
            fechaCreacion: expect.any(Object),
            autor: expect.any(Object),
            entrega: expect.any(Object),
          }),
        );
      });
    });

    describe('getObservacion', () => {
      it('should return NewObservacion for default Observacion initial value', () => {
        const formGroup = service.createObservacionFormGroup(sampleWithNewData);

        const observacion = service.getObservacion(formGroup);

        expect(observacion).toMatchObject(sampleWithNewData);
      });

      it('should return NewObservacion for empty Observacion initial value', () => {
        const formGroup = service.createObservacionFormGroup();

        const observacion = service.getObservacion(formGroup);

        expect(observacion).toMatchObject({});
      });

      it('should return IObservacion', () => {
        const formGroup = service.createObservacionFormGroup(sampleWithRequiredData);

        const observacion = service.getObservacion(formGroup);

        expect(observacion).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IObservacion should not enable id FormControl', () => {
        const formGroup = service.createObservacionFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewObservacion should disable id FormControl', () => {
        const formGroup = service.createObservacionFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
