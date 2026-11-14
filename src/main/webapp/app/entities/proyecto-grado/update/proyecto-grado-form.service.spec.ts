import { beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../proyecto-grado.test-samples';

import { ProyectoGradoFormService } from './proyecto-grado-form.service';

describe('ProyectoGrado Form Service', () => {
  let service: ProyectoGradoFormService;

  beforeEach(() => {
    service = TestBed.inject(ProyectoGradoFormService);
  });

  describe('Service methods', () => {
    describe('createProyectoGradoFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createProyectoGradoFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            titulo: expect.any(Object),
            resumen: expect.any(Object),
            programa: expect.any(Object),
            etapaActual: expect.any(Object),
            estado: expect.any(Object),
            porcentajeAvance: expect.any(Object),
            fechaRegistro: expect.any(Object),
            fechaActualizacion: expect.any(Object),
            tutor: expect.any(Object),
            estudianteses: expect.any(Object),
            juradoses: expect.any(Object),
          }),
        );
      });

      it('passing IProyectoGrado should create a new form with FormGroup', () => {
        const formGroup = service.createProyectoGradoFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            titulo: expect.any(Object),
            resumen: expect.any(Object),
            programa: expect.any(Object),
            etapaActual: expect.any(Object),
            estado: expect.any(Object),
            porcentajeAvance: expect.any(Object),
            fechaRegistro: expect.any(Object),
            fechaActualizacion: expect.any(Object),
            tutor: expect.any(Object),
            estudianteses: expect.any(Object),
            juradoses: expect.any(Object),
          }),
        );
      });
    });

    describe('getProyectoGrado', () => {
      it('should return NewProyectoGrado for default ProyectoGrado initial value', () => {
        const formGroup = service.createProyectoGradoFormGroup(sampleWithNewData);

        const proyectoGrado = service.getProyectoGrado(formGroup);

        expect(proyectoGrado).toMatchObject(sampleWithNewData);
      });

      it('should return NewProyectoGrado for empty ProyectoGrado initial value', () => {
        const formGroup = service.createProyectoGradoFormGroup();

        const proyectoGrado = service.getProyectoGrado(formGroup);

        expect(proyectoGrado).toMatchObject({});
      });

      it('should return IProyectoGrado', () => {
        const formGroup = service.createProyectoGradoFormGroup(sampleWithRequiredData);

        const proyectoGrado = service.getProyectoGrado(formGroup);

        expect(proyectoGrado).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IProyectoGrado should not enable id FormControl', () => {
        const formGroup = service.createProyectoGradoFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewProyectoGrado should disable id FormControl', () => {
        const formGroup = service.createProyectoGradoFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
