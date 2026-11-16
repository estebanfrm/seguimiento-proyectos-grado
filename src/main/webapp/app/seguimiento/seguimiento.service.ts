import { HttpClient, HttpResponse } from '@angular/common/http';
import { Service, inject } from '@angular/core';

import { Observable, forkJoin, map } from 'rxjs';

import { serverApiUrl } from 'app/config';
import { EstadoEntrega } from 'app/entities/enumerations/estado-entrega.model';
import { RestEntrega } from 'app/entities/entrega/service/entrega.service';
import { RestEvaluacion } from 'app/entities/evaluacion/service/evaluacion.service';
import { RestFechaLimiteEtapa } from 'app/entities/fecha-limite-etapa/service/fecha-limite-etapa.service';
import { RestObservacion } from 'app/entities/observacion/service/observacion.service';
import { RestProyectoGrado } from 'app/entities/proyecto-grado/service/proyecto-grado.service';

import { IAsignacionProyecto, IGuardarFechaLimite } from './seguimiento-admin.model';
import {
  IDetalleEntrega,
  INuevaEntrega,
  INuevaEvaluacion,
  INuevaObservacion,
  INuevaPropuesta,
  IRequisitosTransicion,
  ISeguimientoProyecto,
  IUsuarioResumen,
} from './seguimiento.model';

@Service()
export class SeguimientoService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${serverApiUrl}api`;

  proyectos(page = 0, size = 12): Observable<HttpResponse<RestProyectoGrado[]>> {
    return this.http.get<RestProyectoGrado[]>(`${this.apiUrl}/proyecto-grados`, {
      params: { page, size, sort: 'id,desc' },
      observe: 'response',
    });
  }

  seguimiento(id: number): Observable<ISeguimientoProyecto> {
    return this.http.get<ISeguimientoProyecto>(`${this.apiUrl}/proyecto-grados/${id}/seguimiento`);
  }

  workflow(id: number): Observable<IRequisitosTransicion> {
    return this.http.get<IRequisitosTransicion>(`${this.apiUrl}/proyecto-grados/${id}/workflow`);
  }

  avanzar(id: number, comentario: string): Observable<RestProyectoGrado> {
    return this.http.post<RestProyectoGrado>(`${this.apiUrl}/proyecto-grados/${id}/workflow/avanzar`, { comentario });
  }

  cancelar(id: number, comentario: string): Observable<RestProyectoGrado> {
    return this.http.post<RestProyectoGrado>(`${this.apiUrl}/proyecto-grados/${id}/workflow/cancelar`, { comentario });
  }

  revisar(id: number, estado: Exclude<keyof typeof EstadoEntrega, 'ENVIADA'>): Observable<RestEntrega> {
    return this.http.put<RestEntrega>(`${this.apiUrl}/entregas/${id}/revision`, { estado });
  }

  resolver(id: number): Observable<RestObservacion> {
    return this.http.put<RestObservacion>(`${this.apiUrl}/observacions/${id}/resolver`, {});
  }

  crearPropuesta(propuesta: INuevaPropuesta): Observable<RestProyectoGrado> {
    return this.http.post<RestProyectoGrado>(`${this.apiUrl}/proyecto-grados`, propuesta);
  }

  crearEntrega(entrega: INuevaEntrega): Observable<RestEntrega> {
    return this.http.post<RestEntrega>(`${this.apiUrl}/entregas`, entrega);
  }

  crearObservacion(observacion: INuevaObservacion): Observable<RestObservacion> {
    return this.http.post<RestObservacion>(`${this.apiUrl}/observacions`, observacion);
  }

  crearEvaluacion(evaluacion: INuevaEvaluacion): Observable<RestEvaluacion> {
    return this.http.post<RestEvaluacion>(`${this.apiUrl}/evaluacions`, evaluacion);
  }

  usuariosPorRol(authority: string): Observable<IUsuarioResumen[]> {
    return this.http.get<IUsuarioResumen[]>(`${this.apiUrl}/usuarios/por-rol/${encodeURIComponent(authority)}`);
  }

  detalleEntrega(id: number): Observable<IDetalleEntrega> {
    return forkJoin({
      entrega: this.http.get<RestEntrega>(`${this.apiUrl}/entregas/${id}`),
      observaciones: this.http.get<RestObservacion[]>(`${this.apiUrl}/entregas/${id}/observacions`),
    });
  }

  asignarProyecto(asignacion: IAsignacionProyecto): Observable<RestProyectoGrado> {
    return this.http.patch<RestProyectoGrado>(`${this.apiUrl}/proyecto-grados/${asignacion.id}`, asignacion);
  }

  fechasLimite(proyectoId: number): Observable<RestFechaLimiteEtapa[]> {
    // El CRUD no admite criterios: el admin recibe todos los proyectos; los demás, solo los visibles.
    return this.http
      .get<RestFechaLimiteEtapa[]>(`${this.apiUrl}/fecha-limite-etapas`)
      .pipe(map(fechas => fechas.filter(fecha => fecha.proyecto?.id === proyectoId)));
  }

  crearFechaLimite(fecha: IGuardarFechaLimite): Observable<RestFechaLimiteEtapa> {
    return this.http.post<RestFechaLimiteEtapa>(`${this.apiUrl}/fecha-limite-etapas`, fecha);
  }

  actualizarFechaLimite(id: number, fecha: IGuardarFechaLimite): Observable<RestFechaLimiteEtapa> {
    return this.http.put<RestFechaLimiteEtapa>(`${this.apiUrl}/fecha-limite-etapas/${id}`, { id, ...fecha });
  }

  eliminarFechaLimite(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.apiUrl}/fecha-limite-etapas/${id}`);
  }
}
