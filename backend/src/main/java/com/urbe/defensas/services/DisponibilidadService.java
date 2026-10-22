package com.urbe.defensas.services;

import com.urbe.defensas.models.Defensa;
import com.urbe.defensas.models.HorarioClase;
import com.urbe.defensas.repositories.DefensaRepository;
import com.urbe.defensas.repositories.HorarioClaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DisponibilidadService {

    private final HorarioClaseRepository horarioClaseRepository;
    private final DefensaRepository defensaRepository;

    public DisponibilidadService(HorarioClaseRepository horarioClaseRepository,
                                 DefensaRepository defensaRepository) {
        this.horarioClaseRepository = horarioClaseRepository;
        this.defensaRepository = defensaRepository;
    }

    @Transactional(readOnly = true)
    public Disponibilidad calcular(LocalDate inicio, LocalDate fin,
                                   Collection<UUID> espacios,
                                   Collection<Long> docentes,
                                   Collection<UUID> estudiantes,
                                   LocalTime ventanaInicio,
                                   LocalTime ventanaFin) {
        return calcularInterno(inicio, fin, espacios, docentes, estudiantes,
                ventanaInicio, ventanaFin, null);
    }

    @Transactional(readOnly = true)
    public Disponibilidad calcular(LocalDate inicio, LocalDate fin,
                                   Collection<UUID> espacios,
                                   Collection<Long> docentes,
                                   Collection<UUID> estudiantes,
                                   LocalTime ventanaInicio,
                                   LocalTime ventanaFin,
                                   UUID defensaExcluida) {
        return calcularInterno(inicio, fin, espacios, docentes, estudiantes,
                ventanaInicio, ventanaFin, defensaExcluida);
    }

    private Disponibilidad calcularInterno(LocalDate inicio, LocalDate fin,
                                           Collection<UUID> espacios,
                                           Collection<Long> docentes,
                                           Collection<UUID> estudiantes,
                                           LocalTime ventanaInicio,
                                           LocalTime ventanaFin,
                                           UUID defensaExcluida) {
        if (inicio == null || fin == null || ventanaInicio == null || ventanaFin == null) {
            throw new IllegalArgumentException("Los parámetros de disponibilidad son obligatorios");
        }
        if (fin.isBefore(inicio)) {
            throw new IllegalArgumentException("El fin del rango no puede ser anterior al inicio");
        }
        if (!ventanaInicio.isBefore(ventanaFin)) {
            throw new IllegalArgumentException("La ventana horaria debe tener una hora de inicio menor a la de fin");
        }

        List<LocalDate> fechas = new ArrayList<>();
        for (LocalDate fecha = inicio; !fecha.isAfter(fin); fecha = fecha.plusDays(1)) {
            fechas.add(fecha);
        }

        Set<UUID> espaciosSeguros = limpiar(espacios);
        Set<Long> docentesSeguros = limpiar(docentes);
        Set<UUID> estudiantesSeguros = limpiar(estudiantes);

        boolean tieneEspacios = !espaciosSeguros.isEmpty();
        boolean tieneDocentes = !docentesSeguros.isEmpty();
        boolean tieneEstudiantes = !estudiantesSeguros.isEmpty();

        Set<String> dias = fechas.stream()
                .map(this::diaSemana)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<HorarioClase> clases = (tieneEspacios || tieneDocentes || tieneEstudiantes)
                ? horarioClaseRepository.buscarParaDisponibilidad(
                        dias, espaciosSeguros, tieneEspacios,
                        docentesSeguros, tieneDocentes,
                        estudiantesSeguros, tieneEstudiantes)
                : List.of();

        List<Defensa> defensas = (tieneEspacios || tieneDocentes)
                ? defensaRepository.buscarParaDisponibilidad(
                        inicio, fin, espaciosSeguros, tieneEspacios, docentesSeguros, tieneDocentes)
                : List.of();

        Map<String, List<HorarioClase>> clasesPorDia = clases.stream()
                .collect(Collectors.groupingBy(HorarioClase::getDiaSemana));
        Map<LocalDate, List<Defensa>> defensasPorFecha = defensas.stream()
                .collect(Collectors.groupingBy(Defensa::getFecha));

        Map<LocalDate, List<Intervalo>> ocupadoEquipo = new HashMap<>();
        Map<LocalDate, Map<UUID, List<Intervalo>>> ocupadoEspacios = new HashMap<>();

        for (LocalDate fecha : fechas) {
            for (HorarioClase clase : clasesPorDia.getOrDefault(diaSemana(fecha), List.of())) {
                Intervalo intervalo = intervalo(clase.getHoraInicio(), clase.getHoraFin());
                if (intervalo == null) continue;

                if (clase.getDocenteId() != null && docentesSeguros.contains(clase.getDocenteId())) {
                    agregarEquipo(ocupadoEquipo, fecha, intervalo);
                }
                if (clase.getEstudianteId() != null && estudiantesSeguros.contains(clase.getEstudianteId())) {
                    agregarEquipo(ocupadoEquipo, fecha, intervalo);
                }
                if (clase.getEspacioId() != null && espaciosSeguros.contains(clase.getEspacioId())) {
                    agregarEspacio(ocupadoEspacios, fecha, clase.getEspacioId(), intervalo);
                }
            }

            for (Defensa defensa : defensasPorFecha.getOrDefault(fecha, List.of())) {
                if (defensaExcluida != null && defensaExcluida.equals(defensa.getId())) {
                    continue;
                }
                Intervalo intervalo = intervalo(defensa.getHoraInicio(), defensa.getHoraFin());
                if (intervalo == null) continue;

                if (participaDocente(defensa, docentesSeguros)) {
                    agregarEquipo(ocupadoEquipo, fecha, intervalo);
                }
                if (defensa.getEspacioFisico() != null) {
                    UUID espacioId = defensa.getEspacioFisico().getId();
                    if (espacioId != null && espaciosSeguros.contains(espacioId)) {
                        agregarEspacio(ocupadoEspacios, fecha, espacioId, intervalo);
                    }
                }
            }
        }

        Map<LocalDate, Map<UUID, List<Intervalo>>> libresPorFecha = new HashMap<>();
        for (LocalDate fecha : fechas) {
            List<Intervalo> ocupadoEquipoFecha = fusionar(
                    ocupadoEquipo.getOrDefault(fecha, List.of()));
            Map<UUID, List<Intervalo>> libresPorEspacio = new HashMap<>();

            for (UUID espacioId : espaciosSeguros) {
                List<Intervalo> ocupados = new ArrayList<>(ocupadoEquipoFecha);
                ocupados.addAll(ocupadoEspacios
                        .getOrDefault(fecha, Map.of())
                        .getOrDefault(espacioId, List.of()));
                ocupados.add(new Intervalo(LocalTime.of(12, 0), LocalTime.of(13, 0)));
                libresPorEspacio.put(espacioId, restar(ventanaInicio, ventanaFin, fusionar(ocupados)));
            }
            libresPorFecha.put(fecha, libresPorEspacio);
        }

        return new Disponibilidad(libresPorFecha);
    }

    @Transactional(readOnly = true)
    public Disponibilidad calcular(LocalDate inicio, LocalDate fin,
                                   UUID espacioId,
                                   Collection<Long> docentes,
                                   Collection<UUID> estudiantes,
                                   LocalTime ventanaInicio,
                                   LocalTime ventanaFin) {
        return calcularConEspacio(inicio, fin, espacioId, docentes, estudiantes,
                ventanaInicio, ventanaFin, null);
    }

    @Transactional(readOnly = true)
    public Disponibilidad calcular(LocalDate inicio, LocalDate fin,
                                   UUID espacioId,
                                   Collection<Long> docentes,
                                   Collection<UUID> estudiantes,
                                   LocalTime ventanaInicio,
                                   LocalTime ventanaFin,
                                   UUID defensaExcluida) {
        return calcularConEspacio(inicio, fin, espacioId, docentes, estudiantes,
                ventanaInicio, ventanaFin, defensaExcluida);
    }

    private Disponibilidad calcularConEspacio(LocalDate inicio, LocalDate fin,
                                              UUID espacioId,
                                              Collection<Long> docentes,
                                              Collection<UUID> estudiantes,
                                              LocalTime ventanaInicio,
                                              LocalTime ventanaFin,
                                              UUID defensaExcluida) {
        Collection<UUID> espacios = espacioId != null ? List.of(espacioId) : List.of();
        return calcularInterno(inicio, fin, espacios, docentes, estudiantes,
                ventanaInicio, ventanaFin, defensaExcluida);
    }

    private static <T> Set<T> limpiar(Collection<T> valores) {
        if (valores == null) return Set.of();
        return valores.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static void agregarEquipo(Map<LocalDate, List<Intervalo>> mapa,
                                      LocalDate fecha, Intervalo intervalo) {
        mapa.computeIfAbsent(fecha, f -> new ArrayList<>()).add(intervalo);
    }

    private static void agregarEspacio(Map<LocalDate, Map<UUID, List<Intervalo>>> mapa,
                                       LocalDate fecha, UUID espacioId, Intervalo intervalo) {
        mapa.computeIfAbsent(fecha, f -> new HashMap<>())
                .computeIfAbsent(espacioId, e -> new ArrayList<>())
                .add(intervalo);
    }

    private static boolean participaDocente(Defensa defensa, Set<Long> docentes) {
        if (docentes.isEmpty()) return false;
        return docentes.contains(defensa.getJuradoId())
                || docentes.contains(defensa.getTutorAcademicoId());
    }

    private static Intervalo intervalo(LocalTime inicio, LocalTime fin) {
        if (inicio == null || fin == null || !inicio.isBefore(fin)) return null;
        return new Intervalo(inicio, fin);
    }

    private static List<Intervalo> fusionar(Collection<Intervalo> intervalos) {
        if (intervalos == null || intervalos.isEmpty()) return List.of();

        List<Intervalo> ordenados = new ArrayList<>(intervalos);
        ordenados.sort(Comparator.comparing(Intervalo::inicio).thenComparing(Intervalo::fin));

        List<Intervalo> fusionados = new ArrayList<>();
        Intervalo actual = ordenados.get(0);
        for (int i = 1; i < ordenados.size(); i++) {
            Intervalo siguiente = ordenados.get(i);
            if (!siguiente.inicio().isAfter(actual.fin())) {
                LocalTime finMayor = siguiente.fin().isAfter(actual.fin()) ? siguiente.fin() : actual.fin();
                actual = new Intervalo(actual.inicio(), finMayor);
            } else {
                fusionados.add(actual);
                actual = siguiente;
            }
        }
        fusionados.add(actual);
        return fusionados;
    }

    private static List<Intervalo> restar(LocalTime ventanaInicio, LocalTime ventanaFin,
                                           List<Intervalo> ocupados) {
        List<Intervalo> libres = new ArrayList<>();
        LocalTime cursor = ventanaInicio;

        for (Intervalo ocupado : ocupados) {
            if (!ocupado.fin().isAfter(ventanaInicio) || !ocupado.inicio().isBefore(ventanaFin)) {
                continue;
            }
            LocalTime inicioOcupado = ocupado.inicio().isBefore(ventanaInicio)
                    ? ventanaInicio : ocupado.inicio();
            LocalTime finOcupado = ocupado.fin().isAfter(ventanaFin)
                    ? ventanaFin : ocupado.fin();

            if (inicioOcupado.isAfter(cursor)) {
                libres.add(new Intervalo(cursor, inicioOcupado));
            }
            if (finOcupado.isAfter(cursor)) {
                cursor = finOcupado;
            }
        }
        if (cursor.isBefore(ventanaFin)) {
            libres.add(new Intervalo(cursor, ventanaFin));
        }
        return libres;
    }

    private String diaSemana(LocalDate fecha) {
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

    public record Intervalo(LocalTime inicio, LocalTime fin) {
        public Intervalo {
            if (inicio == null || fin == null || !inicio.isBefore(fin)) {
                throw new IllegalArgumentException("El intervalo debe tener inicio menor a fin");
            }
        }
    }

    public static final class Disponibilidad {

        private final Map<LocalDate, Map<UUID, List<Intervalo>>> libresPorFecha;

        private Disponibilidad(Map<LocalDate, Map<UUID, List<Intervalo>>> libresPorFecha) {
            this.libresPorFecha = libresPorFecha;
        }

        public List<Intervalo> libres(LocalDate fecha, UUID espacioId) {
            return libresPorFecha
                    .getOrDefault(fecha, Map.of())
                    .getOrDefault(espacioId, List.of());
        }

        public boolean estaLibre(LocalDate fecha, UUID espacioId, LocalTime inicio, LocalTime fin) {
            if (fecha == null || espacioId == null || inicio == null || fin == null
                    || !inicio.isBefore(fin)) {
                return false;
            }
            return libres(fecha, espacioId).stream()
                    .anyMatch(intervalo ->
                            !inicio.isBefore(intervalo.inicio()) && !fin.isAfter(intervalo.fin()));
        }
    }
}
