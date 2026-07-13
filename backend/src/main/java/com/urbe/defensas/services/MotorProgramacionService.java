package com.urbe.defensas.services;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.urbe.defensas.dtos.SugerenciaHorarioDTO;
import com.urbe.defensas.models.EspacioFisico;
import com.urbe.defensas.repositories.DefensaRepository;
import com.urbe.defensas.repositories.DocenteRepository;
import com.urbe.defensas.repositories.EspacioFisicoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MotorProgramacionService {

    private static final Logger log = LoggerFactory.getLogger(MotorProgramacionService.class);
    private static final LocalTime HORA_INICIO_JORNADA = LocalTime.of(8, 0);
    private static final LocalTime HORA_FIN_JORNADA = LocalTime.of(18, 0);
    private static final int DURACION_BLOQUE_HORAS = 2;
    private static final int MAX_SUGERENCIAS = 3;

    private final ObjectMapper objectMapper;
    private final EspacioFisicoRepository espacioFisicoRepository;
    private final DocenteRepository docenteRepository;
    private final DefensaRepository defensaRepository;

    public MotorProgramacionService(ObjectMapper objectMapper,
                                     EspacioFisicoRepository espacioFisicoRepository,
                                     DocenteRepository docenteRepository,
                                     DefensaRepository defensaRepository) {
        this.objectMapper = objectMapper;
        this.espacioFisicoRepository = espacioFisicoRepository;
        this.docenteRepository = docenteRepository;
        this.defensaRepository = defensaRepository;
    }

    public List<SugerenciaHorarioDTO> calcularDisponibilidad(List<String> cedulasDocentes, UUID espacioFisicoId) {
        EspacioFisico espacio = espacioFisicoRepository.findById(espacioFisicoId)
                .orElseThrow(() -> new IllegalArgumentException("Espacio físico no encontrado: " + espacioFisicoId));

        Map<String, List<BloqueOcupado>> horariosDocentes = cargarHorariosOcupadosMock();

        List<SugerenciaHorarioDTO> sugerencias = new ArrayList<>();
        LocalDate lunes = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDate viernes = lunes.plusDays(4);

        for (LocalDate fecha = lunes; !fecha.isAfter(viernes); fecha = fecha.plusDays(1)) {
            int diaSemana = fecha.getDayOfWeek().getValue();
            List<com.urbe.defensas.models.Defensa> defensasEspacio =
                    defensaRepository.findByEspacioFisicoIdAndFecha(espacioFisicoId, fecha);

            for (LocalTime inicio = HORA_INICIO_JORNADA;
                 inicio.plusHours(DURACION_BLOQUE_HORAS).compareTo(HORA_FIN_JORNADA) <= 0;
                 inicio = inicio.plusHours(DURACION_BLOQUE_HORAS)) {

                final LocalTime inicioBloque = inicio;
                LocalTime fin = inicioBloque.plusHours(DURACION_BLOQUE_HORAS);

                boolean aulaOcupada = defensasEspacio.stream()
                        .anyMatch(d -> bloquesSeSuperponen(inicioBloque, fin, d.getHoraInicio(), d.getHoraFin()));
                if (aulaOcupada) continue;

                boolean docenteOcupado = false;
                for (String cedula : cedulasDocentes) {
                    List<BloqueOcupado> bloques = horariosDocentes.getOrDefault(cedula, Collections.emptyList());
                    for (BloqueOcupado bloque : bloques) {
                        if (bloque.diaSemana == diaSemana
                                && bloquesSeSuperponen(inicioBloque, fin, bloque.horaInicio, bloque.horaFin)) {
                            docenteOcupado = true;
                            break;
                        }
                    }
                    if (docenteOcupado) break;
                }

                if (docenteOcupado) continue;

                sugerencias.add(new SugerenciaHorarioDTO(
                        fecha, inicio, fin, espacioFisicoId, espacio.getCodigoAula()));
                if (sugerencias.size() >= MAX_SUGERENCIAS) {
                    return sugerencias;
                }
            }
        }

        return sugerencias;
    }

    private Map<String, List<BloqueOcupado>> cargarHorariosOcupadosMock() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("mock-horarios-urbe.json")) {
            if (is == null) {
                throw new IllegalStateException(
                        "No se encontró el archivo mock-horarios-urbe.json en el classpath");
            }
            List<HorarioMockDTO> horarios = objectMapper.readValue(
                    is, new TypeReference<List<HorarioMockDTO>>() {});
            Map<String, List<BloqueOcupado>> agrupados = new HashMap<>();
            for (HorarioMockDTO h : horarios) {
                agrupados.computeIfAbsent(h.cedulaDocente, k -> new ArrayList<>())
                        .add(new BloqueOcupado(h.diaSemana, h.horaInicio, h.horaFin));
            }
            return agrupados;
        } catch (Exception e) {
            log.error("Error al cargar horarios ocupados desde mock-horarios-urbe.json", e);
            throw new RuntimeException("Error al procesar el archivo de horarios mock", e);
        }
    }

    private boolean bloquesSeSuperponen(LocalTime inicio1, LocalTime fin1,
                                         LocalTime inicio2, LocalTime fin2) {
        return inicio1.isBefore(fin2) && inicio2.isBefore(fin1);
    }

    private static class HorarioMockDTO {
        @JsonProperty("cedula_docente")
        public String cedulaDocente;
        @JsonProperty("dia_semana")
        public int diaSemana;
        @JsonProperty("hora_inicio")
        @JsonFormat(pattern = "HH:mm")
        public LocalTime horaInicio;
        @JsonProperty("hora_fin")
        @JsonFormat(pattern = "HH:mm")
        public LocalTime horaFin;
        public String periodo;
    }

    private static class BloqueOcupado {
        final int diaSemana;
        final LocalTime horaInicio;
        final LocalTime horaFin;

        BloqueOcupado(int diaSemana, LocalTime horaInicio, LocalTime horaFin) {
            this.diaSemana = diaSemana;
            this.horaInicio = horaInicio;
            this.horaFin = horaFin;
        }
    }
}
