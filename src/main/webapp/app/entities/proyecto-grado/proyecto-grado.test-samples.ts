import dayjs from 'dayjs/esm';

import { IProyectoGrado, NewProyectoGrado } from './proyecto-grado.model';

export const sampleWithRequiredData: IProyectoGrado = {
  id: 26476,
  titulo: 'gownXXXXXX',
  resumen: '../fake-data/blob/hipster.txt',
  programa: 'favorable expostulate advancement',
  etapaActual: 'ANTEPROYECTO',
  estado: 'ACTIVO',
  fechaRegistro: dayjs('2023-12-06T23:36'),
};

export const sampleWithPartialData: IProyectoGrado = {
  id: 29163,
  titulo: 'dratXXXXXX',
  resumen: '../fake-data/blob/hipster.txt',
  programa: 'till on',
  etapaActual: 'DESARROLLO',
  estado: 'REPROBADO',
  porcentajeAvance: 74,
  fechaRegistro: dayjs('2023-12-07T00:46'),
  fechaActualizacion: dayjs('2023-12-07T07:07'),
};

export const sampleWithFullData: IProyectoGrado = {
  id: 22948,
  titulo: 'the haXXXX',
  resumen: '../fake-data/blob/hipster.txt',
  programa: 'unless',
  etapaActual: 'SUSTENTACION',
  estado: 'REPROBADO',
  porcentajeAvance: 27,
  fechaRegistro: dayjs('2023-12-07T03:35'),
  fechaActualizacion: dayjs('2023-12-07T08:06'),
};

export const sampleWithNewData: NewProyectoGrado = {
  titulo: 'produceXXX',
  resumen: '../fake-data/blob/hipster.txt',
  programa: 'ouch',
  etapaActual: 'DESARROLLO',
  estado: 'CANCELADO',
  fechaRegistro: dayjs('2023-12-07T08:49'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
