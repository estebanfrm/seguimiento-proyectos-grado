import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { serverApiUrl } from 'app/config';

import { IAsignacionProyecto, IGuardarFechaLimite } from './seguimiento-admin.model';
import { SeguimientoService } from './seguimiento.service';

describe('SeguimientoService: administración del proyecto', () => {
  let service: SeguimientoService;
  let http: HttpTestingController;
  const api = `${serverApiUrl}api`;
  const fecha: IGuardarFechaLimite = {
    etapa: 'SUSTENTACION',
    fechaLimite: '2027-02-03T19:30:00.000Z',
    descripcion: 'Sustentación pública',
    proyecto: { id: 7 },
  };

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(SeguimientoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('reemplaza asignaciones por PUT sin enviar relaciones, etapa, estado ni avance', () => {
    const asignacion: IAsignacionProyecto = {
      tutorId: 2,
      juradoIds: [3, 4],
      estudianteIds: [1],
    };
    service.asignarProyecto(7, asignacion).subscribe(response => expect(response.id).toBe(7));
    const req = http.expectOne({ method: 'PUT', url: `${api}/proyecto-grados/7/asignaciones` });
    expect(req.request.body).toEqual(asignacion);
    expect(Object.keys(req.request.body)).toEqual(['tutorId', 'juradoIds', 'estudianteIds']);
    req.flush({ id: 7 });
  });

  it('envía tutorId null y listas vacías explícitamente', () => {
    const asignacion: IAsignacionProyecto = { tutorId: null, juradoIds: [], estudianteIds: [] };
    service.asignarProyecto(7, asignacion).subscribe();
    const req = http.expectOne({ method: 'PUT', url: `${api}/proyecto-grados/7/asignaciones` });
    expect(req.request.body).toEqual(asignacion);
    req.flush({ id: 7 });
  });

  it('filtra en el cliente los plazos por proyecto sin asumir soporte de criterios en el CRUD', () => {
    const propia = { id: 21, ...fecha };
    service.fechasLimite(7).subscribe(response => expect(response).toEqual([propia]));
    const req = http.expectOne({ method: 'GET', url: `${api}/fecha-limite-etapas` });
    expect(req.request.params.keys()).toEqual([]);
    req.flush([propia, { id: 22, ...fecha, proyecto: { id: 8 } }, { id: 23, etapa: 'PROPUESTA' }]);
  });

  it('crea y edita por POST y PUT con fecha ISO, descripción y referencia al proyecto', () => {
    service.crearFechaLimite(fecha).subscribe(response => expect(response.id).toBe(21));
    const crear = http.expectOne({ method: 'POST', url: `${api}/fecha-limite-etapas` });
    expect(crear.request.body).toEqual(fecha);
    crear.flush({ id: 21, ...fecha });

    service.actualizarFechaLimite(21, fecha).subscribe(response => expect(response.id).toBe(21));
    const editar = http.expectOne({ method: 'PUT', url: `${api}/fecha-limite-etapas/21` });
    expect(editar.request.body).toEqual({ id: 21, ...fecha });
    editar.flush({ id: 21, ...fecha });
  });

  it('elimina el plazo por id y admite la respuesta 204 sin cuerpo', () => {
    let completado = false;
    service.eliminarFechaLimite(21).subscribe(() => {
      completado = true;
    });
    http.expectOne({ method: 'DELETE', url: `${api}/fecha-limite-etapas/21` }).flush(null, { status: 204, statusText: 'No Content' });
    expect(completado).toBe(true);
  });

  it.each([403, 409])('propaga el error HTTP %s del PUT para mostrarlo en la alerta existente', status => {
    service.asignarProyecto(7, { tutorId: null, juradoIds: [], estudianteIds: [] }).subscribe({
      next: () => expect.fail('No debe anunciar éxito'),
      error(error: HttpErrorResponse) {
        expect(error.status).toBe(status);
        expect(error.error.detail).toBe('No se pudo asignar');
      },
    });
    http
      .expectOne(`${api}/proyecto-grados/7/asignaciones`)
      .flush({ detail: 'No se pudo asignar' }, { status, statusText: status === 403 ? 'Forbidden' : 'Conflict' });
  });
});
