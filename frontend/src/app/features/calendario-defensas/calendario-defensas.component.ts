import { Component, OnInit } from '@angular/core';
import { DefensaService } from '../../core/services/defensa.service';
import { DocenteService } from '../../core/services/docente.service';
import { ProyectoService } from '../../core/services/proyecto.service';
import { PeriodoAcademicoService } from '../../core/services/periodo-academico.service';

interface DiaCalendario {
  fecha: Date;
  numero: number;
  esMesActual: boolean;
  esPrimeroDeMes: boolean;
  esHoy: boolean;
  defensas: any[];
}

@Component({
  selector: 'app-calendario-defensas',
  templateUrl: './calendario-defensas.component.html',
  styleUrl: './calendario-defensas.component.css',
})
export class CalendarioDefensasComponent implements OnInit {
  trimestreInicio = '';
  trimestreFin = '';

  readonly MESES_CORTOS = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
  readonly DIAS_SEMANA = ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb'];

  hoy = new Date();
  fechaActiva = new Date();
  mesActual = '';
  diasDelMes: DiaCalendario[] = [];

  docentes: any[] = [];
  proyectos: any[] = [];
  escuelas: string[] = [];
  tutorIdSeleccionado: number | undefined;
  selectedProyectoId: string | undefined;
  filtroEscuela = 'Todas';

  defensas: any[] = [];
  selectedDate: Date | string | null = null;
  defensasDelDia: any[] = [];

  reporteAbierto = false;
  fechaReporte = '';
  cargandoReporte = false;
  reporte: any[] = [];

  modalReprogramarAbierto = false;
  defensaSeleccionada: any = null;

  constructor(
    private defensaService: DefensaService,
    private docenteService: DocenteService,
    private proyectoService: ProyectoService,
    private periodoAcademico: PeriodoAcademicoService,
  ) {
    const trimestre = this.periodoAcademico.obtenerTrimestreActual();
    this.trimestreInicio = trimestre.fechaInicio;
    this.trimestreFin = trimestre.fechaFin;
  }

  ngOnInit(): void {
    const [ano, mes] = this.trimestreInicio.split('-').map(Number);
    this.fechaActiva = new Date(ano, mes - 1, 1);
    this.construirMallaMensual();
    this.docenteService.getDocentes().subscribe((docentes: any[]) => {
      this.docentes = docentes;
    });
    this.proyectoService.getEscuelas().subscribe((escuelas: string[]) => {
      this.escuelas = escuelas;
    });
    this.cargarEventos();
  }

  cambiarMes(delta: number): void {
    const siguiente = new Date(this.fechaActiva.getFullYear(), this.fechaActiva.getMonth() + delta, 1);
    const minMes = this.primeroDelMes(this.trimestreInicio);
    const maxMes = this.primeroDelMes(this.trimestreFin);
    if (siguiente < minMes) this.fechaActiva = minMes;
    else if (siguiente > maxMes) this.fechaActiva = maxMes;
    else this.fechaActiva = siguiente;
    this.construirMallaMensual();
  }

  irAHoy(): void {
    this.fechaActiva = new Date(this.hoy.getFullYear(), this.hoy.getMonth(), 1);
    this.construirMallaMensual();
  }

  seleccionarDia(dia: DiaCalendario): void {
    this.selectedDate = this.milToYyyyMmDd(dia.fecha);
    this.cargarDefensasDelDia();
  }

  mesCorto(fecha: Date): string {
    return this.MESES_CORTOS[fecha.getMonth()];
  }

  private primeroDelMes(fechaIso: string): Date {
    const [ano, mes] = fechaIso.split('-').map(Number);
    return new Date(ano, mes - 1, 1);
  }

  private construirMallaMensual(): void {
    const anio = this.fechaActiva.getFullYear();
    const mes = this.fechaActiva.getMonth();
    const primero = new Date(anio, mes, 1);
    const diaInicio = 1 - primero.getDay();

    const dias: DiaCalendario[] = [];
    for (let i = 0; i < 42; i++) {
      const fecha = new Date(anio, mes, diaInicio + i);
      dias.push({
        fecha,
        numero: fecha.getDate(),
        esMesActual: fecha.getMonth() === mes && fecha.getFullYear() === anio,
        esPrimeroDeMes: fecha.getDate() === 1,
        esHoy: this.mismaFecha(fecha, this.hoy),
        defensas: this.defensas.filter((d: any) => this.milToYyyyMmDd(d.fecha) === this.milToYyyyMmDd(fecha)),
      });
    }

    this.diasDelMes = dias;
    this.mesActual = this.formatearTituloMes(this.fechaActiva);
  }

