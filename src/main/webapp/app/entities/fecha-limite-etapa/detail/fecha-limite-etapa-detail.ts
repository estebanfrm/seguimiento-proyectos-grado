import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';

import { Alert, AlertError } from 'app/shared/alert';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';
import { IFechaLimiteEtapa } from '../fecha-limite-etapa.model';

@Component({
  selector: 'jhi-fecha-limite-etapa-detail',
  templateUrl: './fecha-limite-etapa-detail.html',
  imports: [FontAwesomeModule, Alert, AlertError, TranslateDirective, RouterLink, FormatMediumDatetimePipe],
})
export class FechaLimiteEtapaDetail {
  readonly fechaLimiteEtapa = input<IFechaLimiteEtapa | null>(null);

  previousState(): void {
    globalThis.history.back();
  }
}
