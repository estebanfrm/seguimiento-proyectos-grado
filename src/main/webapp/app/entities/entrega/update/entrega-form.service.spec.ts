import { beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../entrega.test-samples';

import { EntregaFormService } from './entrega-form.service';

describe('Entrega Form Service', () => {
  let service: EntregaFormService;

  beforeEach(() => {
    service = TestBed.inject(EntregaFormService);
  });

  describe('Service methods', () => {
    describe('createEntregaFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createEntregaFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            etapa: expect.any(Object),
            titulo: expect.any(Object),
            descripcion: expect.any(Object),
            archivo: expect.any(Object),
            urlDocumento: expect.any(Object),
            version: expect.any(Object),
            fechaEntrega: expect.any(Object),
            estado: expect.any(Object),
            autor: expect.any(Object),
            proyecto: expect.any(Object),
          }),
        );
      });

      it('passing IEntrega should create a new form with FormGroup', () => {
        const formGroup = service.createEntregaFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            etapa: expect.any(Object),
            titulo: expect.any(Object),
            descripcion: expect.any(Object),
            archivo: expect.any(Object),
            urlDocumento: expect.any(Object),
            version: expect.any(Object),
            fechaEntrega: expect.any(Object),
            estado: expect.any(Object),
            autor: expect.any(Object),
            proyecto: expect.any(Object),
          }),
        );
      });
    });

    describe('getEntrega', () => {
      it('should return NewEntrega for default Entrega initial value', () => {
        const formGroup = service.createEntregaFormGroup(sampleWithNewData);

        const entrega = service.getEntrega(formGroup);

        expect(entrega).toMatchObject(sampleWithNewData);
      });

      it('should return NewEntrega for empty Entrega initial value', () => {
        const formGroup = service.createEntregaFormGroup();

        const entrega = service.getEntrega(formGroup);

        expect(entrega).toMatchObject({});
      });

      it('should return IEntrega', () => {
        const formGroup = service.createEntregaFormGroup(sampleWithRequiredData);

        const entrega = service.getEntrega(formGroup);

        expect(entrega).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IEntrega should not enable id FormControl', () => {
        const formGroup = service.createEntregaFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewEntrega should disable id FormControl', () => {
        const formGroup = service.createEntregaFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
