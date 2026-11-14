import { Routes } from '@angular/router';

import { ASC } from 'app/config';
import { userRouteAccessService } from 'app/core/auth';

import ProyectoGradoResolve from './route/proyecto-grado-routing-resolve.service';

const proyectoGradoRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/proyecto-grado').then(m => m.ProyectoGrado),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/proyecto-grado-detail').then(m => m.ProyectoGradoDetail),
    resolve: {
      proyectoGrado: ProyectoGradoResolve,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/proyecto-grado-update').then(m => m.ProyectoGradoUpdate),
    resolve: {
      proyectoGrado: ProyectoGradoResolve,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/proyecto-grado-update').then(m => m.ProyectoGradoUpdate),
    resolve: {
      proyectoGrado: ProyectoGradoResolve,
    },
    canActivate: [userRouteAccessService],
  },
];

export default proyectoGradoRoute;
