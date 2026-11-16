import { HttpResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslatePipe } from '@ngx-translate/core';
import { Observable, finalize, map } from 'rxjs';

import { DataUtils, EventManager, EventWithContent, FileLoadError } from 'app/core/util';
import { EstadoProyecto } from 'app/entities/enumerations/estado-proyecto.model';
import { Etapa } from 'app/entities/enumerations/etapa.model';
import { UserService } from 'app/entities/user/service/user.service';
import { IUser } from 'app/entities/user/user.model';
import { AlertError, AlertErrorModel } from 'app/shared/alert';
import { type BlobType } from 'app/shared/jhipster/data-utils';
import { TranslateDirective } from 'app/shared/language';

import { IProyectoGrado } from '../proyecto-grado.model';
import { ProyectoGradoService } from '../service/proyecto-grado.service';

import { ProyectoGradoFormGroup, ProyectoGradoFormService } from './proyecto-grado-form.service';

@Component({
  selector: 'jhi-proyecto-grado-update',
  templateUrl: './proyecto-grado-update.html',
  imports: [TranslateDirective, TranslatePipe, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class ProyectoGradoUpdate implements OnInit {
  readonly isSaving = signal(false);
  proyectoGrado: IProyectoGrado | null = null;
  etapaValues = Object.keys(Etapa);
  estadoProyectoValues = Object.keys(EstadoProyecto);

  usersSharedCollection = signal<IUser[]>([]);

  protected dataUtils = inject(DataUtils);
  protected eventManager = inject(EventManager);
  protected proyectoGradoService = inject(ProyectoGradoService);
  protected proyectoGradoFormService = inject(ProyectoGradoFormService);
  protected userService = inject(UserService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: ProyectoGradoFormGroup = this.proyectoGradoFormService.createProyectoGradoFormGroup();

  compareUser = (o1: IUser | null, o2: IUser | null): boolean => this.userService.compareUser(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ proyectoGrado }) => {
      this.proyectoGrado = proyectoGrado;
      if (proyectoGrado) {
        this.updateForm(proyectoGrado);
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
    const proyectoGrado = this.proyectoGradoFormService.getProyectoGrado(this.editForm);
    if (proyectoGrado.id === null) {
      this.subscribeToSaveResponse(this.proyectoGradoService.create(proyectoGrado));
    } else {
      this.subscribeToSaveResponse(this.proyectoGradoService.update(proyectoGrado));
    }
  }

  protected subscribeToSaveResponse(result: Observable<IProyectoGrado | null>): void {
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

  protected updateForm(proyectoGrado: IProyectoGrado): void {
    this.proyectoGrado = proyectoGrado;
    this.proyectoGradoFormService.resetForm(this.editForm, proyectoGrado);

    this.usersSharedCollection.update(users =>
      this.userService.addUserToCollectionIfMissing<IUser>(
        users,
        proyectoGrado.tutor,
        ...(proyectoGrado.estudiantes ?? []),
        ...(proyectoGrado.jurados ?? []),
      ),
    );
  }

  protected loadRelationshipsOptions(): void {
    this.userService
      .query()
      .pipe(map((res: HttpResponse<IUser[]>) => res.body ?? []))
      .pipe(
        map((users: IUser[]) =>
          this.userService.addUserToCollectionIfMissing<IUser>(
            users,
            this.proyectoGrado?.tutor,
            ...(this.proyectoGrado?.estudiantes ?? []),
            ...(this.proyectoGrado?.jurados ?? []),
          ),
        ),
      )
      .subscribe((users: IUser[]) => this.usersSharedCollection.set(users));
  }
}
