import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { IProyectoGrado } from '../proyecto-grado.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../proyecto-grado.test-samples';

import { ProyectoGradoService, RestProyectoGrado } from './proyecto-grado.service';

const requireRestSample: RestProyectoGrado = {
  ...sampleWithRequiredData,
  fechaRegistro: sampleWithRequiredData.fechaRegistro?.toJSON(),
  fechaActualizacion: sampleWithRequiredData.fechaActualizacion?.toJSON(),
};

describe('ProyectoGrado Service', () => {
  let service: ProyectoGradoService;
  let httpMock: HttpTestingController;
  let expectedResult: IProyectoGrado | IProyectoGrado[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(ProyectoGradoService);
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

    it('should create a ProyectoGrado', () => {
      const proyectoGrado = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(proyectoGrado).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a ProyectoGrado', () => {
      const proyectoGrado = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(proyectoGrado).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a ProyectoGrado', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of ProyectoGrado', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a ProyectoGrado', () => {
      service.delete(123).subscribe();

      const requests = httpMock.match({ method: 'DELETE' });
      expect(requests).toHaveLength(1);
    });

    describe('addProyectoGradoToCollectionIfMissing', () => {
      it('should add a ProyectoGrado to an empty array', () => {
        const proyectoGrado: IProyectoGrado = sampleWithRequiredData;
        expectedResult = service.addProyectoGradoToCollectionIfMissing([], proyectoGrado);
        expect(expectedResult).toEqual([proyectoGrado]);
      });

      it('should not add a ProyectoGrado to an array that contains it', () => {
        const proyectoGrado: IProyectoGrado = sampleWithRequiredData;
        const proyectoGradoCollection: IProyectoGrado[] = [
          {
            ...proyectoGrado,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addProyectoGradoToCollectionIfMissing(proyectoGradoCollection, proyectoGrado);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a ProyectoGrado to an array that doesn't contain it", () => {
        const proyectoGrado: IProyectoGrado = sampleWithRequiredData;
        const proyectoGradoCollection: IProyectoGrado[] = [sampleWithPartialData];
        expectedResult = service.addProyectoGradoToCollectionIfMissing(proyectoGradoCollection, proyectoGrado);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(proyectoGrado);
      });

      it('should add only unique ProyectoGrado to an array', () => {
        const proyectoGradoArray: IProyectoGrado[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const proyectoGradoCollection: IProyectoGrado[] = [sampleWithRequiredData];
        expectedResult = service.addProyectoGradoToCollectionIfMissing(proyectoGradoCollection, ...proyectoGradoArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const proyectoGrado: IProyectoGrado = sampleWithRequiredData;
        const proyectoGrado2: IProyectoGrado = sampleWithPartialData;
        expectedResult = service.addProyectoGradoToCollectionIfMissing([], proyectoGrado, proyectoGrado2);
        expect(expectedResult).toEqual([proyectoGrado, proyectoGrado2]);
      });

      it('should accept null and undefined values', () => {
        const proyectoGrado: IProyectoGrado = sampleWithRequiredData;
        expectedResult = service.addProyectoGradoToCollectionIfMissing([], null, proyectoGrado, undefined);
        expect(expectedResult).toEqual([proyectoGrado]);
      });

      it('should return initial array if no ProyectoGrado is added', () => {
        const proyectoGradoCollection: IProyectoGrado[] = [sampleWithRequiredData];
        expectedResult = service.addProyectoGradoToCollectionIfMissing(proyectoGradoCollection, undefined, null);
        expect(expectedResult).toEqual(proyectoGradoCollection);
      });
    });

    describe('compareProyectoGrado', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareProyectoGrado(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 32010 };
        const entity2 = null;

        const compareResult1 = service.compareProyectoGrado(entity1, entity2);
        const compareResult2 = service.compareProyectoGrado(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 32010 };
        const entity2 = { id: 28782 };

        const compareResult1 = service.compareProyectoGrado(entity1, entity2);
        const compareResult2 = service.compareProyectoGrado(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return true if primaryKey matches', () => {
        const entity1 = { id: 32010 };
        const entity2 = { id: 32010 };

        const compareResult1 = service.compareProyectoGrado(entity1, entity2);
        const compareResult2 = service.compareProyectoGrado(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
