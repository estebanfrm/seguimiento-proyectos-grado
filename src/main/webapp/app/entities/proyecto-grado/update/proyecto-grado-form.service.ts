import { Service } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';

import { DATE_TIME_FORMAT } from 'app/config';
import { IProyectoGrado, NewProyectoGrado } from '../proyecto-grado.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IProyectoGrado for edit and NewProyectoGradoFormGroupInput for create.
 */
type ProyectoGradoFormGroupInput = IProyectoGrado | PartialWithRequiredKeyOf<NewProyectoGrado>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IProyectoGrado | NewProyectoGrado> = Omit<T, 'fechaRegistro' | 'fechaActualizacion'> & {
  fechaRegistro?: string | null;
  fechaActualizacion?: string | null;
};

type ProyectoGradoFormRawValue = FormValueOf<IProyectoGrado>;

type NewProyectoGradoFormRawValue = FormValueOf<NewProyectoGrado>;

type ProyectoGradoFormDefaults = Pick<NewProyectoGrado, 'id' | 'fechaRegistro' | 'fechaActualizacion' | 'estudiantes' | 'jurados'>;

type ProyectoGradoFormGroupContent = {
  id: FormControl<ProyectoGradoFormRawValue['id'] | NewProyectoGrado['id']>;
  titulo: FormControl<ProyectoGradoFormRawValue['titulo']>;
  resumen: FormControl<ProyectoGradoFormRawValue['resumen']>;
  programa: FormControl<ProyectoGradoFormRawValue['programa']>;
  etapaActual: FormControl<ProyectoGradoFormRawValue['etapaActual']>;
  estado: FormControl<ProyectoGradoFormRawValue['estado']>;
  porcentajeAvance: FormControl<ProyectoGradoFormRawValue['porcentajeAvance']>;
  fechaRegistro: FormControl<ProyectoGradoFormRawValue['fechaRegistro']>;
  fechaActualizacion: FormControl<ProyectoGradoFormRawValue['fechaActualizacion']>;
  tutor: FormControl<ProyectoGradoFormRawValue['tutor']>;
  estudiantes: FormControl<ProyectoGradoFormRawValue['estudiantes']>;
  jurados: FormControl<ProyectoGradoFormRawValue['jurados']>;
};

export type ProyectoGradoFormGroup = FormGroup<ProyectoGradoFormGroupContent>;

@Service()
export class ProyectoGradoFormService {
  createProyectoGradoFormGroup(proyectoGrado?: ProyectoGradoFormGroupInput): ProyectoGradoFormGroup {
    const proyectoGradoRawValue = this.convertProyectoGradoToProyectoGradoRawValue({
      ...this.getFormDefaults(),
      ...(proyectoGrado ?? { id: null }),
    });

    return new FormGroup<ProyectoGradoFormGroupContent>({
      id: new FormControl(
        { value: proyectoGradoRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      titulo: new FormControl(proyectoGradoRawValue.titulo, {
        validators: [Validators.required, Validators.minLength(10), Validators.maxLength(250)],
      }),
      resumen: new FormControl(proyectoGradoRawValue.resumen, {
        validators: [Validators.required],
      }),
      programa: new FormControl(proyectoGradoRawValue.programa, {
        validators: [Validators.required, Validators.maxLength(120)],
      }),
      etapaActual: new FormControl(proyectoGradoRawValue.etapaActual, {
        validators: [Validators.required],
      }),
      estado: new FormControl(proyectoGradoRawValue.estado, {
        validators: [Validators.required],
      }),
      porcentajeAvance: new FormControl(proyectoGradoRawValue.porcentajeAvance, {
        validators: [Validators.min(0), Validators.max(100)],
      }),
      fechaRegistro: new FormControl(proyectoGradoRawValue.fechaRegistro, {
        validators: [Validators.required],
      }),
      fechaActualizacion: new FormControl(proyectoGradoRawValue.fechaActualizacion),
      tutor: new FormControl(proyectoGradoRawValue.tutor),
      estudiantes: new FormControl(proyectoGradoRawValue.estudiantes ?? []),
      jurados: new FormControl(proyectoGradoRawValue.jurados ?? []),
    });
  }

  getProyectoGrado(form: ProyectoGradoFormGroup): IProyectoGrado | NewProyectoGrado {
    return this.convertProyectoGradoRawValueToProyectoGrado(form.getRawValue());
  }

  resetForm(form: ProyectoGradoFormGroup, proyectoGrado: ProyectoGradoFormGroupInput): void {
    const proyectoGradoRawValue = this.convertProyectoGradoToProyectoGradoRawValue({ ...this.getFormDefaults(), ...proyectoGrado });
    form.reset({
      ...proyectoGradoRawValue,
      id: { value: proyectoGradoRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): ProyectoGradoFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      fechaRegistro: currentTime,
      fechaActualizacion: currentTime,
      estudiantes: [],
      jurados: [],
    };
  }

  private convertProyectoGradoRawValueToProyectoGrado(
    rawProyectoGrado: ProyectoGradoFormRawValue | NewProyectoGradoFormRawValue,
  ): IProyectoGrado | NewProyectoGrado {
    return {
      ...rawProyectoGrado,
      fechaRegistro: dayjs(rawProyectoGrado.fechaRegistro, DATE_TIME_FORMAT),
      fechaActualizacion: dayjs(rawProyectoGrado.fechaActualizacion, DATE_TIME_FORMAT),
    };
  }

  private convertProyectoGradoToProyectoGradoRawValue(
    proyectoGrado: IProyectoGrado | (Partial<NewProyectoGrado> & ProyectoGradoFormDefaults),
  ): ProyectoGradoFormRawValue | PartialWithRequiredKeyOf<NewProyectoGradoFormRawValue> {
    return {
      ...proyectoGrado,
      fechaRegistro: proyectoGrado.fechaRegistro ? proyectoGrado.fechaRegistro.format(DATE_TIME_FORMAT) : undefined,
      fechaActualizacion: proyectoGrado.fechaActualizacion ? proyectoGrado.fechaActualizacion.format(DATE_TIME_FORMAT) : undefined,
      estudiantes: proyectoGrado.estudiantes ?? [],
      jurados: proyectoGrado.jurados ?? [],
    };
  }
}
