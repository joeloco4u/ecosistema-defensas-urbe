import { Injectable } from '@angular/core';

export interface TrimestreAcademico {
  fechaInicio: string;
  fechaFin: string;
  etiqueta: string;
}

/**
 * Fuente única de verdad del período académico vigente.
 *
 * Antes, el Calendario de Defensas definía las fechas del trimestre como
 * constantes privadas dentro de su propio componente. Aquí se centralizan
 * para que cualquier módulo (Calendario, Módulo de Reportes, etc.) consuma
 * exactamente el mismo período sin duplicar ni re-calcular fechas.
 *
 * Al cambiar de trimestre, o al conectar un endpoint de configuración
 * académica en el backend, este es el único lugar a modificar.
 */
@Injectable({ providedIn: 'root' })
export class PeriodoAcademicoService {

  private readonly trimestreVigente: TrimestreAcademico = {
    fechaInicio: '2026-08-24',
    fechaFin: '2026-11-30',
    etiqueta: 'Trimestre Ago - Dic 2026',
  };

  /** Límites (yyyy-MM-dd) del trimestre académico activo. */
  obtenerTrimestreActual(): TrimestreAcademico {
    return { ...this.trimestreVigente };
  }

  /** Indica si una fecha (yyyy-MM-dd) cae dentro del trimestre activo. */
  estaEnTrimestreActual(fecha: string): boolean {
    return fecha >= this.trimestreVigente.fechaInicio
      && fecha <= this.trimestreVigente.fechaFin;
  }
}
