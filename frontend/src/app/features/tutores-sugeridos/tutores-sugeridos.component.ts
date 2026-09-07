import { Component, OnInit } from '@angular/core';
import { TutorSugeridoService } from '../../core/services/tutor-sugerido.service';
import { TutorSugerido } from '../../core/models/tutor-sugerido.model';
import { ProyectoService } from '../../core/services/proyecto.service';

interface ProyectoSugerencia {
  id: string;
  titulo: string;
  tesista: string;
}

interface ProyectoPendiente {
  id: string;
  titulo?: string;
  estatus?: string;
  estudiante?: { nombres?: string; apellidos?: string };
}

@Component({
  selector: 'app-tutores-sugeridos',
  template: `
    <div class="bg-background-dark p-8 space-y-8 min-h-screen">
      <div>
        <h1 class="text-2xl font-semibold text-accent-white tracking-tight">Tutores Sugeridos</h1>
        <p class="text-sm text-accent-muted mt-1">Sugerir jurados para cada proyecto específico</p>
      </div>

      <div class="bg-surface-dark p-5 rounded-sm border border-surface-border">
        <label class="block text-xs uppercase tracking-widest text-accent-muted font-medium mb-2">
          Proyecto de Seminario III
        </label>
        <select
          [(ngModel)]="proyectoSeleccionado"
          (ngModelChange)="onProyectoChange()"
          class="w-full bg-background-dark text-accent-white border border-surface-border rounded-sm px-3 py-2.5 text-sm focus:outline-none focus:border-accent-muted"
        >
          <option value="" disabled>Seleccione un proyecto</option>
          <option *ngFor="let p of proyectos" [value]="p.id">
            {{ p.tesista }} — {{ p.titulo }}
          </option>
        </select>
      </div>

      <div *ngIf="!proyectoSeleccionado" class="bg-surface-dark rounded-sm border border-surface-border p-12 text-center text-sm text-accent-subtle">
        Seleccione un proyecto para gestionar sus tutores sugeridos de jurado.
      </div>

      <div *ngIf="proyectoSeleccionado">
        <div class="flex justify-between items-center">
          <div>
            <h2 class="text-lg font-medium text-accent-white">Sugerencias de Jurado</h2>
            <p class="text-sm text-accent-muted mt-0.5">Solicitudes pendientes de aprobación para el proyecto seleccionado</p>
          </div>
          <button
            (click)="mostrarFormulario = !mostrarFormulario"
            class="px-4 py-2 text-xs uppercase tracking-widest border border-accent-muted text-accent-white bg-transparent rounded-sm hover:bg-accent-white hover:text-background-dark transition-colors"
          >
            {{ mostrarFormulario ? 'Cancelar' : '+ Nueva Sugerencia' }}
          </button>
        </div>

        <div *ngIf="mostrarFormulario" class="bg-surface-dark p-6 rounded-sm border border-surface-border mb-6">
          <h3 class="text-sm uppercase tracking-widest text-accent-muted font-medium mb-4">Nuevo Jurado Sugerido</h3>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-xs uppercase tracking-widest text-accent-muted mb-1">Nombre</label>
              <input
                type="text"
                [(ngModel)]="nuevoTutor.nombre"
                class="w-full bg-background-dark text-accent-white border border-surface-border rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-accent-muted"
                placeholder="Nombre"
              />
            </div>
            <div>
              <label class="block text-xs uppercase tracking-widest text-accent-muted mb-1">Apellido</label>
              <input
                type="text"
                [(ngModel)]="nuevoTutor.apellido"
                class="w-full bg-background-dark text-accent-white border border-surface-border rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-accent-muted"
                placeholder="Apellido"
              />
            </div>
            <div>
              <label class="block text-xs uppercase tracking-widest text-accent-muted mb-1">Cédula</label>
              <input
                type="text"
                [(ngModel)]="nuevoTutor.cedula"
                class="w-full bg-background-dark text-accent-white border border-surface-border rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-accent-muted"
                placeholder="Cédula"
              />
            </div>
            <div>
              <label class="block text-xs uppercase tracking-widest text-accent-muted mb-1">Área de Investigación</label>
              <input
                type="text"
                [(ngModel)]="nuevoTutor.areaInvestigacion"
                class="w-full bg-background-dark text-accent-white border border-surface-border rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-accent-muted"
                placeholder="Área de Investigación"
              />
            </div>
          </div>
          <div class="mt-4">
            <button
              (click)="enviarSugerencia()"
              class="px-6 py-2 text-xs uppercase tracking-widest border border-accent-muted text-accent-white bg-transparent rounded-sm hover:bg-accent-white hover:text-background-dark transition-colors"
            >
              Guardar Sugerencia
            </button>
          </div>
        </div>

        <div class="bg-surface-dark border border-surface-border rounded-sm overflow-hidden">
          <table class="w-full text-sm">
            <thead>
              <tr class="border-b border-surface-border">
                <th class="text-left px-6 py-4 text-xs uppercase tracking-widest text-accent-muted font-medium">Nombre</th>
                <th class="text-left px-6 py-4 text-xs uppercase tracking-widest text-accent-muted font-medium">Cédula</th>
                <th class="text-left px-6 py-4 text-xs uppercase tracking-widest text-accent-muted font-medium">Área de Investigación</th>
                <th class="text-left px-6 py-4 text-xs uppercase tracking-widest text-accent-muted font-medium">Estado</th>
                <th class="text-left px-6 py-4 text-xs uppercase tracking-widest text-accent-muted font-medium">Acción</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-surface-border">
              <tr *ngFor="let t of pendientes" class="hover:bg-surface-light transition-colors">
                <td class="px-6 py-4 text-accent-white">{{ t.nombre }} {{ t.apellido }}</td>
                <td class="px-6 py-4 text-accent-muted">{{ t.cedula }}</td>
                <td class="px-6 py-4 text-accent-muted">{{ t.areaInvestigacion }}</td>
                <td class="px-6 py-4">
                  <span class="px-3 py-1 text-xs uppercase tracking-widest bg-yellow-900/50 text-yellow-400 border border-yellow-700 rounded-sm">
                    {{ t.estado }}
                  </span>
                </td>
                <td class="px-6 py-4 space-x-2">
                  <button
                    (click)="aprobar(t.id)"
                    class="px-4 py-1.5 text-xs uppercase tracking-widest border border-green-700 text-green-400 bg-transparent rounded-sm hover:bg-green-900/30 transition-colors"
                  >
                    Aprobar
                  </button>
                  <button
                    (click)="rechazar(t.id)"
                    class="px-4 py-1.5 text-xs uppercase tracking-widest border border-red-700 text-red-400 bg-transparent rounded-sm hover:bg-red-900/30 transition-colors"
                  >
                    Rechazar
                  </button>
                </td>
              </tr>
              <tr *ngIf="pendientes.length === 0">
                <td colspan="5" class="px-6 py-12 text-center text-accent-subtle text-sm">
                  No hay solicitudes de tutores de jurado para este proyecto.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `,
})
export class TutoresSugeridosComponent implements OnInit {
  proyectos: ProyectoSugerencia[] = [];
  proyectoSeleccionado = '';
  pendientes: TutorSugerido[] = [];
  mostrarFormulario = false;
  nuevoTutor: Partial<TutorSugerido> = { nombre: '', apellido: '', cedula: '', areaInvestigacion: '' };

