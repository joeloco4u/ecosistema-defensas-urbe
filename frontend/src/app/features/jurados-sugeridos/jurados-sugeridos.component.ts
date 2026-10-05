import { Component, OnInit } from '@angular/core';
import { TutorSugeridoService } from '../../core/services/tutor-sugerido.service';
import { TutorSugerido } from '../../core/models/tutor-sugerido.model';
import { ProyectoService } from '../../core/services/proyecto.service';
import { DocenteService } from '../../core/services/docente.service';

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
  selector: 'app-jurados-sugeridos',
  template: `
    <div class="bg-white p-8 space-y-8 min-h-screen">
      <div>
        <h1 class="text-2xl font-semibold text-gray-900 tracking-tight">Jurados Sugeridos</h1>
        <p class="text-sm text-gray-600 mt-1">Sugerir jurados para cada proyecto específico</p>
      </div>

      <div class="bg-white p-5 rounded-sm border border-gray-200">
        <label class="block text-xs uppercase tracking-widest text-gray-500 font-medium mb-2">
          Proyecto de Seminario III
        </label>
        <div class="relative max-w-4xl">
        <button
          type="button"
          (click)="toggleDropdown()"
          class="w-full flex items-center justify-between bg-white text-gray-900 border border-gray-300 rounded-sm px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400"
        >
          <span class="truncate overflow-hidden whitespace-nowrap text-ellipsis text-left">
            {{ tituloProyectoSeleccionado.length > 90 ? (tituloProyectoSeleccionado | slice:0:90) + '...' : tituloProyectoSeleccionado }}
          </span>
          <svg class="w-4 h-4 text-gray-500 shrink-0 ml-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M19 9l-7 7-7-7" />
          </svg>
        </button>

        <div
          *ngIf="isDropdownOpen"
          class="absolute z-50 w-full mt-1 bg-white border border-gray-300 rounded-md shadow-lg max-h-64 overflow-y-auto"
        >
          <div
            *ngFor="let p of proyectos"
            (click)="selectProyecto(p)"
            class="p-3 cursor-pointer hover:bg-gray-100 text-gray-900 border-b border-gray-200"
          >
            {{ p.titulo.length > 90 ? (p.titulo | slice:0:90) + '...' : p.titulo }}
          </div>
        </div>
      </div>
      </div>

      <div *ngIf="!proyectoSeleccionado" class="bg-white rounded-sm border border-gray-200 p-12 text-center text-sm text-gray-500">
        Seleccione un proyecto para gestionar sus tutores sugeridos de jurado.
      </div>

      <div *ngIf="proyectoSeleccionado">
        <div class="flex justify-between items-center">
          <div>
            <h2 class="text-lg font-medium text-gray-900">Sugerencias de Jurado</h2>
            <p class="text-sm text-gray-600 mt-0.5">Solicitudes pendientes de aprobación para el proyecto seleccionado</p>
          </div>
          <button
            (click)="mostrarFormulario = !mostrarFormulario"
            class="px-4 py-2 text-xs font-semibold uppercase tracking-widest shadow-sm bg-[#8a1538] text-[#fff] rounded-md transition-all duration-100 ease-in-out active:scale-[0.98] hover:brightness-110"
          >
            {{ mostrarFormulario ? 'Cancelar' : '+ Nueva Sugerencia' }}
          </button>
        </div>

        <div *ngIf="mostrarFormulario" class="bg-white p-6 rounded-sm border border-gray-200 mb-6">
          <h3 class="text-sm uppercase tracking-widest text-gray-500 font-medium mb-4">Nuevo Jurado Sugerido</h3>
          <div class="grid grid-cols-1 gap-4">
            <div>
              <label class="block text-xs uppercase tracking-widest text-gray-500 mb-1">Docente</label>
              <select
                [(ngModel)]="docenteSeleccionadoId"
                (ngModelChange)="onDocenteSeleccionado($event)"
                class="w-full bg-white text-gray-900 border border-gray-300 rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-gray-400"
              >
                <option [ngValue]="null" disabled selected>Seleccionar docente</option>
                <option *ngFor="let d of docentes" [ngValue]="d.id">{{ d.nombreCompleto }} ({{ d.codigoInstitucional }})</option>
              </select>
            </div>
          </div>
          <div class="mt-4">
            <button
              (click)="enviarSugerencia()"
              [disabled]="!docenteSeleccionado"
              class="px-4 py-2 text-xs font-semibold uppercase tracking-widest shadow-sm bg-[#8a1538] text-[#fff] rounded-md transition-all duration-100 ease-in-out active:scale-[0.98] enabled:hover:brightness-110 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Guardar Sugerencia
            </button>
          </div>
        </div>

        <div class="bg-white border border-gray-200 rounded-sm overflow-hidden">
          <table class="w-full text-sm">
            <thead>
              <tr class="border-b border-gray-200">
                <th class="text-left px-6 py-4 text-xs uppercase tracking-widest text-gray-500 font-medium">Nombre</th>
                <th class="text-left px-6 py-4 text-xs uppercase tracking-widest text-gray-500 font-medium">Cédula</th>
                <th class="text-left px-6 py-4 text-xs uppercase tracking-widest text-gray-500 font-medium">Área de Investigación</th>
                <th class="text-left px-6 py-4 text-xs uppercase tracking-widest text-gray-500 font-medium">Estado</th>
                <th class="text-left px-6 py-4 text-xs uppercase tracking-widest text-gray-500 font-medium">Acción</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-200">
              <tr *ngFor="let t of pendientes" class="bg-white hover:bg-gray-50 transition-colors text-gray-900">
                <td class="px-6 py-4 text-gray-900">{{ t.nombre }} {{ t.apellido }}</td>
                <td class="px-6 py-4 text-gray-900">{{ t.cedula }}</td>
                <td class="px-6 py-4 text-gray-900">{{ t.areaInvestigacion }}</td>
                <td class="px-6 py-4">
                  <span class="px-3 py-1 text-xs uppercase tracking-widest bg-yellow-100 text-yellow-800 border border-yellow-300 rounded-sm">
                    {{ t.estado }}
                  </span>
                </td>
                <td class="px-6 py-4 space-x-2" *ngIf="t.estado === 'PENDIENTE'">
                  <button
                    (click)="aprobar(t.id)"
                    class="px-4 py-1.5 text-xs uppercase tracking-widest border border-green-600 text-green-700 bg-transparent rounded-sm hover:bg-green-100 transition-colors"
                  >
                    Aprobar
                  </button>
                  <button
                    (click)="rechazar(t.id)"
                    class="px-4 py-1.5 text-xs uppercase tracking-widest border border-red-600 text-red-700 bg-transparent rounded-sm hover:bg-red-100 transition-colors"
                  >
                    Rechazar
                  </button>
                </td>
              </tr>
              <tr *ngIf="pendientes.length === 0">
                <td colspan="5" class="px-6 py-12 text-center text-gray-500 text-sm">
                  No hay solicitudes de jurados para este proyecto.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `,
})
export class JuradosSugeridosComponent implements OnInit {
  proyectos: ProyectoSugerencia[] = [];
  proyectoSeleccionado = '';
  isDropdownOpen = false;
  pendientes: TutorSugerido[] = [];
  mostrarFormulario = false;
  nuevoTutor: Partial<TutorSugerido> = { nombre: '', apellido: '', cedula: '', areaInvestigacion: '' };
  docentes: any[] = [];
  docenteSeleccionadoId: number | null = null;
  docenteSeleccionado: any = null;

