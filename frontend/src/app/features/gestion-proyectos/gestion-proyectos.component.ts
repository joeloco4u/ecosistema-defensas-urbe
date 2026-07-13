import { Component } from '@angular/core';

@Component({
  selector: 'app-gestion-proyectos',
  template: `
    <div class="space-y-6">
      <div class="flex items-center justify-between">
        <h2 class="text-2xl font-bold text-gray-800">Gestión de Proyectos</h2>
        <app-button variant="primary" (onClick)="mostrarModal = true">
          Nuevo Proyecto
        </app-button>
      </div>

      <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <table class="w-full text-sm">
          <thead>
            <tr class="bg-gray-50 border-b border-gray-200">
              <th class="text-left px-6 py-3 font-medium text-gray-600">Título</th>
              <th class="text-left px-6 py-3 font-medium text-gray-600">Estudiante</th>
              <th class="text-left px-6 py-3 font-medium text-gray-600">Estado</th>
              <th class="text-left px-6 py-3 font-medium text-gray-600">Acciones</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
            <tr class="hover:bg-gray-50">
              <td class="px-6 py-4 text-gray-800">Sistema de Gestión Académica</td>
              <td class="px-6 py-4 text-gray-600">Juan Pérez</td>
              <td class="px-6 py-4">
                <span class="px-2 py-1 bg-yellow-100 text-yellow-800 rounded-full text-xs font-medium">En revisión</span>
              </td>
              <td class="px-6 py-4">
                <button class="text-primary-600 hover:text-primary-800 text-sm font-medium">Ver</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <app-modal [visible]="mostrarModal" title="Nuevo Proyecto" (onClose)="mostrarModal = false">
        <p class="text-gray-500 text-sm">Formulario de creación de proyecto.</p>
      </app-modal>
    </div>
  `,
})
export class GestionProyectosComponent {
  mostrarModal = false;
}
