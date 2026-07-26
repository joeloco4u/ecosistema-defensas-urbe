import { Component } from '@angular/core';

@Component({
  selector: 'app-gestion-proyectos',
  template: `
    <div class="bg-background-dark min-h-screen p-8 space-y-6">
      <div class="flex items-center justify-between">
        <h2 class="text-2xl font-bold text-accent-white">Gestión de Proyectos</h2>
        <app-button variant="primary" (onClick)="mostrarModal = true">
          Nuevo Proyecto
        </app-button>
      </div>

      <div class="bg-surface-dark rounded-xl border border-surface-border overflow-hidden">
        <table class="w-full text-sm">
          <thead>
            <tr class="bg-surface-light border-b border-surface-border">
              <th class="text-left px-6 py-3 font-medium text-accent-muted">Título</th>
              <th class="text-left px-6 py-3 font-medium text-accent-muted">Estudiante</th>
              <th class="text-left px-6 py-3 font-medium text-accent-muted">Estado</th>
              <th class="text-left px-6 py-3 font-medium text-accent-muted">Acciones</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-surface-border">
            <tr class="hover:bg-surface-light">
              <td class="px-6 py-4 text-accent-white">Sistema de Gestión Académica</td>
              <td class="px-6 py-4 text-accent-muted">Juan Pérez</td>
              <td class="px-6 py-4">
                <span class="px-2 py-1 bg-yellow-900/30 text-yellow-400 rounded-full text-xs font-medium">En revisión</span>
              </td>
              <td class="px-6 py-4">
                <button class="text-blue-400 hover:text-blue-300 text-sm font-medium">Ver</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <app-modal [visible]="mostrarModal" title="Nuevo Proyecto" (onClose)="mostrarModal = false">
        <p class="text-accent-muted text-sm">Formulario de creación de proyecto.</p>
      </app-modal>
    </div>
  `,
})
export class GestionProyectosComponent {
  mostrarModal = false;
}