  constructor(
    private tutorSugeridoService: TutorSugeridoService,
    private proyectoService: ProyectoService,
  ) {}

  ngOnInit(): void {
    this.cargarProyectos();
  }

  private cargarProyectos(): void {
    this.proyectoService.listarProyectos().subscribe({
      next: (proyectos) => {
        const pendientes = (proyectos as ProyectoPendiente[])
          .filter((p) => p.estatus === 'PENDIENTE')
          .map((p) => ({
            id: p.id,
            titulo: p.titulo ?? 'Sin título',
            tesista: [p.estudiante?.nombres, p.estudiante?.apellidos].filter(Boolean).join(' ') || 'Por asignar',
          }));
        this.proyectos = pendientes;
        if (pendientes.length > 0) {
          this.proyectoSeleccionado = pendientes[0].id;
          this.cargarPendientes();
        }
      },
    });
  }

  onProyectoChange(): void {
    this.pendientes = [];
    this.nuevoTutor = { nombre: '', apellido: '', cedula: '', areaInvestigacion: '' };
    this.mostrarFormulario = false;
    if (this.proyectoSeleccionado) {
      this.cargarPendientes();
    }
  }

  private cargarPendientes(): void {
    if (!this.proyectoSeleccionado) return;
    this.tutorSugeridoService.obtenerPorProyecto(this.proyectoSeleccionado).subscribe({
      next: (data) => {
        this.pendientes = data;
      },
    });
  }

  enviarSugerencia(): void {
    if (!this.proyectoSeleccionado) return;
    this.tutorSugeridoService
      .sugerirTutor({ ...this.nuevoTutor, proyectoId: this.proyectoSeleccionado })
      .subscribe({
        next: () => {
          this.mostrarFormulario = false;
          this.nuevoTutor = { nombre: '', apellido: '', cedula: '', areaInvestigacion: '' };
          this.cargarPendientes();
        },
      });
  }

  aprobar(id: string): void {
    this.tutorSugeridoService.actualizarEstado(id, 'APROBADO').subscribe({
      next: () => this.cargarPendientes(),
    });
  }

  rechazar(id: string): void {
    this.tutorSugeridoService.actualizarEstado(id, 'RECHAZADO').subscribe({
      next: () => this.cargarPendientes(),
    });
  }
}