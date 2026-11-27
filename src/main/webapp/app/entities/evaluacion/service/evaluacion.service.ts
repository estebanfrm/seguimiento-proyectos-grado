import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Service, computed, inject, signal } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { serverApiUrl } from 'app/config';
import { createRequestOption } from 'app/core/request';
import { IEvaluacion, NewEvaluacion } from '../evaluacion.model';

export type PartialUpdateEvaluacion = Partial<IEvaluacion> & Pick<IEvaluacion, 'id'>;

type RestOf<T extends IEvaluacion | NewEvaluacion> = Omit<T, 'fechaEvaluacion'> & {
  fechaEvaluacion?: string | null;
};

export type RestEvaluacion = RestOf<IEvaluacion>;

export type NewRestEvaluacion = RestOf<NewEvaluacion>;

export type PartialUpdateRestEvaluacion = RestOf<PartialUpdateEvaluacion>;

@Service()
export class EvaluacionsService {
  readonly evaluacionsParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly evaluacionsResource = httpResource<RestEvaluacion[]>(() => {
    const params = this.evaluacionsParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of evaluacion that have been fetched. It is updated when the evaluacionsResource emits a new value.
   * In case of error while fetching the evaluacions, the signal is set to an empty array.
   */
  readonly evaluacions = computed(() =>
    (this.evaluacionsResource.hasValue() ? this.evaluacionsResource.value() : []).map(item => this.convertValueFromServer(item)),
  );
  protected readonly resourceUrl = `${serverApiUrl}api/evaluacions`;

  protected convertValueFromServer(restEvaluacion: RestEvaluacion): IEvaluacion {
    return {
      ...restEvaluacion,
      fechaEvaluacion: restEvaluacion.fechaEvaluacion ? dayjs(restEvaluacion.fechaEvaluacion) : undefined,
    };
  }
}

@Service()
export class EvaluacionService extends EvaluacionsService {
  protected readonly http = inject(HttpClient);

  create(evaluacion: NewEvaluacion): Observable<IEvaluacion> {
    const copy = this.convertValueFromClient(evaluacion);
    return this.http.post<RestEvaluacion>(this.resourceUrl, copy).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(evaluacion: IEvaluacion): Observable<IEvaluacion> {
    const copy = this.convertValueFromClient(evaluacion);
    return this.http
      .put<RestEvaluacion>(`${this.resourceUrl}/${encodeURIComponent(this.getEvaluacionIdentifier(evaluacion))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(evaluacion: PartialUpdateEvaluacion): Observable<IEvaluacion> {
    const copy = this.convertValueFromClient(evaluacion);
    return this.http
      .patch<RestEvaluacion>(`${this.resourceUrl}/${encodeURIComponent(this.getEvaluacionIdentifier(evaluacion))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<IEvaluacion> {
    return this.http
      .get<RestEvaluacion>(`${this.resourceUrl}/${encodeURIComponent(id)}`)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<HttpResponse<IEvaluacion[]>> {
    const options = createRequestOption(req);
    return this.http
      .get<RestEvaluacion[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => res.clone({ body: this.convertResponseArrayFromServer(res.body!) })));
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getEvaluacionIdentifier(evaluacion: Pick<IEvaluacion, 'id'>): number {
    return evaluacion.id;
  }

  compareEvaluacion(o1: Pick<IEvaluacion, 'id'> | null, o2: Pick<IEvaluacion, 'id'> | null): boolean {
    return o1 && o2 ? this.getEvaluacionIdentifier(o1) === this.getEvaluacionIdentifier(o2) : o1 === o2;
  }

  addEvaluacionToCollectionIfMissing<Type extends Pick<IEvaluacion, 'id'>>(
    evaluacionCollection: Type[],
    ...evaluacionsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const evaluacions: Type[] = evaluacionsToCheck.filter(evaluacionItem => evaluacionItem !== null && evaluacionItem !== undefined);
    if (evaluacions.length > 0) {
      const evaluacionCollectionIdentifiers = evaluacionCollection.map(evaluacionItem => this.getEvaluacionIdentifier(evaluacionItem));
      const evaluacionsToAdd = evaluacions.filter(evaluacionItem => {
        const evaluacionIdentifier = this.getEvaluacionIdentifier(evaluacionItem);
        if (evaluacionCollectionIdentifiers.includes(evaluacionIdentifier)) {
          return false;
        }
        evaluacionCollectionIdentifiers.push(evaluacionIdentifier);
        return true;
      });
      return [...evaluacionsToAdd, ...evaluacionCollection];
    }
    return evaluacionCollection;
  }

  protected convertValueFromClient<T extends IEvaluacion | NewEvaluacion | PartialUpdateEvaluacion>(evaluacion: T): RestOf<T> {
    return {
      ...evaluacion,
      fechaEvaluacion: evaluacion.fechaEvaluacion?.toJSON() ?? null,
    };
  }

  protected convertResponseFromServer(res: RestEvaluacion): IEvaluacion {
    return this.convertValueFromServer(res);
  }

  protected convertResponseArrayFromServer(res: RestEvaluacion[]): IEvaluacion[] {
    return res.map(item => this.convertValueFromServer(item));
  }
}
