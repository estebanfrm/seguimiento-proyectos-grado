import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, Router } from '@angular/router';

import { EMPTY, Observable, catchError, of } from 'rxjs';

import { IEvaluacion } from '../evaluacion.model';
import { EvaluacionService } from '../service/evaluacion.service';

const evaluacionResolve = (route: ActivatedRouteSnapshot): Observable<null | IEvaluacion> => {
  const { id } = route.params;
  if (id) {
    const router = inject(Router);
    const service = inject(EvaluacionService);
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

export default evaluacionResolve;
