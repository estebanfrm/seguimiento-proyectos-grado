import { Service } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';

import { DATE_TIME_FORMAT } from 'app/config';
import { IHistorialEtapa, NewHistorialEtapa } from '../historial-etapa.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IHistorialEtapa for edit and NewHistorialEtapaFormGroupInput for create.
 */
type HistorialEtapaFormGroupInput = IHistorialEtapa | PartialWithRequiredKeyOf<NewHistorialEtapa>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IHistorialEtapa | NewHistorialEtapa> = Omit<T, 'fecha'> & {
  fecha?: string | null;
};

type HistorialEtapaFormRawValue = FormValueOf<IHistorialEtapa>;

type NewHistorialEtapaFormRawValue = FormValueOf<NewHistorialEtapa>;

type HistorialEtapaFormDefaults = Pick<NewHistorialEtapa, 'id' | 'fecha'>;

type HistorialEtapaFormGroupContent = {
  id: FormControl<HistorialEtapaFormRawValue['id'] | NewHistorialEtapa['id']>;
  etapaOrigen: FormControl<HistorialEtapaFormRawValue['etapaOrigen']>;
  etapaDestino: FormControl<HistorialEtapaFormRawValue['etapaDestino']>;
  fecha: FormControl<HistorialEtapaFormRawValue['fecha']>;
  comentario: FormControl<HistorialEtapaFormRawValue['comentario']>;
  usuario: FormControl<HistorialEtapaFormRawValue['usuario']>;
  proyecto: FormControl<HistorialEtapaFormRawValue['proyecto']>;
};

export type HistorialEtapaFormGroup = FormGroup<HistorialEtapaFormGroupContent>;

@Service()
export class HistorialEtapaFormService {
  createHistorialEtapaFormGroup(historialEtapa?: HistorialEtapaFormGroupInput): HistorialEtapaFormGroup {
    const historialEtapaRawValue = this.convertHistorialEtapaToHistorialEtapaRawValue({
      ...this.getFormDefaults(),
      ...(historialEtapa ?? { id: null }),
    });

    return new FormGroup<HistorialEtapaFormGroupContent>({
      id: new FormControl(
        { value: historialEtapaRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      etapaOrigen: new FormControl(historialEtapaRawValue.etapaOrigen),
      etapaDestino: new FormControl(historialEtapaRawValue.etapaDestino, {
        validators: [Validators.required],
      }),
      fecha: new FormControl(historialEtapaRawValue.fecha, {
        validators: [Validators.required],
      }),
      comentario: new FormControl(historialEtapaRawValue.comentario, {
        validators: [Validators.maxLength(500)],
      }),
      usuario: new FormControl(historialEtapaRawValue.usuario),
      proyecto: new FormControl(historialEtapaRawValue.proyecto, {
        validators: [Validators.required],
      }),
    });
  }

  getHistorialEtapa(form: HistorialEtapaFormGroup): IHistorialEtapa | NewHistorialEtapa {
    return this.convertHistorialEtapaRawValueToHistorialEtapa(form.getRawValue());
  }

  resetForm(form: HistorialEtapaFormGroup, historialEtapa: HistorialEtapaFormGroupInput): void {
    const historialEtapaRawValue = this.convertHistorialEtapaToHistorialEtapaRawValue({ ...this.getFormDefaults(), ...historialEtapa });
    form.reset({
      ...historialEtapaRawValue,
      id: { value: historialEtapaRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): HistorialEtapaFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      fecha: currentTime,
    };
  }

  private convertHistorialEtapaRawValueToHistorialEtapa(
    rawHistorialEtapa: HistorialEtapaFormRawValue | NewHistorialEtapaFormRawValue,
  ): IHistorialEtapa | NewHistorialEtapa {
    return {
      ...rawHistorialEtapa,
      fecha: dayjs(rawHistorialEtapa.fecha, DATE_TIME_FORMAT),
    };
  }

  private convertHistorialEtapaToHistorialEtapaRawValue(
    historialEtapa: IHistorialEtapa | (Partial<NewHistorialEtapa> & HistorialEtapaFormDefaults),
  ): HistorialEtapaFormRawValue | PartialWithRequiredKeyOf<NewHistorialEtapaFormRawValue> {
    return {
      ...historialEtapa,
      fecha: historialEtapa.fecha ? historialEtapa.fecha.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
