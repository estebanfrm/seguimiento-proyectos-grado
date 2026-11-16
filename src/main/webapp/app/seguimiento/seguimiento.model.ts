import { Concepto } from 'app/entities/enumerations/concepto.model';
import { EstadoEntrega } from 'app/entities/enumerations/estado-entrega.model';
import { Etapa } from 'app/entities/enumerations/etapa.model';
import { TipoObservacion } from 'app/entities/enumerations/tipo-observacion.model';
import { RestEntrega } from 'app/entities/entrega/service/entrega.service';
import { RestEvaluacion } from 'app/entities/evaluacion/service/evaluacion.service';
import { RestHistorialEtapa } from 'app/entities/historial-etapa/service/historial-etapa.service';
import { RestObservacion } from 'app/entities/observacion/service/observacion.service';
import { RestProyectoGrado } from 'app/entities/proyecto-grado/service/proyecto-grado.service';

export type CodigoRequisito =
  | 'TUTOR_ASIGNADO'
  | 'ESTUDIANTES_ASIGNADOS'
  | 'ENTREGA_APROBADA'
  | 'SIN_CORRECCIONES_PENDIENTES'
  | 'JURADOS_ASIGNADOS'
  | 'EVALUACIONES_COMPLETAS'
  | 'EVALUACION_APROBATORIA'
  | 'SUSTENTACION_PROGRAMADA';

export interface IRequisitoTransicion {
  codigo: CodigoRequisito;
  descripcion: string;
  cumplido: boolean;
}

export interface IRequisitosTransicion {
  proyectoId: number;
  etapaActual: keyof typeof Etapa;
  etapaSiguiente: keyof typeof Etapa | null;
  puedeAvanzar: boolean;
  requisitos: IRequisitoTransicion[];
}

export interface IEntregaResumen {
  id: number;
  titulo: string;
  version: number;
  estado: keyof typeof EstadoEntrega;
  fechaEntrega: string;
  observacionesPendientes: number;
  tieneArchivo: boolean;
}

export interface IEtapaSeguimiento {
  etapa: keyof typeof Etapa;
  estado: 'COMPLETADA' | 'ACTUAL' | 'PENDIENTE';
  fechaLimite: string | null;
  vencida: boolean;
  diasRestantes: number | null;
  entregas: IEntregaResumen[];
  evaluaciones: RestEvaluacion[];
}

export interface ISeguimientoProyecto {
  proyecto: RestProyectoGrado;
  porcentajeAvance: number;
  etapas: IEtapaSeguimiento[];
  historial: RestHistorialEtapa[];
  requisitos: IRequisitosTransicion;
}

export interface IUsuarioResumen {
  id: number;
  login: string;
  firstName: string | null;
  lastName: string | null;
}

export interface INuevaPropuesta {
  titulo: string;
  resumen: string;
  programa: string;
}

export interface INuevaEntrega {
  titulo: string;
  descripcion: string;
  urlDocumento?: string;
  archivo?: string;
  archivoContentType?: string;
  archivoNombre?: string;
  proyecto: { id: number };
}

export interface INuevaObservacion {
  contenido: string;
  tipo: keyof typeof TipoObservacion;
  entrega: { id: number };
}

export interface INuevaEvaluacion {
  calificacion: number;
  concepto: keyof typeof Concepto;
  comentarios: string;
  proyecto: { id: number };
}

export interface IDetalleEntrega {
  entrega: RestEntrega;
  observaciones: RestObservacion[];
}

export interface IProblemaSeguimiento {
  mensaje: string;
  pendientes: string[];
}
