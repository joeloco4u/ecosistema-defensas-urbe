import { Component, OnInit } from '@angular/core';
import { CalendarOptions } from '@fullcalendar/core';
import dayGridPlugin from '@fullcalendar/daygrid';
import { DefensaService } from '../../core/services/defensa.service';
import { DocenteService } from '../../core/services/docente.service';
import { ProyectoService } from '../../core/services/proyecto.service';

@Component({
  selector: 'app-calendario-defensas',
  templateUrl: './calendario-defensas.component.html',
  styleUrl: './calendario-defensas.component.css',
})
export class CalendarioDefensasComponent implements OnInit {
  calendarOptions: CalendarOptions = {
    plugins: [dayGridPlugin],
    initialView: 'trimestre',
    weekends: true,
    locale: 'es',
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'trimestre',
    },
    views: {
      trimestre: {
        type: 'dayGrid',
        duration: { weeks: 14 },
        buttonText: 'Trimestre',
      },
    },
    events: [],
  };

  docentes: any[] = [];
  proyectos: any[] = [];
  escuelas: string[] = [];
  tutorIdSeleccionado: number | undefined;
  selectedProyectoId: string | undefined;
  selectedEscuela: string | undefined;

  constructor(
    private defensaService: DefensaService,
    private docenteService: DocenteService,
    private proyectoService: ProyectoService,
  ) {}

  ngOnInit(): void {
    this.docenteService.getDocentes().subscribe((docentes: any[]) => {
      this.docentes = docentes;
    });
    this.proyectoService.listarProyectos().subscribe((proyectos: any[]) => {
      this.proyectos = proyectos;
      this.escuelas = [...new Set(proyectos.map((p: any) => p.escuela).filter(Boolean))];
    });
    this.cargarEventos();
  }

  onFiltroCambiar(): void {
    this.cargarEventos();
  }

  private cargarEventos(): void {
    this.defensaService.listarDefensas(this.tutorIdSeleccionado, this.selectedProyectoId, this.selectedEscuela)
      .subscribe((defensas: any[]) => {
        this.calendarOptions.events = defensas.map((d: any) => ({
          title: d.proyecto?.titulo ?? 'Defensa',
          start: this.combinedDate(d.fecha, d.horaInicio),
          end: this.combinedDate(d.fecha, d.horaFin),
        }));
      });
  }

  private combinedDate(fecha: string, hora: string): string {
    return `${fecha}T${hora}`;
  }
}
