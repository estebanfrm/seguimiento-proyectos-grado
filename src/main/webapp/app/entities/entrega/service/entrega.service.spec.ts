import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { IEntrega } from '../entrega.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../entrega.test-samples';

import { EntregaService, RestEntrega } from './entrega.service';

const requireRestSample: RestEntrega = {
  ...sampleWithRequiredData,
  fechaEntrega: sampleWithRequiredData.fechaEntrega?.toJSON(),
};

describe('Entrega Service', () => {
  let service: EntregaService;
  let httpMock: HttpTestingController;
  let expectedResult: IEntrega | IEntrega[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(EntregaService);
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

    it('should create a Entrega', () => {
      const entrega = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(entrega).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Entrega', () => {
      const entrega = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(entrega).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Entrega', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Entrega', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Entrega', () => {
      service.delete(123).subscribe();

      const requests = httpMock.match({ method: 'DELETE' });
      expect(requests).toHaveLength(1);
    });

    describe('addEntregaToCollectionIfMissing', () => {
      it('should add a Entrega to an empty array', () => {
        const entrega: IEntrega = sampleWithRequiredData;
        expectedResult = service.addEntregaToCollectionIfMissing([], entrega);
        expect(expectedResult).toEqual([entrega]);
      });

      it('should not add a Entrega to an array that contains it', () => {
        const entrega: IEntrega = sampleWithRequiredData;
        const entregaCollection: IEntrega[] = [
          {
            ...entrega,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addEntregaToCollectionIfMissing(entregaCollection, entrega);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Entrega to an array that doesn't contain it", () => {
        const entrega: IEntrega = sampleWithRequiredData;
        const entregaCollection: IEntrega[] = [sampleWithPartialData];
        expectedResult = service.addEntregaToCollectionIfMissing(entregaCollection, entrega);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(entrega);
      });

      it('should add only unique Entrega to an array', () => {
        const entregaArray: IEntrega[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const entregaCollection: IEntrega[] = [sampleWithRequiredData];
        expectedResult = service.addEntregaToCollectionIfMissing(entregaCollection, ...entregaArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const entrega: IEntrega = sampleWithRequiredData;
        const entrega2: IEntrega = sampleWithPartialData;
        expectedResult = service.addEntregaToCollectionIfMissing([], entrega, entrega2);
        expect(expectedResult).toEqual([entrega, entrega2]);
      });

      it('should accept null and undefined values', () => {
        const entrega: IEntrega = sampleWithRequiredData;
        expectedResult = service.addEntregaToCollectionIfMissing([], null, entrega, undefined);
        expect(expectedResult).toEqual([entrega]);
      });

      it('should return initial array if no Entrega is added', () => {
        const entregaCollection: IEntrega[] = [sampleWithRequiredData];
        expectedResult = service.addEntregaToCollectionIfMissing(entregaCollection, undefined, null);
        expect(expectedResult).toEqual(entregaCollection);
      });
    });

    describe('compareEntrega', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareEntrega(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 14050 };
        const entity2 = null;

        const compareResult1 = service.compareEntrega(entity1, entity2);
        const compareResult2 = service.compareEntrega(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 14050 };
        const entity2 = { id: 5892 };

        const compareResult1 = service.compareEntrega(entity1, entity2);
        const compareResult2 = service.compareEntrega(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return true if primaryKey matches', () => {
        const entity1 = { id: 14050 };
        const entity2 = { id: 14050 };

        const compareResult1 = service.compareEntrega(entity1, entity2);
        const compareResult2 = service.compareEntrega(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
