import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';

const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent),
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [AuthGuard],
  },
  {
    path: 'calendario-defensas',
    loadChildren: () => import('./features/calendario-defensas/calendario-defensas.module').then(m => m.CalendarioDefensasModule),
    canActivate: [AuthGuard],
  },
  {
    path: 'gestion-proyectos',
    loadChildren: () => import('./features/gestion-proyectos/gestion-proyectos.module').then(m => m.GestionProyectosModule),
    canActivate: [AuthGuard],
  },
  {
    path: 'contingencias',
    loadChildren: () => import('./features/contingencias/contingencias.module').then(m => m.ContingenciasModule),
    canActivate: [AuthGuard],
  },
  {
    path: 'tutores-sugeridos',
    loadChildren: () => import('./features/tutores-sugeridos/tutores-sugeridos.module').then(m => m.TutoresSugeridosModule),
    canActivate: [AuthGuard],
  },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule { }

