import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import PizZip from 'pizzip';
import Docxtemplater from 'docxtemplater';
import { saveAs } from 'file-saver';
import { ReporteService, DefensaDTO, TesistaInfo } from '../../core/services/reporte.service';
import { DocenteService } from '../../core/services/docente.service';
import { ProyectoService } from '../../core/services/proyecto.service';

@Component({
  selector: 'app-documentos',
  templateUrl: './documentos.component.html',
})
export class DocumentosComponent implements OnInit {

  todas: DefensaDTO[] = [];
  seleccionada: DefensaDTO | null = null;
  modoImpresion: 'veredicto' | 'entrega' | 'planificacion' = 'veredicto';

  escuelas: string[] = [];
  escuelaSeleccionada = 'Todas';
  defensasEscuela: DefensaDTO[] = [];

  cargando = true;
  error = '';

  private docentesPorId = new Map<number, { nombre: string; cedula: string }>();

  constructor(
    private reporteService: ReporteService,
    private docenteService: DocenteService,
    private proyectoService: ProyectoService,
    private http: HttpClient,
  ) {}

  ngOnInit(): void {
    this.docenteService.getDocentes().subscribe({
      next: (docentes) => {
        this.docentesPorId = new Map(
          (docentes ?? []).map(
            (d: any) =>
              [
                d.id as number,
                { nombre: d.nombreCompleto ?? '', cedula: d.cedula ?? d.codigoInstitucional ?? '' },
              ] as [number, { nombre: string; cedula: string }],
          ),
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
    this.cargarDefensas();
  }

  onEscuelaChange(): void {
    this.aplicarFiltroEscuela();
  }

  escuelaDe(defensa: DefensaDTO): string {
    return defensa.proyecto?.escuela || 'Sin escuela';
  }

  private aplicarFiltroEscuela(): void {
    if (!this.escuelaSeleccionada || this.escuelaSeleccionada === 'Todas') {
      this.defensasEscuela = [];
      return;
    }
    this.defensasEscuela = this.todas
      .filter((d) => this.escuelaDe(d) === this.escuelaSeleccionada)
      .sort((a, b) =>
        (a.fecha ?? '').localeCompare(b.fecha ?? '')
        || this.formatHora(a.horaInicio).localeCompare(this.formatHora(b.horaInicio)),
      );
  }

  seleccionar(defensa: DefensaDTO): void {
    this.modoImpresion = 'veredicto';
    this.seleccionada = defensa;
  }

  seleccionarEntrega(defensa: DefensaDTO): void {
    this.modoImpresion = 'entrega';
    this.seleccionada = defensa;
  }

  verPlanificacion(): void {
    this.modoImpresion = 'planificacion';
  }

  get proyectosPlanificacion(): DefensaDTO[] {
    return this.escuelaSeleccionada && this.escuelaSeleccionada !== 'Todas'
      ? this.defensasEscuela
      : this.todas;
  }

  imprimirVeredicto(): void {
    if (!this.seleccionada) return;
    window.print();
  }

  descargarWord(def: DefensaDTO): void {
    this.http.get('assets/template-veredicto.docx', { responseType: 'arraybuffer' }).subscribe({
      next: (contenido) => {
        const proyecto = def.proyecto;
        const estudiantes: TesistaInfo[] = [
          proyecto?.estudiante,
          proyecto?.estudiante2,
          proyecto?.estudiante3,
        ].filter((e): e is TesistaInfo => !!e);

        const alumnos = estudiantes
          .map((e) => [e.nombres, e.apellidos].filter(Boolean).join(' ').trim())
          .filter((nombre) => nombre.length > 0)
          .join(', ') || '________________';

        const cedulas = estudiantes
          .map((e) => (e.cedula ? e.cedula.replace(/[^0-9]/g, '') : '________________'))
          .join(', V-') || '________________';

        const data = {
          titulo: proyecto?.titulo || '________________',
          alumnos,
          cedulas,
          tutor_ac: this.nombreTutorAcademico(def) || '________________',
          tutor_met: this.nombreTutorMetodologico(def) || '________________',
          jurado: this.nombreJurado(def) || '________________',
          escuela: proyecto?.escuela || 'INFORMÁTICA',
          fecha_firma: this.fechaFirmaWord(def.fecha),
          seccion: '________',
          expediente: proyecto?.expediente || '________',
        };

        const zip = new PizZip(contenido);
        const doc = new Docxtemplater(zip, {
          paragraphLoop: true,
          linebreaks: true,
          delimiters: { start: '{{', end: '}}' },
        });
        doc.render(data);

        const blob = doc.getZip().generate({
          type: 'blob',
          mimeType: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        });
        saveAs(blob, `Veredicto_${proyecto?.expediente || 'Sin_Expediente'}.docx`);
      },
      error: (err) => console.error('Error al generar el documento Word', err),
    });
  }

  private fechaFirmaWord(fecha?: string): string {
    const date = this.aFechaLocal(fecha) ?? new Date();
    return new Intl.DateTimeFormat('es-VE', {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    }).format(date);
  }

  imprimirPreDefensa(): void {
    const escuela = encodeURIComponent(this.escuelaSeleccionada);
    window.open(`/imprimir/pre-defensa/${escuela}`, '_blank');
  }

  tesistas(d: DefensaDTO): string {
    const p = d.proyecto;
    const nombres = [p?.estudiante, p?.estudiante2, p?.estudiante3]
      .map((e) => [e?.nombres, e?.apellidos].filter(Boolean).join(' ').trim())
      .filter((nombre) => nombre.length > 0);
    return nombres.length > 0 ? nombres.join(', ') : 'Por asignar';
  }

  filasEstudiantes(d: DefensaDTO): { nombre: string; cedula: string }[] {
    const p = d.proyecto;
    const filas = [p?.estudiante, p?.estudiante2, p?.estudiante3]
      .map((e) => ({
        nombre: [e?.nombres, e?.apellidos].filter(Boolean).join(' ').trim(),
        cedula: e?.cedula ? this.formatearCedula(e.cedula) : '_________',
      }))
      .filter((fila) => fila.nombre.length > 0);
    return filas.length > 0 ? filas : [{ nombre: 'Por asignar', cedula: '_________' }];
  }

  nombreDocente(id?: number | null): string {
    if (id === null || id === undefined) return 'Por asignar';
    return this.docentesPorId.get(id)?.nombre ?? `Docente #${id}`;
  }

  cedulaDocente(id?: number | null): string {
    if (id === null || id === undefined) return '____________';
    return this.formatearCedula(this.docentesPorId.get(id)?.cedula);
  }

  cedulas(d: DefensaDTO): string {
    const p = d.proyecto;
    const cedulas = [p?.estudiante, p?.estudiante2, p?.estudiante3]
      .map((e) => e?.cedula)
      .filter((c): c is string => !!c && c.trim().length > 0)
      .map((c) => this.formatearCedula(c));
    return cedulas.length > 0 ? cedulas.join(' y ') : '_________';
  }

  escuelaTitulo(d: DefensaDTO): string {
    return this.partesEscuela(d).escuela;
  }

  mencion(d: DefensaDTO): string {
    return this.partesEscuela(d).mencion;
  }

  tutorAcademico(d: DefensaDTO): string {
    return d.proyecto?.tutor?.nombreCompleto || this.nombreDocente(d.tutorAcademicoId);
  }

  tutorMetodologico(d: DefensaDTO): string {
    return d.proyecto?.tutorMetodologico?.nombreCompleto || this.nombreDocente(d.tutorMetodologicoId);
  }

  nombreTutorAcademico(d: DefensaDTO): string {
    return this.nombreDeDocente(d.tutorAcademicoId, d.proyecto?.tutor?.nombreCompleto);
  }

  nombreTutorMetodologico(d: DefensaDTO): string {
    return this.nombreDeDocente(d.tutorMetodologicoId, d.proyecto?.tutorMetodologico?.nombreCompleto);
  }

  nombreJurado(d: DefensaDTO): string {
    return this.nombreDeDocente(d.juradoId, d.proyecto?.jurado?.nombreCompleto || d.proyecto?.juradoNombre);
  }

  cedulaTutorAcademico(d: DefensaDTO): string {
    return this.cedulaDeDocente(d.tutorAcademicoId, d.proyecto?.tutor?.cedula);
  }

  cedulaTutorMetodologico(d: DefensaDTO): string {
    return this.cedulaDeDocente(d.tutorMetodologicoId, d.proyecto?.tutorMetodologico?.cedula);
  }

  cedulaJurado(d: DefensaDTO): string {
    return this.cedulaDeDocente(d.juradoId, d.proyecto?.jurado?.cedula);
  }

  private nombreDeDocente(id?: number | null, nombreDirecto?: string | null): string {
    return (nombreDirecto || (id != null ? this.docentesPorId.get(id)?.nombre : '') || '').trim();
  }

  private cedulaDeDocente(id?: number | null, cedulaDirecta?: string | null): string {
    const cedula = cedulaDirecta || (id != null ? this.docentesPorId.get(id)?.cedula : '');
    return (cedula || '').replace(/[^0-9]/g, '');
  }

  fechaLarga(fecha?: string): string {
    const d = this.aFechaLocal(fecha);
    if (!d) return '____ de ____________ de ______';
    const mes = d.toLocaleDateString('es-ES', { month: 'long' });
    const mesCapitalizado = mes.charAt(0).toUpperCase() + mes.slice(1);
    return `${d.getDate()} de ${mesCapitalizado} de ${d.getFullYear()}`;
  }

  fechaPartes(fecha?: string): { dia: string; mes: string; anio: string } {
    const d = this.aFechaLocal(fecha);
    if (!d) return { dia: '—', mes: '—', anio: '—' };
    return {
      dia: String(d.getDate()),
      mes: d.toLocaleDateString('es-ES', { month: 'long' }),
      anio: String(d.getFullYear()),
    };
  }

  private partesEscuela(d: DefensaDTO): { escuela: string; mencion: string } {
    const raw = (d.proyecto?.escuela || '—').trim();
    const partes = raw.split(/\s+MENCI[ÓO]N\s+/i);
    if (partes.length > 1) {
      return { escuela: partes[0].trim(), mencion: partes.slice(1).join(' MENCIÓN ').trim() };
    }
    return { escuela: raw, mencion: raw };
  }

  private formatearCedula(cedula?: string | null): string {
    if (!cedula) return '____________';
    const digitos = cedula.replace(/[^0-9]/g, '');
    return digitos.length > 0 ? `V-${digitos}` : cedula.trim();
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
        this.aplicarFiltroEscuela();
        this.cargando = false;
      },
      error: (err) => {
        console.error('Error al cargar las defensas', err);
        this.error = 'No se pudieron cargar las defensas.';
        this.cargando = false;
      },
    });
  }

  private aFechaLocal(fecha?: string): Date | null {
    if (!fecha) return null;
    const d = new Date(`${fecha}T00:00:00`);
    return isNaN(d.getTime()) ? null : d;
  }
}
