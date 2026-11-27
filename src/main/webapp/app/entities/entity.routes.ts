import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'user-management',
    title: 'userManagement.home.title',
    loadChildren: () => import('./admin/user-management/user-management.routes'),
  },
  {
    path: 'authority',
    title: 'sgpgApp.adminAuthority.home.title',
    loadChildren: () => import('./admin/authority/authority.routes'),
  },
  {
    path: 'proyecto-grado',
    title: 'sgpgApp.proyectoGrado.home.title',
    loadChildren: () => import('./proyecto-grado/proyecto-grado.routes'),
  },
  {
    path: 'fecha-limite-etapa',
    title: 'sgpgApp.fechaLimiteEtapa.home.title',
    loadChildren: () => import('./fecha-limite-etapa/fecha-limite-etapa.routes'),
  },
  {
    path: 'entrega',
    title: 'sgpgApp.entrega.home.title',
    loadChildren: () => import('./entrega/entrega.routes'),
  },
  {
    path: 'observacion',
    title: 'sgpgApp.observacion.home.title',
    loadChildren: () => import('./observacion/observacion.routes'),
  },
  {
    path: 'evaluacion',
    title: 'sgpgApp.evaluacion.home.title',
    loadChildren: () => import('./evaluacion/evaluacion.routes'),
  },
  {
    path: 'historial-etapa',
    title: 'sgpgApp.historialEtapa.home.title',
    loadChildren: () => import('./historial-etapa/historial-etapa.routes'),
  },
  // jhipster-needle-add-entity-route - JHipster will add entity modules routes here
];

export default routes;
