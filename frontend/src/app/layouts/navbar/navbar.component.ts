import { Component } from '@angular/core';

@Component({
  selector: 'app-navbar',
  template: `
    <header class="bg-surface-dark border-b border-surface-border px-6 py-3 flex items-center justify-between">
      <div class="flex items-center gap-4">
        <span class="text-sm font-medium text-accent-muted">{{ currentDate }}</span>
      </div>
      <div class="flex items-center gap-4">
        <button class="relative p-2 text-accent-subtle hover:text-accent-white rounded-lg hover:bg-surface-light">
          <span class="sr-only">Notificaciones</span>
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"></path>
          </svg>
        </button>
        <div class="flex items-center gap-2">
          <div class="w-8 h-8 bg-surface-light rounded-full flex items-center justify-center text-accent-white text-sm font-medium">
            C
          </div>
          <span class="text-sm font-medium text-accent-white">Coordinador</span>
        </div>
      </div>
    </header>
  `,
})
export class NavbarComponent {
  currentDate = new Date().toLocaleDateString('es-ES', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
}
