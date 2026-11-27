import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { IFechaLimiteEtapa } from '../fecha-limite-etapa.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../fecha-limite-etapa.test-samples';

import { FechaLimiteEtapaService, RestFechaLimiteEtapa } from './fecha-limite-etapa.service';

const requireRestSample: RestFechaLimiteEtapa = {
  ...sampleWithRequiredData,
  fechaLimite: sampleWithRequiredData.fechaLimite?.toJSON(),
};

describe('FechaLimiteEtapa Service', () => {
  let service: FechaLimiteEtapaService;
  let httpMock: HttpTestingController;
  let expectedResult: IFechaLimiteEtapa | IFechaLimiteEtapa[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(FechaLimiteEtapaService);
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

    it('should create a FechaLimiteEtapa', () => {
      const fechaLimiteEtapa = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(fechaLimiteEtapa).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a FechaLimiteEtapa', () => {
      const fechaLimiteEtapa = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(fechaLimiteEtapa).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a FechaLimiteEtapa', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of FechaLimiteEtapa', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a FechaLimiteEtapa', () => {
      service.delete(123).subscribe();

      const requests = httpMock.match({ method: 'DELETE' });
      expect(requests).toHaveLength(1);
    });

    describe('addFechaLimiteEtapaToCollectionIfMissing', () => {
      it('should add a FechaLimiteEtapa to an empty array', () => {
        const fechaLimiteEtapa: IFechaLimiteEtapa = sampleWithRequiredData;
        expectedResult = service.addFechaLimiteEtapaToCollectionIfMissing([], fechaLimiteEtapa);
        expect(expectedResult).toEqual([fechaLimiteEtapa]);
      });

      it('should not add a FechaLimiteEtapa to an array that contains it', () => {
        const fechaLimiteEtapa: IFechaLimiteEtapa = sampleWithRequiredData;
        const fechaLimiteEtapaCollection: IFechaLimiteEtapa[] = [
          {
            ...fechaLimiteEtapa,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addFechaLimiteEtapaToCollectionIfMissing(fechaLimiteEtapaCollection, fechaLimiteEtapa);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a FechaLimiteEtapa to an array that doesn't contain it", () => {
        const fechaLimiteEtapa: IFechaLimiteEtapa = sampleWithRequiredData;
        const fechaLimiteEtapaCollection: IFechaLimiteEtapa[] = [sampleWithPartialData];
        expectedResult = service.addFechaLimiteEtapaToCollectionIfMissing(fechaLimiteEtapaCollection, fechaLimiteEtapa);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(fechaLimiteEtapa);
      });

      it('should add only unique FechaLimiteEtapa to an array', () => {
        const fechaLimiteEtapaArray: IFechaLimiteEtapa[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const fechaLimiteEtapaCollection: IFechaLimiteEtapa[] = [sampleWithRequiredData];
        expectedResult = service.addFechaLimiteEtapaToCollectionIfMissing(fechaLimiteEtapaCollection, ...fechaLimiteEtapaArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const fechaLimiteEtapa: IFechaLimiteEtapa = sampleWithRequiredData;
        const fechaLimiteEtapa2: IFechaLimiteEtapa = sampleWithPartialData;
        expectedResult = service.addFechaLimiteEtapaToCollectionIfMissing([], fechaLimiteEtapa, fechaLimiteEtapa2);
        expect(expectedResult).toEqual([fechaLimiteEtapa, fechaLimiteEtapa2]);
      });

      it('should accept null and undefined values', () => {
        const fechaLimiteEtapa: IFechaLimiteEtapa = sampleWithRequiredData;
        expectedResult = service.addFechaLimiteEtapaToCollectionIfMissing([], null, fechaLimiteEtapa, undefined);
        expect(expectedResult).toEqual([fechaLimiteEtapa]);
      });

      it('should return initial array if no FechaLimiteEtapa is added', () => {
        const fechaLimiteEtapaCollection: IFechaLimiteEtapa[] = [sampleWithRequiredData];
        expectedResult = service.addFechaLimiteEtapaToCollectionIfMissing(fechaLimiteEtapaCollection, undefined, null);
        expect(expectedResult).toEqual(fechaLimiteEtapaCollection);
      });
    });

    describe('compareFechaLimiteEtapa', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareFechaLimiteEtapa(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 31350 };
        const entity2 = null;

        const compareResult1 = service.compareFechaLimiteEtapa(entity1, entity2);
        const compareResult2 = service.compareFechaLimiteEtapa(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 31350 };
        const entity2 = { id: 20756 };

        const compareResult1 = service.compareFechaLimiteEtapa(entity1, entity2);
        const compareResult2 = service.compareFechaLimiteEtapa(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return true if primaryKey matches', () => {
        const entity1 = { id: 31350 };
        const entity2 = { id: 31350 };

        const compareResult1 = service.compareFechaLimiteEtapa(entity1, entity2);
        const compareResult2 = service.compareFechaLimiteEtapa(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
