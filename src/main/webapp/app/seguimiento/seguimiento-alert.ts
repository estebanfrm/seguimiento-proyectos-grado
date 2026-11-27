import { Component, input } from '@angular/core';

import { TranslatePipe } from '@ngx-translate/core';

import { IProblemaSeguimiento } from './seguimiento.model';

@Component({
  selector: 'jhi-seguimiento-alert',
  imports: [TranslatePipe],
  template: `
    @if (problema(); as error) {
      <div class="alert alert-danger" role="alert">
        @if (error.mensaje.startsWith('seguimiento.errors.')) {
          <p class="mb-0">{{ error.mensaje | translate }}</p>
        } @else {
          <p class="mb-0">{{ error.mensaje }}</p>
        }
        @if (error.pendientes.length) {
          <ul class="mb-0 mt-2">
            @for (pendiente of error.pendientes; track $index) {
              <li>{{ pendiente }}</li>
            }
          </ul>
        }
      </div>
    }
  `,
})
export class SeguimientoAlert {
  readonly problema = input<IProblemaSeguimiento | null>(null);
}
