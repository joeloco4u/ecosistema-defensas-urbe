import { Component, OnInit } from '@angular/core';
import { ReporteService, FiltroReporte, DefensaDTO, ProyectoReporte } from '../../core/services/reporte.service';
import { DocenteService } from '../../core/services/docente.service';
import { ProyectoService } from '../../core/services/proyecto.service';
import { PeriodoAcademicoService } from '../../core/services/periodo-academico.service';

type TabReporte = 'DEFENSAS' | 'DOCENTES';
type RolDocenteFiltro = 'TODOS' | 'JURADO' | 'TUTOR_ACADEMICO' | 'TUTOR_METODOLOGICO';

interface AsignacionDocente {
  id: number;
  rol: string;
  rolCodigo: RolDocenteFiltro;
}

interface FilaDocente {
  docente: string;
  rol: string;
  defensa: DefensaDTO;
}

@Component({
  selector: 'app-reportes',
  templateUrl: './reportes.component.html',
})
export class ReportesComponent implements OnInit {

  activeTab: TabReporte = 'DEFENSAS';

  docentes: any[] = [];
  escuelas: string[] = [];
  semanas: string[] = ['Todas', 'Esta Semana', ...Array.from({ length: 14 }, (_, i) => `Semana ${i + 1}`)];

  escuelaSeleccionada = 'Todas';
  semanaDefensas = 'Todas';

  docenteSeleccionadoId: number | null = null;
  rolSeleccionado: RolDocenteFiltro = 'TODOS';
  semanaDocentes = 'Todas';

  resultadosDefensas: DefensaDTO[] = [];
  resultadosDocentes: FilaDocente[] = [];

  cargando = false;
  reporteGenerado = false;

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

  get totalResultados(): number {
    return this.activeTab === 'DEFENSAS' ? this.resultadosDefensas.length : this.resultadosDocentes.length;
  }

  get tituloReporte(): string {
    return this.activeTab === 'DEFENSAS' ? 'Reporte de Defensas' : 'Reporte de Docentes';
  }

  cambiarTab(tab: TabReporte): void {
    if (this.activeTab === tab) return;
    this.activeTab = tab;
    this.limpiarResultados();
  }

  generarReporte(): void {
    if (this.activeTab === 'DEFENSAS') {
      this.generarReporteDefensas();
    } else {
      this.generarReporteDocentes();
    }
  }

  limpiarFiltros(): void {
    this.escuelaSeleccionada = 'Todas';
    this.semanaDefensas = 'Todas';
    this.docenteSeleccionadoId = null;
    this.rolSeleccionado = 'TODOS';
    this.semanaDocentes = 'Todas';
    this.limpiarResultados();
  }

  imprimirReporte(): void {
    window.print();
  }

