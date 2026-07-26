import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-sidebar',
  template: `
    <aside class="w-64 bg-surface-dark border-r border-surface-border flex flex-col">
      <div class="p-6 border-b border-surface-border">
        <h1 class="text-lg font-bold text-accent-white">Defensas URBE</h1>
        <p class="text-xs text-accent-muted mt-1">Panel del Coordinador</p>
      </div>
      <nav class="flex-1 p-4 space-y-1">
        <a *ngFor="let item of menuItems"
           [routerLink]="item.path"
           routerLinkActive="bg-surface-light text-white border-l-4 border-white"
           class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-accent-muted hover:bg-surface-light hover:text-white transition-colors">
          <span class="text-lg">{{ item.icon }}</span>
          {{ item.label }}
        </a>
      </nav>
      <div class="p-4 border-t border-surface-border">
        <a (click)="logout()" class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-red-400 hover:bg-surface-light transition-colors cursor-pointer">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path>
          </svg>
          Cerrar Sesión
        </a>
      </div>
    </aside>
  `,
})
export class SidebarComponent {
  menuItems = [
    { label: 'Dashboard', path: '/dashboard', icon: '📊' },
    { label: 'Calendario Defensas', path: '/calendario-defensas', icon: '📅' },
    { label: 'Gestión de Proyectos', path: '/gestion-proyectos', icon: '📂' },
    { label: 'Contingencias', path: '/contingencias', icon: '⚠️' },
    { label: 'Tutores Sugeridos', path: '/tutores-sugeridos', icon: '👤' },
  ];

  constructor(
    private authService: AuthService,
    private router: Router,
  ) {}

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
