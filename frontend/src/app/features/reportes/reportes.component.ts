import { Component, OnInit } from '@angular/core';
import { ReporteService, FiltroReporte, DefensaDTO, ProyectoReporte } from '../../core/services/reporte.service';
import { DocenteService } from '../../core/services/docente.service';
import { ProyectoService } from '../../core/services/proyecto.service';
import { PeriodoAcademicoService } from '../../core/services/periodo-academico.service';

@Component({
  selector: 'app-reportes',
  templateUrl: './reportes.component.html',
})
export class ReportesComponent implements OnInit {

  filtro: FiltroReporte = this.filtroVacio();
  resultados: DefensaDTO[] = [];

  docentes: any[] = [];
  escuelas: string[] = [];

  expandedRows: Set<string> = new Set();

  cargando = false;
  busquedaRealizada = false;

  private docentesPorId = new Map<number, string>();

  constructor(
    private reporteService: ReporteService,
    private docenteService: DocenteService,
    private proyectoService: ProyectoService,
    private periodoAcademico: PeriodoAcademicoService,
  ) {}

  ngOnInit(): void {
    this.docenteService.getDocentes().subscribe({
      next: (docentes) => {
        this.docentes = docentes ?? [];
        this.docentesPorId = new Map(
          this.docentes.map((d) => [d.id as number, d.nombreCompleto ?? ''] as [number, string]),
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
  }

  generarReporte(): void {
    this.cargando = true;
    this.reporteService.filtrarReportes(this.filtro).subscribe({
      next: (defensas) => {
        this.resultados = defensas ?? [];
        this.expandedRows.clear();
        this.cargando = false;
        this.busquedaRealizada = true;
      },
      error: (err) => {
        console.error('Error al generar el reporte', err);
        this.cargando = false;
        window.alert('No se pudo generar el reporte: ' + (err.error?.mensaje || err.message));
      },
    });
  }

  limpiarFiltros(): void {
    this.filtro = this.filtroVacio();
    this.resultados = [];
    this.expandedRows.clear();
    this.busquedaRealizada = false;
  }

  tesistas(p?: ProyectoReporte | null): string {
    if (!p) return 'Por asignar';
    const nombres = [p.estudiante, p.estudiante2, p.estudiante3]
      .map((e) => [e?.nombres, e?.apellidos].filter(Boolean).join(' ').trim())
      .filter((nombre) => nombre.length > 0);
    return nombres.length > 0 ? nombres.join(', ') : 'Por asignar';
  }

  formatFecha(fecha?: string): string {
    if (!fecha) return '—';
    const [anio, mes, dia] = fecha.split('-');
    return anio && mes && dia ? `${dia}/${mes}/${anio}` : fecha;
  }

  formatHora(hora?: string): string {
    return hora ? hora.slice(0, 5) : '—';
  }

  toggleRow(id: string): void {
    if (this.expandedRows.has(id)) {
      this.expandedRows.delete(id);
    } else {
      this.expandedRows.add(id);
    }
  }

  isExpanded(id: string): boolean {
    return this.expandedRows.has(id);
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

  juradoAsignado(d: DefensaDTO): string {
    return this.nombreDocente(d.juradoId);
  }

  imprimirPDF(): void {
    window.print();
  }

  seleccionarTrimestreActual(): void {
    const { fechaInicio, fechaFin } = this.periodoAcademico.obtenerTrimestreActual();
    this.filtro.fechaInicio = fechaInicio;
    this.filtro.fechaFin = fechaFin;
  }

  descargarCSV(): void {
    if (!this.resultados || this.resultados.length === 0) return;

    const encabezado = ['Expediente', 'Proyecto', 'Tesistas', 'Fecha', 'Hora', 'Espacio'];
    const filas = this.resultados.map((d) => [
      this.csvCampo(d.proyecto?.expediente || ''),
      this.csvCampo(d.proyecto?.titulo || ''),
      this.csvCampo(this.nombresTesistas(d.proyecto)),
      this.csvCampo(this.formatearFechaCSV(d.fecha)),
      this.csvCampo(`${this.formatHora(d.horaInicio)} - ${this.formatHora(d.horaFin)}`),
      this.csvCampo(d.espacioFisico?.codigoAula || ''),
    ]);

    const contenido = [encabezado, ...filas].map((fila) => fila.join(',')).join('\r\n');
    const blob = new Blob(['\uFEFF' + contenido], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const enlace = document.createElement('a');

    enlace.href = url;
    enlace.download = 'reporte-defensas.csv';
    document.body.appendChild(enlace);
    enlace.click();
    document.body.removeChild(enlace);
    URL.revokeObjectURL(url);
  }

  private formatearFechaCSV(fecha?: string): string {
    if (!fecha) return '';
    const date = new Date(fecha);
    if (isNaN(date.getTime())) return fecha;
    const dia = String(date.getUTCDate()).padStart(2, '0');
    const mes = String(date.getUTCMonth() + 1).padStart(2, '0');
    const anio = date.getUTCFullYear();
    return `${dia}/${mes}/${anio}`;
  }

  private csvCampo(valor: unknown): string {
    const texto = valor === null || valor === undefined ? '' : String(valor);
    return `"${texto.replace(/"/g, '""')}"`;
  }

  private nombresTesistas(p?: ProyectoReporte | null): string {
    if (!p) return '';
    return [p.estudiante, p.estudiante2, p.estudiante3]
      .map((e) => [e?.nombres, e?.apellidos].filter(Boolean).join(' ').trim())
      .filter((nombre) => nombre.length > 0)
      .join(', ');
  }

  private filtroVacio(): FiltroReporte {
    return {
      fechaInicio: null,
      fechaFin: null,
      docenteId: null,
      rol: 'AMBOS',
      escuela: null,
    };
  }
}
