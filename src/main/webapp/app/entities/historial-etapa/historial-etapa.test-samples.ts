import dayjs from 'dayjs/esm';

import { IHistorialEtapa, NewHistorialEtapa } from './historial-etapa.model';

export const sampleWithRequiredData: IHistorialEtapa = {
  id: 3617,
  etapaDestino: 'SUSTENTACION',
  fecha: dayjs('2023-12-06T22:31'),
};

export const sampleWithPartialData: IHistorialEtapa = {
  id: 10301,
  etapaDestino: 'SUSTENTACION',
  fecha: dayjs('2023-12-06T23:59'),
  comentario: 'worriedly gadzooks unaccountably',
};

export const sampleWithFullData: IHistorialEtapa = {
  id: 24626,
  etapaOrigen: 'DESARROLLO',
  etapaDestino: 'DESARROLLO',
  fecha: dayjs('2023-12-07T15:30'),
  comentario: 'energetically',
};

export const sampleWithNewData: NewHistorialEtapa = {
  etapaDestino: 'PROPUESTA',
  fecha: dayjs('2023-12-07T15:09'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
