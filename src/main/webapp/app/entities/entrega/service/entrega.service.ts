import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Service, computed, inject, signal } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { serverApiUrl } from 'app/config';
import { createRequestOption } from 'app/core/request';
import { IEntrega, NewEntrega } from '../entrega.model';

export type PartialUpdateEntrega = Partial<IEntrega> & Pick<IEntrega, 'id'>;

type RestOf<T extends IEntrega | NewEntrega> = Omit<T, 'fechaEntrega'> & {
  fechaEntrega?: string | null;
};

export type RestEntrega = RestOf<IEntrega>;

export type NewRestEntrega = RestOf<NewEntrega>;

export type PartialUpdateRestEntrega = RestOf<PartialUpdateEntrega>;

@Service()
export class EntregasService {
  readonly entregasParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly entregasResource = httpResource<RestEntrega[]>(() => {
    const params = this.entregasParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of entrega that have been fetched. It is updated when the entregasResource emits a new value.
   * In case of error while fetching the entregas, the signal is set to an empty array.
   */
  readonly entregas = computed(() =>
    (this.entregasResource.hasValue() ? this.entregasResource.value() : []).map(item => this.convertValueFromServer(item)),
  );
  protected readonly resourceUrl = `${serverApiUrl}api/entregas`;

  protected convertValueFromServer(restEntrega: RestEntrega): IEntrega {
    return {
      ...restEntrega,
      fechaEntrega: restEntrega.fechaEntrega ? dayjs(restEntrega.fechaEntrega) : undefined,
    };
  }
}

@Service()
export class EntregaService extends EntregasService {
  protected readonly http = inject(HttpClient);

  create(entrega: NewEntrega): Observable<IEntrega> {
    const copy = this.convertValueFromClient(entrega);
    return this.http.post<RestEntrega>(this.resourceUrl, copy).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(entrega: IEntrega): Observable<IEntrega> {
    const copy = this.convertValueFromClient(entrega);
    return this.http
      .put<RestEntrega>(`${this.resourceUrl}/${encodeURIComponent(this.getEntregaIdentifier(entrega))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(entrega: PartialUpdateEntrega): Observable<IEntrega> {
    const copy = this.convertValueFromClient(entrega);
    return this.http
      .patch<RestEntrega>(`${this.resourceUrl}/${encodeURIComponent(this.getEntregaIdentifier(entrega))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<IEntrega> {
    return this.http
      .get<RestEntrega>(`${this.resourceUrl}/${encodeURIComponent(id)}`)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<HttpResponse<IEntrega[]>> {
    const options = createRequestOption(req);
    return this.http
      .get<RestEntrega[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => res.clone({ body: this.convertResponseArrayFromServer(res.body!) })));
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getEntregaIdentifier(entrega: Pick<IEntrega, 'id'>): number {
    return entrega.id;
  }

  compareEntrega(o1: Pick<IEntrega, 'id'> | null, o2: Pick<IEntrega, 'id'> | null): boolean {
    return o1 && o2 ? this.getEntregaIdentifier(o1) === this.getEntregaIdentifier(o2) : o1 === o2;
  }

  addEntregaToCollectionIfMissing<Type extends Pick<IEntrega, 'id'>>(
    entregaCollection: Type[],
    ...entregasToCheck: (Type | null | undefined)[]
  ): Type[] {
    const entregas: Type[] = entregasToCheck.filter(entregaItem => entregaItem !== null && entregaItem !== undefined);
    if (entregas.length > 0) {
      const entregaCollectionIdentifiers = entregaCollection.map(entregaItem => this.getEntregaIdentifier(entregaItem));
      const entregasToAdd = entregas.filter(entregaItem => {
        const entregaIdentifier = this.getEntregaIdentifier(entregaItem);
        if (entregaCollectionIdentifiers.includes(entregaIdentifier)) {
          return false;
        }
        entregaCollectionIdentifiers.push(entregaIdentifier);
        return true;
      });
      return [...entregasToAdd, ...entregaCollection];
    }
    return entregaCollection;
  }

  protected convertValueFromClient<T extends IEntrega | NewEntrega | PartialUpdateEntrega>(entrega: T): RestOf<T> {
    return {
      ...entrega,
      fechaEntrega: entrega.fechaEntrega?.toJSON() ?? null,
    };
  }

  protected convertResponseFromServer(res: RestEntrega): IEntrega {
    return this.convertValueFromServer(res);
  }

  protected convertResponseArrayFromServer(res: RestEntrega[]): IEntrega[] {
    return res.map(item => this.convertValueFromServer(item));
  }
}
