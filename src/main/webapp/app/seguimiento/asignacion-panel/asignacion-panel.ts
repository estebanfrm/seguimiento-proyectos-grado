import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnChanges, computed, inject, input, output, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';

import { Subject, finalize, forkJoin, takeUntil } from 'rxjs';

import { AccountService } from 'app/core/auth';
import { RestProyectoGrado } from 'app/entities/proyecto-grado/service/proyecto-grado.service';
import { TranslateDirective } from 'app/shared/language';

import { SeguimientoAlert } from '../seguimiento-alert';
import { problemaSeguimiento } from '../seguimiento-error';
import { IProblemaSeguimiento, IUsuarioResumen } from '../seguimiento.model';
import { SeguimientoService } from '../seguimiento.service';

@Component({
  selector: 'jhi-asignacion-panel',
  templateUrl: './asignacion-panel.html',
  imports: [ReactiveFormsModule, TranslateDirective, SeguimientoAlert],
})
export class AsignacionPanel implements OnChanges {
  readonly proyecto = input.required<RestProyectoGrado>();
  readonly actualizado = output();
  readonly cargando = signal(false);
  readonly guardando = signal(false);
  readonly usuariosCargados = signal(false);
  readonly problema = signal<IProblemaSeguimiento | null>(null);
  readonly tutores = signal<IUsuarioResumen[]>([]);
  readonly jurados = signal<IUsuarioResumen[]>([]);
  readonly estudiantes = signal<IUsuarioResumen[]>([]);
  readonly esAdmin = computed(() => this.accountService.hasAnyAuthority('ROLE_ADMIN'));
  readonly form = inject(FormBuilder).group({
    tutorId: [null as number | null],
    juradoIds: [[] as number[]],
    estudianteIds: [[] as number[]],
  });
  readonly seleccion = toSignal(this.form.valueChanges, { initialValue: this.form.getRawValue() });
  readonly faltaTutor = computed(() => this.seleccion().tutorId == null);
  readonly faltanJurados = computed(() => this.proyecto().etapaActual === 'ANTEPROYECTO' && (this.seleccion().juradoIds?.length ?? 0) < 2);
  readonly opcionesTutores = computed(() => this.conAsignados(this.tutores(), this.proyecto().tutor ? [this.proyecto().tutor!] : []));
  readonly opcionesJurados = computed(() => this.conAsignados(this.jurados(), this.proyecto().jurados ?? []));
  readonly opcionesEstudiantes = computed(() => this.conAsignados(this.estudiantes(), this.proyecto().estudiantes ?? []));

  private readonly accountService = inject(AccountService);
  private readonly service = inject(SeguimientoService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cambioProyecto = new Subject<void>();

  ngOnChanges(): void {
    this.cambioProyecto.next();
    const proyecto = this.proyecto();
    this.form.reset({
      tutorId: proyecto.tutor?.id ?? null,
      juradoIds: proyecto.jurados?.map(usuario => usuario.id) ?? [],
      estudianteIds: proyecto.estudiantes?.map(usuario => usuario.id) ?? [],
    });
    this.cargarUsuarios();
  }

  cargarUsuarios(): void {
    if (!this.esAdmin() || this.cargando()) {
      return;
    }
    this.cargando.set(true);
    this.usuariosCargados.set(false);
    this.problema.set(null);
    forkJoin({
      tutores: this.service.usuariosPorRol('ROLE_TUTOR'),
      jurados: this.service.usuariosPorRol('ROLE_JURADO'),
      estudiantes: this.service.usuariosPorRol('ROLE_ESTUDIANTE'),
    })
      .pipe(
        takeUntil(this.cambioProyecto),
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.cargando.set(false)),
      )
      .subscribe({
        next: usuarios => {
          this.tutores.set(usuarios.tutores);
          this.jurados.set(usuarios.jurados);
          this.estudiantes.set(usuarios.estudiantes);
          this.usuariosCargados.set(true);
        },
        error: (error: HttpErrorResponse) => this.problema.set(problemaSeguimiento(error)),
      });
  }

  guardar(): void {
    if (!this.esAdmin() || !this.usuariosCargados() || this.cargando() || this.guardando()) {
      return;
    }
    const { tutorId, juradoIds, estudianteIds } = this.form.getRawValue();
    const relacion = (usuario: IUsuarioResumen): { id: number; login: string } => ({ id: usuario.id, login: usuario.login });
    const tutor = this.opcionesTutores().find(usuario => usuario.id === tutorId);
    this.guardando.set(true);
    this.problema.set(null);
    this.service
      .asignarProyecto({
        id: this.proyecto().id,
        tutor: tutor ? relacion(tutor) : null,
        jurados: this.opcionesJurados()
          .filter(usuario => juradoIds?.includes(usuario.id))
          .map(relacion),
        estudiantes: this.opcionesEstudiantes()
          .filter(usuario => estudianteIds?.includes(usuario.id))
          .map(relacion),
      })
      .pipe(
        takeUntil(this.cambioProyecto),
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.guardando.set(false)),
      )
      .subscribe({
        next: () => this.actualizado.emit(),
        error: (error: HttpErrorResponse) => this.problema.set(problemaSeguimiento(error)),
      });
  }

  private conAsignados(usuarios: IUsuarioResumen[], asignados: { id: number; login?: string | null }[]): IUsuarioResumen[] {
    return [
      ...usuarios,
      ...asignados
        .filter(asignado => !usuarios.some(usuario => usuario.id === asignado.id))
        .map(asignado => ({ id: asignado.id, login: asignado.login ?? '', firstName: null, lastName: null })),
    ];
  }
}
