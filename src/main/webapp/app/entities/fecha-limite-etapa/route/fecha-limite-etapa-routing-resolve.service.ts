import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, Router } from '@angular/router';

import { EMPTY, Observable, catchError, of } from 'rxjs';

import { IFechaLimiteEtapa } from '../fecha-limite-etapa.model';
import { FechaLimiteEtapaService } from '../service/fecha-limite-etapa.service';

const fechaLimiteEtapaResolve = (route: ActivatedRouteSnapshot): Observable<null | IFechaLimiteEtapa> => {
  const { id } = route.params;
  if (id) {
    const router = inject(Router);
    const service = inject(FechaLimiteEtapaService);
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

export default fechaLimiteEtapaResolve;
