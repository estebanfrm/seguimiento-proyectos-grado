import { Component, effect, inject, signal, untracked } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Data, ParamMap, Router, RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap/modal';
import { combineLatest, filter, map, tap } from 'rxjs';

import { DEFAULT_SORT_DATA, ITEM_DELETED_EVENT, SORT } from 'app/config';
import { Alert, AlertError } from 'app/shared/alert';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';
import { SortByDirective, SortDirective, SortService, type SortState, sortStateSignal } from 'app/shared/sort';
import { FechaLimiteEtapaDeleteDialog } from '../delete/fecha-limite-etapa-delete-dialog';
import { IFechaLimiteEtapa } from '../fecha-limite-etapa.model';
import { FechaLimiteEtapaService } from '../service/fecha-limite-etapa.service';

@Component({
  selector: 'jhi-fecha-limite-etapa',
  templateUrl: './fecha-limite-etapa.html',
  imports: [RouterLink, FontAwesomeModule, AlertError, Alert, SortDirective, SortByDirective, TranslateDirective, FormatMediumDatetimePipe],
})
export class FechaLimiteEtapa {
  readonly fechaLimiteEtapas = signal<IFechaLimiteEtapa[]>([]);

  sortState = sortStateSignal({});

  readonly router = inject(Router);
  protected readonly fechaLimiteEtapaService = inject(FechaLimiteEtapaService);
  // eslint-disable-next-line @typescript-eslint/member-ordering
  readonly isLoading = this.fechaLimiteEtapaService.fechaLimiteEtapasResource.isLoading;
  protected readonly activatedRoute = inject(ActivatedRoute);
  protected readonly activatedRouteState = toSignal(
    combineLatest([this.activatedRoute.queryParamMap, this.activatedRoute.data]).pipe(
      map(([queryParamMap, data]) => ({ queryParamMap, data })),
    ),
    { initialValue: { queryParamMap: this.activatedRoute.snapshot.queryParamMap, data: this.activatedRoute.snapshot.data } },
  );
  protected readonly sortService = inject(SortService);
  protected modalService = inject(NgbModal);

  constructor() {
    effect(() => {
      this.fechaLimiteEtapas.set(this.fillComponentAttributesFromResponseBody([...this.fechaLimiteEtapaService.fechaLimiteEtapas()]));
    });
    effect(() => {
      const activatedRouteState = this.activatedRouteState();
      untracked(() => {
        // Only watch for route changes. Other signals should be ignored.
        this.fillComponentAttributeFromRoute(activatedRouteState.queryParamMap, activatedRouteState.data);
        this.load();
      });
    });
  }

  trackId = (item: IFechaLimiteEtapa): number => this.fechaLimiteEtapaService.getFechaLimiteEtapaIdentifier(item);

  delete(fechaLimiteEtapa: IFechaLimiteEtapa): void {
    const modalRef = this.modalService.open(FechaLimiteEtapaDeleteDialog, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.fechaLimiteEtapa = fechaLimiteEtapa;
    // unsubscribe not needed because closed completes on modal close
    modalRef.closed
      .pipe(
        filter(reason => reason === ITEM_DELETED_EVENT),
        tap(() => this.load()),
      )
      .subscribe();
  }

  load(): void {
    this.queryBackend();
  }

  navigateToWithComponentValues(event: SortState): void {
    this.handleNavigation(event);
  }

  protected fillComponentAttributeFromRoute(params: ParamMap, data: Data): void {
    this.sortState.set(this.sortService.parseSortParam(params.get(SORT) ?? data[DEFAULT_SORT_DATA]));
  }

  protected refineData(data: IFechaLimiteEtapa[]): IFechaLimiteEtapa[] {
    const { predicate, order } = this.sortState();
    return predicate && order ? data.sort(this.sortService.startSort({ predicate, order })) : data;
  }

  protected fillComponentAttributesFromResponseBody(data: IFechaLimiteEtapa[]): IFechaLimiteEtapa[] {
    return this.refineData(data);
  }

  protected queryBackend(): void {
    const queryObject: any = {
      eagerload: true,
      sort: this.sortService.buildSortParam(this.sortState()),
    };
    this.fechaLimiteEtapaService.fechaLimiteEtapasParams.set(queryObject);
  }

  protected handleNavigation(sortState: SortState): void {
    const queryParamsObj = {
      sort: this.sortService.buildSortParam(sortState),
    };

    this.router.navigate(['./'], {
      relativeTo: this.activatedRoute,
      queryParams: queryParamsObj,
    });
  }
}
