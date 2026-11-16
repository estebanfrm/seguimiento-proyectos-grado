import { HttpResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslatePipe } from '@ngx-translate/core';
import { Observable, finalize, map } from 'rxjs';

import { DataUtils, EventManager, EventWithContent, FileLoadError } from 'app/core/util';
import { EstadoEntrega } from 'app/entities/enumerations/estado-entrega.model';
import { Etapa } from 'app/entities/enumerations/etapa.model';
import { IProyectoGrado } from 'app/entities/proyecto-grado/proyecto-grado.model';
import { UserService } from 'app/entities/user/service/user.service';
import { IUser } from 'app/entities/user/user.model';
import { AlertError, AlertErrorModel } from 'app/shared/alert';
import { TranslateDirective } from 'app/shared/language';

import { IEntrega } from '../entrega.model';
import { EntregaService } from '../service/entrega.service';

import { EntregaFormGroup, EntregaFormService } from './entrega-form.service';
import { type BlobType } from 'app/shared/jhipster/data-utils';
import { ProyectoGradoService } from 'app/entities/proyecto-grado/service/proyecto-grado.service';

@Component({
  selector: 'jhi-entrega-update',
  templateUrl: './entrega-update.html',
  imports: [TranslateDirective, TranslatePipe, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class EntregaUpdate implements OnInit {
  readonly isSaving = signal(false);
  entrega: IEntrega | null = null;
  etapaValues = Object.keys(Etapa);
  estadoEntregaValues = Object.keys(EstadoEntrega);

  usersSharedCollection = signal<IUser[]>([]);
  proyectoGradosSharedCollection = signal<IProyectoGrado[]>([]);

  protected dataUtils = inject(DataUtils);
  protected eventManager = inject(EventManager);
  protected entregaService = inject(EntregaService);
  protected entregaFormService = inject(EntregaFormService);
  protected userService = inject(UserService);
  protected proyectoGradoService = inject(ProyectoGradoService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: EntregaFormGroup = this.entregaFormService.createEntregaFormGroup();

  compareUser = (o1: IUser | null, o2: IUser | null): boolean => this.userService.compareUser(o1, o2);

  compareProyectoGrado = (o1: IProyectoGrado | null, o2: IProyectoGrado | null): boolean =>
    this.proyectoGradoService.compareProyectoGrado(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ entrega }) => {
      this.entrega = entrega;
      if (entrega) {
        this.updateForm(entrega);
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
    const entrega = this.entregaFormService.getEntrega(this.editForm);
    if (entrega.id === null) {
      this.subscribeToSaveResponse(this.entregaService.create(entrega));
    } else {
      this.subscribeToSaveResponse(this.entregaService.update(entrega));
    }
  }

  protected subscribeToSaveResponse(result: Observable<IEntrega | null>): void {
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

  protected updateForm(entrega: IEntrega): void {
    this.entrega = entrega;
    this.entregaFormService.resetForm(this.editForm, entrega);

    this.usersSharedCollection.update(users => this.userService.addUserToCollectionIfMissing<IUser>(users, entrega.autor));
    this.proyectoGradosSharedCollection.update(proyectoGrados =>
      this.proyectoGradoService.addProyectoGradoToCollectionIfMissing<IProyectoGrado>(proyectoGrados, entrega.proyecto),
    );
  }

  protected loadRelationshipsOptions(): void {
    this.userService
      .query()
      .pipe(map((res: HttpResponse<IUser[]>) => res.body ?? []))
      .pipe(map((users: IUser[]) => this.userService.addUserToCollectionIfMissing<IUser>(users, this.entrega?.autor)))
      .subscribe((users: IUser[]) => this.usersSharedCollection.set(users));

    this.proyectoGradoService
      .query()
      .pipe(map((res: HttpResponse<IProyectoGrado[]>) => res.body ?? []))
      .pipe(
        map((proyectoGrados: IProyectoGrado[]) =>
          this.proyectoGradoService.addProyectoGradoToCollectionIfMissing<IProyectoGrado>(proyectoGrados, this.entrega?.proyecto),
        ),
      )
      .subscribe((proyectoGrados: IProyectoGrado[]) => this.proyectoGradosSharedCollection.set(proyectoGrados));
  }
}
