import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Service, computed, inject, signal } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { serverApiUrl } from 'app/config';
import { createRequestOption } from 'app/core/request';
import { IObservacion, NewObservacion } from '../observacion.model';

export type PartialUpdateObservacion = Partial<IObservacion> & Pick<IObservacion, 'id'>;

type RestOf<T extends IObservacion | NewObservacion> = Omit<T, 'fechaCreacion'> & {
  fechaCreacion?: string | null;
};

export type RestObservacion = RestOf<IObservacion>;

export type NewRestObservacion = RestOf<NewObservacion>;

export type PartialUpdateRestObservacion = RestOf<PartialUpdateObservacion>;

@Service()
export class ObservacionsService {
  readonly observacionsParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly observacionsResource = httpResource<RestObservacion[]>(() => {
    const params = this.observacionsParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of observacion that have been fetched. It is updated when the observacionsResource emits a new value.
   * In case of error while fetching the observacions, the signal is set to an empty array.
   */
  readonly observacions = computed(() =>
    (this.observacionsResource.hasValue() ? this.observacionsResource.value() : []).map(item => this.convertValueFromServer(item)),
  );
  protected readonly resourceUrl = `${serverApiUrl}api/observacions`;

  protected convertValueFromServer(restObservacion: RestObservacion): IObservacion {
    return {
      ...restObservacion,
      fechaCreacion: restObservacion.fechaCreacion ? dayjs(restObservacion.fechaCreacion) : undefined,
    };
  }
}

@Service()
export class ObservacionService extends ObservacionsService {
  protected readonly http = inject(HttpClient);

  create(observacion: NewObservacion): Observable<IObservacion> {
    const copy = this.convertValueFromClient(observacion);
    return this.http.post<RestObservacion>(this.resourceUrl, copy).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(observacion: IObservacion): Observable<IObservacion> {
    const copy = this.convertValueFromClient(observacion);
    return this.http
      .put<RestObservacion>(`${this.resourceUrl}/${encodeURIComponent(this.getObservacionIdentifier(observacion))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(observacion: PartialUpdateObservacion): Observable<IObservacion> {
    const copy = this.convertValueFromClient(observacion);
    return this.http
      .patch<RestObservacion>(`${this.resourceUrl}/${encodeURIComponent(this.getObservacionIdentifier(observacion))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<IObservacion> {
    return this.http
      .get<RestObservacion>(`${this.resourceUrl}/${encodeURIComponent(id)}`)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<HttpResponse<IObservacion[]>> {
    const options = createRequestOption(req);
    return this.http
      .get<RestObservacion[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => res.clone({ body: this.convertResponseArrayFromServer(res.body!) })));
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getObservacionIdentifier(observacion: Pick<IObservacion, 'id'>): number {
    return observacion.id;
  }

  compareObservacion(o1: Pick<IObservacion, 'id'> | null, o2: Pick<IObservacion, 'id'> | null): boolean {
    return o1 && o2 ? this.getObservacionIdentifier(o1) === this.getObservacionIdentifier(o2) : o1 === o2;
  }

  addObservacionToCollectionIfMissing<Type extends Pick<IObservacion, 'id'>>(
    observacionCollection: Type[],
    ...observacionsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const observacions: Type[] = observacionsToCheck.filter(observacionItem => observacionItem !== null && observacionItem !== undefined);
    if (observacions.length > 0) {
      const observacionCollectionIdentifiers = observacionCollection.map(observacionItem => this.getObservacionIdentifier(observacionItem));
      const observacionsToAdd = observacions.filter(observacionItem => {
        const observacionIdentifier = this.getObservacionIdentifier(observacionItem);
        if (observacionCollectionIdentifiers.includes(observacionIdentifier)) {
          return false;
        }
        observacionCollectionIdentifiers.push(observacionIdentifier);
        return true;
      });
      return [...observacionsToAdd, ...observacionCollection];
    }
    return observacionCollection;
  }

  protected convertValueFromClient<T extends IObservacion | NewObservacion | PartialUpdateObservacion>(observacion: T): RestOf<T> {
    return {
      ...observacion,
      fechaCreacion: observacion.fechaCreacion?.toJSON() ?? null,
    };
  }

  protected convertResponseFromServer(res: RestObservacion): IObservacion {
    return this.convertValueFromServer(res);
  }

  protected convertResponseArrayFromServer(res: RestObservacion[]): IObservacion[] {
    return res.map(item => this.convertValueFromServer(item));
  }
}
