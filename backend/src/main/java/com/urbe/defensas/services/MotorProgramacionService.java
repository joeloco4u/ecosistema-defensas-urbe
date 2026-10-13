package com.urbe.defensas.services;

import com.urbe.defensas.dtos.SugerenciaHorarioDTO;
import com.urbe.defensas.models.Docente;
import com.urbe.defensas.models.EspacioFisico;
import com.urbe.defensas.models.Estudiante;
import com.urbe.defensas.models.Proyecto;
import com.urbe.defensas.repositories.DefensaRepository;
import com.urbe.defensas.repositories.DocenteRepository;
import com.urbe.defensas.repositories.EspacioFisicoRepository;
import com.urbe.defensas.repositories.HorarioClaseRepository;
import com.urbe.defensas.repositories.ProyectoRepository;
import com.urbe.defensas.util.DiasFeriados;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MotorProgramacionService {

    private static final LocalTime HORA_INICIO_JORNADA = LocalTime.of(8, 0);
    private static final LocalTime HORA_ULTIMO_INICIO = LocalTime.of(19, 0);
    private static final LocalTime PRIME_INICIO = LocalTime.of(13, 0);
    private static final LocalTime PRIME_FIN = LocalTime.of(16, 0);
    private static final LocalTime MANANA_INICIO = LocalTime.of(8, 0);
    private static final LocalTime MANANA_FIN = LocalTime.of(12, 0);
    private static final LocalTime TARDE_INICIO = LocalTime.of(17, 0);
    private static final LocalTime TARDE_FIN = LocalTime.of(19, 0);
    private static final int DURACION_BLOQUE_MINUTOS = 40;
    private static final int MAX_SUGERENCIAS = 5;

    @Value("${app.periodo-academico.inicio:}")
    private String periodoAcademicoInicio;

    private final DocenteRepository docenteRepository;
    private final EspacioFisicoRepository espacioFisicoRepository;
    private final HorarioClaseRepository horarioClaseRepository;
    private final DefensaRepository defensaRepository;
    private final ProyectoRepository proyectoRepository;

    public MotorProgramacionService(DocenteRepository docenteRepository,
                                    EspacioFisicoRepository espacioFisicoRepository,
                                    HorarioClaseRepository horarioClaseRepository,
                                    DefensaRepository defensaRepository,
                                    ProyectoRepository proyectoRepository) {
        this.docenteRepository = docenteRepository;
        this.espacioFisicoRepository = espacioFisicoRepository;
        this.horarioClaseRepository = horarioClaseRepository;
        this.defensaRepository = defensaRepository;
        this.proyectoRepository = proyectoRepository;
    }

    public List<SugerenciaHorarioDTO> calcularDisponibilidad(List<String> cedulasDocentes, UUID espacioFisicoId) {
        return calcularDisponibilidad(cedulasDocentes, espacioFisicoId, null, null);
    }

    @Transactional(readOnly = true)
    public List<SugerenciaHorarioDTO> calcularDisponibilidad(List<String> cedulasDocentes, UUID espacioFisicoId,
                                                             Integer semana, UUID proyectoId) {
        List<Long> docenteIds = resolverIdsDocentes(cedulasDocentes);
        List<UUID> estudianteIds = resolverIdsEstudiantes(proyectoId);
        List<EspacioFisico> espacios = resolverEspacios(espacioFisicoId);

        LocalDate lunes = resolverLunes(semana);
        LocalDate viernes = lunes.plusDays(4);

        List<BloqueCandidato> candidatos = new ArrayList<>();
        for (LocalDate fecha = lunes; !fecha.isAfter(viernes); fecha = fecha.plusDays(1)) {
            // Nunca se sugieren franjas en fines de semana ni en días feriados.
            if (!DiasFeriados.esDiaHabil(fecha)) {
                continue;
            }
            String dia = diaSemanaEspanol(fecha);
            for (LocalTime inicio = HORA_INICIO_JORNADA;
                 !inicio.isAfter(HORA_ULTIMO_INICIO);
                 inicio = inicio.plusHours(1)) {
                LocalTime fin = inicio.plusMinutes(DURACION_BLOQUE_MINUTOS);
                int prioridad = calcularPrioridad(inicio);
                if (prioridad == 0) continue;

                for (EspacioFisico espacio : espacios) {
                    candidatos.add(new BloqueCandidato(fecha, dia, inicio, fin, prioridad, espacio));
                }
            }
        }

        candidatos.sort(Comparator.comparingInt(BloqueCandidato::getPrioridad)
                .thenComparing(BloqueCandidato::getFecha)
                .thenComparing(BloqueCandidato::getInicio));

        boolean tieneDocentes = !docenteIds.isEmpty();
        boolean tieneEstudiantes = !estudianteIds.isEmpty();

        List<SugerenciaHorarioDTO> sugerencias = new ArrayList<>();
        for (BloqueCandidato candidato : candidatos) {
            boolean choqueConClases = horarioClaseRepository.existeChoqueDeClases(
                    candidato.dia, candidato.espacio.getId(),
                    docenteIds, tieneDocentes,
                    estudianteIds, tieneEstudiantes,
                    candidato.inicio, candidato.fin);
            if (choqueConClases) continue;

            boolean choqueConDefensas = defensaRepository.existeDefensaEnHorario(
                    candidato.fecha, candidato.espacio.getId(), docenteIds, candidato.inicio, candidato.fin, null);
            if (choqueConDefensas) continue;

            sugerencias.add(new SugerenciaHorarioDTO(
                    candidato.fecha, candidato.inicio, candidato.fin,
                    candidato.espacio.getId(), candidato.espacio.getCodigoAula()));
            if (sugerencias.size() >= MAX_SUGERENCIAS) {
                return sugerencias;
            }
        }

        return sugerencias;
    }

    /**
     * Resuelve las cédulas de los docentes a sus IDs. Es ESTRICTO: si una cédula
     * provista no existe en el sistema, aborta con 400 para que el coordinador vea
     * el error en lugar de agendar con un docente fantasma.
     */
    private List<Long> resolverIdsDocentes(List<String> cedulasDocentes) {
        if (cedulasDocentes == null) return List.of();
        List<Long> docenteIds = new ArrayList<>();
        for (String cedula : cedulasDocentes) {
            if (cedula == null || cedula.isBlank()) continue;
            Docente docente = docenteRepository.findByCodigoInstitucional(cedula.trim())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Cédula no encontrada en el sistema: " + cedula));
            docenteIds.add(docente.getId());
        }
        return docenteIds;
    }

    /**
     * Extrae los IDs de los tesistas (estudiante, estudiante2, estudiante3) del
     * proyecto que se va a defender, para verificar choques con sus horarios de clase.
     */
    private List<UUID> resolverIdsEstudiantes(UUID proyectoId) {
        if (proyectoId == null) return List.of();
        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Proyecto no encontrado en el sistema: " + proyectoId));
        List<UUID> estudianteIds = new ArrayList<>();
        agregarEstudiante(proyecto.getEstudiante(), estudianteIds);
        agregarEstudiante(proyecto.getEstudiante2(), estudianteIds);
        agregarEstudiante(proyecto.getEstudiante3(), estudianteIds);
        return estudianteIds;
    }

    private void agregarEstudiante(Estudiante estudiante, List<UUID> destino) {
        if (estudiante != null && estudiante.getId() != null) {
            destino.add(estudiante.getId());
        }
    }

    private List<EspacioFisico> resolverEspacios(UUID espacioFisicoId) {
        if (espacioFisicoId != null) {
            return espacioFisicoRepository.findById(espacioFisicoId)
                    .filter(EspacioFisico::getEstatusOperativo)
                    .map(List::of)
                    .orElseGet(List::of);
        }
        return espacioFisicoRepository.findByEstatusOperativoTrue();
    }

    private int calcularPrioridad(LocalTime inicio) {
        if (!inicio.isBefore(PRIME_INICIO) && !inicio.isAfter(PRIME_FIN)) return 1;
        if (!inicio.isBefore(MANANA_INICIO) && !inicio.isAfter(MANANA_FIN)) return 2;
        if (!inicio.isBefore(TARDE_INICIO) && !inicio.isAfter(TARDE_FIN)) return 3;
        return 0;
    }

    /**
     * Resuelve el lunes de la semana solicitada dentro del trimestre académico.
     * Si no se especifica semana (o el trimestre no está configurado), usa el
     * próximo lunes a partir de hoy para mantener el comportamiento previo.
     */
    private LocalDate resolverLunes(Integer semana) {
        LocalDate inicio = parseInicioTrimestre();
        if (semana == null || semana < 1 || inicio == null) {
            return LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        }
        LocalDate primerLunes = inicio.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return primerLunes.plusWeeks(semana - 1L);
    }

    private LocalDate parseInicioTrimestre() {
        if (periodoAcademicoInicio == null || periodoAcademicoInicio.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(periodoAcademicoInicio.trim());
        } catch (RuntimeException e) {
            return null;
        }
    }

    private String diaSemanaEspanol(LocalDate fecha) {
        DayOfWeek dia = fecha.getDayOfWeek();
        return switch (dia) {
            case MONDAY -> "LUNES";
            case TUESDAY -> "MARTES";
            case WEDNESDAY -> "MIERCOLES";
            case THURSDAY -> "JUEVES";
            case FRIDAY -> "VIERNES";
            case SATURDAY -> "SABADO";
            default -> "DOMINGO";
        };
    }

    private static class BloqueCandidato {
        final LocalDate fecha;
        final String dia;
        final LocalTime inicio;
        final LocalTime fin;
        final int prioridad;
        final EspacioFisico espacio;

        BloqueCandidato(LocalDate fecha, String dia, LocalTime inicio, LocalTime fin,
                        int prioridad, EspacioFisico espacio) {
            this.fecha = fecha;
            this.dia = dia;
            this.inicio = inicio;
            this.fin = fin;
            this.prioridad = prioridad;
            this.espacio = espacio;
        }

        public LocalDate getFecha() { return fecha; }
        public LocalTime getInicio() { return inicio; }
        public int getPrioridad() { return prioridad; }
    }
}