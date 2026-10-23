import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProyectoService } from '../../core/services/proyecto.service';
import { DashboardService } from '../../core/services/dashboard.service';

interface ProyectoPendiente {
  id: string;
  expediente?: string | null;
  titulo: string;
  tutor?: { id?: number; nombreCompleto?: string } | null;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
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
            titulo: p.titulo ?? 'Sin título',
            tutor: p.tutor ?? null,
          }));
      },
      error: (err) => {
        console.error('Error al cargar los proyectos reales', err);
      },
    });
  }
}
