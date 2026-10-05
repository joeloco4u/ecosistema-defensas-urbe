import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-sidebar',
  template: `
    <nav class="w-full h-16 bg-white border-t-4 border-t-[#8A1538] border-b border-b-gray-200 shadow-sm flex items-center justify-between px-6 sticky top-0 z-50 print:hidden">
      <div class="flex items-center gap-3 shrink-0 min-w-0">
        <img class="h-9 w-auto" src="https://cdn.urbe.edu/portal-urbe/images/logos/urbe-hd.png" alt="Logo URBE" />
        <div class="hidden lg:block border-l border-l-gray-200 pl-3 leading-tight min-w-0">
          <p class="text-sm font-semibold text-gray-800 tracking-tight whitespace-nowrap">Ecosistema Digital de Defensas</p>
          <p class="text-xs text-gray-600 whitespace-nowrap">Universidad Privada Dr. Rafael Belloso Chacín</p>
        </div>
      </div>

      <div class="flex-1 min-w-0 overflow-x-auto no-scrollbar scroll-smooth overscroll-x-contain">
        <ul class="flex w-max items-center gap-2 sm:gap-3 lg:gap-5 mx-auto px-2">
          <li *ngFor="let item of menuItems" class="shrink-0">
            <a
              [routerLink]="item.path"
              routerLinkActive="text-[#8A1538] font-semibold bg-red-50"
              class="block px-3 py-2 rounded-md text-sm font-medium text-gray-600 hover:text-[#002b5c] hover:bg-gray-100 transition-colors whitespace-nowrap"
            >
              {{ item.label }}
            </a>
          </li>
        </ul>
      </div>

      <div class="flex items-center gap-2 shrink-0">
        <a (click)="logout()" class="inline-flex items-center gap-2 px-3 py-2 rounded-md text-sm font-bold text-[#8A1538] hover:bg-red-50 transition-colors cursor-pointer">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path>
          </svg>
          <span class="hidden md:inline">Cerrar Sesión</span>
        </a>
      </div>
    </nav>
  `,
})
export class SidebarComponent {
  menuItems = [
    { label: 'Dashboard', path: '/dashboard' },
    { label: 'Calendario Defensas', path: '/calendario-defensas' },
    { label: 'Gestión de Proyectos', path: '/gestion-proyectos' },
    { label: 'Jurados Sugeridos', path: '/jurados-sugeridos' },
    { label: 'Directorio Docentes', path: '/directorio-docentes' },
    { label: 'Reportes', path: '/reportes' },
    { label: 'Documentos', path: '/documentos' },
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
