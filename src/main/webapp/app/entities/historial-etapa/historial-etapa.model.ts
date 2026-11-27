import dayjs from 'dayjs/esm';

import { Etapa } from 'app/entities/enumerations/etapa.model';
import { IProyectoGrado } from 'app/entities/proyecto-grado/proyecto-grado.model';
import { IUser } from 'app/entities/user/user.model';

export interface IHistorialEtapa {
  id: number;
  etapaOrigen?: keyof typeof Etapa | null;
  etapaDestino?: keyof typeof Etapa | null;
  fecha?: dayjs.Dayjs | null;
  comentario?: string | null;
  usuario?: Pick<IUser, 'id' | 'login'> | null;
  proyecto?: Pick<IProyectoGrado, 'id' | 'titulo'> | null;
}

export type NewHistorialEtapa = Omit<IHistorialEtapa, 'id'> & { id: null };
