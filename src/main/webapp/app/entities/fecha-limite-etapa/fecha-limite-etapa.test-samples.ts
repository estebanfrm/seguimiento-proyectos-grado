import dayjs from 'dayjs/esm';

import { IFechaLimiteEtapa, NewFechaLimiteEtapa } from './fecha-limite-etapa.model';

export const sampleWithRequiredData: IFechaLimiteEtapa = {
  id: 916,
  etapa: 'DESARROLLO',
  fechaLimite: dayjs('2023-12-07T18:44'),
};

export const sampleWithPartialData: IFechaLimiteEtapa = {
  id: 8400,
  etapa: 'DESARROLLO',
  fechaLimite: dayjs('2023-12-07T00:19'),
};

export const sampleWithFullData: IFechaLimiteEtapa = {
  id: 7573,
  etapa: 'ANTEPROYECTO',
  fechaLimite: dayjs('2023-12-07T15:09'),
  descripcion: 'weakly who',
};

export const sampleWithNewData: NewFechaLimiteEtapa = {
  etapa: 'PROPUESTA',
  fechaLimite: dayjs('2023-12-07T12:28'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