  private mismaFecha(a: Date, b: Date): boolean {
    return a.getFullYear() === b.getFullYear()
      && a.getMonth() === b.getMonth()
      && a.getDate() === b.getDate();
  }

  private formatearTituloMes(fecha: Date): string {
    const mes = fecha.toLocaleDateString('es-ES', { month: 'long' });
    return `${mes.charAt(0).toUpperCase()}${mes.slice(1)} ${fecha.getFullYear()}`;
  }

  abrirReporte(): void {
    this.fechaReporte = '';
    this.reporte = [];
    this.cargandoReporte = false;
    this.reporteAbierto = true;
  }

  cerrarReporte(): void {
    this.reporteAbierto = false;
    this.reporte = [];
  }

  onFechaReporte(): void {
    if (!this.fechaReporte) {
      this.reporte = [];
      return;
    }
    this.cargandoReporte = true;
    this.defensaService.obtenerReporteDiario(this.fechaReporte).subscribe({
      next: (data) => {
        this.reporte = data;
        this.cargandoReporte = false;
      },
      error: () => {
        this.cargandoReporte = false;
      },
    });
  }

  imprimirReporte(): void {
    window.print();
  }

  onFiltroCambiar(): void {
    this.cargarEventos();
  }

  aplicarFiltros(): void {
    this.cargarEventos();
  }

  private cargarEventos(): void {
    const escuela = this.filtroEscuela === 'Todas' ? undefined : this.filtroEscuela;
    this.defensaService.listarDefensas(this.tutorIdSeleccionado, this.selectedProyectoId, escuela)
      .subscribe((defensas: any[]) => {
        const filtradas = defensas.filter(
          (d: any) => this.filtroEscuela === 'Todas' || d.proyecto?.escuela === this.filtroEscuela,
        );
        this.defensas = filtradas;
        this.construirMallaMensual();
        if (this.selectedDate) {
          this.cargarDefensasDelDia();
        }
      });
  }

  private cargarDefensasDelDia(): void {
    if (!this.selectedDate) {
      this.defensasDelDia = [];
      return;
    }
    const fecha = this.milToYyyyMmDd(this.selectedDate);
    this.defensasDelDia = this.defensas.filter((d: any) => this.milToYyyyMmDd(d.fecha) === fecha);
  }

  private milToYyyyMmDd(fecha: Date | string): string {
    const d = fecha instanceof Date ? fecha : new Date(`${fecha}T00:00:00`);
    if (isNaN(d.getTime())) return '';
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  }

  formattedDate(): string {
    if (!this.selectedDate) return 'Seleccione un día';
    const d = this.selectedDate instanceof Date ? this.selectedDate : new Date(`${this.selectedDate}T00:00:00`);
    return d.toLocaleDateString('es-ES', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' });
  }

  abrirReprogramar(defensa: any): void {
    this.defensaSeleccionada = defensa;
    this.modalReprogramarAbierto = true;
  }

  cerrarReprogramar(): void {
    this.modalReprogramarAbierto = false;
    this.defensaSeleccionada = null;
  }

  proyectoDeDefensa(defensa: any): any {
    const estudiante = defensa?.proyecto?.estudiante;
    const tesista = [estudiante?.nombres, estudiante?.apellidos].filter(Boolean).join(' ') || 'Por asignar';
    return {
      id: defensa?.proyecto?.id,
      tesista,
      titulo: defensa?.proyecto?.titulo || 'Sin título',
      tutor: defensa?.proyecto?.tutor ?? null,
      tutorMetodologico: defensa?.proyecto?.tutorMetodologico ?? null,
    };
  }

  nombreEstudiante(e: any): string {
    return [e?.nombres, e?.apellidos].filter(Boolean).join(' ') || 'Por asignar';
  }

  onConfirmarReprogramar(evento: any): void {
    if (!this.defensaSeleccionada) return;
    const defensa = this.defensaSeleccionada;

    const juradosIds = [evento.juradoId, evento.tutorAcademicoId, evento.tutorMetodologicoId]
      .filter((id: any) => id != null);

    const body = {
      espacioId: evento.espacioId,
      fecha: evento.fecha,
      horaInicio: evento.horaInicio.length === 5 ? `${evento.horaInicio}:00` : evento.horaInicio,
      horaFin: evento.horaFin.length === 5 ? `${evento.horaFin}:00` : evento.horaFin,
      juradosIds,
    };

    this.defensaService.reprogramarDefensa(defensa.id, body).subscribe({
      next: () => {
        alert('Defensa reprogramada exitosamente.');
        this.cerrarReprogramar();
        this.cargarEventos();
      },
      error: (err) => {
        console.error('Error al reprogramar la defensa', err);
        alert('No se pudo reprogramar la defensa: ' + err.message);
      },
    });
  }
}
