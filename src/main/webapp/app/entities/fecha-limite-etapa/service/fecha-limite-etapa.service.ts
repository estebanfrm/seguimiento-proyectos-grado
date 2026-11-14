import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Service, computed, inject, signal } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { serverApiUrl } from 'app/config';
import { createRequestOption } from 'app/core/request';
import { IFechaLimiteEtapa, NewFechaLimiteEtapa } from '../fecha-limite-etapa.model';

export type PartialUpdateFechaLimiteEtapa = Partial<IFechaLimiteEtapa> & Pick<IFechaLimiteEtapa, 'id'>;

type RestOf<T extends IFechaLimiteEtapa | NewFechaLimiteEtapa> = Omit<T, 'fechaLimite'> & {
  fechaLimite?: string | null;
};

export type RestFechaLimiteEtapa = RestOf<IFechaLimiteEtapa>;

export type NewRestFechaLimiteEtapa = RestOf<NewFechaLimiteEtapa>;

export type PartialUpdateRestFechaLimiteEtapa = RestOf<PartialUpdateFechaLimiteEtapa>;

@Service()
export class FechaLimiteEtapasService {
  readonly fechaLimiteEtapasParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly fechaLimiteEtapasResource = httpResource<RestFechaLimiteEtapa[]>(() => {
    const params = this.fechaLimiteEtapasParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of fechaLimiteEtapa that have been fetched. It is updated when the fechaLimiteEtapasResource emits a new value.
   * In case of error while fetching the fechaLimiteEtapas, the signal is set to an empty array.
   */
  readonly fechaLimiteEtapas = computed(() =>
    (this.fechaLimiteEtapasResource.hasValue() ? this.fechaLimiteEtapasResource.value() : []).map(item =>
      this.convertValueFromServer(item),
    ),
  );
  protected readonly resourceUrl = `${serverApiUrl}api/fecha-limite-etapas`;

  protected convertValueFromServer(restFechaLimiteEtapa: RestFechaLimiteEtapa): IFechaLimiteEtapa {
    return {
      ...restFechaLimiteEtapa,
      fechaLimite: restFechaLimiteEtapa.fechaLimite ? dayjs(restFechaLimiteEtapa.fechaLimite) : undefined,
    };
  }
}

@Service()
export class FechaLimiteEtapaService extends FechaLimiteEtapasService {
  protected readonly http = inject(HttpClient);

  create(fechaLimiteEtapa: NewFechaLimiteEtapa): Observable<IFechaLimiteEtapa> {
    const copy = this.convertValueFromClient(fechaLimiteEtapa);
    return this.http.post<RestFechaLimiteEtapa>(this.resourceUrl, copy).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(fechaLimiteEtapa: IFechaLimiteEtapa): Observable<IFechaLimiteEtapa> {
    const copy = this.convertValueFromClient(fechaLimiteEtapa);
    return this.http
      .put<RestFechaLimiteEtapa>(`${this.resourceUrl}/${encodeURIComponent(this.getFechaLimiteEtapaIdentifier(fechaLimiteEtapa))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(fechaLimiteEtapa: PartialUpdateFechaLimiteEtapa): Observable<IFechaLimiteEtapa> {
    const copy = this.convertValueFromClient(fechaLimiteEtapa);
    return this.http
      .patch<RestFechaLimiteEtapa>(`${this.resourceUrl}/${encodeURIComponent(this.getFechaLimiteEtapaIdentifier(fechaLimiteEtapa))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<IFechaLimiteEtapa> {
    return this.http
      .get<RestFechaLimiteEtapa>(`${this.resourceUrl}/${encodeURIComponent(id)}`)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<HttpResponse<IFechaLimiteEtapa[]>> {
    const options = createRequestOption(req);
    return this.http
      .get<RestFechaLimiteEtapa[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => res.clone({ body: this.convertResponseArrayFromServer(res.body!) })));
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getFechaLimiteEtapaIdentifier(fechaLimiteEtapa: Pick<IFechaLimiteEtapa, 'id'>): number {
    return fechaLimiteEtapa.id;
  }

  compareFechaLimiteEtapa(o1: Pick<IFechaLimiteEtapa, 'id'> | null, o2: Pick<IFechaLimiteEtapa, 'id'> | null): boolean {
    return o1 && o2 ? this.getFechaLimiteEtapaIdentifier(o1) === this.getFechaLimiteEtapaIdentifier(o2) : o1 === o2;
  }

  addFechaLimiteEtapaToCollectionIfMissing<Type extends Pick<IFechaLimiteEtapa, 'id'>>(
    fechaLimiteEtapaCollection: Type[],
    ...fechaLimiteEtapasToCheck: (Type | null | undefined)[]
  ): Type[] {
    const fechaLimiteEtapas: Type[] = fechaLimiteEtapasToCheck.filter(
      fechaLimiteEtapaItem => fechaLimiteEtapaItem !== null && fechaLimiteEtapaItem !== undefined,
    );
    if (fechaLimiteEtapas.length > 0) {
      const fechaLimiteEtapaCollectionIdentifiers = fechaLimiteEtapaCollection.map(fechaLimiteEtapaItem =>
        this.getFechaLimiteEtapaIdentifier(fechaLimiteEtapaItem),
      );
      const fechaLimiteEtapasToAdd = fechaLimiteEtapas.filter(fechaLimiteEtapaItem => {
        const fechaLimiteEtapaIdentifier = this.getFechaLimiteEtapaIdentifier(fechaLimiteEtapaItem);
        if (fechaLimiteEtapaCollectionIdentifiers.includes(fechaLimiteEtapaIdentifier)) {
          return false;
        }
        fechaLimiteEtapaCollectionIdentifiers.push(fechaLimiteEtapaIdentifier);
        return true;
      });
      return [...fechaLimiteEtapasToAdd, ...fechaLimiteEtapaCollection];
    }
    return fechaLimiteEtapaCollection;
  }

  protected convertValueFromClient<T extends IFechaLimiteEtapa | NewFechaLimiteEtapa | PartialUpdateFechaLimiteEtapa>(
    fechaLimiteEtapa: T,
  ): RestOf<T> {
    return {
      ...fechaLimiteEtapa,
      fechaLimite: fechaLimiteEtapa.fechaLimite?.toJSON() ?? null,
    };
  }

  protected convertResponseFromServer(res: RestFechaLimiteEtapa): IFechaLimiteEtapa {
    return this.convertValueFromServer(res);
  }

  protected convertResponseArrayFromServer(res: RestFechaLimiteEtapa[]): IFechaLimiteEtapa[] {
    return res.map(item => this.convertValueFromServer(item));
  }
}
