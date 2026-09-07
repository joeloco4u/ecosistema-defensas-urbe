import { Component, OnInit } from '@angular/core';
import { DocenteService } from '../../core/services/docente.service';

interface DocenteInfo {
  id: number;
  codigoInstitucional: string;
  nombreCompleto: string;
  email?: string;
  departamento?: string;
  cargaMaximaSemanal?: number;
  activo?: boolean;
}

@Component({
  selector: 'app-directorio-docentes',
  templateUrl: './directorio-docentes.component.html',
})
export class DirectorioDocentesComponent implements OnInit {
  cargando = false;
  docentes: DocenteInfo[] = [];
  busqueda = '';

  expedienteAbierto = false;
  cargandoExpediente = false;
  expediente: any = null;

  constructor(private docenteService: DocenteService) {}

  ngOnInit(): void {
    this.cargarDocentes();
  }

  cargarDocentes(): void {
    this.cargando = true;
    this.docenteService.getDocentes().subscribe({
      next: (docentes) => {
        this.docentes = docentes;
        this.cargando = false;
      },
      error: () => {
        this.cargando = false;
      },
    });
  }

  get docentesFiltrados(): DocenteInfo[] {
    const termino = this.busqueda.trim().toLowerCase();
    if (!termino) return this.docentes;
    return this.docentes.filter(
      (d) =>
        (d.nombreCompleto ?? '').toLowerCase().includes(termino) ||
        (d.codigoInstitucional ?? '').toLowerCase().includes(termino),
    );
  }

  verExpediente(docente: DocenteInfo): void {
    this.expedienteAbierto = true;
    this.cargandoExpediente = true;
    this.expediente = null;
    this.docenteService.getExpediente(docente.id).subscribe({
      next: (data) => {
        this.expediente = data;
        this.cargandoExpediente = false;
      },
      error: () => {
        this.cargandoExpediente = false;
      },
    });
  }

  cerrarExpediente(): void {
    this.expedienteAbierto = false;
    this.expediente = null;
  }

  proyectoEstatusClase(estatus: string): string {
    switch (estatus) {
      case 'PENDIENTE': return 'bg-yellow-900/30 text-yellow-400';
      case 'AGENDADO': return 'bg-blue-900/30 text-blue-400';
      case 'DEFENDIDO': return 'bg-emerald-900/30 text-emerald-400';
      default: return 'bg-surface-light text-accent-muted';
    }
  }

  defensaEstatusClase(estatus: string): string {
    switch (estatus) {
      case 'PROGRAMADA': return 'bg-blue-900/30 text-blue-400';
      case 'REPROGRAMADA': return 'bg-yellow-900/30 text-yellow-400';
      case 'FINALIZADA': return 'bg-emerald-900/30 text-emerald-400';
      default: return 'bg-surface-light text-accent-muted';
    }
  }

  rolTutoriaLabel(rol: string): string {
    return rol === 'METODOLOGICO' ? 'Tutor Metodológico' : 'Tutor Académico';
  }
}