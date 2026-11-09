import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DefensaDTO } from '../../../core/services/reporte.service';

interface FilaEstudiante {
  nombre: string;
  cedula: string;
}

@Component({
  selector: 'app-entrega-veredicto',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './entrega-veredicto.component.html',
  styleUrl: './entrega-veredicto.component.css',
})
export class EntregaVeredictoComponent {
  @Input({ required: true }) defensa!: DefensaDTO;

  get expediente(): string {
    return this.defensa?.proyecto?.expediente || '—';
  }

  get titulo(): string {
    return this.defensa?.proyecto?.titulo || '—';
  }

  get filasEstudiantes(): FilaEstudiante[] {
    const proyecto = this.defensa?.proyecto;
    const filas = [proyecto?.estudiante, proyecto?.estudiante2, proyecto?.estudiante3]
      .map((e) => ({
        nombre: [e?.nombres, e?.apellidos].filter(Boolean).join(' ').trim(),
        cedula: e?.cedula ? this.formatearCedula(e.cedula) : '_________',
      }))
      .filter((fila) => fila.nombre.length > 0);
    return filas.length > 0 ? filas : [{ nombre: 'Por asignar', cedula: '_________' }];
  }

  get fechaEntrega(): string {
    return new Intl.DateTimeFormat('es-VE', {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    }).format(new Date());
  }

  private formatearCedula(cedula?: string | null): string {
    if (!cedula) return '_________';
    const digitos = cedula.replace(/[^0-9]/g, '');
    return digitos.length > 0 ? `V-${digitos}` : cedula.trim();
  }
}
