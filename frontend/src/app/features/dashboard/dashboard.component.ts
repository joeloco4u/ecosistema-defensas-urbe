import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ModalAgendamientoComponent } from '../../shared/components/modal-agendamiento/modal-agendamiento.component';
import { DefensaService } from '../../core/services/defensa.service';
import { ProyectoService } from '../../core/services/proyecto.service';
import { DashboardService } from '../../core/services/dashboard.service';

interface ProyectoPendiente {
  id: string;
  expediente?: string | null;
  tesista: string;
  estudiante?: EstudianteInfo;
  estudiante2?: EstudianteInfo;
  estudiante3?: EstudianteInfo;
  titulo: string;
  tutor?: { id?: number; nombreCompleto?: string } | null;
  tutorMetodologico?: { id?: number; nombreCompleto?: string } | null;
}

interface EstudianteInfo {
  cedula?: string;
  nombres?: string;
  apellidos?: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, ModalAgendamientoComponent],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
  modalAbierto = false;
  proyectoSeleccionado: ProyectoPendiente | null = null;

  proyectosPendientes: ProyectoPendiente[] = [];
  busqueda = '';

  stats: any = null;

  get proyectosPendientesFiltrados(): ProyectoPendiente[] {
    const termino = this.busqueda.trim().toLowerCase();
    if (!termino) return this.proyectosPendientes;
    return this.proyectosPendientes.filter((p) =>
      (p.expediente ?? '').toLowerCase().includes(termino) ||
      (p.titulo ?? '').toLowerCase().includes(termino) ||
      (p.tutor?.nombreCompleto ?? '').toLowerCase().includes(termino)
    );
  }

  constructor(
    private defensaService: DefensaService,
    private proyectoService: ProyectoService,
    private dashboardService: DashboardService,
  ) {}

  ngOnInit(): void {
    this.dashboardService.getStats().subscribe({
      next: (data) => {
        this.stats = data;
      },
      error: (err) => {
        console.error('Error al cargar las estadísticas del dashboard', err);
      },
    });

    this.proyectoService.listarProyectos().subscribe({
      next: (proyectos) => {
        this.proyectosPendientes = proyectos
          .filter((p) => p.estatus === 'PENDIENTE')
          .map((p) => ({
            id: p.id,
            expediente: p.expediente ?? null,
            tesista: [p.estudiante?.nombres, p.estudiante?.apellidos].filter(Boolean).join(' ') || 'Por asignar',
            estudiante: p.estudiante ?? null,
            estudiante2: p.estudiante2 ?? null,
            estudiante3: p.estudiante3 ?? null,
            titulo: p.titulo ?? 'Sin título',
            tutor: p.tutor ?? null,
            tutorMetodologico: p.tutorMetodologico ?? null,
          }));
      },
      error: (err) => {
        console.error('Error al cargar los proyectos reales', err);
      },
    });
  }

  abrirModalAgendamiento(proyecto: ProyectoPendiente): void {
    this.proyectoSeleccionado = proyecto;
    this.modalAbierto = true;
  }

  cerrarModal(): void {
    this.modalAbierto = false;
    this.proyectoSeleccionado = null;
  }

  onConfirmarDefensa(evento: any): void {
    const body = {
      proyectoId: this.proyectoSeleccionado!.id,
      espacioId: evento.espacioId,
      fecha: evento.fecha,
      horaInicio: evento.horaInicio.length === 5 ? evento.horaInicio + ':00' : evento.horaInicio,
      horaFin: evento.horaFin.length === 5 ? evento.horaFin + ':00' : evento.horaFin,
      tutorAcademicoId: evento.tutorAcademicoId,
      tutorMetodologicoId: evento.tutorMetodologicoId,
      juradoId: evento.juradoId,
    };

    this.defensaService.crearDefensa(body).subscribe({
      next: () => {
        alert('¡Defensa guardada exitosamente en la BD!');
        this.cerrarModal();
      },
      error: (err) => {
        console.error('Error al guardar la defensa', err);
        alert('No se pudo guardar la defensa: ' + err.message);
      },
    });
  }
}