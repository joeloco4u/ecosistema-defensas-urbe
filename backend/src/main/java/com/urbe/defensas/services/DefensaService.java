package com.urbe.defensas.services;

import com.urbe.defensas.dtos.RegistroDefensaDTO;
import com.urbe.defensas.dtos.ReporteDefensaDTO;
import com.urbe.defensas.models.Defensa;
import com.urbe.defensas.models.Docente;
import com.urbe.defensas.models.EspacioFisico;
import com.urbe.defensas.models.Proyecto;
import com.urbe.defensas.repositories.DefensaRepository;
import com.urbe.defensas.repositories.DocenteRepository;
import com.urbe.defensas.repositories.EspacioFisicoRepository;
import com.urbe.defensas.repositories.ProyectoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DefensaService {

    private final DefensaRepository defensaRepository;
    private final ProyectoRepository proyectoRepository;
    private final EspacioFisicoRepository espacioFisicoRepository;
    private final DocenteRepository docenteRepository;

    public DefensaService(DefensaRepository defensaRepository,
                          ProyectoRepository proyectoRepository,
                          EspacioFisicoRepository espacioFisicoRepository,
                          DocenteRepository docenteRepository) {
        this.defensaRepository = defensaRepository;
        this.proyectoRepository = proyectoRepository;
        this.espacioFisicoRepository = espacioFisicoRepository;
        this.docenteRepository = docenteRepository;
    }

    public Defensa programar(RegistroDefensaDTO dto) {
        Proyecto proyecto = proyectoRepository.findById(dto.getProyectoId())
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
        EspacioFisico espacio = espacioFisicoRepository.findById(dto.getEspacioId())
                .orElseThrow(() -> new RuntimeException("Espacio físico no encontrado"));

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

        proyecto.setEstatus(Proyecto.EstatusProyecto.AGENDADO);
        proyectoRepository.save(proyecto);

        return guardada;
    }

    public Defensa reprogramar(UUID id, Defensa defensa) {
        Defensa existente = defensaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Defensa no encontrada"));
        existente.setFecha(defensa.getFecha());
        existente.setHoraInicio(defensa.getHoraInicio());
        existente.setHoraFin(defensa.getHoraFin());
        existente.setEspacioFisico(defensa.getEspacioFisico());
        existente.setEstatus(defensa.getEstatus());
        return defensaRepository.save(existente);
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
            if (proyecto.getEstudiante() != null) {
                reporte.setTesista(proyecto.getEstudiante().getNombres() + " " + proyecto.getEstudiante().getApellidos());
            }
        }

        reporte.setTutorAcademico(resolverDocente(defensa.getTutorAcademicoId()));
        reporte.setJurado(resolverDocente(defensa.getJuradoId()));
        return reporte;
    }

    private String resolverDocente(Long id) {
        if (id == null) return null;
        return docenteRepository.findById(id).map(Docente::getNombreCompleto).orElse(null);
    }
}