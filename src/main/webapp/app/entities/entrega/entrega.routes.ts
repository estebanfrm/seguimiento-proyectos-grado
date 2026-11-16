import { Routes } from '@angular/router';

import { ASC } from 'app/config';
import { userRouteAccessService } from 'app/core/auth';

import EntregaResolve from './route/entrega-routing-resolve.service';

const entregaRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/entrega').then(m => m.Entrega),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/entrega-detail').then(m => m.EntregaDetail),
    resolve: {
      entrega: EntregaResolve,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/entrega-update').then(m => m.EntregaUpdate),
    resolve: {
      entrega: EntregaResolve,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/entrega-update').then(m => m.EntregaUpdate),
    resolve: {
      entrega: EntregaResolve,
    },
    canActivate: [userRouteAccessService],
  },
];

export default entregaRoute;
