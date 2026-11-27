import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, Router } from '@angular/router';

import { EMPTY, Observable, catchError, of } from 'rxjs';

import { IHistorialEtapa } from '../historial-etapa.model';
import { HistorialEtapaService } from '../service/historial-etapa.service';

const historialEtapaResolve = (route: ActivatedRouteSnapshot): Observable<null | IHistorialEtapa> => {
  const { id } = route.params;
  if (id) {
    const router = inject(Router);
    const service = inject(HistorialEtapaService);
    return service.find(id).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 404) {
          router.navigate(['404']);
        } else {
          router.navigate(['error']);
        }
        return EMPTY;
      }),
    );
  }

  return of(null);
};

export default historialEtapaResolve;
