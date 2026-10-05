import { Component, OnInit } from '@angular/core';
import { ReporteService, DefensaDTO } from '../../core/services/reporte.service';
import { DocenteService } from '../../core/services/docente.service';

@Component({
  selector: 'app-documentos',
  templateUrl: './documentos.component.html',
})
export class DocumentosComponent implements OnInit {

  terminoBusqueda = '';
  resultadosBusqueda: DefensaDTO[] = [];
  busquedaRealizada = false;

  todas: DefensaDTO[] = [];
  proximas: DefensaDTO[] = [];
  seleccionada: DefensaDTO | null = null;

  cargando = true;
  error = '';

  private docentesPorId = new Map<number, string>();

  constructor(
    private reporteService: ReporteService,
    private docenteService: DocenteService,
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
    this.cargarDefensas();
  }

  buscar(): void {
    const termino = this.terminoBusqueda.trim().toLowerCase();
    this.busquedaRealizada = termino.length > 0;
    if (!termino) {
      this.resultadosBusqueda = [];
      return;
    }
    this.resultadosBusqueda = this.todas.filter((d) =>
      (d.proyecto?.expediente ?? '').toLowerCase().includes(termino),
    );
  }

  seleccionar(defensa: DefensaDTO): void {
    this.seleccionada = defensa;
    this.resultadosBusqueda = [];
    this.busquedaRealizada = false;
    this.terminoBusqueda = defensa.proyecto?.expediente ?? '';
  }

  imprimirVeredicto(): void {
    if (!this.seleccionada) return;
    window.print();
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

  jurado(d: DefensaDTO): string {
    return this.nombreDocente(d.juradoId);
  }

  formatFecha(fecha?: string): string {
    const d = this.aFechaLocal(fecha);
    if (!d) return '—';
    const dia = String(d.getDate()).padStart(2, '0');
    const mes = String(d.getMonth() + 1).padStart(2, '0');
    return `${dia}/${mes}/${d.getFullYear()}`;
  }

  formatFechaLarga(fecha?: string): string {
    const d = this.aFechaLocal(fecha);
    if (!d) return '—';
    return d.toLocaleDateString('es-ES', { day: 'numeric', month: 'long', year: 'numeric' });
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
        this.proximas = this.calcularProximas(this.todas);
        this.cargando = false;
      },
      error: (err) => {
        console.error('Error al cargar las defensas', err);
        this.error = 'No se pudieron cargar las defensas.';
        this.cargando = false;
      },
    });
  }

  private calcularProximas(defensas: DefensaDTO[]): DefensaDTO[] {
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    const limite = new Date(hoy);
    limite.setDate(limite.getDate() + 7);

    return defensas
      .filter((d) => {
        const fecha = this.aFechaLocal(d.fecha);
        return fecha !== null && fecha >= hoy && fecha <= limite;
      })
      .sort((a, b) =>
        (a.fecha ?? '').localeCompare(b.fecha ?? '')
        || this.formatHora(a.horaInicio).localeCompare(this.formatHora(b.horaInicio)),
      );
  }

  private aFechaLocal(fecha?: string): Date | null {
    if (!fecha) return null;
    const d = new Date(`${fecha}T00:00:00`);
    return isNaN(d.getTime()) ? null : d;
  }
}
