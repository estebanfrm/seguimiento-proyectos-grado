import dayjs from 'dayjs/esm';

import { Concepto } from 'app/entities/enumerations/concepto.model';
import { Etapa } from 'app/entities/enumerations/etapa.model';
import { IProyectoGrado } from 'app/entities/proyecto-grado/proyecto-grado.model';
import { IUser } from 'app/entities/user/user.model';

export interface IEvaluacion {
  id: number;
  etapa?: keyof typeof Etapa | null;
  calificacion?: number | null;
  concepto?: keyof typeof Concepto | null;
  comentarios?: string | null;
  fechaEvaluacion?: dayjs.Dayjs | null;
  jurado?: Pick<IUser, 'id' | 'login'> | null;
  proyecto?: Pick<IProyectoGrado, 'id' | 'titulo'> | null;
}

export type NewEvaluacion = Omit<IEvaluacion, 'id'> & { id: null };
