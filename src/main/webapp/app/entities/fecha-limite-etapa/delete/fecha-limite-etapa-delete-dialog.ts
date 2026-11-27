import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap/modal';

import { ITEM_DELETED_EVENT } from 'app/config';
import { AlertError } from 'app/shared/alert';
import { TranslateDirective } from 'app/shared/language';
import { IFechaLimiteEtapa } from '../fecha-limite-etapa.model';
import { FechaLimiteEtapaService } from '../service/fecha-limite-etapa.service';

@Component({
  templateUrl: './fecha-limite-etapa-delete-dialog.html',
  imports: [TranslateDirective, FormsModule, FontAwesomeModule, AlertError],
})
export class FechaLimiteEtapaDeleteDialog {
  fechaLimiteEtapa?: IFechaLimiteEtapa;

  protected readonly fechaLimiteEtapaService = inject(FechaLimiteEtapaService);
  protected readonly activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.fechaLimiteEtapaService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
