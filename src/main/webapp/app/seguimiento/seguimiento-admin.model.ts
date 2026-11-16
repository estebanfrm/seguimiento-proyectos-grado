import { Etapa } from 'app/entities/enumerations/etapa.model';

export interface IAsignacionProyecto {
  tutorId: number | null;
  juradoIds: number[];
  estudianteIds: number[];
}

export interface IGuardarFechaLimite {
  etapa: keyof typeof Etapa;
  fechaLimite: string;
  descripcion: string;
  proyecto: { id: number };
}
