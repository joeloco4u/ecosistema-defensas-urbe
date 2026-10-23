import { Component, OnInit } from '@angular/core';
import { ProyectoService } from '../../core/services/proyecto.service';
import { DefensaService } from '../../core/services/defensa.service';

interface EstudianteInfo {
  cedula?: string;
  nombres?: string;
  apellidos?: string;
}

interface TutorInfo {
  nombreCompleto?: string;
}

interface ProyectoRegistro {
  id: string;
  titulo?: string;
  expediente?: string;
  escuela?: string;
  estudiante?: EstudianteInfo;
  estudiante2?: EstudianteInfo;
  estudiante3?: EstudianteInfo;
  tutor?: TutorInfo;
  tutorMetodologico?: TutorInfo;
  juradoNombre?: string;
  estatus: string;
}

@Component({
  selector: 'app-gestion-proyectos',
  templateUrl: './gestion-proyectos.component.html',
})
export class GestionProyectosComponent implements OnInit {
  mostrarModal = false;
  cargando = false;
  exportando = false;
  ejecutandoTransicion = false;

  proyectos: ProyectoRegistro[] = [];
  busqueda = '';
  filtroEstatus = 'TODOS';
  filtroEscuela = 'Todas';

  estatusDisponibles = ['TODOS', 'PENDIENTE', 'AGENDADO', 'DEFENDIDO'];
  escuelas: string[] = [];

  proyectoSeleccionado: any = null;
  mostrarModalExpediente = false;

  modalReagendarAbierto = false;
  reagendarDefensa: any = null;

  modalAgendarAbierto = false;
  proyectoAgendar: ProyectoRegistro | null = null;

  constructor(
    private proyectoService: ProyectoService,
    private defensaService: DefensaService,
  ) {}

  ngOnInit(): void {
    this.cargarProyectos();
    this.proyectoService.getEscuelas().subscribe((escuelas: string[]) => {
      this.escuelas = escuelas;
    });
  }

  cargarProyectos(): void {
    this.cargando = true;
    this.proyectoService.listarProyectos().subscribe({
      next: (proyectos) => {
        this.proyectos = proyectos;
        this.cargando = false;
      },
      error: () => {
        this.cargando = false;
      },
    });
  }

