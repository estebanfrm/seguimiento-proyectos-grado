import { Service } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';

import { DATE_TIME_FORMAT } from 'app/config';
import { IEvaluacion, NewEvaluacion } from '../evaluacion.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IEvaluacion for edit and NewEvaluacionFormGroupInput for create.
 */
type EvaluacionFormGroupInput = IEvaluacion | PartialWithRequiredKeyOf<NewEvaluacion>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IEvaluacion | NewEvaluacion> = Omit<T, 'fechaEvaluacion'> & {
  fechaEvaluacion?: string | null;
};

type EvaluacionFormRawValue = FormValueOf<IEvaluacion>;

type NewEvaluacionFormRawValue = FormValueOf<NewEvaluacion>;

type EvaluacionFormDefaults = Pick<NewEvaluacion, 'id' | 'fechaEvaluacion'>;

type EvaluacionFormGroupContent = {
  id: FormControl<EvaluacionFormRawValue['id'] | NewEvaluacion['id']>;
  etapa: FormControl<EvaluacionFormRawValue['etapa']>;
  calificacion: FormControl<EvaluacionFormRawValue['calificacion']>;
  concepto: FormControl<EvaluacionFormRawValue['concepto']>;
  comentarios: FormControl<EvaluacionFormRawValue['comentarios']>;
  fechaEvaluacion: FormControl<EvaluacionFormRawValue['fechaEvaluacion']>;
  jurado: FormControl<EvaluacionFormRawValue['jurado']>;
  proyecto: FormControl<EvaluacionFormRawValue['proyecto']>;
};

export type EvaluacionFormGroup = FormGroup<EvaluacionFormGroupContent>;

@Service()
export class EvaluacionFormService {
  createEvaluacionFormGroup(evaluacion?: EvaluacionFormGroupInput): EvaluacionFormGroup {
    const evaluacionRawValue = this.convertEvaluacionToEvaluacionRawValue({
      ...this.getFormDefaults(),
      ...(evaluacion ?? { id: null }),
    });

    return new FormGroup<EvaluacionFormGroupContent>({
      id: new FormControl(
        { value: evaluacionRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      etapa: new FormControl(evaluacionRawValue.etapa, {
        validators: [Validators.required],
      }),
      calificacion: new FormControl(evaluacionRawValue.calificacion, {
        validators: [Validators.required, Validators.min(0), Validators.max(5)],
      }),
      concepto: new FormControl(evaluacionRawValue.concepto, {
        validators: [Validators.required],
      }),
      comentarios: new FormControl(evaluacionRawValue.comentarios),
      fechaEvaluacion: new FormControl(evaluacionRawValue.fechaEvaluacion, {
        validators: [Validators.required],
      }),
      jurado: new FormControl(evaluacionRawValue.jurado),
      proyecto: new FormControl(evaluacionRawValue.proyecto, {
        validators: [Validators.required],
      }),
    });
  }

  getEvaluacion(form: EvaluacionFormGroup): IEvaluacion | NewEvaluacion {
    return this.convertEvaluacionRawValueToEvaluacion(form.getRawValue());
  }

  resetForm(form: EvaluacionFormGroup, evaluacion: EvaluacionFormGroupInput): void {
    const evaluacionRawValue = this.convertEvaluacionToEvaluacionRawValue({ ...this.getFormDefaults(), ...evaluacion });
    form.reset({
      ...evaluacionRawValue,
      id: { value: evaluacionRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): EvaluacionFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      fechaEvaluacion: currentTime,
    };
  }

  private convertEvaluacionRawValueToEvaluacion(
    rawEvaluacion: EvaluacionFormRawValue | NewEvaluacionFormRawValue,
  ): IEvaluacion | NewEvaluacion {
    return {
      ...rawEvaluacion,
      fechaEvaluacion: dayjs(rawEvaluacion.fechaEvaluacion, DATE_TIME_FORMAT),
    };
  }

  private convertEvaluacionToEvaluacionRawValue(
    evaluacion: IEvaluacion | (Partial<NewEvaluacion> & EvaluacionFormDefaults),
  ): EvaluacionFormRawValue | PartialWithRequiredKeyOf<NewEvaluacionFormRawValue> {
    return {
      ...evaluacion,
      fechaEvaluacion: evaluacion.fechaEvaluacion ? evaluacion.fechaEvaluacion.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