  constructor(
    private tutorSugeridoService: TutorSugeridoService,
    private proyectoService: ProyectoService,
    private docenteService: DocenteService,
  ) {}

  ngOnInit(): void {
    this.cargarProyectos();
    this.cargarDocentes();
  }

  private cargarDocentes(): void {
    this.docenteService.getDocentes().subscribe({
      next: (data) => {
        this.docentes = data;
      },
    });
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

  get tituloProyectoSeleccionado(): string {
    const p = this.proyectos.find((x) => x.id === this.proyectoSeleccionado);
    return p ? p.titulo : 'Seleccione un proyecto';
  }

  toggleDropdown(): void {
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  selectProyecto(proyecto: ProyectoSugerencia): void {
    this.proyectoSeleccionado = proyecto.id;
    this.isDropdownOpen = false;
    this.onProyectoChange();
  }

  onProyectoChange(): void {
    this.pendientes = [];
    this.nuevoTutor = { nombre: '', apellido: '', cedula: '', areaInvestigacion: '' };
    this.docenteSeleccionadoId = null;
    this.docenteSeleccionado = null;
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

  onDocenteSeleccionado(docenteId: number | null): void {
    this.docenteSeleccionadoId = docenteId;
    this.docenteSeleccionado = this.docentes.find((d) => d.id === docenteId) ?? null;
    this.nuevoTutor = { nombre: '', apellido: '', cedula: '', areaInvestigacion: '' };
    if (this.docenteSeleccionado) {
      const partes = String(this.docenteSeleccionado.nombreCompleto ?? '').trim().split(/\s+/);
      this.nuevoTutor = {
        nombre: partes[0] ?? '',
        apellido: partes.slice(1).join(' '),
        cedula: this.docenteSeleccionado.codigoInstitucional ?? '',
        areaInvestigacion: this.docenteSeleccionado.departamento ?? '',
      };
    }
  }

  enviarSugerencia(): void {
    if (!this.proyectoSeleccionado) return;
    this.tutorSugeridoService
      .sugerirTutor({ ...this.nuevoTutor, proyectoId: this.proyectoSeleccionado })
      .subscribe({
        next: () => {
          this.mostrarFormulario = false;
          this.nuevoTutor = { nombre: '', apellido: '', cedula: '', areaInvestigacion: '' };
          this.docenteSeleccionadoId = null;
          this.docenteSeleccionado = null;
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