import { Component } from '@angular/core';

@Component({
  selector: 'app-contingencias',
  template: `
    <div class="space-y-6">
      <h2 class="text-2xl font-bold text-gray-800">Contingencias</h2>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div class="bg-white rounded-xl shadow-sm p-6 border border-gray-100">
          <h3 class="text-lg font-semibold text-gray-800 mb-4">Incidencias Activas</h3>
          <div class="space-y-3">
            <div class="flex items-center justify-between p-3 bg-red-50 rounded-lg border border-red-200">
              <div>
                <p class="text-sm font-medium text-red-800">Cancelación de espacio</p>
                <p class="text-xs text-red-600 mt-1">Aula 201 fuera de servicio</p>
              </div>
              <span class="text-xs bg-red-200 text-red-800 px-2 py-1 rounded-full">Crítica</span>
            </div>
            <div class="flex items-center justify-between p-3 bg-yellow-50 rounded-lg border border-yellow-200">
              <div>
                <p class="text-sm font-medium text-yellow-800">Docente no disponible</p>
                <p class="text-xs text-yellow-600 mt-1">Dr. García solicitó permiso</p>
              </div>
              <span class="text-xs bg-yellow-200 text-yellow-800 px-2 py-1 rounded-full">Media</span>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-xl shadow-sm p-6 border border-gray-100">
          <h3 class="text-lg font-semibold text-gray-800 mb-4">Historial de Reprogramaciones</h3>
          <p class="text-gray-500 text-sm">No hay reprogramaciones recientes.</p>
        </div>
      </div>
    </div>
  `,
})
export class ContingenciasComponent {}
