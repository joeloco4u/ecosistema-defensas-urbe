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
  franjasAgrupadas = new Map<string, SugerenciaHorario[]>();
  sugerenciaSeleccionada: SugerenciaHorario | null = null;
  franjaActual: SugerenciaHorario | null = null;
cargando = false;
  isSearching = false;

  docentes: any[] = [];
  espacios: any[] = [];
  juradoId: number | null = null;
  tutorAcademicoId: number | null = null;
  tutorMetodologicoId: number | null = null;
  espacioId = '';
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
      this.franjasAgrupadas = new Map<string, SugerenciaHorario[]>();
      this.sugerenciaSeleccionada = null;
      this.juradoId = this.juradoInicial;
      this.tutorAcademicoId = this.proyecto?.tutor?.id ?? this.tutorAcademicoInicial ?? null;
      this.tutorMetodologicoId = this.proyecto?.tutorMetodologico?.id ?? this.tutorMetodologicoInicial ?? null;
      this.espacioId = this.espacioInicial;
      this.franjaActual = this.fechaInicial && this.horaInicioInicial && this.horaFinInicial
        ? { fecha: this.fechaInicial, horaInicio: this.horaInicioInicial, horaFin: this.horaFinInicial, codigoAula: '' }
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
      if (d?.codigoInstitucional) {
        docentesPorCedula.set(d.codigoInstitucional.trim().toLowerCase(), d.id);
      }
    }
    this.juradosSugeridosIds = new Set(
      this.sugerenciasJurado
        .filter(s => s.estado === 'APROBADO' && s.cedula)
        .map(s => docentesPorCedula.get(s.cedula.trim().toLowerCase()))
        .filter((id): id is number => id != null)
    );
  }

  aulaActual(): string {
    return this.espacios.find(e => e.id === this.espacioId)?.codigoAula || '—';
  }

  nombreDocente(docenteId: number | null): string {
    if (docenteId == null) return 'Por asignar';
    const docente = this.docentes.find(d => d.id === docenteId);
    return docente?.nombreCompleto || 'Por asignar';
  }

  private getCedula(docenteId: number): string | undefined {
    return this.docentes.find(d => d.id === docenteId)?.codigoInstitucional;
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
    this.cargando = true;
    this.sugerencias = [];
    this.franjasAgrupadas = new Map<string, SugerenciaHorario[]>();
    this.sugerenciaSeleccionada = null;
    const cedulas = [
      this.getCedula(this.juradoId),
      this.getCedula(this.tutorAcademicoId),
      this.getCedula(this.tutorMetodologicoId),
    ].filter((c): c is string => !!c);
    this.programacionService.getSugerencias(cedulas, this.espacioId).subscribe({
      next: (data) => {
        this.sugerencias = data.map(item => ({
          fecha: item.fecha,
          horaInicio: item.horaInicio,
          horaFin: item.horaFin,
          codigoAula: item.codigoAula
        }));
        this.franjasAgrupadas = this.agruparPorFecha(this.sugerencias);
        this.cargando = false;
        this.isSearching = false;
      },
      error: () => {
        this.cargando = false;
        this.isSearching = false;
      }
    });
  }

  seleccionarSugerencia(s: SugerenciaHorario): void {
    this.sugerenciaSeleccionada = s;
  }

  private agruparPorFecha(franjas: SugerenciaHorario[]): Map<string, SugerenciaHorario[]> {
    const grupos = new Map<string, SugerenciaHorario[]>();
    for (const franja of franjas) {
      const fecha = franja.fecha;
      const grupo = grupos.get(fecha);
      if (grupo) {
        grupo.push(franja);
      } else {
        grupos.set(fecha, [franja]);
      }
    }
    return grupos;
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
