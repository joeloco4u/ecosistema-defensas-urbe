import { Component, OnInit } from '@angular/core';
import { ReporteService, DefensaDTO } from '../../core/services/reporte.service';
import { DocenteService } from '../../core/services/docente.service';
import { ProyectoService } from '../../core/services/proyecto.service';

@Component({
  selector: 'app-documentos',
  templateUrl: './documentos.component.html',
})
export class DocumentosComponent implements OnInit {

  todas: DefensaDTO[] = [];
  seleccionada: DefensaDTO | null = null;

  escuelas: string[] = [];
  escuelaSeleccionada = 'Todas';
  defensasEscuela: DefensaDTO[] = [];

  cargando = true;
  error = '';

  private docentesPorId = new Map<number, string>();

  constructor(
    private reporteService: ReporteService,
    private docenteService: DocenteService,
    private proyectoService: ProyectoService,
  ) {}

  ngOnInit(): void {
    this.docenteService.getDocentes().subscribe({
      next: (docentes) => {
        this.docentesPorId = new Map(
          (docentes ?? []).map((d: any) => [d.id as number, d.nombreCompleto ?? ''] as [number, string]),
        );
      },
      error: (err) => console.error('Error al cargar los docentes', err),
    });
    this.proyectoService.getEscuelas().subscribe({
      next: (escuelas) => {
        this.escuelas = escuelas ?? [];
      },
      error: (err) => console.error('Error al cargar las escuelas', err),
    });
    this.cargarDefensas();
  }

  onEscuelaChange(): void {
    this.aplicarFiltroEscuela();
  }

  escuelaDe(defensa: DefensaDTO): string {
    return defensa.proyecto?.escuela || 'Sin escuela';
  }

  private aplicarFiltroEscuela(): void {
    if (!this.escuelaSeleccionada || this.escuelaSeleccionada === 'Todas') {
      this.defensasEscuela = [];
      return;
    }
    this.defensasEscuela = this.todas
      .filter((d) => this.escuelaDe(d) === this.escuelaSeleccionada)
      .sort((a, b) =>
        (a.fecha ?? '').localeCompare(b.fecha ?? '')
        || this.formatHora(a.horaInicio).localeCompare(this.formatHora(b.horaInicio)),
      );
  }

  seleccionar(defensa: DefensaDTO): void {
    this.seleccionada = defensa;
  }

  imprimirVeredicto(): void {
    if (!this.seleccionada) return;
    window.print();
  }

  imprimirPreDefensa(): void {
    const escuela = encodeURIComponent(this.escuelaSeleccionada);
    window.open(`/imprimir/pre-defensa/${escuela}`, '_blank');
  }

  tesistas(d: DefensaDTO): string {
    const p = d.proyecto;
    const nombres = [p?.estudiante, p?.estudiante2, p?.estudiante3]
      .map((e) => [e?.nombres, e?.apellidos].filter(Boolean).join(' ').trim())
      .filter((nombre) => nombre.length > 0);
    return nombres.length > 0 ? nombres.join(', ') : 'Por asignar';
  }

  nombreDocente(id?: number | null): string {
    if (id === null || id === undefined) return 'Por asignar';
    return this.docentesPorId.get(id) ?? `Docente #${id}`;
  }

  tutorAcademico(d: DefensaDTO): string {
    return d.proyecto?.tutor?.nombreCompleto || this.nombreDocente(d.tutorAcademicoId);
  }

  tutorMetodologico(d: DefensaDTO): string {
    return d.proyecto?.tutorMetodologico?.nombreCompleto || this.nombreDocente(d.tutorMetodologicoId);
  }

  fechaPartes(fecha?: string): { dia: string; mes: string; anio: string } {
    const d = this.aFechaLocal(fecha);
    if (!d) return { dia: '—', mes: '—', anio: '—' };
    return {
      dia: String(d.getDate()),
      mes: d.toLocaleDateString('es-ES', { month: 'long' }),
      anio: String(d.getFullYear()),
    };
  }

  formatHora(hora?: string): string {
    return hora ? hora.slice(0, 5) : '—';
  }

  private cargarDefensas(): void {
    this.cargando = true;
    this.error = '';
    this.reporteService.filtrarReportes({}).subscribe({
      next: (defensas) => {
        this.todas = defensas ?? [];
        this.aplicarFiltroEscuela();
        this.cargando = false;
      },
      error: (err) => {
        console.error('Error al cargar las defensas', err);
        this.error = 'No se pudieron cargar las defensas.';
        this.cargando = false;
      },
    });
  }

  private aFechaLocal(fecha?: string): Date | null {
    if (!fecha) return null;
    const d = new Date(`${fecha}T00:00:00`);
    return isNaN(d.getTime()) ? null : d;
  }
}
