import { Component } from '@angular/core';

@Component({
  selector: 'app-contingencias',
  template: `
    <div class="bg-background-dark min-h-screen p-8 space-y-6">
      <h2 class="text-2xl font-bold text-accent-white">Contingencias</h2>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div class="bg-surface-dark rounded-xl p-6 border border-surface-border">
          <h3 class="text-lg font-semibold text-accent-white mb-4">Incidencias Activas</h3>
          <div class="space-y-3">
            <div class="flex items-center justify-between p-3 bg-red-950/30 rounded-lg border border-red-900/50">
              <div>
                <p class="text-sm font-medium text-red-400">Cancelación de espacio</p>
                <p class="text-xs text-red-500 mt-1">Aula 201 fuera de servicio</p>
              </div>
              <span class="text-xs bg-red-900/50 text-red-400 px-2 py-1 rounded-full">Crítica</span>
            </div>
            <div class="flex items-center justify-between p-3 bg-yellow-950/30 rounded-lg border border-yellow-900/50">
              <div>
                <p class="text-sm font-medium text-yellow-400">Docente no disponible</p>
                <p class="text-xs text-yellow-500 mt-1">Dr. García solicitó permiso</p>
              </div>
              <span class="text-xs bg-yellow-900/50 text-yellow-400 px-2 py-1 rounded-full">Media</span>
            </div>
          </div>
        </div>

        <div class="bg-surface-dark rounded-xl p-6 border border-surface-border">
          <h3 class="text-lg font-semibold text-accent-white mb-4">Historial de Reprogramaciones</h3>
          <p class="text-accent-muted text-sm">No hay reprogramaciones recientes.</p>
        </div>
      </div>
    </div>
  `,
})
export class ContingenciasComponent {}