  exportarBackup(): void {
    this.exportando = true;
    this.proyectoService.descargarBackupCsv().subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'backup_proyectos_urbe.csv';
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
        this.exportando = false;
      },
      error: (err) => {
        console.error('Error al exportar el backup', err);
        this.exportando = false;
      },
    });
  }

  get proyectosFiltrados(): ProyectoRegistro[] {
    const termino = this.busqueda.trim().toLowerCase();
    return this.proyectos.filter((p) => {
      if (this.filtroEstatus !== 'TODOS' && p.estatus !== this.filtroEstatus) return false;
      if (this.filtroEscuela !== 'Todas' && p.escuela !== this.filtroEscuela) return false;
      if (!termino) return true;
      const expediente = (p.expediente ?? '').toLowerCase();
      const titulo = (p.titulo ?? '').toLowerCase();
      const tutor = (p.tutor?.nombreCompleto ?? '').toLowerCase();
      return expediente.includes(termino) || titulo.includes(termino) || tutor.includes(termino);
    });
  }

  aplicarFiltros(): void {
    return;
  }

  ejecutarTransicion(): void {
    const confirmado = window.confirm(
      '¿Está seguro? Esto archivará Seminario 3 y promoverá a los alumnos de Seminario 1 y 2. Esta acción no se puede deshacer.',
    );
    if (!confirmado) return;

    this.ejecutandoTransicion = true;
    this.proyectoService.transicionSeminarios().subscribe({
      next: () => {
        this.ejecutandoTransicion = false;
        window.alert('Transición de seminarios ejecutada exitosamente.');
      },
      error: () => {
        this.ejecutandoTransicion = false;
        window.alert('No se pudo ejecutar la transición de seminarios.');
      },
    });
  }

  abrirExpediente(proyecto: any): void {
    this.proyectoSeleccionado = proyecto;
    this.mostrarModalExpediente = true;
  }

  cerrarExpediente(): void {
    this.mostrarModalExpediente = false;
    this.proyectoSeleccionado = null;
  }

  abrirModalAgendar(proyecto: ProyectoRegistro): void {
    this.proyectoAgendar = proyecto;
    this.modalAgendarAbierto = true;
  }

  cerrarModalAgendar(): void {
    this.modalAgendarAbierto = false;
    this.proyectoAgendar = null;
  }

  infoProyectoAgendar(): any {
    const proyecto = this.proyectoAgendar;
    if (!proyecto) return null;

    return {
      id: proyecto.id,
      tesista: this.nombreEstudiante(proyecto.estudiante),
      titulo: proyecto.titulo || 'Sin título',
      expediente: proyecto.expediente ?? null,
      estudiante: proyecto.estudiante ?? null,
      estudiante2: proyecto.estudiante2 ?? null,
      estudiante3: proyecto.estudiante3 ?? null,
      tutor: proyecto.tutor ?? null,
      tutorMetodologico: proyecto.tutorMetodologico ?? null,
    };
  }

  onConfirmarAgendar(evento: any): void {
    if (!this.proyectoAgendar) return;

    const body = {
      proyectoId: this.proyectoAgendar.id,
      espacioId: evento.espacioId,
      fecha: evento.fecha,
      horaInicio: evento.horaInicio.length === 5 ? `${evento.horaInicio}:00` : evento.horaInicio,
      horaFin: evento.horaFin.length === 5 ? `${evento.horaFin}:00` : evento.horaFin,
      tutorAcademicoId: evento.tutorAcademicoId,
      tutorMetodologicoId: evento.tutorMetodologicoId,
      juradoId: evento.juradoId,
    };

    this.defensaService.crearDefensa(body).subscribe({
      next: () => {
        window.alert('Defensa agendada exitosamente.');
        this.cerrarModalAgendar();
        this.cargarProyectos();
      },
      error: (err) => {
        console.error('Error al agendar la defensa', err);
        window.alert('No se pudo agendar la defensa: ' + err.message);
      },
    });
  }

  abrirModalReagendar(proyecto: any): void {
    this.defensaService.listarDefensas(undefined, proyecto.id).subscribe({
      next: (defensas) => {
        const defensa = defensas && defensas.length > 0 ? defensas[0] : null;
        if (!defensa) {
          window.alert('Este proyecto está marcado como AGENDADO, pero no se encontró la defensa asociada.');
          return;
        }
        this.reagendarDefensa = defensa;
        this.modalReagendarAbierto = true;
      },
      error: (err) => {
        console.error('Error al cargar la defensa a reagendar', err);
        window.alert('No se pudo cargar la defensa del proyecto.');
      },
    });
  }

  cerrarModalReagendar(): void {
    this.modalReagendarAbierto = false;
    this.reagendarDefensa = null;
  }

  infoProyectoDefensa(): any {
    const proyecto = this.reagendarDefensa?.proyecto;
    const estudiante = proyecto?.estudiante;
    const tesista = [estudiante?.nombres, estudiante?.apellidos].filter(Boolean).join(' ') || 'Por asignar';
    return {
      id: proyecto?.id,
      tesista,
      titulo: proyecto?.titulo || 'Sin título',
      expediente: proyecto?.expediente ?? null,
      estudiante: proyecto?.estudiante ?? null,
      estudiante2: proyecto?.estudiante2 ?? null,
      estudiante3: proyecto?.estudiante3 ?? null,
      tutor: proyecto?.tutor ?? null,
      tutorMetodologico: proyecto?.tutorMetodologico ?? null,
    };
  }

  cancelarDefensa(proyecto: ProyectoRegistro): void {
    const confirmado = window.confirm(
      `¿Cancelar la defensa agendada del proyecto ${proyecto.expediente || proyecto.id}? El proyecto volverá al estado PENDIENTE.`,
    );
    if (!confirmado) return;

    this.defensaService.cancelarDefensaPorProyecto(proyecto.id).subscribe({
      next: () => this.cargarProyectos(),
      error: (err) => {
        console.error('Error al cancelar la defensa', err);
        window.alert('No se pudo cancelar la defensa del proyecto.');
      },
    });
  }

  onConfirmarReagendar(evento: any): void {
    if (!this.reagendarDefensa) return;

    const juradosIds = [evento.juradoId, evento.tutorAcademicoId, evento.tutorMetodologicoId]
      .filter((id: any) => id != null);

    const body = {
      espacioId: evento.espacioId,
      fecha: evento.fecha,
      horaInicio: evento.horaInicio.length === 5 ? `${evento.horaInicio}:00` : evento.horaInicio,
      horaFin: evento.horaFin.length === 5 ? `${evento.horaFin}:00` : evento.horaFin,
      juradosIds,
    };

    this.defensaService.reprogramarDefensa(this.reagendarDefensa.id, body).subscribe({
      next: () => {
        window.alert('Defensa reagendada exitosamente.');
        this.cerrarModalReagendar();
        this.cargarProyectos();
      },
      error: (err) => {
        console.error('Error al reagendar la defensa', err);
        window.alert('No se pudo reagendar la defensa: ' + err.message);
      },
    });
  }

  nombreEstudiante(e?: EstudianteInfo | null): string {
    return [e?.nombres, e?.apellidos].filter(Boolean).join(' ') || 'Por asignar';
  }

  integrantesEquipo(p: ProyectoRegistro): EstudianteInfo[] {
    return [p.estudiante, p.estudiante2, p.estudiante3]
      .filter((e): e is EstudianteInfo => !!e && !!this.nombreEstudiante(e) && this.nombreEstudiante(e) !== 'Por asignar');
  }

  estatusClase(estatus: string): string {
    switch (estatus) {
      case 'PENDIENTE': return 'bg-amber-100 text-amber-800';
      case 'AGENDADO': return 'bg-blue-100 text-blue-800';
      case 'DEFENDIDO': return 'bg-emerald-100 text-emerald-800';
      default: return 'bg-gray-100 text-gray-700';
    }
  }
}