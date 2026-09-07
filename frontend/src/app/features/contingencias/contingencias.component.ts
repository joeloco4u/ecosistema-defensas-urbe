import { Component } from '@angular/core';
import { ProyectoService } from '../../core/services/proyecto.service';

@Component({
  selector: 'app-contingencias',
  templateUrl: './contingencias.component.html',
})
export class ContingenciasComponent {
  ejecutando = false;

  constructor(private proyectoService: ProyectoService) {}

  ejecutarTransicion(): void {
    const confirmado = window.confirm(
      '¿Está seguro? Esto archivará Seminario 3 y promoverá a los alumnos de Seminario 1 y 2. Esta acción no se puede deshacer.',
    );
    if (!confirmado) return;

    this.ejecutando = true;
    this.proyectoService.transicionSeminarios().subscribe({
      next: () => {
        this.ejecutando = false;
        window.alert('Transición de seminarios ejecutada exitosamente.');
      },
      error: () => {
        this.ejecutando = false;
        window.alert('No se pudo ejecutar la transición de seminarios.');
      },
    });
  }
}