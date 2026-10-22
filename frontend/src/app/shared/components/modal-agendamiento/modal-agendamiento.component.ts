import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProgramacionService } from '../../../core/services/programacion.service';
import { DocenteService } from '../../../core/services/docente.service';
import { EspacioFisicoService } from '../../../core/services/espacio-fisico.service';
import { TutorSugeridoService } from '../../../core/services/tutor-sugerido.service';
import { TutorSugerido } from '../../../core/models/tutor-sugerido.model';

interface SugerenciaHorario {
  fecha: string;
  horaInicio: string;
  horaFin: string;
  codigoAula: string;
  espacioId?: string;
  juradoId?: number;
  tutorAcademicoId?: number;
  tutorMetodologicoId?: number;
}

type Turno = 'matutino' | 'vespertino' | 'nocturno';

interface GrupoTurno {
  turno: Turno;
  etiqueta: string;
  franjas: SugerenciaHorario[];
}

interface GrupoFecha {
  fecha: string;
  turnos: GrupoTurno[];
}

interface DocenteInfo {
  id?: number | null;
  nombreCompleto?: string;
}

interface EstudianteInfo {
  nombres?: string;
  apellidos?: string;
}

interface ProyectoInfo {
  id: string;
  tesista: string;
  titulo: string;
  expediente?: string | null;
  estudiante?: EstudianteInfo | null;
  estudiante2?: EstudianteInfo | null;
  estudiante3?: EstudianteInfo | null;
  tutor?: DocenteInfo | null;
  tutorMetodologico?: DocenteInfo | null;
}

@Component({
  selector: 'app-modal-agendamiento',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './modal-agendamiento.component.html',
  styleUrl: './modal-agendamiento.component.css'
})
export class ModalAgendamientoComponent implements OnChanges {
  @Input() proyecto: ProyectoInfo | null = null;
  @Input() isOpen = false;
  @Input() tituloModal = 'Sugerencias de Agendamiento';
  @Input() botonConfirmar = 'Confirmar Defensa';
  @Input() juradoInicial: number | null = null;
  @Input() tutorAcademicoInicial: number | null = null;
  @Input() tutorMetodologicoInicial: number | null = null;
  @Input() espacioInicial = '';
  @Input() fechaInicial = '';
  @Input() horaInicioInicial = '';
  @Input() horaFinInicial = '';
  @Output() closeModal = new EventEmitter<void>();
  @Output() confirmar = new EventEmitter<SugerenciaHorario>();

  sugerencias: SugerenciaHorario[] = [];
  gruposFranjas: GrupoFecha[] = [];
  sugerenciaSeleccionada: SugerenciaHorario | null = null;
  franjaActual: SugerenciaHorario | null = null;
cargando = false;
  isSearching = false;
  busquedaRealizada = false;

  docentes: any[] = [];
  espacios: any[] = [];
  juradoId: number | null = null;
  tutorAcademicoId: number | null = null;
  tutorMetodologicoId: number | null = null;
  espacioId = '';
  semanas: number[] = Array.from({ length: 14 }, (_, i) => i + 1);
  semanaSeleccionada: number = 12;
  diasSemana: { label: string; value: number | null }[] = [
    { label: 'Toda la semana', value: null },
    { label: 'Lunes', value: 1 },
    { label: 'Martes', value: 2 },
    { label: 'Miércoles', value: 3 },
    { label: 'Jueves', value: 4 },
    { label: 'Viernes', value: 5 },
    { label: 'Sábado', value: 6 },
  ];
  diaSeleccionado: number | null = null;
  sugerenciasJurado: TutorSugerido[] = [];
  juradosSugeridosIds = new Set<number>();

  constructor(
    private programacionService: ProgramacionService,
    private docenteService: DocenteService,
    private espacioFisicoService: EspacioFisicoService,
    private tutorSugeridoService: TutorSugeridoService
  ) {}

  get integrantesEquipo(): string[] {
    if (!this.proyecto) return [];
    const nombres = [this.proyecto.estudiante, this.proyecto.estudiante2, this.proyecto.estudiante3]
      .map((e) => [e?.nombres, e?.apellidos].filter(Boolean).join(' ').trim())
      .filter((nombre) => nombre.length > 0);
    if (nombres.length === 0 && this.proyecto.tesista && this.proyecto.tesista !== 'Por asignar') {
      return [this.proyecto.tesista];
    }
    return nombres;
  }

