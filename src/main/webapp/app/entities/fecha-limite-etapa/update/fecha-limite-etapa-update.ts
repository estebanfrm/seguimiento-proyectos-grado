import { HttpResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslatePipe } from '@ngx-translate/core';
import { Observable, finalize, map } from 'rxjs';

import { Etapa } from 'app/entities/enumerations/etapa.model';
import { IProyectoGrado } from 'app/entities/proyecto-grado/proyecto-grado.model';
import { ProyectoGradoService } from 'app/entities/proyecto-grado/service/proyecto-grado.service';
import { AlertError } from 'app/shared/alert';
import { TranslateDirective } from 'app/shared/language';
import { IFechaLimiteEtapa } from '../fecha-limite-etapa.model';
import { FechaLimiteEtapaService } from '../service/fecha-limite-etapa.service';

import { FechaLimiteEtapaFormGroup, FechaLimiteEtapaFormService } from './fecha-limite-etapa-form.service';

@Component({
  selector: 'jhi-fecha-limite-etapa-update',
  templateUrl: './fecha-limite-etapa-update.html',
  imports: [TranslateDirective, TranslatePipe, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class FechaLimiteEtapaUpdate implements OnInit {
  readonly isSaving = signal(false);
  fechaLimiteEtapa: IFechaLimiteEtapa | null = null;
  etapaValues = Object.keys(Etapa);

  proyectoGradosSharedCollection = signal<IProyectoGrado[]>([]);

  protected fechaLimiteEtapaService = inject(FechaLimiteEtapaService);
  protected fechaLimiteEtapaFormService = inject(FechaLimiteEtapaFormService);
  protected proyectoGradoService = inject(ProyectoGradoService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: FechaLimiteEtapaFormGroup = this.fechaLimiteEtapaFormService.createFechaLimiteEtapaFormGroup();

  compareProyectoGrado = (o1: IProyectoGrado | null, o2: IProyectoGrado | null): boolean =>
    this.proyectoGradoService.compareProyectoGrado(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ fechaLimiteEtapa }) => {
      this.fechaLimiteEtapa = fechaLimiteEtapa;
      if (fechaLimiteEtapa) {
        this.updateForm(fechaLimiteEtapa);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const fechaLimiteEtapa = this.fechaLimiteEtapaFormService.getFechaLimiteEtapa(this.editForm);
    if (fechaLimiteEtapa.id === null) {
      this.subscribeToSaveResponse(this.fechaLimiteEtapaService.create(fechaLimiteEtapa));
    } else {
      this.subscribeToSaveResponse(this.fechaLimiteEtapaService.update(fechaLimiteEtapa));
    }
  }

  protected subscribeToSaveResponse(result: Observable<IFechaLimiteEtapa | null>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving.set(false);
  }

  protected updateForm(fechaLimiteEtapa: IFechaLimiteEtapa): void {
    this.fechaLimiteEtapa = fechaLimiteEtapa;
    this.fechaLimiteEtapaFormService.resetForm(this.editForm, fechaLimiteEtapa);

    this.proyectoGradosSharedCollection.update(proyectoGrados =>
      this.proyectoGradoService.addProyectoGradoToCollectionIfMissing<IProyectoGrado>(proyectoGrados, fechaLimiteEtapa.proyecto),
    );
  }

  protected loadRelationshipsOptions(): void {
    this.proyectoGradoService
      .query()
      .pipe(map((res: HttpResponse<IProyectoGrado[]>) => res.body ?? []))
      .pipe(
        map((proyectoGrados: IProyectoGrado[]) =>
          this.proyectoGradoService.addProyectoGradoToCollectionIfMissing<IProyectoGrado>(proyectoGrados, this.fechaLimiteEtapa?.proyecto),
        ),
      )
      .subscribe((proyectoGrados: IProyectoGrado[]) => this.proyectoGradosSharedCollection.set(proyectoGrados));
  }
}
