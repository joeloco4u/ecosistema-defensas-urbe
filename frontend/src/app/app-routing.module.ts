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
    path: 'jurados-sugeridos',
    loadChildren: () => import('./features/jurados-sugeridos/jurados-sugeridos.module').then(m => m.JuradosSugeridosModule),
    canActivate: [AuthGuard],
  },
  {
    path: 'directorio-docentes',
    loadChildren: () => import('./features/directorio-docentes/directorio-docentes.module').then(m => m.DirectorioDocentesModule),
    canActivate: [AuthGuard],
  },
  {
    path: 'reportes',
    loadChildren: () => import('./features/reportes/reportes.module').then(m => m.ReportesModule),
    canActivate: [AuthGuard],
  },
  {
    path: 'documentos',
    loadChildren: () => import('./features/documentos/documentos.module').then(m => m.DocumentosModule),
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

