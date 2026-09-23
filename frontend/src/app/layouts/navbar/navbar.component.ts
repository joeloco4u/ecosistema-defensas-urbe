import { Component } from '@angular/core';

@Component({
  selector: 'app-navbar',
  template: `
    <header class="bg-surface-dark border-b border-surface-border px-6 py-3 flex items-center justify-between">
      <div class="flex items-center gap-4">
        <span class="text-sm font-medium text-accent-muted">{{ currentDate }}</span>
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
