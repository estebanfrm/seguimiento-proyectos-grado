import dayjs from 'dayjs/esm';

import { EstadoProyecto } from 'app/entities/enumerations/estado-proyecto.model';
import { Etapa } from 'app/entities/enumerations/etapa.model';
import { IUser } from 'app/entities/user/user.model';

export interface IProyectoGrado {
  id: number;
  titulo?: string | null;
  resumen?: string | null;
  programa?: string | null;
  etapaActual?: keyof typeof Etapa | null;
  estado?: keyof typeof EstadoProyecto | null;
  porcentajeAvance?: number | null;
  fechaRegistro?: dayjs.Dayjs | null;
  fechaActualizacion?: dayjs.Dayjs | null;
  tutor?: Pick<IUser, 'id' | 'login'> | null;
  estudianteses?: Pick<IUser, 'id' | 'login'>[] | null;
  juradoses?: Pick<IUser, 'id' | 'login'>[] | null;
}

export type NewProyectoGrado = Omit<IProyectoGrado, 'id'> & { id: null };
