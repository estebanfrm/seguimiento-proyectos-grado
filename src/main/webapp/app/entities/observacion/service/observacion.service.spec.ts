import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { IObservacion } from '../observacion.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../observacion.test-samples';

import { ObservacionService, RestObservacion } from './observacion.service';

const requireRestSample: RestObservacion = {
  ...sampleWithRequiredData,
  fechaCreacion: sampleWithRequiredData.fechaCreacion?.toJSON(),
};

describe('Observacion Service', () => {
  let service: ObservacionService;
  let httpMock: HttpTestingController;
  let expectedResult: IObservacion | IObservacion[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(ObservacionService);
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

    it('should create a Observacion', () => {
      const observacion = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(observacion).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Observacion', () => {
      const observacion = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(observacion).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Observacion', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Observacion', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Observacion', () => {
      service.delete(123).subscribe();

      const requests = httpMock.match({ method: 'DELETE' });
      expect(requests).toHaveLength(1);
    });

    describe('addObservacionToCollectionIfMissing', () => {
      it('should add a Observacion to an empty array', () => {
        const observacion: IObservacion = sampleWithRequiredData;
        expectedResult = service.addObservacionToCollectionIfMissing([], observacion);
        expect(expectedResult).toEqual([observacion]);
      });

      it('should not add a Observacion to an array that contains it', () => {
        const observacion: IObservacion = sampleWithRequiredData;
        const observacionCollection: IObservacion[] = [
          {
            ...observacion,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addObservacionToCollectionIfMissing(observacionCollection, observacion);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Observacion to an array that doesn't contain it", () => {
        const observacion: IObservacion = sampleWithRequiredData;
        const observacionCollection: IObservacion[] = [sampleWithPartialData];
        expectedResult = service.addObservacionToCollectionIfMissing(observacionCollection, observacion);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(observacion);
      });

      it('should add only unique Observacion to an array', () => {
        const observacionArray: IObservacion[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const observacionCollection: IObservacion[] = [sampleWithRequiredData];
        expectedResult = service.addObservacionToCollectionIfMissing(observacionCollection, ...observacionArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const observacion: IObservacion = sampleWithRequiredData;
        const observacion2: IObservacion = sampleWithPartialData;
        expectedResult = service.addObservacionToCollectionIfMissing([], observacion, observacion2);
        expect(expectedResult).toEqual([observacion, observacion2]);
      });

      it('should accept null and undefined values', () => {
        const observacion: IObservacion = sampleWithRequiredData;
        expectedResult = service.addObservacionToCollectionIfMissing([], null, observacion, undefined);
        expect(expectedResult).toEqual([observacion]);
      });

      it('should return initial array if no Observacion is added', () => {
        const observacionCollection: IObservacion[] = [sampleWithRequiredData];
        expectedResult = service.addObservacionToCollectionIfMissing(observacionCollection, undefined, null);
        expect(expectedResult).toEqual(observacionCollection);
      });
    });

    describe('compareObservacion', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareObservacion(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 9028 };
        const entity2 = null;

        const compareResult1 = service.compareObservacion(entity1, entity2);
        const compareResult2 = service.compareObservacion(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 9028 };
        const entity2 = { id: 6876 };

        const compareResult1 = service.compareObservacion(entity1, entity2);
        const compareResult2 = service.compareObservacion(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return true if primaryKey matches', () => {
        const entity1 = { id: 9028 };
        const entity2 = { id: 9028 };

        const compareResult1 = service.compareObservacion(entity1, entity2);
        const compareResult2 = service.compareObservacion(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
