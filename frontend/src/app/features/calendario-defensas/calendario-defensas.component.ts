import { Component, OnInit } from '@angular/core';
import { CalendarOptions } from '@fullcalendar/core';
import type { DateClickArg } from '@fullcalendar/interaction';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';
import { DefensaService } from '../../core/services/defensa.service';
import { DocenteService } from '../../core/services/docente.service';
import { ProyectoService } from '../../core/services/proyecto.service';

@Component({
  selector: 'app-calendario-defensas',
  templateUrl: './calendario-defensas.component.html',
  styleUrl: './calendario-defensas.component.css',
})
export class CalendarioDefensasComponent implements OnInit {
  trimestreInicio = '2026-08-24';
  trimestreFin = '2026-11-30';

  calendarOptions: CalendarOptions = {
    plugins: [dayGridPlugin, interactionPlugin],
    initialView: 'catorceSemanas',
    initialDate: this.trimestreInicio,
    validRange: { start: this.trimestreInicio, end: this.trimestreFin },
    weekends: true,
    locale: 'es',
    monthStartFormat: { day: 'numeric' },
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'catorceSemanas',
    },
    views: {
      catorceSemanas: {
        type: 'dayGrid',
        duration: { weeks: 14 },
        buttonText: 'Trimestre',
      },
    },
    dateClick: (info: DateClickArg) => {
      this.onDayClick(info.dateStr);
    },
    events: [],
  };

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

  constructor(
    private defensaService: DefensaService,
    private docenteService: DocenteService,
    private proyectoService: ProyectoService,
  ) {}

  ngOnInit(): void {
    this.docenteService.getDocentes().subscribe((docentes: any[]) => {
      this.docentes = docentes;
    });
    this.proyectoService.getEscuelas().subscribe((escuelas: string[]) => {
      this.escuelas = escuelas;
    });
    this.cargarEventos();
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
        this.calendarOptions.events = filtradas.map((d: any) => ({
          title: d.proyecto?.titulo ?? 'Defensa',
          start: this.combinedDate(d.fecha, d.horaInicio),
          end: this.combinedDate(d.fecha, d.horaFin),
        }));
        if (this.selectedDate) {
          this.cargarDefensasDelDia();
        }
      });
  }

  onDayClick(dateStr: string): void {
    this.selectedDate = dateStr;
    this.cargarDefensasDelDia();
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

  private combinedDate(fecha: string, hora: string): string {
    return `${fecha}T${hora}`;
  }
}
