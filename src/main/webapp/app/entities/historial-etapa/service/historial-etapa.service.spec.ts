import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { IHistorialEtapa } from '../historial-etapa.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../historial-etapa.test-samples';

import { HistorialEtapaService, RestHistorialEtapa } from './historial-etapa.service';

const requireRestSample: RestHistorialEtapa = {
  ...sampleWithRequiredData,
  fecha: sampleWithRequiredData.fecha?.toJSON(),
};

describe('HistorialEtapa Service', () => {
  let service: HistorialEtapaService;
  let httpMock: HttpTestingController;
  let expectedResult: IHistorialEtapa | IHistorialEtapa[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(HistorialEtapaService);
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

    it('should create a HistorialEtapa', () => {
      const historialEtapa = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(historialEtapa).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a HistorialEtapa', () => {
      const historialEtapa = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(historialEtapa).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a HistorialEtapa', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of HistorialEtapa', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a HistorialEtapa', () => {
      service.delete(123).subscribe();

      const requests = httpMock.match({ method: 'DELETE' });
      expect(requests).toHaveLength(1);
    });

    describe('addHistorialEtapaToCollectionIfMissing', () => {
      it('should add a HistorialEtapa to an empty array', () => {
        const historialEtapa: IHistorialEtapa = sampleWithRequiredData;
        expectedResult = service.addHistorialEtapaToCollectionIfMissing([], historialEtapa);
        expect(expectedResult).toEqual([historialEtapa]);
      });

      it('should not add a HistorialEtapa to an array that contains it', () => {
        const historialEtapa: IHistorialEtapa = sampleWithRequiredData;
        const historialEtapaCollection: IHistorialEtapa[] = [
          {
            ...historialEtapa,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addHistorialEtapaToCollectionIfMissing(historialEtapaCollection, historialEtapa);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a HistorialEtapa to an array that doesn't contain it", () => {
        const historialEtapa: IHistorialEtapa = sampleWithRequiredData;
        const historialEtapaCollection: IHistorialEtapa[] = [sampleWithPartialData];
        expectedResult = service.addHistorialEtapaToCollectionIfMissing(historialEtapaCollection, historialEtapa);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(historialEtapa);
      });

      it('should add only unique HistorialEtapa to an array', () => {
        const historialEtapaArray: IHistorialEtapa[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const historialEtapaCollection: IHistorialEtapa[] = [sampleWithRequiredData];
        expectedResult = service.addHistorialEtapaToCollectionIfMissing(historialEtapaCollection, ...historialEtapaArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const historialEtapa: IHistorialEtapa = sampleWithRequiredData;
        const historialEtapa2: IHistorialEtapa = sampleWithPartialData;
        expectedResult = service.addHistorialEtapaToCollectionIfMissing([], historialEtapa, historialEtapa2);
        expect(expectedResult).toEqual([historialEtapa, historialEtapa2]);
      });

      it('should accept null and undefined values', () => {
        const historialEtapa: IHistorialEtapa = sampleWithRequiredData;
        expectedResult = service.addHistorialEtapaToCollectionIfMissing([], null, historialEtapa, undefined);
        expect(expectedResult).toEqual([historialEtapa]);
      });

      it('should return initial array if no HistorialEtapa is added', () => {
        const historialEtapaCollection: IHistorialEtapa[] = [sampleWithRequiredData];
        expectedResult = service.addHistorialEtapaToCollectionIfMissing(historialEtapaCollection, undefined, null);
        expect(expectedResult).toEqual(historialEtapaCollection);
      });
    });

    describe('compareHistorialEtapa', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareHistorialEtapa(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 2724 };
        const entity2 = null;

        const compareResult1 = service.compareHistorialEtapa(entity1, entity2);
        const compareResult2 = service.compareHistorialEtapa(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 2724 };
        const entity2 = { id: 21158 };

        const compareResult1 = service.compareHistorialEtapa(entity1, entity2);
        const compareResult2 = service.compareHistorialEtapa(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return true if primaryKey matches', () => {
        const entity1 = { id: 2724 };
        const entity2 = { id: 2724 };

        const compareResult1 = service.compareHistorialEtapa(entity1, entity2);
        const compareResult2 = service.compareHistorialEtapa(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
