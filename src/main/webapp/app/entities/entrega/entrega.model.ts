import dayjs from 'dayjs/esm';

import { EstadoEntrega } from 'app/entities/enumerations/estado-entrega.model';
import { Etapa } from 'app/entities/enumerations/etapa.model';
import { IProyectoGrado } from 'app/entities/proyecto-grado/proyecto-grado.model';
import { IUser } from 'app/entities/user/user.model';

export interface IEntrega {
  id: number;
  etapa?: keyof typeof Etapa | null;
  titulo?: string | null;
  descripcion?: string | null;
  archivo?: string | null;
  archivoContentType?: string | null;
  urlDocumento?: string | null;
  version?: number | null;
  fechaEntrega?: dayjs.Dayjs | null;
  estado?: keyof typeof EstadoEntrega | null;
  autor?: Pick<IUser, 'id' | 'login'> | null;
  proyecto?: Pick<IProyectoGrado, 'id' | 'titulo'> | null;
}

export type NewEntrega = Omit<IEntrega, 'id'> & { id: null };
