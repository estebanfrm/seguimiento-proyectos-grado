import { HttpResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslatePipe } from '@ngx-translate/core';
import { Observable, finalize, map } from 'rxjs';

import { DataUtils, EventManager, EventWithContent, FileLoadError } from 'app/core/util';
import { IEntrega } from 'app/entities/entrega/entrega.model';
import { EntregaService } from 'app/entities/entrega/service/entrega.service';
import { TipoObservacion } from 'app/entities/enumerations/tipo-observacion.model';
import { UserService } from 'app/entities/user/service/user.service';
import { IUser } from 'app/entities/user/user.model';
import { AlertError, AlertErrorModel } from 'app/shared/alert';
import { type BlobType } from 'app/shared/jhipster/data-utils';
import { TranslateDirective } from 'app/shared/language';
import { IObservacion } from '../observacion.model';
import { ObservacionService } from '../service/observacion.service';

import { ObservacionFormGroup, ObservacionFormService } from './observacion-form.service';

@Component({
  selector: 'jhi-observacion-update',
  templateUrl: './observacion-update.html',
  imports: [TranslateDirective, TranslatePipe, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class ObservacionUpdate implements OnInit {
  readonly isSaving = signal(false);
  observacion: IObservacion | null = null;
  tipoObservacionValues = Object.keys(TipoObservacion);

  usersSharedCollection = signal<IUser[]>([]);
  entregasSharedCollection = signal<IEntrega[]>([]);

  protected dataUtils = inject(DataUtils);
  protected eventManager = inject(EventManager);
  protected observacionService = inject(ObservacionService);
  protected observacionFormService = inject(ObservacionFormService);
  protected userService = inject(UserService);
  protected entregaService = inject(EntregaService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: ObservacionFormGroup = this.observacionFormService.createObservacionFormGroup();

  compareUser = (o1: IUser | null, o2: IUser | null): boolean => this.userService.compareUser(o1, o2);

  compareEntrega = (o1: IEntrega | null, o2: IEntrega | null): boolean => this.entregaService.compareEntrega(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ observacion }) => {
      this.observacion = observacion;
      if (observacion) {
        this.updateForm(observacion);
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
    const observacion = this.observacionFormService.getObservacion(this.editForm);
    if (observacion.id === null) {
      this.subscribeToSaveResponse(this.observacionService.create(observacion));
    } else {
      this.subscribeToSaveResponse(this.observacionService.update(observacion));
    }
  }

  protected subscribeToSaveResponse(result: Observable<IObservacion | null>): void {
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

  protected updateForm(observacion: IObservacion): void {
    this.observacion = observacion;
    this.observacionFormService.resetForm(this.editForm, observacion);

    this.usersSharedCollection.update(users => this.userService.addUserToCollectionIfMissing<IUser>(users, observacion.autor));
    this.entregasSharedCollection.update(entregas =>
      this.entregaService.addEntregaToCollectionIfMissing<IEntrega>(entregas, observacion.entrega),
    );
  }

  protected loadRelationshipsOptions(): void {
    this.userService
      .query()
      .pipe(map((res: HttpResponse<IUser[]>) => res.body ?? []))
      .pipe(map((users: IUser[]) => this.userService.addUserToCollectionIfMissing<IUser>(users, this.observacion?.autor)))
      .subscribe((users: IUser[]) => this.usersSharedCollection.set(users));

    this.entregaService
      .query()
      .pipe(map((res: HttpResponse<IEntrega[]>) => res.body ?? []))
      .pipe(
        map((entregas: IEntrega[]) => this.entregaService.addEntregaToCollectionIfMissing<IEntrega>(entregas, this.observacion?.entrega)),
      )
      .subscribe((entregas: IEntrega[]) => this.entregasSharedCollection.set(entregas));
  }
}
