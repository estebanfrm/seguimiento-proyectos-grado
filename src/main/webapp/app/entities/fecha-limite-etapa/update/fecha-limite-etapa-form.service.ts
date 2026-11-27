import { Service } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';

import { DATE_TIME_FORMAT } from 'app/config';
import { IFechaLimiteEtapa, NewFechaLimiteEtapa } from '../fecha-limite-etapa.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IFechaLimiteEtapa for edit and NewFechaLimiteEtapaFormGroupInput for create.
 */
type FechaLimiteEtapaFormGroupInput = IFechaLimiteEtapa | PartialWithRequiredKeyOf<NewFechaLimiteEtapa>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IFechaLimiteEtapa | NewFechaLimiteEtapa> = Omit<T, 'fechaLimite'> & {
  fechaLimite?: string | null;
};

type FechaLimiteEtapaFormRawValue = FormValueOf<IFechaLimiteEtapa>;

type NewFechaLimiteEtapaFormRawValue = FormValueOf<NewFechaLimiteEtapa>;

type FechaLimiteEtapaFormDefaults = Pick<NewFechaLimiteEtapa, 'id' | 'fechaLimite'>;

type FechaLimiteEtapaFormGroupContent = {
  id: FormControl<FechaLimiteEtapaFormRawValue['id'] | NewFechaLimiteEtapa['id']>;
  etapa: FormControl<FechaLimiteEtapaFormRawValue['etapa']>;
  fechaLimite: FormControl<FechaLimiteEtapaFormRawValue['fechaLimite']>;
  descripcion: FormControl<FechaLimiteEtapaFormRawValue['descripcion']>;
  proyecto: FormControl<FechaLimiteEtapaFormRawValue['proyecto']>;
};

export type FechaLimiteEtapaFormGroup = FormGroup<FechaLimiteEtapaFormGroupContent>;

@Service()
export class FechaLimiteEtapaFormService {
  createFechaLimiteEtapaFormGroup(fechaLimiteEtapa?: FechaLimiteEtapaFormGroupInput): FechaLimiteEtapaFormGroup {
    const fechaLimiteEtapaRawValue = this.convertFechaLimiteEtapaToFechaLimiteEtapaRawValue({
      ...this.getFormDefaults(),
      ...(fechaLimiteEtapa ?? { id: null }),
    });

    return new FormGroup<FechaLimiteEtapaFormGroupContent>({
      id: new FormControl(
        { value: fechaLimiteEtapaRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      etapa: new FormControl(fechaLimiteEtapaRawValue.etapa, {
        validators: [Validators.required],
      }),
      fechaLimite: new FormControl(fechaLimiteEtapaRawValue.fechaLimite, {
        validators: [Validators.required],
      }),
      descripcion: new FormControl(fechaLimiteEtapaRawValue.descripcion, {
        validators: [Validators.maxLength(250)],
      }),
      proyecto: new FormControl(fechaLimiteEtapaRawValue.proyecto, {
        validators: [Validators.required],
      }),
    });
  }

  getFechaLimiteEtapa(form: FechaLimiteEtapaFormGroup): IFechaLimiteEtapa | NewFechaLimiteEtapa {
    return this.convertFechaLimiteEtapaRawValueToFechaLimiteEtapa(form.getRawValue());
  }

  resetForm(form: FechaLimiteEtapaFormGroup, fechaLimiteEtapa: FechaLimiteEtapaFormGroupInput): void {
    const fechaLimiteEtapaRawValue = this.convertFechaLimiteEtapaToFechaLimiteEtapaRawValue({
      ...this.getFormDefaults(),
      ...fechaLimiteEtapa,
    });
    form.reset({
      ...fechaLimiteEtapaRawValue,
      id: { value: fechaLimiteEtapaRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): FechaLimiteEtapaFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      fechaLimite: currentTime,
    };
  }

  private convertFechaLimiteEtapaRawValueToFechaLimiteEtapa(
    rawFechaLimiteEtapa: FechaLimiteEtapaFormRawValue | NewFechaLimiteEtapaFormRawValue,
  ): IFechaLimiteEtapa | NewFechaLimiteEtapa {
    return {
      ...rawFechaLimiteEtapa,
      fechaLimite: dayjs(rawFechaLimiteEtapa.fechaLimite, DATE_TIME_FORMAT),
    };
  }

  private convertFechaLimiteEtapaToFechaLimiteEtapaRawValue(
    fechaLimiteEtapa: IFechaLimiteEtapa | (Partial<NewFechaLimiteEtapa> & FechaLimiteEtapaFormDefaults),
  ): FechaLimiteEtapaFormRawValue | PartialWithRequiredKeyOf<NewFechaLimiteEtapaFormRawValue> {
    return {
      ...fechaLimiteEtapa,
      fechaLimite: fechaLimiteEtapa.fechaLimite ? fechaLimiteEtapa.fechaLimite.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
