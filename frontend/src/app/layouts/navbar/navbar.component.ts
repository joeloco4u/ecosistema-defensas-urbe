import { Component } from '@angular/core';

@Component({
  selector: 'app-navbar',
  template: `
    <header class="w-full bg-white border-b border-gray-200 px-6 py-2 flex items-center justify-between print:hidden">
      <span class="text-xs uppercase tracking-wide text-gray-500 font-semibold">Panel del Coordinador</span>
      <span class="text-sm font-medium text-gray-700">{{ currentDate }}</span>
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