  descargarCSV(): void {
    const filas = this.activeTab === 'DEFENSAS'
      ? this.filasCsvDefensas()
      : this.filasCsvDocentes();
    if (filas.length <= 1) return;

    const contenido = filas.map((fila) => fila.map((c) => this.csvCampo(c)).join(',')).join('\r\n');
    const blob = new Blob(['\uFEFF' + contenido], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const enlace = document.createElement('a');

    enlace.href = url;
    enlace.download = this.activeTab === 'DEFENSAS' ? 'reporte-defensas.csv' : 'reporte-docentes.csv';
    document.body.appendChild(enlace);
    enlace.click();
    document.body.removeChild(enlace);
    URL.revokeObjectURL(url);
  }

  private generarReporteDefensas(): void {
    this.cargando = true;
    this.reporteGenerado = false;
    const { fechaInicio, fechaFin } = this.rangoSemana(this.semanaDefensas);
    const filtro: FiltroReporte = {
      fechaInicio,
      fechaFin,
      docenteId: null,
      rol: 'AMBOS',
      escuela: this.escuelaSeleccionada === 'Todas' ? null : this.escuelaSeleccionada,
    };

    this.reporteService.filtrarReportes(filtro).subscribe({
      next: (defensas) => {
        this.resultadosDefensas = defensas ?? [];
        this.resultadosDocentes = [];
        this.cargando = false;
        this.reporteGenerado = true;
      },
      error: (err) => this.manejarError(err),
    });
  }

  private generarReporteDocentes(): void {
    this.cargando = true;
    this.reporteGenerado = false;
    const { fechaInicio, fechaFin } = this.rangoSemana(this.semanaDocentes);

    this.reporteService.filtrarReportes({ fechaInicio, fechaFin }).subscribe({
      next: (defensas) => {
        this.resultadosDocentes = this.construirFilasDocentes(defensas ?? []);
        this.resultadosDefensas = [];
        this.cargando = false;
        this.reporteGenerado = true;
      },
      error: (err) => this.manejarError(err),
    });
  }

  private construirFilasDocentes(defensas: DefensaDTO[]): FilaDocente[] {
    const filas: FilaDocente[] = [];
    for (const d of defensas) {
      for (const asignacion of this.asignacionesDocente(d)) {
        if (this.docenteSeleccionadoId != null && asignacion.id !== this.docenteSeleccionadoId) continue;
        if (this.rolSeleccionado !== 'TODOS' && this.rolSeleccionado !== asignacion.rolCodigo) continue;
        filas.push({ docente: this.nombreDocentePorId(asignacion.id), rol: asignacion.rol, defensa: d });
      }
    }
    return filas.sort((a, b) =>
      (a.defensa.fecha ?? '').localeCompare(b.defensa.fecha ?? '')
      || this.formatHora(a.defensa.horaInicio).localeCompare(this.formatHora(b.defensa.horaInicio))
      || a.docente.localeCompare(b.docente),
    );
  }

  private asignacionesDocente(d: DefensaDTO): AsignacionDocente[] {
    const asignaciones: AsignacionDocente[] = [];

    if (d.juradoId != null) {
      asignaciones.push({ id: d.juradoId, rol: 'Jurado', rolCodigo: 'JURADO' });
    }

    const tutorAcademicoId = d.proyecto?.tutor?.id ?? d.tutorAcademicoId ?? null;
    if (tutorAcademicoId != null) {
      asignaciones.push({ id: tutorAcademicoId, rol: 'Tutor Académico', rolCodigo: 'TUTOR_ACADEMICO' });
    }

    const tutorMetodologicoId = d.proyecto?.tutorMetodologico?.id ?? d.tutorMetodologicoId ?? null;
    if (tutorMetodologicoId != null) {
      asignaciones.push({ id: tutorMetodologicoId, rol: 'Tutor Metodológico', rolCodigo: 'TUTOR_METODOLOGICO' });
    }

    return asignaciones;
  }

  private manejarError(err: any): void {
    console.error('Error al generar el reporte', err);
    this.cargando = false;
    window.alert('No se pudo generar el reporte: ' + (err?.error?.mensaje || err?.message || ''));
  }

  private limpiarResultados(): void {
    this.resultadosDefensas = [];
    this.resultadosDocentes = [];
    this.reporteGenerado = false;
  }

  /** Rango de fechas (yyyy-MM-dd) para una semana del trimestre, 'Esta Semana' o 'Todas'. */
  private rangoSemana(semana: string): { fechaInicio: string | null; fechaFin: string | null } {
    if (!semana || semana === 'Todas') {
      return { fechaInicio: null, fechaFin: null };
    }

    if (semana === 'Esta Semana') {
      const lunes = this.lunesDe(new Date());
      return { fechaInicio: this.aIso(lunes), fechaFin: this.aIso(this.sumarDias(lunes, 6)) };
    }

    const numero = Number(semana.replace(/[^0-9]/g, ''));
    if (!numero) {
      return { fechaInicio: null, fechaFin: null };
    }

    const inicioTrimestre = this.periodoAcademico.obtenerTrimestreActual().fechaInicio;
    const primerLunes = this.lunesDe(new Date(`${inicioTrimestre}T00:00:00`));
    const lunes = this.sumarDias(primerLunes, (numero - 1) * 7);
    return { fechaInicio: this.aIso(lunes), fechaFin: this.aIso(this.sumarDias(lunes, 6)) };
  }

  private lunesDe(fecha: Date): Date {
    const resultado = new Date(fecha);
    const dia = resultado.getDay();
    resultado.setDate(resultado.getDate() + (dia === 0 ? -6 : 1 - dia));
    resultado.setHours(0, 0, 0, 0);
    return resultado;
  }

  private sumarDias(fecha: Date, dias: number): Date {
    const resultado = new Date(fecha);
    resultado.setDate(resultado.getDate() + dias);
    return resultado;
  }

  private aIso(fecha: Date): string {
    const anio = fecha.getFullYear();
    const mes = String(fecha.getMonth() + 1).padStart(2, '0');
    const dia = String(fecha.getDate()).padStart(2, '0');
    return `${anio}-${mes}-${dia}`;
  }

  tesistas(d: DefensaDTO): string {
    return this.nombresTesistas(d.proyecto) || 'Por asignar';
  }

  escuelaDe(d: DefensaDTO): string {
    return d.proyecto?.escuela || '—';
  }

  formatFecha(fecha?: string): string {
    if (!fecha) return '—';
    const [anio, mes, dia] = fecha.split('-');
    return anio && mes && dia ? `${dia}/${mes}/${anio}` : fecha;
  }

  formatHora(hora?: string): string {
    return hora ? hora.slice(0, 5) : '—';
  }

  nombreDocentePorId(id?: number | null): string {
    if (id === null || id === undefined) return 'Por asignar';
    return this.docentesPorId.get(id) ?? `Docente #${id}`;
  }

  tutorAcademico(d: DefensaDTO): string {
    return d.proyecto?.tutor?.nombreCompleto || this.nombreDocentePorId(d.tutorAcademicoId);
  }

  tutorMetodologico(d: DefensaDTO): string {
    return d.proyecto?.tutorMetodologico?.nombreCompleto || this.nombreDocentePorId(d.tutorMetodologicoId);
  }

  juradoAsignado(d: DefensaDTO): string {
    return this.nombreDocentePorId(d.juradoId);
  }

  private filasCsvDefensas(): string[][] {
    const encabezado = ['Expediente', 'Título', 'Escuela', 'Tesistas', 'Tutor', 'Fecha', 'Hora', 'Espacio'];
    const filas = this.resultadosDefensas.map((d) => [
      d.proyecto?.expediente || '',
      d.proyecto?.titulo || '',
      this.escuelaDe(d),
      this.nombresTesistas(d.proyecto),
      this.tutorAcademico(d),
      this.formatFecha(d.fecha),
      `${this.formatHora(d.horaInicio)} - ${this.formatHora(d.horaFin)}`,
      d.espacioFisico?.codigoAula || '',
    ]);
    return [encabezado, ...filas];
  }

  private filasCsvDocentes(): string[][] {
    const encabezado = ['Docente', 'Rol', 'Expediente', 'Título', 'Escuela', 'Fecha', 'Hora', 'Espacio'];
    const filas = this.resultadosDocentes.map((f) => [
      f.docente,
      f.rol,
      f.defensa.proyecto?.expediente || '',
      f.defensa.proyecto?.titulo || '',
      this.escuelaDe(f.defensa),
      this.formatFecha(f.defensa.fecha),
      `${this.formatHora(f.defensa.horaInicio)} - ${this.formatHora(f.defensa.horaFin)}`,
      f.defensa.espacioFisico?.codigoAula || '',
    ]);
    return [encabezado, ...filas];
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
}
