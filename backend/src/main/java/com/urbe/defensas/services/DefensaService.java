package com.urbe.defensas.services;

import com.urbe.defensas.dtos.RegistroDefensaDTO;
import com.urbe.defensas.dtos.ReprogramacionDTO;
import com.urbe.defensas.dtos.ReporteDefensaDTO;
import com.urbe.defensas.exceptions.ConflictException;
import com.urbe.defensas.models.Defensa;
import com.urbe.defensas.models.Docente;
import com.urbe.defensas.models.EspacioFisico;
import com.urbe.defensas.models.Estudiante;
import com.urbe.defensas.models.JuradoDefensa;
import com.urbe.defensas.models.Proyecto;
import com.urbe.defensas.repositories.DefensaRepository;
import com.urbe.defensas.repositories.DocenteRepository;
import com.urbe.defensas.repositories.EspacioFisicoRepository;
import com.urbe.defensas.repositories.JuradoDefensaRepository;
import com.urbe.defensas.repositories.ProyectoRepository;
import com.urbe.defensas.util.DiasFeriados;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class DefensaService {

    private static final LocalTime HORA_MAXIMA_FIN_DEFENSA = LocalTime.of(21, 0);

    private final DefensaRepository defensaRepository;
    private final ProyectoRepository proyectoRepository;
    private final EspacioFisicoRepository espacioFisicoRepository;
    private final DocenteRepository docenteRepository;
    private final JuradoDefensaRepository juradoDefensaRepository;
    private final DisponibilidadService disponibilidadService;

    public DefensaService(DefensaRepository defensaRepository,
                          ProyectoRepository proyectoRepository,
                          EspacioFisicoRepository espacioFisicoRepository,
                          DocenteRepository docenteRepository,
                          JuradoDefensaRepository juradoDefensaRepository,
                          DisponibilidadService disponibilidadService) {
        this.defensaRepository = defensaRepository;
        this.proyectoRepository = proyectoRepository;
        this.espacioFisicoRepository = espacioFisicoRepository;
        this.docenteRepository = docenteRepository;
        this.juradoDefensaRepository = juradoDefensaRepository;
        this.disponibilidadService = disponibilidadService;
    }

    public Defensa programar(RegistroDefensaDTO dto) {
        validarFranja(dto.getFecha(), dto.getHoraInicio(), dto.getHoraFin());
        validarFechaProgramable(dto.getFecha());

        List<Long> docentesIds = Arrays.asList(
                dto.getJuradoId(), dto.getTutorAcademicoId(), dto.getTutorMetodologicoId());
        validarDocentesParticipantes(docentesIds);

        Proyecto proyecto = proyectoRepository.findById(dto.getProyectoId())
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
        // Bloqueo pesimista del aula: serializa las reservas concurrentes del mismo espacio.
        EspacioFisico espacio = espacioFisicoRepository.findByIdForUpdate(dto.getEspacioId())
                .orElseThrow(() -> new RuntimeException("Espacio físico no encontrado"));

        List<UUID> estudianteIds = idsEstudiantesDe(proyecto);
        // El Tutor Metodológico ya no debe estar presente: no restringe la franja.
        List<Long> docentesDisponibilidad = Arrays.asList(
                dto.getJuradoId(), dto.getTutorAcademicoId());

        validarDisponibilidad(dto.getFecha(), dto.getEspacioId(), docentesDisponibilidad, estudianteIds,
                dto.getHoraInicio(), dto.getHoraFin(), null);

        Defensa defensa = new Defensa();
        defensa.setProyecto(proyecto);
        defensa.setEspacioFisico(espacio);
        defensa.setFecha(dto.getFecha());
        defensa.setHoraInicio(dto.getHoraInicio());
        defensa.setHoraFin(dto.getHoraFin());
        defensa.setTutorAcademicoId(dto.getTutorAcademicoId());
        defensa.setTutorMetodologicoId(dto.getTutorMetodologicoId());
        defensa.setJuradoId(dto.getJuradoId());
        defensa.setEstatus(Defensa.EstatusDefensa.PROGRAMADA);

        Defensa guardada = defensaRepository.save(defensa);
        sincronizarJurados(guardada,
                Arrays.asList(dto.getJuradoId(), dto.getTutorAcademicoId(), dto.getTutorMetodologicoId()));

        proyecto.setEstatus(Proyecto.EstatusProyecto.AGENDADO);
        proyectoRepository.save(proyecto);

        return guardada;
    }

    public Defensa reprogramar(UUID id, ReprogramacionDTO dto) {
        Defensa existente = defensaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Defensa no encontrada"));

        validarFranja(dto.getFecha(), dto.getHoraInicio(), dto.getHoraFin());
        validarFechaProgramable(dto.getFecha());

        List<Long> docentesIds = dto.getJuradosIds() != null && !dto.getJuradosIds().isEmpty()
                ? dto.getJuradosIds()
                : docentesActuales(existente);
        validarDocentesParticipantes(docentesIds);

        // Bloqueo pesimista del aula destino antes de validar y reprogramar.
        EspacioFisico espacio = espacioFisicoRepository.findByIdForUpdate(dto.getEspacioId())
                .orElseThrow(() -> new RuntimeException("Espacio físico no encontrado"));

        List<UUID> estudianteIds = idsEstudiantesDe(existente.getProyecto());
        // El Tutor Metodológico no participa en la validación de disponibilidad.
        List<Long> docentesDisponibilidad = docentesIds.subList(0, 2);

        validarDisponibilidad(dto.getFecha(), dto.getEspacioId(), docentesDisponibilidad, estudianteIds,
                dto.getHoraInicio(), dto.getHoraFin(), id);

        existente.setFecha(dto.getFecha());
        existente.setHoraInicio(dto.getHoraInicio());
        existente.setHoraFin(dto.getHoraFin());
        existente.setEspacioFisico(espacio);

        existente.setJuradoId(valorEn(docentesIds, 0));
        existente.setTutorAcademicoId(valorEn(docentesIds, 1));
        existente.setTutorMetodologicoId(valorEn(docentesIds, 2));
        existente.setEstatus(Defensa.EstatusDefensa.REPROGRAMADA);

        Defensa actualizada = defensaRepository.save(existente);
        sincronizarJurados(actualizada, docentesIds);

        return actualizada;
    }

    public Defensa confirmar(UUID id) {
        Defensa existente = defensaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Defensa no encontrada"));
        existente.setEstatus(Defensa.EstatusDefensa.FINALIZADA);
        return defensaRepository.save(existente);
    }

    public void cancelar(UUID id) {
        Defensa existente = defensaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Defensa no encontrada"));

        Proyecto proyecto = existente.getProyecto();
        juradoDefensaRepository.deleteByDefensaId(id);
        defensaRepository.delete(existente);

        if (proyecto != null) {
            proyecto.setEstatus(Proyecto.EstatusProyecto.PENDIENTE);
            proyectoRepository.save(proyecto);
        }
    }

    public void cancelarPorProyecto(UUID proyectoId) {
        Defensa defensa = defensaRepository.findByProyectoId(proyectoId).stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró una defensa agendada para el proyecto: " + proyectoId));
        cancelar(defensa.getId());
    }

    public Defensa obtenerPorId(UUID id) {
        return defensaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Defensa no encontrada"));
    }

    public List<Defensa> listarConFiltros(Long tutorId, UUID proyectoId, String escuela) {
        return defensaRepository.buscarConFiltros(tutorId, proyectoId, escuela);
    }

    public List<ReporteDefensaDTO> generarReporteDiario(LocalDate fecha) {
        return defensaRepository.findByFechaOrderByHoraInicioAsc(fecha).stream()
                .map(this::mapearReporte)
                .toList();
    }

    private void validarDisponibilidad(LocalDate fecha, UUID espacioId, List<Long> docentesIds,
                                       List<UUID> estudianteIds, LocalTime horaInicio, LocalTime horaFin,
                                       UUID excludedId) {
        List<Long> docentesSeguros = docentesIds != null ? docentesIds : List.of();
        List<UUID> estudiantesSeguros = estudianteIds != null ? estudianteIds : List.of();

        DisponibilidadService.Disponibilidad disponibilidad = disponibilidadService.calcular(
                fecha, fecha, espacioId, docentesSeguros, estudiantesSeguros,
                LocalTime.MIN, LocalTime.MAX, excludedId);

        if (!disponibilidad.estaLibre(fecha, espacioId, horaInicio, horaFin)) {
            throw new ConflictException(
                    "Conflicto de horario: el docente, el estudiante o el espacio ya están ocupados "
                            + "por clases u otra defensa el " + fecha
                            + " de " + horaInicio + " a " + horaFin + ".");
        }
    }

    private void validarFranja(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        if (fecha == null || horaInicio == null || horaFin == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La fecha y las horas de la defensa son obligatorias");
        }
        if (!horaInicio.isBefore(horaFin)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La hora de inicio debe ser anterior a la hora de fin");
        }
        if (!horaInicio.isBefore(HORA_MAXIMA_FIN_DEFENSA)
                || horaFin.isAfter(HORA_MAXIMA_FIN_DEFENSA)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La defensa debe finalizar a más tardar a las 21:00");
        }
    }

    private void validarFechaProgramable(LocalDate fecha) {
        if (fecha.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "No se puede programar una defensa en una fecha pasada");
        }
        if (DiasFeriados.esFeriado(fecha)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "No se puede programar una defensa en un día feriado");
        }
    }

    private void validarDocentesParticipantes(List<Long> docentesIds) {
        if (docentesIds == null || docentesIds.size() != 3 || docentesIds.contains(null)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La defensa requiere exactamente tres docentes: jurado, tutor académico y tutor metodológico");
        }
        Set<Long> distintos = new HashSet<>(docentesIds);
        if (distintos.size() != docentesIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Los docentes participantes no pueden repetirse");
        }
        // Bloqueo pesimista determinista de los participantes: serializa defensas
        // concurrentes que compartan docentes, incluso si usan aulas distintas.
        List<Docente> docentes = docenteRepository.findByIdInForUpdate(distintos);
        if (docentes.size() != distintos.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Uno o más docentes participantes no existen en el sistema");
        }
        for (Docente docente : docentes) {
            if (!Boolean.TRUE.equals(docente.getActivo())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El docente " + docente.getNombreCompleto() + " está inactivo y no puede participar");
            }
        }
    }

    private List<Long> docentesActuales(Defensa defensa) {
        List<Long> ids = new ArrayList<>();
        if (defensa.getJuradoId() != null) ids.add(defensa.getJuradoId());
        if (defensa.getTutorAcademicoId() != null) ids.add(defensa.getTutorAcademicoId());
        if (defensa.getTutorMetodologicoId() != null) ids.add(defensa.getTutorMetodologicoId());
        return ids;
    }

    private void sincronizarJurados(Defensa defensa, List<Long> juradosIds) {
        juradoDefensaRepository.deleteByDefensaId(defensa.getId());
        if (juradosIds == null || juradosIds.isEmpty()) return;

        JuradoDefensa.RolJurado[] roles = {
                JuradoDefensa.RolJurado.PRESIDENTE,
                JuradoDefensa.RolJurado.PRINCIPAL,
                JuradoDefensa.RolJurado.SUPLENTE
        };

        for (int i = 0; i < juradosIds.size() && i < roles.length; i++) {
            Long docenteId = juradosIds.get(i);
            if (docenteId == null) continue;
            JuradoDefensa.RolJurado rolActual = roles[i];
            docenteRepository.findById(docenteId).ifPresent(docente -> {
                JuradoDefensa juradoDefensa = new JuradoDefensa();
                juradoDefensa.setDefensa(defensa);
                juradoDefensa.setDocente(docente);
                juradoDefensa.setRolJurado(rolActual);
                juradoDefensaRepository.save(juradoDefensa);
            });
        }
    }

    private Long valorEn(List<Long> lista, int indice) {
        return lista != null && lista.size() > indice ? lista.get(indice) : null;
    }

    /**
     * Extrae los IDs de los tesistas del proyecto (estudiante, estudiante2, estudiante3)
     * para que el motor verifique también sus horarios de clase al agendar.
     */
    private List<UUID> idsEstudiantesDe(Proyecto proyecto) {
        List<UUID> estudianteIds = new ArrayList<>();
        if (proyecto == null) return estudianteIds;
        agregarIdEstudiante(proyecto.getEstudiante(), estudianteIds);
        agregarIdEstudiante(proyecto.getEstudiante2(), estudianteIds);
        agregarIdEstudiante(proyecto.getEstudiante3(), estudianteIds);
        return estudianteIds;
    }

    private void agregarIdEstudiante(Estudiante estudiante, List<UUID> destino) {
        if (estudiante != null && estudiante.getId() != null) {
            destino.add(estudiante.getId());
        }
    }

    private ReporteDefensaDTO mapearReporte(Defensa defensa) {
        ReporteDefensaDTO reporte = new ReporteDefensaDTO();
        reporte.setHoraInicio(defensa.getHoraInicio());
        reporte.setHoraFin(defensa.getHoraFin());

        if (defensa.getEspacioFisico() != null) {
            reporte.setEspacio(defensa.getEspacioFisico().getCodigoAula());
        }

        Proyecto proyecto = defensa.getProyecto();
        if (proyecto != null) {
            reporte.setTitulo(proyecto.getTitulo());
            reporte.setTesista(nombreCompleto(proyecto.getEstudiante()));
            reporte.setTesista2(nombreCompleto(proyecto.getEstudiante2()));
            reporte.setTesista3(nombreCompleto(proyecto.getEstudiante3()));
        }

        reporte.setTutorAcademico(resolverDocente(defensa.getTutorAcademicoId()));
        reporte.setJurado(resolverDocente(defensa.getJuradoId()));
        return reporte;
    }

    private String resolverDocente(Long id) {
        if (id == null) return null;
        return docenteRepository.findById(id).map(Docente::getNombreCompleto).orElse(null);
    }

    private String nombreCompleto(com.urbe.defensas.models.Estudiante estudiante) {
        if (estudiante == null) return null;
        List<String> partes = new ArrayList<>();
        if (estudiante.getNombres() != null && !estudiante.getNombres().isBlank()) {
            partes.add(estudiante.getNombres());
        }
        if (estudiante.getApellidos() != null && !estudiante.getApellidos().isBlank()) {
            partes.add(estudiante.getApellidos());
        }
        return partes.isEmpty() ? null : String.join(" ", partes);
    }
}