import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Service, computed, inject, signal } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { serverApiUrl } from 'app/config';
import { createRequestOption } from 'app/core/request';
import { IProyectoGrado, NewProyectoGrado } from '../proyecto-grado.model';

export type PartialUpdateProyectoGrado = Partial<IProyectoGrado> & Pick<IProyectoGrado, 'id'>;

type RestOf<T extends IProyectoGrado | NewProyectoGrado> = Omit<T, 'fechaRegistro' | 'fechaActualizacion'> & {
  fechaRegistro?: string | null;
  fechaActualizacion?: string | null;
};

export type RestProyectoGrado = RestOf<IProyectoGrado>;

export type NewRestProyectoGrado = RestOf<NewProyectoGrado>;

export type PartialUpdateRestProyectoGrado = RestOf<PartialUpdateProyectoGrado>;

@Service()
export class ProyectoGradosService {
  readonly proyectoGradosParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly proyectoGradosResource = httpResource<RestProyectoGrado[]>(() => {
    const params = this.proyectoGradosParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of proyectoGrado that have been fetched. It is updated when the proyectoGradosResource emits a new value.
   * In case of error while fetching the proyectoGrados, the signal is set to an empty array.
   */
  readonly proyectoGrados = computed(() =>
    (this.proyectoGradosResource.hasValue() ? this.proyectoGradosResource.value() : []).map(item => this.convertValueFromServer(item)),
  );
  protected readonly resourceUrl = `${serverApiUrl}api/proyecto-grados`;

  protected convertValueFromServer(restProyectoGrado: RestProyectoGrado): IProyectoGrado {
    return {
      ...restProyectoGrado,
      fechaRegistro: restProyectoGrado.fechaRegistro ? dayjs(restProyectoGrado.fechaRegistro) : undefined,
      fechaActualizacion: restProyectoGrado.fechaActualizacion ? dayjs(restProyectoGrado.fechaActualizacion) : undefined,
    };
  }
}

@Service()
export class ProyectoGradoService extends ProyectoGradosService {
  protected readonly http = inject(HttpClient);

  create(proyectoGrado: NewProyectoGrado): Observable<IProyectoGrado> {
    const copy = this.convertValueFromClient(proyectoGrado);
    return this.http.post<RestProyectoGrado>(this.resourceUrl, copy).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(proyectoGrado: IProyectoGrado): Observable<IProyectoGrado> {
    const copy = this.convertValueFromClient(proyectoGrado);
    return this.http
      .put<RestProyectoGrado>(`${this.resourceUrl}/${encodeURIComponent(this.getProyectoGradoIdentifier(proyectoGrado))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(proyectoGrado: PartialUpdateProyectoGrado): Observable<IProyectoGrado> {
    const copy = this.convertValueFromClient(proyectoGrado);
    return this.http
      .patch<RestProyectoGrado>(`${this.resourceUrl}/${encodeURIComponent(this.getProyectoGradoIdentifier(proyectoGrado))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<IProyectoGrado> {
    return this.http
      .get<RestProyectoGrado>(`${this.resourceUrl}/${encodeURIComponent(id)}`)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<HttpResponse<IProyectoGrado[]>> {
    const options = createRequestOption(req);
    return this.http
      .get<RestProyectoGrado[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => res.clone({ body: this.convertResponseArrayFromServer(res.body!) })));
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getProyectoGradoIdentifier(proyectoGrado: Pick<IProyectoGrado, 'id'>): number {
    return proyectoGrado.id;
  }

  compareProyectoGrado(o1: Pick<IProyectoGrado, 'id'> | null, o2: Pick<IProyectoGrado, 'id'> | null): boolean {
    return o1 && o2 ? this.getProyectoGradoIdentifier(o1) === this.getProyectoGradoIdentifier(o2) : o1 === o2;
  }

  addProyectoGradoToCollectionIfMissing<Type extends Pick<IProyectoGrado, 'id'>>(
    proyectoGradoCollection: Type[],
    ...proyectoGradosToCheck: (Type | null | undefined)[]
  ): Type[] {
    const proyectoGrados: Type[] = proyectoGradosToCheck.filter(
      proyectoGradoItem => proyectoGradoItem !== null && proyectoGradoItem !== undefined,
    );
    if (proyectoGrados.length > 0) {
      const proyectoGradoCollectionIdentifiers = proyectoGradoCollection.map(proyectoGradoItem =>
        this.getProyectoGradoIdentifier(proyectoGradoItem),
      );
      const proyectoGradosToAdd = proyectoGrados.filter(proyectoGradoItem => {
        const proyectoGradoIdentifier = this.getProyectoGradoIdentifier(proyectoGradoItem);
        if (proyectoGradoCollectionIdentifiers.includes(proyectoGradoIdentifier)) {
          return false;
        }
        proyectoGradoCollectionIdentifiers.push(proyectoGradoIdentifier);
        return true;
      });
      return [...proyectoGradosToAdd, ...proyectoGradoCollection];
    }
    return proyectoGradoCollection;
  }

  protected convertValueFromClient<T extends IProyectoGrado | NewProyectoGrado | PartialUpdateProyectoGrado>(proyectoGrado: T): RestOf<T> {
    return {
      ...proyectoGrado,
      fechaRegistro: proyectoGrado.fechaRegistro?.toJSON() ?? null,
      fechaActualizacion: proyectoGrado.fechaActualizacion?.toJSON() ?? null,
    };
  }

  protected convertResponseFromServer(res: RestProyectoGrado): IProyectoGrado {
    return this.convertValueFromServer(res);
  }

  protected convertResponseArrayFromServer(res: RestProyectoGrado[]): IProyectoGrado[] {
    return res.map(item => this.convertValueFromServer(item));
  }
}
