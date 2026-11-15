import { Routes } from '@angular/router';

import { userRouteAccessService } from 'app/core/auth';

const routes: Routes = [
  {
    path: '',
    title: 'seguimiento.title',
    canActivate: [userRouteAccessService],
    loadComponent: () => import('./mis-proyectos'),
  },
  {
    path: ':id',
    title: 'seguimiento.detailTitle',
    canActivate: [userRouteAccessService],
    loadComponent: () => import('./seguimiento-detail'),
  },
];

export default routes;
