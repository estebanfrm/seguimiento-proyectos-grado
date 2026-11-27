import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';

import { NgbModal } from '@ng-bootstrap/ng-bootstrap/modal';
import { NgbPagination } from '@ng-bootstrap/ng-bootstrap/pagination';
import { TranslatePipe } from '@ngx-translate/core';
import { finalize } from 'rxjs';

import { AccountService } from 'app/core/auth';
import { RestProyectoGrado } from 'app/entities/proyecto-grado/service/proyecto-grado.service';
import { HasAnyAuthorityDirective } from 'app/shared/auth';
import { TranslateDirective } from 'app/shared/language';

import { SeguimientoAlert } from './seguimiento-alert';
import { SeguimientoDialog } from './seguimiento-dialog';
import { problemaSeguimiento } from './seguimiento-error';
import { IProblemaSeguimiento } from './seguimiento.model';
import { SeguimientoService } from './seguimiento.service';

@Component({
  selector: 'jhi-mis-proyectos',
  templateUrl: './mis-proyectos.html',
  imports: [RouterLink, NgbPagination, TranslatePipe, TranslateDirective, HasAnyAuthorityDirective, SeguimientoAlert],
})
export default class MisProyectos implements OnInit {
  readonly proyectos = signal<RestProyectoGrado[]>([]);
  readonly cargando = signal(false);
  readonly problema = signal<IProblemaSeguimiento | null>(null);
  readonly total = signal(0);
  readonly pagina = signal(1);
  readonly porPagina = 12;

  private readonly service = inject(SeguimientoService);
  private readonly modal = inject(NgbModal);
  private readonly accountService = inject(AccountService);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(pagina = this.pagina()): void {
    this.pagina.set(pagina);
    this.cargando.set(true);
    this.problema.set(null);
    this.service
      .proyectos(pagina - 1, this.porPagina)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.cargando.set(false)),
      )
      .subscribe({
        next: response => {
          this.proyectos.set(response.body ?? []);
          this.total.set(Number(response.headers.get('X-Total-Count') ?? response.body?.length ?? 0));
        },
        error: (error: HttpErrorResponse) => this.problema.set(problemaSeguimiento(error)),
      });
  }

  registrar(): void {
    if (!this.accountService.hasAnyAuthority('ROLE_ESTUDIANTE')) {
      return;
    }
    const ref = this.modal.open(SeguimientoDialog, { size: 'lg', backdrop: 'static', ariaLabelledBy: 'seguimiento-dialog-title' });
    (ref.componentInstance as SeguimientoDialog).preparar('propuesta');
    ref.closed.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => this.cargar(1));
  }
}
