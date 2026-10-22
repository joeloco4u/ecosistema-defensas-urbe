package com.urbe.defensas.services;

import com.urbe.defensas.dtos.SugerenciaHorarioDTO;
import com.urbe.defensas.models.Docente;
import com.urbe.defensas.models.EspacioFisico;
import com.urbe.defensas.models.Estudiante;
import com.urbe.defensas.models.Proyecto;
import com.urbe.defensas.repositories.DocenteRepository;
import com.urbe.defensas.repositories.EspacioFisicoRepository;
import com.urbe.defensas.repositories.ProyectoRepository;
import com.urbe.defensas.services.DisponibilidadService.Intervalo;
import com.urbe.defensas.util.CedulaUtil;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class MotorProgramacionService {

    private static final LocalTime HORA_INICIO_JORNADA = LocalTime.of(8, 0);
    private static final LocalTime HORA_FIN_JORNADA = LocalTime.of(22, 20);
    private static final LocalTime HORA_MAXIMA_FIN_DEFENSA = LocalTime.of(21, 0);
    private static final LocalTime PRIME_INICIO = LocalTime.of(13, 0);
    private static final LocalTime PRIME_FIN = LocalTime.of(16, 0);
    private static final LocalTime MANANA_INICIO = LocalTime.of(8, 0);
    private static final LocalTime MANANA_FIN = LocalTime.of(12, 0);
    private static final LocalTime TARDE_INICIO = LocalTime.of(17, 0);
    private static final LocalTime TARDE_FIN = LocalTime.of(19, 0);
    private static final LocalTime NOCHE_INICIO = LocalTime.of(19, 0);
    private static final int DURACION_BLOQUE_MINUTOS = 40;

    @Value("${app.periodo-academico.inicio:}")
    private String periodoAcademicoInicio;

    private final DocenteRepository docenteRepository;
    private final EspacioFisicoRepository espacioFisicoRepository;
    private final ProyectoRepository proyectoRepository;
    private final DisponibilidadService disponibilidadService;

    public MotorProgramacionService(DocenteRepository docenteRepository,
                                    EspacioFisicoRepository espacioFisicoRepository,
                                    ProyectoRepository proyectoRepository,
                                    DisponibilidadService disponibilidadService) {
        this.docenteRepository = docenteRepository;
        this.espacioFisicoRepository = espacioFisicoRepository;
        this.proyectoRepository = proyectoRepository;
        this.disponibilidadService = disponibilidadService;
    }

    public List<SugerenciaHorarioDTO> calcularDisponibilidad(List<String> cedulasDocentes, UUID espacioFisicoId) {
        return calcularDisponibilidad(cedulasDocentes, espacioFisicoId, null, null, null);
    }

    public List<SugerenciaHorarioDTO> calcularDisponibilidad(List<String> cedulasDocentes, UUID espacioFisicoId,
                                                             Integer semana, UUID proyectoId) {
        return calcularDisponibilidad(cedulasDocentes, espacioFisicoId, semana, proyectoId, null);
    }

    @Transactional(readOnly = true)
    public List<SugerenciaHorarioDTO> calcularDisponibilidad(List<String> cedulasDocentes, UUID espacioFisicoId,
                                                             Integer semana, UUID proyectoId, Integer diaSemana) {
        List<Long> docenteIds = resolverIdsDocentes(cedulasDocentes);
        List<UUID> estudianteIds = resolverIdsEstudiantes(proyectoId);
        List<EspacioFisico> espacios = resolverEspacios(espacioFisicoId);

        List<LocalDate> fechasValidas = new ArrayList<>();
        for (LocalDate fecha : resolverFechasCandidatas(semana, diaSemana)) {
            // Los feriados nunca son hábiles. Los fines de semana sólo se excluyen
            // cuando no se pidió explícitamente ese día de la semana.
            if (DiasFeriados.esFeriado(fecha)) {
                continue;
            }
            if (diaSemana == null && DiasFeriados.esFinDeSemana(fecha)) {
                continue;
            }
            fechasValidas.add(fecha);
        }

        if (fechasValidas.isEmpty() || espacios.isEmpty()) {
            return List.of();
        }

        List<UUID> espacioIds = espacios.stream()
                .map(EspacioFisico::getId)
                .toList();

        DisponibilidadService.Disponibilidad disponibilidad = disponibilidadService.calcular(
                fechasValidas.get(0),
                fechasValidas.get(fechasValidas.size() - 1),
                espacioIds,
                docenteIds,
                estudianteIds,
                HORA_INICIO_JORNADA,
                HORA_FIN_JORNADA);

        List<BloqueCandidato> candidatos = new ArrayList<>();
        for (LocalDate fecha : fechasValidas) {
            for (EspacioFisico espacio : espacios) {
                for (Intervalo intervalo : disponibilidad.libres(fecha, espacio.getId())) {
                    LocalTime inicio = intervalo.inicio();
                    while (!inicio.plusMinutes(DURACION_BLOQUE_MINUTOS).isAfter(intervalo.fin())) {
                        if (!inicio.isBefore(HORA_MAXIMA_FIN_DEFENSA)) {
                            break;
                        }
                        LocalTime fin = inicio.plusMinutes(DURACION_BLOQUE_MINUTOS);
                        if (fin.isAfter(HORA_MAXIMA_FIN_DEFENSA)) {
                            break;
                        }
                        int prioridad = calcularPrioridad(inicio);
                        if (prioridad > 0) {
                            candidatos.add(new BloqueCandidato(fecha, inicio, fin, prioridad, espacio));
                        }
                        inicio = inicio.plusMinutes(DURACION_BLOQUE_MINUTOS);
                    }
                }
            }
        }

        candidatos.sort(Comparator.comparingInt(BloqueCandidato::getPrioridad)
                .thenComparing(BloqueCandidato::getFecha)
                .thenComparing(BloqueCandidato::getInicio));

        return candidatos.stream()
                .map(candidato -> new SugerenciaHorarioDTO(
                        candidato.fecha, candidato.inicio, candidato.fin,
                        candidato.espacio.getId(), candidato.espacio.getCodigoAula()))
                .toList();
    }

    /**
     * Resuelve las cédulas de los docentes a sus IDs. Es ESTRICTO: si una cédula
     * provista no existe en el sistema, aborta con 400 para que el coordinador vea
     * el error en lugar de agendar con un docente fantasma.
     */
    private List<Long> resolverIdsDocentes(List<String> cedulasDocentes) {
        if (cedulasDocentes == null) return List.of();
        List<Long> docenteIds = new ArrayList<>();
        Set<Long> vistos = new HashSet<>();
        for (String cedula : cedulasDocentes) {
            if (!CedulaUtil.esValida(cedula)) continue;
            Docente docente = docenteRepository.findByCedulaNormalizada(cedula)
                    .filter(d -> Boolean.TRUE.equals(d.getActivo()))
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Cédula no encontrada o docente inactivo en el sistema: " + cedula));
            if (vistos.add(docente.getId())) {
                docenteIds.add(docente.getId());
            }
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
        if (!inicio.isBefore(NOCHE_INICIO) && inicio.isBefore(HORA_FIN_JORNADA)) return 4;
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

    /**
     * Determina las fechas de la semana que se evaluarán como candidatas.
     * <p>
     * Si {@code diaSemana} es nulo se conserva el comportamiento habitual: de lunes
     * a viernes. Si se especifica un día (1 = lunes … 7 = domingo), se restringe
     * estrictamente a esa fecha; al elegir explícitamente un día del fin de semana
     * se permite agendar en él (ver {@link #calcularDisponibilidad}).
     */
    private List<LocalDate> resolverFechasCandidatas(Integer semana, Integer diaSemana) {
        LocalDate lunes = resolverLunes(semana);
        if (diaSemana != null) {
            if (diaSemana < 1 || diaSemana > 7) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Día de la semana inválido (rango permitido: 1-7): " + diaSemana);
            }
            return List.of(lunes.plusDays(diaSemana - 1L));
        }
        List<LocalDate> fechas = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            fechas.add(lunes.plusDays(i));
        }
        return fechas;
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

    private static class BloqueCandidato {
        final LocalDate fecha;
        final LocalTime inicio;
        final LocalTime fin;
        final int prioridad;
        final EspacioFisico espacio;

        BloqueCandidato(LocalDate fecha, LocalTime inicio, LocalTime fin,
                        int prioridad, EspacioFisico espacio) {
            this.fecha = fecha;
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