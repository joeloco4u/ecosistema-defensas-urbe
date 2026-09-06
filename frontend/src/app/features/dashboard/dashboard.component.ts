import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalAgendamientoComponent } from '../../shared/components/modal-agendamiento/modal-agendamiento.component';
import { DefensaService } from '../../core/services/defensa.service';
import { ProyectoService } from '../../core/services/proyecto.service';

interface ProyectoPendiente {
  id: string;
  tesista: string;
  titulo: string;
  tutor: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, ModalAgendamientoComponent],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
  modalAbierto = false;
  proyectoSeleccionado: ProyectoPendiente | null = null;

  proyectosPendientes: ProyectoPendiente[] = [];

  constructor(
    private defensaService: DefensaService,
    private proyectoService: ProyectoService,
  ) {}

  ngOnInit(): void {
    this.proyectoService.listarProyectos().subscribe({
      next: (proyectos) => {
        this.proyectosPendientes = proyectos
          .filter((p) => p.estatus === 'PENDIENTE')
          .map((p) => ({
            id: p.id,
            tesista: [p.estudiante?.nombres, p.estudiante?.apellidos].filter(Boolean).join(' ') || 'Por asignar',
            titulo: p.titulo ?? 'Sin título',
            tutor: p.tutor?.nombreCompleto ?? 'Por asignar',
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