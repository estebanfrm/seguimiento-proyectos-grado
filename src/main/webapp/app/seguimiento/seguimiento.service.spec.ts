import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { serverApiUrl } from 'app/config';
import { EstadoEntrega } from 'app/entities/enumerations/estado-entrega.model';

import { problemaSeguimiento } from './seguimiento-error';
import { seguimientoEjemplo } from './seguimiento.test-samples';
import { SeguimientoService } from './seguimiento.service';

describe('SeguimientoService: contrato de API', () => {
  let service: SeguimientoService;
  let http: HttpTestingController;
  const api = `${serverApiUrl}api`;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(SeguimientoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('consulta proyectos con paginación y conserva el total del servidor', () => {
    const proyecto = seguimientoEjemplo().proyecto;
    service.proyectos(2, 12).subscribe(response => {
      expect(response.body).toEqual([proyecto]);
      expect(response.headers.get('X-Total-Count')).toBe('37');
    });
    const req = http.expectOne(request => request.url === `${api}/proyecto-grados`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('page')).toBe('2');
    expect(req.request.params.get('size')).toBe('12');
    expect(req.request.params.get('sort')).toBe('id,desc');
    req.flush([proyecto], { headers: { 'X-Total-Count': '37' } });
  });

  it('lee seguimiento y requisitos sin convertir las fechas ISO de los DTO', () => {
    const data = seguimientoEjemplo();
    service.seguimiento(7).subscribe(response => expect(response).toEqual(data));
    http.expectOne({ method: 'GET', url: `${api}/proyecto-grados/7/seguimiento` }).flush(data);
    service.workflow(7).subscribe(response => expect(response).toEqual(data.requisitos));
    http.expectOne({ method: 'GET', url: `${api}/proyecto-grados/7/workflow` }).flush(data.requisitos);
  });

  it.each(['avanzar', 'cancelar'] as const)('envía el comentario para %s por el endpoint de workflow', accion => {
    const proyecto = seguimientoEjemplo().proyecto;
    service[accion](7, 'Decisión académica').subscribe(response => expect(response).toEqual(proyecto));
    const req = http.expectOne({ method: 'POST', url: `${api}/proyecto-grados/7/workflow/${accion}` });
    expect(req.request.body).toEqual({ comentario: 'Decisión académica' });
    req.flush(proyecto);
  });

  it('revisa una entrega y resuelve una observación con las rutas exactas del contrato', () => {
    service.revisar(9, EstadoEntrega.APROBADA).subscribe(response => expect(response.estado).toBe('APROBADA'));
    const revision = http.expectOne({ method: 'PUT', url: `${api}/entregas/9/revision` });
    expect(revision.request.body).toEqual({ estado: 'APROBADA' });
    revision.flush({ id: 9, estado: 'APROBADA' });
    service.resolver(12).subscribe(response => expect(response.resuelta).toBe(true));
    const resolver = http.expectOne({ method: 'PUT', url: `${api}/observacions/12/resolver` });
    expect(resolver.request.body).toEqual({});
    resolver.flush({ id: 12, resuelta: true });
  });

  it('crea propuesta, entrega, observación y evaluación sin enviar campos forzados por el servidor', () => {
    const propuesta = { titulo: 'Proyecto académico', resumen: 'Resumen', programa: 'Sistemas' };
    service.crearPropuesta(propuesta).subscribe();
    const proyecto = http.expectOne({ method: 'POST', url: `${api}/proyecto-grados` });
    expect(proyecto.request.body).toEqual(propuesta);
    proyecto.flush({ id: 7 });

    const entrega = { titulo: 'Documento', descripcion: 'Versión revisada', urlDocumento: 'https://example.org/doc', proyecto: { id: 7 } };
    service.crearEntrega(entrega).subscribe();
    const documento = http.expectOne({ method: 'POST', url: `${api}/entregas` });
    expect(documento.request.body).toEqual(entrega);
    documento.flush({ id: 9 });

    const observacion = { contenido: 'Ajustar objetivos', tipo: 'CORRECCION_REQUERIDA' as const, entrega: { id: 9 } };
    service.crearObservacion(observacion).subscribe();
    const nota = http.expectOne({ method: 'POST', url: `${api}/observacions` });
    expect(nota.request.body).toEqual(observacion);
    nota.flush({ id: 12 });

    const evaluacion = { calificacion: 4.1, concepto: 'APROBADO' as const, comentarios: 'Buen trabajo', proyecto: { id: 7 } };
    service.crearEvaluacion(evaluacion).subscribe();
    const concepto = http.expectOne({ method: 'POST', url: `${api}/evaluacions` });
    expect(concepto.request.body).toEqual(evaluacion);
    concepto.flush({ id: 15 });
  });

  it('consulta el directorio por authority', () => {
    const usuarios = [{ id: 2, login: 'tutor', firstName: 'Ana', lastName: 'Pérez' }];
    service.usuariosPorRol('ROLE_TUTOR').subscribe(response => expect(response).toEqual(usuarios));
    http.expectOne({ method: 'GET', url: `${api}/usuarios/por-rol/ROLE_TUTOR` }).flush(usuarios);
  });

  it('expande la entrega con sus observaciones y conserva el orden del endpoint específico', () => {
    const observaciones = [
      { id: 14, contenido: 'Primera observación', fechaCreacion: '2026-11-01T12:00:00Z', entrega: { id: 9 } },
      { id: 12, contenido: 'Segunda observación', fechaCreacion: '2026-11-02T12:00:00Z', entrega: { id: 9 } },
    ];
    service.detalleEntrega(9).subscribe(response => {
      expect(response.entrega.id).toBe(9);
      expect(response.observaciones).toEqual(observaciones);
    });
    http.expectNone({ method: 'GET', url: `${api}/observacions` });
    http.expectOne({ method: 'GET', url: `${api}/entregas/9` }).flush({ id: 9, urlDocumento: 'https://example.org/doc' });
    http.expectOne({ method: 'GET', url: `${api}/entregas/9/observacions` }).flush(observaciones);
  });

  it('propaga el 403 cuando el endpoint de observaciones rechaza el acceso', () => {
    service.detalleEntrega(9).subscribe({
      next: () => expect.fail('El servidor debe rechazar el acceso'),
      error: (error: HttpErrorResponse) => expect(error.status).toBe(403),
    });
    http.expectOne({ method: 'GET', url: `${api}/entregas/9` }).flush({ id: 9 });
    http
      .expectOne({ method: 'GET', url: `${api}/entregas/9/observacions` })
      .flush({ detail: 'Sin acceso al proyecto' }, { status: 403, statusText: 'Forbidden' });
  });

  it('conserva el detail y pendientes del ProblemDetail 409', () => {
    service.avanzar(7, '').subscribe({
      next: () => expect.fail('El servidor debe rechazar el avance'),
      error: (error: HttpErrorResponse) =>
        expect(problemaSeguimiento(error)).toEqual({
          mensaje: 'Faltan requisitos',
          pendientes: ['Asignar tutor', 'Una entrega aprobada'],
        }),
    });
    http.expectOne(`${api}/proyecto-grados/7/workflow/avanzar`).flush(
      {
        detail: 'Faltan requisitos',
        pendientes: ['Asignar tutor', { codigo: 'ENTREGA_APROBADA', descripcion: 'Una entrega aprobada', cumplido: false }],
      },
      { status: 409, statusText: 'Conflict' },
    );
  });

  it('traduce el 403 a un mensaje de permisos y admite errores sin body', () => {
    expect(problemaSeguimiento(new HttpErrorResponse({ status: 403, error: { detail: 'Forbidden' } }))).toEqual({
      mensaje: 'seguimiento.errors.forbidden',
      pendientes: [],
    });
    expect(problemaSeguimiento(new HttpErrorResponse({ status: 0 }))).toEqual({ mensaje: 'seguimiento.errors.generic', pendientes: [] });
  });
});
