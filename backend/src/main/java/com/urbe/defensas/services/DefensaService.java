package com.urbe.defensas.services;

import com.urbe.defensas.dtos.RegistroDefensaDTO;
import com.urbe.defensas.dtos.ReprogramacionDTO;
import com.urbe.defensas.dtos.ReporteDefensaDTO;
import com.urbe.defensas.exceptions.ConflictException;
import com.urbe.defensas.models.Defensa;
import com.urbe.defensas.models.Docente;
import com.urbe.defensas.models.EspacioFisico;
import com.urbe.defensas.models.JuradoDefensa;
import com.urbe.defensas.models.Proyecto;
import com.urbe.defensas.repositories.DefensaRepository;
import com.urbe.defensas.repositories.DocenteRepository;
import com.urbe.defensas.repositories.EspacioFisicoRepository;
import com.urbe.defensas.repositories.HorarioClaseRepository;
import com.urbe.defensas.repositories.JuradoDefensaRepository;
import com.urbe.defensas.repositories.ProyectoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DefensaService {

    private final DefensaRepository defensaRepository;
    private final ProyectoRepository proyectoRepository;
    private final EspacioFisicoRepository espacioFisicoRepository;
    private final DocenteRepository docenteRepository;
    private final HorarioClaseRepository horarioClaseRepository;
    private final JuradoDefensaRepository juradoDefensaRepository;

    public DefensaService(DefensaRepository defensaRepository,
                          ProyectoRepository proyectoRepository,
                          EspacioFisicoRepository espacioFisicoRepository,
                          DocenteRepository docenteRepository,
                          HorarioClaseRepository horarioClaseRepository,
                          JuradoDefensaRepository juradoDefensaRepository) {
        this.defensaRepository = defensaRepository;
        this.proyectoRepository = proyectoRepository;
        this.espacioFisicoRepository = espacioFisicoRepository;
        this.docenteRepository = docenteRepository;
        this.horarioClaseRepository = horarioClaseRepository;
        this.juradoDefensaRepository = juradoDefensaRepository;
    }

    public Defensa programar(RegistroDefensaDTO dto) {
        Proyecto proyecto = proyectoRepository.findById(dto.getProyectoId())
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
        EspacioFisico espacio = espacioFisicoRepository.findById(dto.getEspacioId())
                .orElseThrow(() -> new RuntimeException("Espacio físico no encontrado"));

        List<Long> docentesIds = new ArrayList<>();
        if (dto.getJuradoId() != null) docentesIds.add(dto.getJuradoId());
        if (dto.getTutorAcademicoId() != null) docentesIds.add(dto.getTutorAcademicoId());
        if (dto.getTutorMetodologicoId() != null) docentesIds.add(dto.getTutorMetodologicoId());
        validarDisponibilidad(dto.getFecha(), dto.getEspacioId(), docentesIds,
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

        List<Long> docentesIds = dto.getJuradosIds() != null ? dto.getJuradosIds() : List.of();
        validarDisponibilidad(dto.getFecha(), dto.getEspacioId(), docentesIds,
                dto.getHoraInicio(), dto.getHoraFin(), id);

        EspacioFisico espacio = espacioFisicoRepository.findById(dto.getEspacioId())
                .orElseThrow(() -> new RuntimeException("Espacio físico no encontrado"));

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
        defensaRepository.delete(existente);
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
                                       LocalTime horaInicio, LocalTime horaFin, UUID excludedId) {
        String dia = diaSemanaEspanol(fecha);

        boolean choqueClases = horarioClaseRepository.existeChoqueDeClases(
                dia, espacioId, docentesIds, horaInicio, horaFin);
        if (choqueClases) {
            throw new ConflictException(
                    "Conflicto de horario: el docente o el espacio ya están ocupados con clases regulares "
                            + "el " + dia + " de " + horaInicio + " a " + horaFin + ".");
        }

        boolean choqueDefensas = defensaRepository.existeDefensaEnHorario(
                fecha, espacioId, docentesIds, horaInicio, horaFin, excludedId);
        if (choqueDefensas) {
            throw new ConflictException(
                    "Conflicto de horario: el espacio o alguno de los docentes seleccionados ya está asignado "
                            + "a otra defensa el " + fecha + " de " + horaInicio + " a " + horaFin + ".");
        }
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