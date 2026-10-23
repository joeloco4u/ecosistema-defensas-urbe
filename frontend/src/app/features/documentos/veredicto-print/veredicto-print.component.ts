import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ProyectoService } from '../../../core/services/proyecto.service';
import { DefensaService } from '../../../core/services/defensa.service';

interface TesistaInfo {
  cedula?: string;
  nombres?: string;
  apellidos?: string;
}

interface DocenteInfo {
  nombreCompleto?: string;
}

interface ProyectoVeredicto {
  id: string;
  titulo?: string;
  expediente?: string;
  escuela?: string;
  estudiante?: TesistaInfo | null;
  estudiante2?: TesistaInfo | null;
  estudiante3?: TesistaInfo | null;
  tutor?: DocenteInfo | null;
  tutorMetodologico?: DocenteInfo | null;
  juradoNombre?: string | null;
}

interface DefensaVeredicto {
  id: string;
  fecha?: string;
}

interface TesistaVeredicto {
  nombre: string;
  cedula: string;
}

@Component({
  selector: 'app-veredicto-print',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './veredicto-print.component.html',
})
export class VeredictoPrintComponent implements OnInit {

  cargando = true;
  error = '';
  proyecto: ProyectoVeredicto | null = null;
  defensa: DefensaVeredicto | null = null;

  private readonly meses = [
    'enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio',
    'julio', 'agosto', 'septiembre', 'octubre', 'noviembre', 'diciembre',
  ];

  constructor(
    private route: ActivatedRoute,
    private proyectoService: ProyectoService,
    private defensaService: DefensaService,
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.error = 'No se recibió el identificador del proyecto.';
      this.cargando = false;
      return;
    }

    forkJoin({
      proyecto: this.proyectoService.obtenerProyecto(id),
      defensas: this.defensaService.listarDefensas(undefined, id),
    }).subscribe({
      next: ({ proyecto, defensas }) => {
        this.proyecto = proyecto;
        this.defensa = defensas && defensas.length > 0 ? defensas[0] : null;
        this.cargando = false;

        if (!this.defensa) {
          this.error = 'No se encontró una defensa agendada para este proyecto.';
          return;
        }

        setTimeout(() => window.print(), 500);
      },
      error: (err) => {
        console.error('Error al cargar el veredicto', err);
        this.error = 'No se pudo cargar la información del veredicto.';
        this.cargando = false;
      },
    });
  }

  get tesistas(): TesistaVeredicto[] {
    if (!this.proyecto) return [];

    const filas = [
      this.proyecto.estudiante,
      this.proyecto.estudiante2,
      this.proyecto.estudiante3,
    ]
      .map((estudiante) => ({
        nombre: [estudiante?.nombres, estudiante?.apellidos]
          .filter(Boolean)
          .join(' ')
          .trim(),
        cedula: this.formatearCedula(estudiante?.cedula),
      }))
      .filter((fila) => fila.nombre.length > 0);

    return filas.length > 0
      ? filas
      : [{ nombre: 'Por asignar', cedula: '__________' }];
  }

  get tutorAcademico(): string {
    return this.proyecto?.tutor?.nombreCompleto || 'Por asignar';
  }

  get tutorMetodologico(): string {
    return this.proyecto?.tutorMetodologico?.nombreCompleto || 'Por asignar';
  }

  get jurado(): string {
    return this.proyecto?.juradoNombre || 'Por asignar';
  }

  get escuela(): string {
    return this.proyecto?.escuela || 'Ingeniería';
  }

  get fechaDefensa(): string {
    const fecha = this.defensa?.fecha;
    if (!fecha) return '____ de ____________ de ______';

    const [anio, mes, dia] = fecha.split('-').map(Number);
    if (!anio || !mes || !dia) return fecha;

    return `${dia} de ${this.meses[mes - 1]} de ${anio}`;
  }

  private formatearCedula(cedula?: string | null): string {
    if (!cedula) return '__________';
    const digitos = cedula.replace(/[^0-9]/g, '');
    return digitos.length > 0 ? `V-${digitos}` : cedula.trim();
  }
}
