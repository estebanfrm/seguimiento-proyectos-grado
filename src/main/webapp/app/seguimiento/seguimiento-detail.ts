import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { NgbModal } from '@ng-bootstrap/ng-bootstrap/modal';
import { TranslatePipe } from '@ngx-translate/core';
import { EMPTY, Subject, catchError, finalize, map, startWith, switchMap, takeUntil } from 'rxjs';

import { AccountService } from 'app/core/auth';
import { TranslateDirective } from 'app/shared/language';

import { SeguimientoAlert } from './seguimiento-alert';
import { AccionSeguimiento, SeguimientoDialog } from './seguimiento-dialog';
import { problemaSeguimiento } from './seguimiento-error';
import { IDetalleEntrega, IEtapaSeguimiento, IProblemaSeguimiento, ISeguimientoProyecto } from './seguimiento.model';
import { SeguimientoService } from './seguimiento.service';

@Component({
  selector: 'jhi-seguimiento-detail',
  templateUrl: './seguimiento-detail.html',
  styleUrl: './seguimiento-detail.scss',
  imports: [RouterLink, DatePipe, TranslatePipe, TranslateDirective, SeguimientoAlert],
})
export default class SeguimientoDetail implements OnInit {
  readonly seguimiento = signal<ISeguimientoProyecto | null>(null);
  readonly cargando = signal(false);
  readonly guardando = signal(false);
  readonly problema = signal<IProblemaSeguimiento | null>(null);
  readonly detalles = signal<Partial<Record<number, IDetalleEntrega>>>({});
  readonly expandidas = signal<number[]>([]);
  readonly cargandoEntregas = signal<number[]>([]);
  readonly activa = computed(() => {
    const proyecto = this.seguimiento()?.proyecto;
    return proyecto?.estado === 'ACTIVO' && proyecto.etapaActual !== 'FINALIZADO';
  });
  readonly esAdmin = computed(() => this.accountService.hasAnyAuthority('ROLE_ADMIN'));
  readonly esEstudiante = computed(() => {
    const login = this.accountService.account()?.login;
    return (
      this.accountService.hasAnyAuthority('ROLE_ESTUDIANTE') &&
      !!this.seguimiento()?.proyecto.estudiantes?.some(user => user.login === login)
    );
  });
  readonly esTutor = computed(
    () =>
      this.accountService.hasAnyAuthority('ROLE_TUTOR') &&
      this.seguimiento()?.proyecto.tutor?.login === this.accountService.account()?.login,
  );
  readonly puedeRevisar = computed(() => this.activa() && (this.esAdmin() || this.esTutor()));
  readonly puedeEvaluar = computed(() => {
    const data = this.seguimiento();
    const login = this.accountService.account()?.login;
    if (
      !data ||
      !this.activa() ||
      !this.accountService.hasAnyAuthority('ROLE_JURADO') ||
      !data.proyecto.jurados?.some(user => user.login === login)
    ) {
      return false;
    }
    const etapa = data.proyecto.etapaActual;
    return (
      (etapa === 'ANTEPROYECTO' || etapa === 'SUSTENTACION') &&
      !data.etapas.find(item => item.etapa === etapa)?.evaluaciones.some(item => item.jurado?.login === login)
    );
  });
  readonly puedeEntregar = computed(() => {
    const data = this.seguimiento();
    const etapa = data?.etapas.find(item => item.etapa === data.proyecto.etapaActual);
    return (
      this.activa() &&
      this.esEstudiante() &&
      !etapa?.vencida &&
      !etapa?.entregas.some(item => ['ENVIADA', 'EN_REVISION', 'APROBADA'].includes(item.estado))
    );
  });

  private readonly accountService = inject(AccountService);
  private readonly service = inject(SeguimientoService);
  private readonly route = inject(ActivatedRoute);
  private readonly modal = inject(NgbModal);
  private readonly destroyRef = inject(DestroyRef);
  private readonly refrescar = new Subject<void>();

  ngOnInit(): void {
    this.route.paramMap
      .pipe(
        map(params => Number(params.get('id'))),
        switchMap(id =>
          this.refrescar.pipe(
            startWith(undefined),
            switchMap(() => {
              this.cargando.set(true);
              this.problema.set(null);
              this.seguimiento.set(null);
              this.detalles.set({});
              this.expandidas.set([]);
              this.cargandoEntregas.set([]);
              return this.service.seguimiento(id).pipe(
                catchError((error: HttpErrorResponse) => {
                  this.problema.set(problemaSeguimiento(error));
                  return EMPTY;
                }),
                finalize(() => this.cargando.set(false)),
              );
            }),
          ),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(data => this.seguimiento.set(data));
  }

  recargar(): void {
    this.refrescar.next();
  }

  esEtapaActual(etapa: IEtapaSeguimiento): boolean {
    return etapa.etapa === this.seguimiento()?.proyecto.etapaActual;
  }

  abrir(accion: AccionSeguimiento, entregaId = 0): void {
    const data = this.seguimiento();
    if (!data) {
      return;
    }
    const ref = this.modal.open(SeguimientoDialog, { size: 'lg', backdrop: 'static', ariaLabelledBy: 'seguimiento-dialog-title' });
    (ref.componentInstance as SeguimientoDialog).preparar(accion, data.proyecto.id, entregaId);
    ref.closed.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => this.recargar());
  }

  expandir(id: number): void {
    if (this.expandidas().includes(id)) {
      this.expandidas.update(ids => ids.filter(value => value !== id));
      return;
    }
    this.expandidas.update(ids => [...ids, id]);
    if (this.detalles()[id] || this.cargandoEntregas().includes(id)) {
      return;
    }
    const proyectoId = this.seguimiento()?.proyecto.id;
    this.cargandoEntregas.update(ids => [...ids, id]);
    this.service
      .detalleEntrega(id)
      .pipe(
        takeUntil(this.refrescar),
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.cargandoEntregas.update(ids => ids.filter(value => value !== id))),
      )
      .subscribe({
        next: detalle => {
          if (proyectoId === this.seguimiento()?.proyecto.id) {
            this.detalles.update(values => ({ ...values, [id]: detalle }));
          }
        },
        error: (error: HttpErrorResponse) => this.problema.set(problemaSeguimiento(error)),
      });
  }

  resolver(id: number): void {
    if (!this.puedeRevisar() || this.guardando()) {
      return;
    }
    this.guardando.set(true);
    this.problema.set(null);
    this.service
      .resolver(id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.guardando.set(false)),
      )
      .subscribe({
        next: () => this.recargar(),
        error: (error: HttpErrorResponse) => this.problema.set(problemaSeguimiento(error)),
      });
  }

  descargarPdf(id: number): void {
    this.service
      .descargarArchivo(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next(response) {
          if (!response.body) return;
          const nombreCodificado = /filename\*=UTF-8''([^;]+)/i.exec(response.headers.get('Content-Disposition') ?? '')?.[1];
          let nombre = `entrega-${id}.pdf`;
          if (nombreCodificado) {
            try {
              nombre = decodeURIComponent(nombreCodificado);
            } catch {
              // Se conserva el nombre de respaldo si el encabezado está mal codificado.
            }
          }
          const url = URL.createObjectURL(response.body);
          const enlace = document.createElement('a');
          enlace.href = url;
          enlace.download = nombre;
          document.body.append(enlace);
          enlace.click();
          enlace.remove();
          setTimeout(() => URL.revokeObjectURL(url), 0);
        },
        error: (error: HttpErrorResponse) => this.problema.set(problemaSeguimiento(error)),
      });
  }
}
