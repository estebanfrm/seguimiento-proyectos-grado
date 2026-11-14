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
import { UserService } from 'app/entities/user/service/user.service';
import { IUser } from 'app/entities/user/user.model';
import { AlertError } from 'app/shared/alert';
import { TranslateDirective } from 'app/shared/language';
import { IHistorialEtapa } from '../historial-etapa.model';
import { HistorialEtapaService } from '../service/historial-etapa.service';

import { HistorialEtapaFormGroup, HistorialEtapaFormService } from './historial-etapa-form.service';

@Component({
  selector: 'jhi-historial-etapa-update',
  templateUrl: './historial-etapa-update.html',
  imports: [TranslateDirective, TranslatePipe, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class HistorialEtapaUpdate implements OnInit {
  readonly isSaving = signal(false);
  historialEtapa: IHistorialEtapa | null = null;
  etapaValues = Object.keys(Etapa);

  usersSharedCollection = signal<IUser[]>([]);
  proyectoGradosSharedCollection = signal<IProyectoGrado[]>([]);

  protected historialEtapaService = inject(HistorialEtapaService);
  protected historialEtapaFormService = inject(HistorialEtapaFormService);
  protected userService = inject(UserService);
  protected proyectoGradoService = inject(ProyectoGradoService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: HistorialEtapaFormGroup = this.historialEtapaFormService.createHistorialEtapaFormGroup();

  compareUser = (o1: IUser | null, o2: IUser | null): boolean => this.userService.compareUser(o1, o2);

  compareProyectoGrado = (o1: IProyectoGrado | null, o2: IProyectoGrado | null): boolean =>
    this.proyectoGradoService.compareProyectoGrado(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ historialEtapa }) => {
      this.historialEtapa = historialEtapa;
      if (historialEtapa) {
        this.updateForm(historialEtapa);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const historialEtapa = this.historialEtapaFormService.getHistorialEtapa(this.editForm);
    if (historialEtapa.id === null) {
      this.subscribeToSaveResponse(this.historialEtapaService.create(historialEtapa));
    } else {
      this.subscribeToSaveResponse(this.historialEtapaService.update(historialEtapa));
    }
  }

  protected subscribeToSaveResponse(result: Observable<IHistorialEtapa | null>): void {
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

  protected updateForm(historialEtapa: IHistorialEtapa): void {
    this.historialEtapa = historialEtapa;
    this.historialEtapaFormService.resetForm(this.editForm, historialEtapa);

    this.usersSharedCollection.update(users => this.userService.addUserToCollectionIfMissing<IUser>(users, historialEtapa.usuario));
    this.proyectoGradosSharedCollection.update(proyectoGrados =>
      this.proyectoGradoService.addProyectoGradoToCollectionIfMissing<IProyectoGrado>(proyectoGrados, historialEtapa.proyecto),
    );
  }

  protected loadRelationshipsOptions(): void {
    this.userService
      .query()
      .pipe(map((res: HttpResponse<IUser[]>) => res.body ?? []))
      .pipe(map((users: IUser[]) => this.userService.addUserToCollectionIfMissing<IUser>(users, this.historialEtapa?.usuario)))
      .subscribe((users: IUser[]) => this.usersSharedCollection.set(users));

    this.proyectoGradoService
      .query()
      .pipe(map((res: HttpResponse<IProyectoGrado[]>) => res.body ?? []))
      .pipe(
        map((proyectoGrados: IProyectoGrado[]) =>
          this.proyectoGradoService.addProyectoGradoToCollectionIfMissing<IProyectoGrado>(proyectoGrados, this.historialEtapa?.proyecto),
        ),
      )
      .subscribe((proyectoGrados: IProyectoGrado[]) => this.proyectoGradosSharedCollection.set(proyectoGrados));
  }
}
