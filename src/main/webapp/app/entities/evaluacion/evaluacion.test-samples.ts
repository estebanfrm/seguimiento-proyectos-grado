import dayjs from 'dayjs/esm';

import { IEvaluacion, NewEvaluacion } from './evaluacion.model';

export const sampleWithRequiredData: IEvaluacion = {
  id: 18693,
  etapa: 'ANTEPROYECTO',
  calificacion: 3.44,
  concepto: 'APROBADO_CON_MODIFICACIONES',
  fechaEvaluacion: dayjs('2023-12-07T00:49'),
};

export const sampleWithPartialData: IEvaluacion = {
  id: 13725,
  etapa: 'DESARROLLO',
  calificacion: 3.16,
  concepto: 'REPROBADO',
  fechaEvaluacion: dayjs('2023-12-07T05:10'),
};

export const sampleWithFullData: IEvaluacion = {
  id: 5354,
  etapa: 'SUSTENTACION',
  calificacion: 1.03,
  concepto: 'APROBADO',
  comentarios: '../fake-data/blob/hipster.txt',
  fechaEvaluacion: dayjs('2023-12-06T23:05'),
};

export const sampleWithNewData: NewEvaluacion = {
  etapa: 'FINALIZADO',
  calificacion: 1.9,
  concepto: 'APROBADO',
  fechaEvaluacion: dayjs('2023-12-07T00:24'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
