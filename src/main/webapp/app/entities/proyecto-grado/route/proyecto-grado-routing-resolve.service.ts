import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, Router } from '@angular/router';

import { EMPTY, Observable, catchError, of } from 'rxjs';

import { IProyectoGrado } from '../proyecto-grado.model';
import { ProyectoGradoService } from '../service/proyecto-grado.service';

const proyectoGradoResolve = (route: ActivatedRouteSnapshot): Observable<null | IProyectoGrado> => {
  const { id } = route.params;
  if (id) {
    const router = inject(Router);
    const service = inject(ProyectoGradoService);
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

export default proyectoGradoResolve;
