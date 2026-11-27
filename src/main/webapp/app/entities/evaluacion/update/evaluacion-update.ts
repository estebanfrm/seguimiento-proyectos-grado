import { HttpResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslatePipe } from '@ngx-translate/core';
import { Observable, finalize, map } from 'rxjs';

import { DataUtils, EventManager, EventWithContent, FileLoadError } from 'app/core/util';
import { Concepto } from 'app/entities/enumerations/concepto.model';
import { Etapa } from 'app/entities/enumerations/etapa.model';
import { IProyectoGrado } from 'app/entities/proyecto-grado/proyecto-grado.model';
import { UserService } from 'app/entities/user/service/user.service';
import { IUser } from 'app/entities/user/user.model';
import { AlertError, AlertErrorModel } from 'app/shared/alert';
import { TranslateDirective } from 'app/shared/language';

import { IEvaluacion } from '../evaluacion.model';
import { EvaluacionService } from '../service/evaluacion.service';

import { EvaluacionFormGroup, EvaluacionFormService } from './evaluacion-form.service';
import { type BlobType } from 'app/shared/jhipster/data-utils';
import { ProyectoGradoService } from 'app/entities/proyecto-grado/service/proyecto-grado.service';

@Component({
  selector: 'jhi-evaluacion-update',
  templateUrl: './evaluacion-update.html',
  imports: [TranslateDirective, TranslatePipe, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class EvaluacionUpdate implements OnInit {
  readonly isSaving = signal(false);
  evaluacion: IEvaluacion | null = null;
  etapaValues = Object.keys(Etapa);
  conceptoValues = Object.keys(Concepto);

  usersSharedCollection = signal<IUser[]>([]);
  proyectoGradosSharedCollection = signal<IProyectoGrado[]>([]);

  protected dataUtils = inject(DataUtils);
  protected eventManager = inject(EventManager);
  protected evaluacionService = inject(EvaluacionService);
  protected evaluacionFormService = inject(EvaluacionFormService);
  protected userService = inject(UserService);
  protected proyectoGradoService = inject(ProyectoGradoService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: EvaluacionFormGroup = this.evaluacionFormService.createEvaluacionFormGroup();

  compareUser = (o1: IUser | null, o2: IUser | null): boolean => this.userService.compareUser(o1, o2);

  compareProyectoGrado = (o1: IProyectoGrado | null, o2: IProyectoGrado | null): boolean =>
    this.proyectoGradoService.compareProyectoGrado(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ evaluacion }) => {
      this.evaluacion = evaluacion;
      if (evaluacion) {
        this.updateForm(evaluacion);
      }

      this.loadRelationshipsOptions();
    });
  }

  byteSize(base64String: string): string {
    return this.dataUtils.byteSize(base64String);
  }

  openFile(base64String: string, contentType: string | null | undefined, blobType?: BlobType): void {
    this.dataUtils.openFile(base64String, contentType, blobType);
  }

  setFileData(event: Event, field: string, isImage: boolean): void {
    this.dataUtils.loadFileToForm(event, this.editForm, field, isImage).subscribe({
      error: (err: FileLoadError) =>
        this.eventManager.broadcast(new EventWithContent<AlertErrorModel>('sgpgApp.error', { ...err, key: `error.file.${err.key}` })),
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const evaluacion = this.evaluacionFormService.getEvaluacion(this.editForm);
    if (evaluacion.id === null) {
      this.subscribeToSaveResponse(this.evaluacionService.create(evaluacion));
    } else {
      this.subscribeToSaveResponse(this.evaluacionService.update(evaluacion));
    }
  }

  protected subscribeToSaveResponse(result: Observable<IEvaluacion | null>): void {
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

  protected updateForm(evaluacion: IEvaluacion): void {
    this.evaluacion = evaluacion;
    this.evaluacionFormService.resetForm(this.editForm, evaluacion);

    this.usersSharedCollection.update(users => this.userService.addUserToCollectionIfMissing<IUser>(users, evaluacion.jurado));
    this.proyectoGradosSharedCollection.update(proyectoGrados =>
      this.proyectoGradoService.addProyectoGradoToCollectionIfMissing<IProyectoGrado>(proyectoGrados, evaluacion.proyecto),
    );
  }

  protected loadRelationshipsOptions(): void {
    this.userService
      .query()
      .pipe(map((res: HttpResponse<IUser[]>) => res.body ?? []))
      .pipe(map((users: IUser[]) => this.userService.addUserToCollectionIfMissing<IUser>(users, this.evaluacion?.jurado)))
      .subscribe((users: IUser[]) => this.usersSharedCollection.set(users));

    this.proyectoGradoService
      .query()
      .pipe(map((res: HttpResponse<IProyectoGrado[]>) => res.body ?? []))
      .pipe(
        map((proyectoGrados: IProyectoGrado[]) =>
          this.proyectoGradoService.addProyectoGradoToCollectionIfMissing<IProyectoGrado>(proyectoGrados, this.evaluacion?.proyecto),
        ),
      )
      .subscribe((proyectoGrados: IProyectoGrado[]) => this.proyectoGradosSharedCollection.set(proyectoGrados));
  }
}
