import { Routes } from '@angular/router';

import { ASC } from 'app/config';
import { userRouteAccessService } from 'app/core/auth';

import EvaluacionResolve from './route/evaluacion-routing-resolve.service';

const evaluacionRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/evaluacion').then(m => m.Evaluacion),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/evaluacion-detail').then(m => m.EvaluacionDetail),
    resolve: {
      evaluacion: EvaluacionResolve,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/evaluacion-update').then(m => m.EvaluacionUpdate),
    resolve: {
      evaluacion: EvaluacionResolve,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/evaluacion-update').then(m => m.EvaluacionUpdate),
    resolve: {
      evaluacion: EvaluacionResolve,
    },
    canActivate: [userRouteAccessService],
  },
];

export default evaluacionRoute;
