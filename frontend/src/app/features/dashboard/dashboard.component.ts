import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../core/services/dashboard.service';

interface DashboardStats {
  totalProyectos: number;
  proyectosPendientes: number;
  proyectosAgendados: number;
  proyectosDefendidos: number;
  defensasEstaSemana: number;
  reprogramaciones: number;
  proyectosPorEscuela: Record<string, number>;
}

interface EscuelaTotal {
  nombre: string;
  total: number;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
  stats: DashboardStats | null = null;

  get escuelas(): EscuelaTotal[] {
    const proyectosPorEscuela = this.stats?.proyectosPorEscuela ?? {};
    return Object.entries(proyectosPorEscuela)
      .map(([nombre, total]) => ({ nombre, total }))
      .sort((a, b) => b.total - a.total || a.nombre.localeCompare(b.nombre));
  }

  get maxProyectosEscuela(): number {
    return Math.max(1, ...this.escuelas.map((escuela) => escuela.total));
  }

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.dashboardService.getStats().subscribe({
      next: (data) => {
        this.stats = data;
      },
      error: (err) => {
        console.error('Error al cargar las estadísticas del dashboard', err);
      },
    });
  }

  porcentajeEscuela(total: number): number {
    return Math.max(6, Math.round((total / this.maxProyectosEscuela) * 100));
  }
}
