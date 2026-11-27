import dayjs from 'dayjs/esm';

import { Etapa } from 'app/entities/enumerations/etapa.model';
import { IProyectoGrado } from 'app/entities/proyecto-grado/proyecto-grado.model';

export interface IFechaLimiteEtapa {
  id: number;
  etapa?: keyof typeof Etapa | null;
  fechaLimite?: dayjs.Dayjs | null;
  descripcion?: string | null;
  proyecto?: Pick<IProyectoGrado, 'id' | 'titulo'> | null;
}

export type NewFechaLimiteEtapa = Omit<IFechaLimiteEtapa, 'id'> & { id: null };
