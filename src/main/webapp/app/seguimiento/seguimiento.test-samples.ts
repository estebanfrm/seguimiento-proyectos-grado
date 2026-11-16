import { Account } from 'app/core/auth';
import { Etapa } from 'app/entities/enumerations/etapa.model';

import { ISeguimientoProyecto } from './seguimiento.model';

export function cuentaSeguimiento(rol: string, login = 'ana'): Account {
  return new Account(true, ['ROLE_USER', rol], 'ana@example.org', 'Ana', 'es', 'Pérez', login, null);
}

export function seguimientoEjemplo(): ISeguimientoProyecto {
  return {
    proyecto: {
      id: 7,
      titulo: 'Sistema de seguimiento académico',
      resumen: 'Un proyecto para acompañar el proceso de grado.',
      programa: 'Ingeniería de sistemas',
      etapaActual: 'ANTEPROYECTO',
      estado: 'ACTIVO',
      porcentajeAvance: 35,
      estudiantes: [{ id: 1, login: 'ana' }],
      tutor: { id: 2, login: 'tutor' },
      jurados: [
        { id: 3, login: 'jurado' },
        { id: 4, login: 'jurado2' },
      ],
    },
    porcentajeAvance: 35,
    etapas: Object.values(Etapa).map((etapa, index) => ({
      etapa,
      estado: index === 0 ? 'COMPLETADA' : index === 1 ? 'ACTUAL' : 'PENDIENTE',
      fechaLimite: index === 1 ? '2026-12-01T23:59:00Z' : null,
      vencida: false,
      diasRestantes: index === 1 ? 18 : null,
      entregas:
        index === 1
          ? [
              {
                id: 9,
                titulo: 'Documento del anteproyecto',
                version: 2,
                estado: 'CON_OBSERVACIONES',
                fechaEntrega: '2026-11-10T15:00:00Z',
                observacionesPendientes: 1,
                tieneArchivo: false,
              },
            ]
          : [],
      evaluaciones: [],
    })),
    historial: [
      {
        id: 5,
        etapaOrigen: 'PROPUESTA',
        etapaDestino: 'ANTEPROYECTO',
        fecha: '2026-11-01T12:00:00Z',
        comentario: 'Propuesta aprobada',
        usuario: { id: 2, login: 'tutor' },
      },
    ],
    requisitos: {
      proyectoId: 7,
      etapaActual: 'ANTEPROYECTO',
      etapaSiguiente: 'DESARROLLO',
      puedeAvanzar: false,
      requisitos: [
        { codigo: 'JURADOS_ASIGNADOS', descripcion: 'Al menos dos jurados asignados', cumplido: true },
        { codigo: 'ENTREGA_APROBADA', descripcion: 'Una entrega aprobada', cumplido: false },
      ],
    },
  };
}
