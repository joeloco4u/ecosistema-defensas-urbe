import { Component, OnInit } from '@angular/core';
import { ProyectoService } from '../../core/services/proyecto.service';

interface EstudianteInfo {
  cedula?: string;
  nombres?: string;
  apellidos?: string;
}

interface TutorInfo {
  nombreCompleto?: string;
}

interface ProyectoRegistro {
  id: string;
  titulo?: string;
  escuela?: string;
  estudiante?: EstudianteInfo;
  tutor?: TutorInfo;
  estatus: string;
}

@Component({
  selector: 'app-gestion-proyectos',
  templateUrl: './gestion-proyectos.component.html',
})
export class GestionProyectosComponent implements OnInit {
  mostrarModal = false;
  cargando = false;
  exportando = false;

  proyectos: ProyectoRegistro[] = [];
  busqueda = '';
  filtroEstatus = 'TODOS';
  filtroEscuela = 'Todas';

  estatusDisponibles = ['TODOS', 'PENDIENTE', 'AGENDADO', 'DEFENDIDO'];
  escuelas: string[] = [];

  proyectoSeleccionado: any = null;
  mostrarModalExpediente = false;

  constructor(private proyectoService: ProyectoService) {}

  ngOnInit(): void {
    this.cargarProyectos();
    this.proyectoService.getEscuelas().subscribe((escuelas: string[]) => {
      this.escuelas = escuelas;
    });
  }

  cargarProyectos(): void {
    this.cargando = true;
    this.proyectoService.listarProyectos().subscribe({
      next: (proyectos) => {
        this.proyectos = proyectos;
        this.cargando = false;
      },
      error: () => {
        this.cargando = false;
      },
    });
  }

  exportarBackup(): void {
    this.exportando = true;
    this.proyectoService.descargarBackupCsv().subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'backup_proyectos_urbe.csv';
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
        this.exportando = false;
      },
      error: (err) => {
        console.error('Error al exportar el backup', err);
        this.exportando = false;
      },
    });
  }

  get proyectosFiltrados(): ProyectoRegistro[] {
    const termino = this.busqueda.trim().toLowerCase();
    return this.proyectos.filter((p) => {
      if (this.filtroEstatus !== 'TODOS' && p.estatus !== this.filtroEstatus) return false;
      if (this.filtroEscuela !== 'Todas' && p.escuela !== this.filtroEscuela) return false;
      if (!termino) return true;
      const tesista = [p.estudiante?.nombres, p.estudiante?.apellidos].filter(Boolean).join(' ').toLowerCase();
      const cedula = (p.estudiante?.cedula ?? '').toLowerCase();
      const tutor = (p.tutor?.nombreCompleto ?? '').toLowerCase();
      const titulo = (p.titulo ?? '').toLowerCase();
      return tesista.includes(termino) || cedula.includes(termino) || tutor.includes(termino) || titulo.includes(termino);
    });
  }

  aplicarFiltros(): void {
    return;
  }

  abrirExpediente(proyecto: any): void {
    this.proyectoSeleccionado = proyecto;
    this.mostrarModalExpediente = true;
  }

  cerrarExpediente(): void {
    this.mostrarModalExpediente = false;
    this.proyectoSeleccionado = null;
  }

  tesistaNombre(p: ProyectoRegistro): string {
    return [p.estudiante?.nombres, p.estudiante?.apellidos].filter(Boolean).join(' ') || 'Por asignar';
  }

  estatusClase(estatus: string): string {
    switch (estatus) {
      case 'PENDIENTE': return 'bg-yellow-900/30 text-yellow-400';
      case 'AGENDADO': return 'bg-blue-900/30 text-blue-400';
      case 'DEFENDIDO': return 'bg-emerald-900/30 text-emerald-400';
      default: return 'bg-surface-light text-accent-muted';
    }
  }
}