import { Service } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';

import { DATE_TIME_FORMAT } from 'app/config';
import { IEntrega, NewEntrega } from '../entrega.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IEntrega for edit and NewEntregaFormGroupInput for create.
 */
type EntregaFormGroupInput = IEntrega | PartialWithRequiredKeyOf<NewEntrega>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IEntrega | NewEntrega> = Omit<T, 'fechaEntrega'> & {
  fechaEntrega?: string | null;
};

type EntregaFormRawValue = FormValueOf<IEntrega>;

type NewEntregaFormRawValue = FormValueOf<NewEntrega>;

type EntregaFormDefaults = Pick<NewEntrega, 'id' | 'fechaEntrega'>;

type EntregaFormGroupContent = {
  id: FormControl<EntregaFormRawValue['id'] | NewEntrega['id']>;
  etapa: FormControl<EntregaFormRawValue['etapa']>;
  titulo: FormControl<EntregaFormRawValue['titulo']>;
  descripcion: FormControl<EntregaFormRawValue['descripcion']>;
  archivo: FormControl<EntregaFormRawValue['archivo']>;
  archivoContentType: FormControl<EntregaFormRawValue['archivoContentType']>;
  urlDocumento: FormControl<EntregaFormRawValue['urlDocumento']>;
  version: FormControl<EntregaFormRawValue['version']>;
  fechaEntrega: FormControl<EntregaFormRawValue['fechaEntrega']>;
  estado: FormControl<EntregaFormRawValue['estado']>;
  autor: FormControl<EntregaFormRawValue['autor']>;
  proyecto: FormControl<EntregaFormRawValue['proyecto']>;
};

export type EntregaFormGroup = FormGroup<EntregaFormGroupContent>;

@Service()
export class EntregaFormService {
  createEntregaFormGroup(entrega?: EntregaFormGroupInput): EntregaFormGroup {
    const entregaRawValue = this.convertEntregaToEntregaRawValue({
      ...this.getFormDefaults(),
      ...(entrega ?? { id: null }),
    });

    return new FormGroup<EntregaFormGroupContent>({
      id: new FormControl(
        { value: entregaRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      etapa: new FormControl(entregaRawValue.etapa, {
        validators: [Validators.required],
      }),
      titulo: new FormControl(entregaRawValue.titulo, {
        validators: [Validators.required, Validators.maxLength(200)],
      }),
      descripcion: new FormControl(entregaRawValue.descripcion),
      archivo: new FormControl(entregaRawValue.archivo),
      archivoContentType: new FormControl(entregaRawValue.archivoContentType),
      urlDocumento: new FormControl(entregaRawValue.urlDocumento, {
        validators: [Validators.maxLength(500)],
      }),
      version: new FormControl(entregaRawValue.version, {
        validators: [Validators.required, Validators.min(1)],
      }),
      fechaEntrega: new FormControl(entregaRawValue.fechaEntrega, {
        validators: [Validators.required],
      }),
      estado: new FormControl(entregaRawValue.estado, {
        validators: [Validators.required],
      }),
      autor: new FormControl(entregaRawValue.autor),
      proyecto: new FormControl(entregaRawValue.proyecto, {
        validators: [Validators.required],
      }),
    });
  }

  getEntrega(form: EntregaFormGroup): IEntrega | NewEntrega {
    return this.convertEntregaRawValueToEntrega(form.getRawValue());
  }

  resetForm(form: EntregaFormGroup, entrega: EntregaFormGroupInput): void {
    const entregaRawValue = this.convertEntregaToEntregaRawValue({ ...this.getFormDefaults(), ...entrega });
    form.reset({
      ...entregaRawValue,
      id: { value: entregaRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): EntregaFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      fechaEntrega: currentTime,
    };
  }

  private convertEntregaRawValueToEntrega(rawEntrega: EntregaFormRawValue | NewEntregaFormRawValue): IEntrega | NewEntrega {
    return {
      ...rawEntrega,
      fechaEntrega: dayjs(rawEntrega.fechaEntrega, DATE_TIME_FORMAT),
    };
  }

  private convertEntregaToEntregaRawValue(
    entrega: IEntrega | (Partial<NewEntrega> & EntregaFormDefaults),
  ): EntregaFormRawValue | PartialWithRequiredKeyOf<NewEntregaFormRawValue> {
    return {
      ...entrega,
      fechaEntrega: entrega.fechaEntrega ? entrega.fechaEntrega.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
