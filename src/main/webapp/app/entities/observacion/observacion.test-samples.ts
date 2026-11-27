import dayjs from 'dayjs/esm';

import { IObservacion, NewObservacion } from './observacion.model';

export const sampleWithRequiredData: IObservacion = {
  id: 12015,
  contenido: '../fake-data/blob/hipster.txt',
  tipo: 'GENERAL',
  resuelta: false,
  fechaCreacion: dayjs('2023-12-06T21:48'),
};

export const sampleWithPartialData: IObservacion = {
  id: 21476,
  contenido: '../fake-data/blob/hipster.txt',
  tipo: 'GENERAL',
  resuelta: false,
  fechaCreacion: dayjs('2023-12-06T22:29'),
};

export const sampleWithFullData: IObservacion = {
  id: 3562,
  contenido: '../fake-data/blob/hipster.txt',
  tipo: 'GENERAL',
  resuelta: true,
  fechaCreacion: dayjs('2023-12-07T17:24'),
};

export const sampleWithNewData: NewObservacion = {
  contenido: '../fake-data/blob/hipster.txt',
  tipo: 'CORRECCION_REQUERIDA',
  resuelta: true,
  fechaCreacion: dayjs('2023-12-07T15:01'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
