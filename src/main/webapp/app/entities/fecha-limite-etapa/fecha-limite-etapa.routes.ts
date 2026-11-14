import { Routes } from '@angular/router';

import { ASC } from 'app/config';
import { userRouteAccessService } from 'app/core/auth';

import FechaLimiteEtapaResolve from './route/fecha-limite-etapa-routing-resolve.service';

const fechaLimiteEtapaRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/fecha-limite-etapa').then(m => m.FechaLimiteEtapa),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/fecha-limite-etapa-detail').then(m => m.FechaLimiteEtapaDetail),
    resolve: {
      fechaLimiteEtapa: FechaLimiteEtapaResolve,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/fecha-limite-etapa-update').then(m => m.FechaLimiteEtapaUpdate),
    resolve: {
      fechaLimiteEtapa: FechaLimiteEtapaResolve,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/fecha-limite-etapa-update').then(m => m.FechaLimiteEtapaUpdate),
    resolve: {
      fechaLimiteEtapa: FechaLimiteEtapaResolve,
    },
    canActivate: [userRouteAccessService],
  },
];

export default fechaLimiteEtapaRoute;
