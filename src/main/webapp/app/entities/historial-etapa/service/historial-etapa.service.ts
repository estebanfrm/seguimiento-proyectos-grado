import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Service, computed, inject, signal } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { serverApiUrl } from 'app/config';
import { createRequestOption } from 'app/core/request';
import { IHistorialEtapa, NewHistorialEtapa } from '../historial-etapa.model';

export type PartialUpdateHistorialEtapa = Partial<IHistorialEtapa> & Pick<IHistorialEtapa, 'id'>;

type RestOf<T extends IHistorialEtapa | NewHistorialEtapa> = Omit<T, 'fecha'> & {
  fecha?: string | null;
};

export type RestHistorialEtapa = RestOf<IHistorialEtapa>;

export type NewRestHistorialEtapa = RestOf<NewHistorialEtapa>;

export type PartialUpdateRestHistorialEtapa = RestOf<PartialUpdateHistorialEtapa>;

@Service()
export class HistorialEtapasService {
  readonly historialEtapasParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly historialEtapasResource = httpResource<RestHistorialEtapa[]>(() => {
    const params = this.historialEtapasParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of historialEtapa that have been fetched. It is updated when the historialEtapasResource emits a new value.
   * In case of error while fetching the historialEtapas, the signal is set to an empty array.
   */
  readonly historialEtapas = computed(() =>
    (this.historialEtapasResource.hasValue() ? this.historialEtapasResource.value() : []).map(item => this.convertValueFromServer(item)),
  );
  protected readonly resourceUrl = `${serverApiUrl}api/historial-etapas`;

  protected convertValueFromServer(restHistorialEtapa: RestHistorialEtapa): IHistorialEtapa {
    return {
      ...restHistorialEtapa,
      fecha: restHistorialEtapa.fecha ? dayjs(restHistorialEtapa.fecha) : undefined,
    };
  }
}

@Service()
export class HistorialEtapaService extends HistorialEtapasService {
  protected readonly http = inject(HttpClient);

  create(historialEtapa: NewHistorialEtapa): Observable<IHistorialEtapa> {
    const copy = this.convertValueFromClient(historialEtapa);
    return this.http.post<RestHistorialEtapa>(this.resourceUrl, copy).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(historialEtapa: IHistorialEtapa): Observable<IHistorialEtapa> {
    const copy = this.convertValueFromClient(historialEtapa);
    return this.http
      .put<RestHistorialEtapa>(`${this.resourceUrl}/${encodeURIComponent(this.getHistorialEtapaIdentifier(historialEtapa))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(historialEtapa: PartialUpdateHistorialEtapa): Observable<IHistorialEtapa> {
    const copy = this.convertValueFromClient(historialEtapa);
    return this.http
      .patch<RestHistorialEtapa>(`${this.resourceUrl}/${encodeURIComponent(this.getHistorialEtapaIdentifier(historialEtapa))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<IHistorialEtapa> {
    return this.http
      .get<RestHistorialEtapa>(`${this.resourceUrl}/${encodeURIComponent(id)}`)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<HttpResponse<IHistorialEtapa[]>> {
    const options = createRequestOption(req);
    return this.http
      .get<RestHistorialEtapa[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => res.clone({ body: this.convertResponseArrayFromServer(res.body!) })));
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getHistorialEtapaIdentifier(historialEtapa: Pick<IHistorialEtapa, 'id'>): number {
    return historialEtapa.id;
  }

  compareHistorialEtapa(o1: Pick<IHistorialEtapa, 'id'> | null, o2: Pick<IHistorialEtapa, 'id'> | null): boolean {
    return o1 && o2 ? this.getHistorialEtapaIdentifier(o1) === this.getHistorialEtapaIdentifier(o2) : o1 === o2;
  }

  addHistorialEtapaToCollectionIfMissing<Type extends Pick<IHistorialEtapa, 'id'>>(
    historialEtapaCollection: Type[],
    ...historialEtapasToCheck: (Type | null | undefined)[]
  ): Type[] {
    const historialEtapas: Type[] = historialEtapasToCheck.filter(
      historialEtapaItem => historialEtapaItem !== null && historialEtapaItem !== undefined,
    );
    if (historialEtapas.length > 0) {
      const historialEtapaCollectionIdentifiers = historialEtapaCollection.map(historialEtapaItem =>
        this.getHistorialEtapaIdentifier(historialEtapaItem),
      );
      const historialEtapasToAdd = historialEtapas.filter(historialEtapaItem => {
        const historialEtapaIdentifier = this.getHistorialEtapaIdentifier(historialEtapaItem);
        if (historialEtapaCollectionIdentifiers.includes(historialEtapaIdentifier)) {
          return false;
        }
        historialEtapaCollectionIdentifiers.push(historialEtapaIdentifier);
        return true;
      });
      return [...historialEtapasToAdd, ...historialEtapaCollection];
    }
    return historialEtapaCollection;
  }

  protected convertValueFromClient<T extends IHistorialEtapa | NewHistorialEtapa | PartialUpdateHistorialEtapa>(
    historialEtapa: T,
  ): RestOf<T> {
    return {
      ...historialEtapa,
      fecha: historialEtapa.fecha?.toJSON() ?? null,
    };
  }

  protected convertResponseFromServer(res: RestHistorialEtapa): IHistorialEtapa {
    return this.convertValueFromServer(res);
  }

  protected convertResponseArrayFromServer(res: RestHistorialEtapa[]): IHistorialEtapa[] {
    return res.map(item => this.convertValueFromServer(item));
  }
}
