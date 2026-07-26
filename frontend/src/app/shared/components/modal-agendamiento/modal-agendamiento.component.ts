import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProgramacionService } from '../../../core/services/programacion.service';
import { DocenteService } from '../../../core/services/docente.service';
import { EspacioFisicoService } from '../../../core/services/espacio-fisico.service';

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

interface ProyectoInfo {
  id: string;
  tesista: string;
  titulo: string;
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
  @Output() closeModal = new EventEmitter<void>();
  @Output() confirmar = new EventEmitter<SugerenciaHorario>();

  sugerencias: SugerenciaHorario[] = [];
  sugerenciaSeleccionada: SugerenciaHorario | null = null;
  cargando = false;

  docentes: any[] = [];
  espacios: any[] = [];
  juradoId: number | null = null;
  tutorAcademicoId: number | null = null;
  tutorMetodologicoId: number | null = null;
  espacioId = '';

  constructor(
    private programacionService: ProgramacionService,
    private docenteService: DocenteService,
    private espacioFisicoService: EspacioFisicoService
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['isOpen'] && this.isOpen) {
      this.sugerencias = [];
      this.sugerenciaSeleccionada = null;
      this.juradoId = null;
      this.tutorAcademicoId = null;
      this.tutorMetodologicoId = null;
      this.espacioId = '';
      this.cargando = true;
      this.docenteService.getDocentes().subscribe({
        next: (data) => { this.docentes = data; },
        error: () => { this.cargando = false; }
      });
      this.espacioFisicoService.getEspaciosFisicos().subscribe({
        next: (data) => { this.espacios = data; },
        error: () => { this.cargando = false; }
      });
      this.cargando = false;
    }
  }

  private getCedula(docenteId: number): string | undefined {
    return this.docentes.find(d => d.id === docenteId)?.codigoInstitucional;
  }

  buscarDisponibilidad(): void {
    if (!this.juradoId || !this.tutorAcademicoId || !this.tutorMetodologicoId || !this.espacioId) return;
    this.cargando = true;
    this.sugerencias = [];
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
        this.cargando = false;
      },
      error: () => {
        this.cargando = false;
      }
    });
  }

  seleccionarSugerencia(s: SugerenciaHorario): void {
    this.sugerenciaSeleccionada = s;
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