  get docentesSugeridos(): any[] {
    return this.docentes.filter(d => this.juradosSugeridosIds.has(d.id));
  }

  get docentesNoSugeridos(): any[] {
    return this.docentes.filter(d => !this.juradosSugeridosIds.has(d.id));
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['isOpen'] && this.isOpen) {
      this.sugerencias = [];
      this.gruposFranjas = [];
      this.sugerenciaSeleccionada = null;
      this.juradoId = this.juradoInicial;
      this.tutorAcademicoId = this.proyecto?.tutor?.id ?? this.tutorAcademicoInicial ?? null;
      this.tutorMetodologicoId = this.proyecto?.tutorMetodologico?.id ?? this.tutorMetodologicoInicial ?? null;
      this.espacioId = this.espacioInicial;
      this.franjaActual = this.fechaInicial && this.horaInicioInicial && this.horaFinInicial
        ? {
            fecha: this.fechaInicial,
            horaInicio: this.formatearHora(this.horaInicioInicial),
            horaFin: this.formatearHora(this.horaFinInicial),
            codigoAula: ''
          }
        : null;
      this.sugerenciaSeleccionada = this.franjaActual;
      this.cargando = true;
      this.sugerenciasJurado = [];
      this.juradosSugeridosIds = new Set<number>();
      this.docenteService.getDocentes().subscribe({
        next: (data) => {
          this.docentes = data;
          this.computarJuradosSugeridos();
        },
        error: () => { this.cargando = false; }
      });
      this.espacioFisicoService.getEspaciosFisicos().subscribe({
        next: (data) => { this.espacios = data; },
        error: () => { this.cargando = false; }
      });
      this.cargarSugerenciasJurado();
      this.cargando = false;
      this.isSearching = false;
      this.busquedaRealizada = false;
    }
  }

  private cargarSugerenciasJurado(): void {
    if (!this.proyecto?.id) return;
    this.tutorSugeridoService.obtenerPorProyecto(this.proyecto.id).subscribe({
      next: (sugerencias) => {
        this.sugerenciasJurado = sugerencias;
        this.computarJuradosSugeridos();
      },
    });
  }

  private computarJuradosSugeridos(): void {
    const docentesPorCedula = new Map<string, number>();
    for (const d of this.docentes) {
      const cedula = this.normalizarCedula(d?.cedula ?? d?.codigoInstitucional);
      if (cedula) {
        docentesPorCedula.set(cedula, d.id);
      }
    }
    this.juradosSugeridosIds = new Set(
      this.sugerenciasJurado
        .filter(s => s.estado === 'APROBADO' && s.cedula)
        .map(s => docentesPorCedula.get(this.normalizarCedula(s.cedula)))
        .filter((id): id is number => id != null)
    );
  }

  private normalizarCedula(valor: string | null | undefined): string {
    return String(valor ?? '').replace(/\D/g, '').replace(/^0+/, '');
  }

  aulaActual(): string {
    return this.espacios.find(e => e.id === this.espacioId)?.codigoAula || '—';
  }

  formatearFecha(fecha: string): string {
    if (!fecha) return '';
    const d = new Date(`${fecha}T00:00:00`);
    if (isNaN(d.getTime())) return fecha;
    const dia = d.toLocaleDateString('es-ES', { weekday: 'long' });
    const diaCapitalizado = dia.charAt(0).toUpperCase() + dia.slice(1);
    const pad = (n: number) => String(n).padStart(2, '0');
    const iso = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
    return `${diaCapitalizado}, ${iso}`;
  }

  formatearHora(hora: string | null | undefined): string {
    const valor = String(hora ?? '').trim();
    const coincidencia = valor.match(/^(\d{1,2}):(\d{2})/);
    if (!coincidencia) return valor;
    return `${coincidencia[1].padStart(2, '0')}:${coincidencia[2]}`;
  }

  nombreDocente(docenteId: number | null): string {
    if (docenteId == null) return 'Por asignar';
    const docente = this.docentes.find(d => d.id === docenteId);
    return docente?.nombreCompleto || 'Por asignar';
  }

  private getCedula(docenteId: number): string | undefined {
    const docente = this.docentes.find(d => d.id === docenteId);
    return docente?.cedula ?? docente?.codigoInstitucional;
  }

  isDocenteSeleccionado(docenteId: number, rolActual: string): boolean {
    if (rolActual !== 'jurado' && docenteId === this.juradoId) return true;
    if (rolActual !== 'tutorAcademico' && docenteId === this.tutorAcademicoId) return true;
    if (rolActual !== 'tutorMetodologico' && docenteId === this.tutorMetodologicoId) return true;
    return false;
  }

  buscarDisponibilidad(): void {
    if (!this.juradoId || !this.tutorAcademicoId || !this.tutorMetodologicoId || !this.espacioId) return;
    this.isSearching = true;
    this.busquedaRealizada = false;
    this.cargando = true;
    this.sugerencias = [];
    this.gruposFranjas = [];
    this.sugerenciaSeleccionada = null;
    const cedulas = [
      this.getCedula(this.juradoId),
      this.getCedula(this.tutorAcademicoId),
    ].filter((c): c is string => !!c);
    this.programacionService.getSugerencias(cedulas, this.espacioId, this.semanaSeleccionada, this.proyecto?.id, this.diaSeleccionado).subscribe({
      next: (data) => {
        this.sugerencias = data.map(item => ({
          fecha: item.fecha,
          horaInicio: this.formatearHora(item.horaInicio),
          horaFin: this.formatearHora(item.horaFin),
          codigoAula: item.codigoAula
        }));
        this.gruposFranjas = this.agruparPorFechaYTurno(this.sugerencias);
        this.cargando = false;
        this.isSearching = false;
        this.busquedaRealizada = true;
      },
      error: () => {
        this.cargando = false;
        this.isSearching = false;
        this.busquedaRealizada = true;
      }
    });
  }

  seleccionarSugerencia(s: SugerenciaHorario): void {
    this.sugerenciaSeleccionada = s;
  }

  private agruparPorFechaYTurno(franjas: SugerenciaHorario[]): GrupoFecha[] {
    const porFecha = new Map<string, SugerenciaHorario[]>();
    for (const franja of franjas) {
      const grupo = porFecha.get(franja.fecha);
      if (grupo) {
        grupo.push(franja);
      } else {
        porFecha.set(franja.fecha, [franja]);
      }
    }

    return Array.from(porFecha.entries())
      .sort(([fechaA], [fechaB]) => fechaA.localeCompare(fechaB))
      .map(([fecha, franjasFecha]) => {
        const turnos: GrupoTurno[] = [
          { turno: 'matutino', etiqueta: 'Matutino', franjas: [] },
          { turno: 'vespertino', etiqueta: 'Vespertino', franjas: [] },
          { turno: 'nocturno', etiqueta: 'Nocturno', franjas: [] },
        ];

        for (const franja of franjasFecha) {
          const minutos = this.minutosDelDia(franja.horaInicio);
          if (minutos < 12 * 60) {
            turnos[0].franjas.push(franja);
          } else if (minutos < 18 * 60) {
            turnos[1].franjas.push(franja);
          } else {
            turnos[2].franjas.push(franja);
          }
        }

        for (const turno of turnos) {
          turno.franjas.sort((a, b) => a.horaInicio.localeCompare(b.horaInicio));
        }

        return {
          fecha,
          turnos: turnos.filter((turno) => turno.franjas.length > 0),
        };
      });
  }

  private minutosDelDia(hora: string): number {
    const [horas, minutos] = hora.split(':').map(Number);
    return (horas || 0) * 60 + (minutos || 0);
  }

  cerrar(): void {
    this.sugerenciaSeleccionada = null;
    this.closeModal.emit();
  }

  confirmarDefensa(): void {
    if (this.sugerenciaSeleccionada) {
      this.sugerenciaSeleccionada.espacioId = this.espacioId;
      this.sugerenciaSeleccionada.juradoId = this.juradoId!;
      this.sugerenciaSeleccionada.tutorAcademicoId = this.tutorAcademicoId!;
      this.sugerenciaSeleccionada.tutorMetodologicoId = this.tutorMetodologicoId!;
      this.confirmar.emit(this.sugerenciaSeleccionada);
      this.cerrar();
    }
  }
}
