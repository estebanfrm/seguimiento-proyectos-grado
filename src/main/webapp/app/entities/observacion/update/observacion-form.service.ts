import { Service } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';

import { DATE_TIME_FORMAT } from 'app/config';
import { IObservacion, NewObservacion } from '../observacion.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IObservacion for edit and NewObservacionFormGroupInput for create.
 */
type ObservacionFormGroupInput = IObservacion | PartialWithRequiredKeyOf<NewObservacion>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IObservacion | NewObservacion> = Omit<T, 'fechaCreacion'> & {
  fechaCreacion?: string | null;
};

type ObservacionFormRawValue = FormValueOf<IObservacion>;

type NewObservacionFormRawValue = FormValueOf<NewObservacion>;

type ObservacionFormDefaults = Pick<NewObservacion, 'id' | 'resuelta' | 'fechaCreacion'>;

type ObservacionFormGroupContent = {
  id: FormControl<ObservacionFormRawValue['id'] | NewObservacion['id']>;
  contenido: FormControl<ObservacionFormRawValue['contenido']>;
  tipo: FormControl<ObservacionFormRawValue['tipo']>;
  resuelta: FormControl<ObservacionFormRawValue['resuelta']>;
  fechaCreacion: FormControl<ObservacionFormRawValue['fechaCreacion']>;
  autor: FormControl<ObservacionFormRawValue['autor']>;
  entrega: FormControl<ObservacionFormRawValue['entrega']>;
};

export type ObservacionFormGroup = FormGroup<ObservacionFormGroupContent>;

@Service()
export class ObservacionFormService {
  createObservacionFormGroup(observacion?: ObservacionFormGroupInput): ObservacionFormGroup {
    const observacionRawValue = this.convertObservacionToObservacionRawValue({
      ...this.getFormDefaults(),
      ...(observacion ?? { id: null }),
    });

    return new FormGroup<ObservacionFormGroupContent>({
      id: new FormControl(
        { value: observacionRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      contenido: new FormControl(observacionRawValue.contenido, {
        validators: [Validators.required],
      }),
      tipo: new FormControl(observacionRawValue.tipo, {
        validators: [Validators.required],
      }),
      resuelta: new FormControl(observacionRawValue.resuelta, {
        validators: [Validators.required],
      }),
      fechaCreacion: new FormControl(observacionRawValue.fechaCreacion, {
        validators: [Validators.required],
      }),
      autor: new FormControl(observacionRawValue.autor),
      entrega: new FormControl(observacionRawValue.entrega, {
        validators: [Validators.required],
      }),
    });
  }

  getObservacion(form: ObservacionFormGroup): IObservacion | NewObservacion {
    return this.convertObservacionRawValueToObservacion(form.getRawValue());
  }

  resetForm(form: ObservacionFormGroup, observacion: ObservacionFormGroupInput): void {
    const observacionRawValue = this.convertObservacionToObservacionRawValue({ ...this.getFormDefaults(), ...observacion });
    form.reset({
      ...observacionRawValue,
      id: { value: observacionRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): ObservacionFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      resuelta: false,
      fechaCreacion: currentTime,
    };
  }

  private convertObservacionRawValueToObservacion(
    rawObservacion: ObservacionFormRawValue | NewObservacionFormRawValue,
  ): IObservacion | NewObservacion {
    return {
      ...rawObservacion,
      fechaCreacion: dayjs(rawObservacion.fechaCreacion, DATE_TIME_FORMAT),
    };
  }

  private convertObservacionToObservacionRawValue(
    observacion: IObservacion | (Partial<NewObservacion> & ObservacionFormDefaults),
  ): ObservacionFormRawValue | PartialWithRequiredKeyOf<NewObservacionFormRawValue> {
    return {
      ...observacion,
      fechaCreacion: observacion.fechaCreacion ? observacion.fechaCreacion.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
