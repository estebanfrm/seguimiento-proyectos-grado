import { HttpErrorResponse } from '@angular/common/http';

import { IProblemaSeguimiento, IRequisitoTransicion } from './seguimiento.model';

export function problemaSeguimiento(error: HttpErrorResponse): IProblemaSeguimiento {
  if (error.status === 403) {
    return { mensaje: 'seguimiento.errors.forbidden', pendientes: [] };
  }
  const body = error.error as { detail?: string; pendientes?: (string | IRequisitoTransicion)[] } | null;
  return {
    mensaje: body?.detail ?? 'seguimiento.errors.generic',
    pendientes: (body?.pendientes ?? []).map(item => (typeof item === 'string' ? item : item.descripcion)),
  };
}
