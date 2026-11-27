import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';

import { Alert, AlertError } from 'app/shared/alert';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';
import { IHistorialEtapa } from '../historial-etapa.model';

@Component({
  selector: 'jhi-historial-etapa-detail',
  templateUrl: './historial-etapa-detail.html',
  imports: [FontAwesomeModule, Alert, AlertError, TranslateDirective, RouterLink, FormatMediumDatetimePipe],
})
export class HistorialEtapaDetail {
  readonly historialEtapa = input<IHistorialEtapa | null>(null);

  previousState(): void {
    globalThis.history.back();
  }
}
