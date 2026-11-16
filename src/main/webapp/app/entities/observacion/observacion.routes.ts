import { Routes } from '@angular/router';

import { ASC } from 'app/config';
import { userRouteAccessService } from 'app/core/auth';

import ObservacionResolve from './route/observacion-routing-resolve.service';

const observacionRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/observacion').then(m => m.Observacion),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/observacion-detail').then(m => m.ObservacionDetail),
    resolve: {
      observacion: ObservacionResolve,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/observacion-update').then(m => m.ObservacionUpdate),
    resolve: {
      observacion: ObservacionResolve,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/observacion-update').then(m => m.ObservacionUpdate),
    resolve: {
      observacion: ObservacionResolve,
    },
    canActivate: [userRouteAccessService],
  },
];

export default observacionRoute;
