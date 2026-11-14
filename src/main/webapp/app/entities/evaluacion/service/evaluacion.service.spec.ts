import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { IEvaluacion } from '../evaluacion.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../evaluacion.test-samples';

import { EvaluacionService, RestEvaluacion } from './evaluacion.service';

const requireRestSample: RestEvaluacion = {
  ...sampleWithRequiredData,
  fechaEvaluacion: sampleWithRequiredData.fechaEvaluacion?.toJSON(),
};

describe('Evaluacion Service', () => {
  let service: EvaluacionService;
  let httpMock: HttpTestingController;
  let expectedResult: IEvaluacion | IEvaluacion[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(EvaluacionService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find(123).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a Evaluacion', () => {
      const evaluacion = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(evaluacion).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Evaluacion', () => {
      const evaluacion = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(evaluacion).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Evaluacion', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Evaluacion', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Evaluacion', () => {
      service.delete(123).subscribe();

      const requests = httpMock.match({ method: 'DELETE' });
      expect(requests).toHaveLength(1);
    });

    describe('addEvaluacionToCollectionIfMissing', () => {
      it('should add a Evaluacion to an empty array', () => {
        const evaluacion: IEvaluacion = sampleWithRequiredData;
        expectedResult = service.addEvaluacionToCollectionIfMissing([], evaluacion);
        expect(expectedResult).toEqual([evaluacion]);
      });

      it('should not add a Evaluacion to an array that contains it', () => {
        const evaluacion: IEvaluacion = sampleWithRequiredData;
        const evaluacionCollection: IEvaluacion[] = [
          {
            ...evaluacion,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addEvaluacionToCollectionIfMissing(evaluacionCollection, evaluacion);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Evaluacion to an array that doesn't contain it", () => {
        const evaluacion: IEvaluacion = sampleWithRequiredData;
        const evaluacionCollection: IEvaluacion[] = [sampleWithPartialData];
        expectedResult = service.addEvaluacionToCollectionIfMissing(evaluacionCollection, evaluacion);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(evaluacion);
      });

      it('should add only unique Evaluacion to an array', () => {
        const evaluacionArray: IEvaluacion[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const evaluacionCollection: IEvaluacion[] = [sampleWithRequiredData];
        expectedResult = service.addEvaluacionToCollectionIfMissing(evaluacionCollection, ...evaluacionArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const evaluacion: IEvaluacion = sampleWithRequiredData;
        const evaluacion2: IEvaluacion = sampleWithPartialData;
        expectedResult = service.addEvaluacionToCollectionIfMissing([], evaluacion, evaluacion2);
        expect(expectedResult).toEqual([evaluacion, evaluacion2]);
      });

      it('should accept null and undefined values', () => {
        const evaluacion: IEvaluacion = sampleWithRequiredData;
        expectedResult = service.addEvaluacionToCollectionIfMissing([], null, evaluacion, undefined);
        expect(expectedResult).toEqual([evaluacion]);
      });

      it('should return initial array if no Evaluacion is added', () => {
        const evaluacionCollection: IEvaluacion[] = [sampleWithRequiredData];
        expectedResult = service.addEvaluacionToCollectionIfMissing(evaluacionCollection, undefined, null);
        expect(expectedResult).toEqual(evaluacionCollection);
      });
    });

    describe('compareEvaluacion', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareEvaluacion(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 10220 };
        const entity2 = null;

        const compareResult1 = service.compareEvaluacion(entity1, entity2);
        const compareResult2 = service.compareEvaluacion(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 10220 };
        const entity2 = { id: 1988 };

        const compareResult1 = service.compareEvaluacion(entity1, entity2);
        const compareResult2 = service.compareEvaluacion(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return true if primaryKey matches', () => {
        const entity1 = { id: 10220 };
        const entity2 = { id: 10220 };

        const compareResult1 = service.compareEvaluacion(entity1, entity2);
        const compareResult2 = service.compareEvaluacion(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
