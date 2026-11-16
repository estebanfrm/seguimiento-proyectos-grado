import { Routes } from '@angular/router';

import { ASC } from 'app/config';
import { userRouteAccessService } from 'app/core/auth';

import HistorialEtapaResolve from './route/historial-etapa-routing-resolve.service';

const historialEtapaRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/historial-etapa').then(m => m.HistorialEtapa),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/historial-etapa-detail').then(m => m.HistorialEtapaDetail),
    resolve: {
      historialEtapa: HistorialEtapaResolve,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/historial-etapa-update').then(m => m.HistorialEtapaUpdate),
    resolve: {
      historialEtapa: HistorialEtapaResolve,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/historial-etapa-update').then(m => m.HistorialEtapaUpdate),
    resolve: {
      historialEtapa: HistorialEtapaResolve,
    },
    canActivate: [userRouteAccessService],
  },
];

export default historialEtapaRoute;
