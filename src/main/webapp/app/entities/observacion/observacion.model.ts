import dayjs from 'dayjs/esm';

import { IEntrega } from 'app/entities/entrega/entrega.model';
import { TipoObservacion } from 'app/entities/enumerations/tipo-observacion.model';
import { IUser } from 'app/entities/user/user.model';

export interface IObservacion {
  id: number;
  contenido?: string | null;
  tipo?: keyof typeof TipoObservacion | null;
  resuelta?: boolean | null;
  fechaCreacion?: dayjs.Dayjs | null;
  autor?: Pick<IUser, 'id' | 'login'> | null;
  entrega?: Pick<IEntrega, 'id' | 'titulo'> | null;
}

export type NewObservacion = Omit<IObservacion, 'id'> & { id: null };
