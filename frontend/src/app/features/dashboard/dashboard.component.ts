import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalAgendamientoComponent } from '../../shared/components/modal-agendamiento/modal-agendamiento.component';
import { DefensaService } from '../../core/services/defensa.service';

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
export class DashboardComponent {
  modalAbierto = false;
  proyectoSeleccionado: ProyectoPendiente | null = null;

  proyectosPendientes: ProyectoPendiente[] = [
    {
      id: '1f43e502-a40d-46d0-9a15-c882932db697',
      tesista: 'Viktor Gonzalez',
      titulo: 'Aplicación Web para la Gestión de Tutorías Académicas',
      tutor: 'María Rodríguez',
    },
    {
      id: '223e4567-e89b-12d3-a456-426614174001',
      tesista: 'Sebastián Cárdenas',
      titulo: 'Sistema de Detección de Anomalías en Redes de Datos',
      tutor: 'Carlos Mendoza',
    },
    {
      id: '323e4567-e89b-12d3-a456-426614174002',
      tesista: 'Andreina Paredes',
      titulo: 'Análisis de Cobertura de Redes 5G en Zonas Urbanas',
      tutor: 'Ana López',
    },
  ];

  constructor(private defensaService: DefensaService) {}

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
      proyecto: { id: this.proyectoSeleccionado!.id },
      espacioFisico: { id: evento.espacioId },
      fecha: evento.fecha,
      horaInicio: evento.horaInicio.length === 5 ? evento.horaInicio + ':00' : evento.horaInicio,
      horaFin: evento.horaFin.length === 5 ? evento.horaFin + ':00' : evento.horaFin,
      juradoId: evento.juradoId,
      tutorAcademicoId: evento.tutorAcademicoId,
      tutorMetodologicoId: evento.tutorMetodologicoId,
      estatus: 'PROGRAMADA',
    };

    this.defensaService.crearDefensa(body).subscribe({
      next: () => {
        alert('¡Defensa guardada exitosamente en la BD!');
        this.cerrarModal();
      },
    });
  }
}
