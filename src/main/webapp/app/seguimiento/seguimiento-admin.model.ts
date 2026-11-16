import { Etapa } from 'app/entities/enumerations/etapa.model';

export interface IUsuarioAsignacion {
  id: number;
  login: string;
}

export interface IAsignacionProyecto {
  id: number;
  tutor: IUsuarioAsignacion | null;
  jurados: IUsuarioAsignacion[];
  estudiantes: IUsuarioAsignacion[];
}

export interface IGuardarFechaLimite {
  etapa: keyof typeof Etapa;
  fechaLimite: string;
  descripcion: string;
  proyecto: { id: number };
}
