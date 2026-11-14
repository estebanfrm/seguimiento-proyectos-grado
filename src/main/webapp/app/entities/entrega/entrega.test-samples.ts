import dayjs from 'dayjs/esm';

import { IEntrega, NewEntrega } from './entrega.model';

export const sampleWithRequiredData: IEntrega = {
  id: 18286,
  etapa: 'SUSTENTACION',
  titulo: 'yowza',
  version: 26140,
  fechaEntrega: dayjs('2023-12-07T16:49'),
  estado: 'EN_REVISION',
};

export const sampleWithPartialData: IEntrega = {
  id: 623,
  etapa: 'SUSTENTACION',
  titulo: 'tenant',
  descripcion: '../fake-data/blob/hipster.txt',
  archivo: '../fake-data/blob/hipster.png',
  archivoContentType: 'unknown',
  urlDocumento: 'ingratiate whenever',
  version: 6742,
  fechaEntrega: dayjs('2023-12-07T14:43'),
  estado: 'APROBADA',
};

export const sampleWithFullData: IEntrega = {
  id: 18312,
  etapa: 'SUSTENTACION',
  titulo: 'because concerning',
  descripcion: '../fake-data/blob/hipster.txt',
  archivo: '../fake-data/blob/hipster.png',
  archivoContentType: 'unknown',
  urlDocumento: 'and',
  version: 25626,
  fechaEntrega: dayjs('2023-12-07T11:26'),
  estado: 'EN_REVISION',
};

export const sampleWithNewData: NewEntrega = {
  etapa: 'PROPUESTA',
  titulo: 'unlike oh',
  version: 15942,
  fechaEntrega: dayjs('2023-12-07T10:45'),
  estado: 'EN_REVISION',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
