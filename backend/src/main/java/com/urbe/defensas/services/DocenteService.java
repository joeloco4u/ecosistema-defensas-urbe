package com.urbe.defensas.services;

import com.urbe.defensas.dtos.ExpedienteDocenteDTO;
import com.urbe.defensas.dtos.ExpedienteDocenteDTO.DefensaJuradoDTO;
import com.urbe.defensas.dtos.ExpedienteDocenteDTO.ProyectoTutoriaDTO;
import com.urbe.defensas.dtos.ExpedienteDocenteDTO.RolJurado;
import com.urbe.defensas.dtos.ExpedienteDocenteDTO.RolTutoria;
import com.urbe.defensas.models.Defensa;
import com.urbe.defensas.models.Docente;
import com.urbe.defensas.models.JuradoDefensa;
import com.urbe.defensas.models.Proyecto;
import com.urbe.defensas.repositories.DefensaRepository;
import com.urbe.defensas.repositories.DocenteRepository;
import com.urbe.defensas.repositories.JuradoDefensaRepository;
import com.urbe.defensas.repositories.ProyectoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class DocenteService {

    private final DocenteRepository docenteRepository;
    private final ProyectoRepository proyectoRepository;
    private final DefensaRepository defensaRepository;
    private final JuradoDefensaRepository juradoDefensaRepository;

    public DocenteService(DocenteRepository docenteRepository,
                          ProyectoRepository proyectoRepository,
                          DefensaRepository defensaRepository,
                          JuradoDefensaRepository juradoDefensaRepository) {
        this.docenteRepository = docenteRepository;
        this.proyectoRepository = proyectoRepository;
        this.defensaRepository = defensaRepository;
        this.juradoDefensaRepository = juradoDefensaRepository;
    }

    public Docente crear(Docente docente) {
        return docenteRepository.save(docente);
    }

    public Docente actualizar(Long id, Docente docente) {
        Docente existente = docenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Docente no encontrado"));
        existente.setNombreCompleto(docente.getNombreCompleto());
        existente.setEmail(docente.getEmail());
        existente.setDepartamento(docente.getDepartamento());
        existente.setCargaMaximaSemanal(docente.getCargaMaximaSemanal());
        existente.setActivo(docente.getActivo());
        return docenteRepository.save(existente);
    }

    public Docente obtenerPorId(Long id) {
        return docenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Docente no encontrado"));
    }

    public List<Docente> listarTodos() {
        return docenteRepository.findAll();
    }

    public List<Docente> listarActivos() {
        return docenteRepository.findByActivoTrue();
    }

    public ExpedienteDocenteDTO obtenerExpediente(Long id) {
        Docente docente = docenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Docente no encontrado"));

        List<Proyecto> proyectosTutorados = proyectoRepository.findByTutorId(id);
        List<Defensa> defensasParticipacion = defensaRepository.findByTutorId(id);

        Set<String> seminarios = new LinkedHashSet<>();
        for (Proyecto p : proyectosTutorados) {
            if (p.getEscuela() != null && !p.getEscuela().isBlank()) {
                seminarios.add(p.getEscuela());
            }
        }
        for (Defensa d : defensasParticipacion) {
            Proyecto p = d.getProyecto();
            if (p != null && p.getEscuela() != null && !p.getEscuela().isBlank()) {
                seminarios.add(p.getEscuela());
            }
        }

        Map<UUID, ProyectoTutoriaDTO> proyectosTutor = new LinkedHashMap<>();
        for (Defensa d : defensasParticipacion) {
            Proyecto p = d.getProyecto();
            if (p == null) continue;

            RolTutoria rol = null;
            if (id.equals(d.getTutorAcademicoId())) rol = RolTutoria.ACADEMICO;
            if (id.equals(d.getTutorMetodologicoId())) rol = RolTutoria.METODOLOGICO;
            if (rol == null || proyectosTutor.containsKey(p.getId())) continue;

            String tesista = (p.getEstudiante() != null)
                    ? p.getEstudiante().getNombres() + " " + p.getEstudiante().getApellidos()
                    : null;
            proyectosTutor.put(p.getId(),
                    new ProyectoTutoriaDTO(p.getId(), p.getTitulo(), p.getEscuela(), tesista, rol, p.getEstatus()));
        }

        Map<UUID, DefensaJuradoDTO> defensasJurado = new LinkedHashMap<>();
        for (Defensa d : defensasParticipacion) {
            if (!id.equals(d.getJuradoId()) || d.getProyecto() == null) continue;
            defensasJurado.putIfAbsent(d.getId(), toDefensaJurado(d, null));
        }
        for (JuradoDefensa jd : juradoDefensaRepository.findByDocenteId(id)) {
            Defensa d = jd.getDefensa();
            if (d == null || d.getProyecto() == null) continue;
            defensasJurado.putIfAbsent(d.getId(), toDefensaJurado(d, RolJurado.valueOf(jd.getRolJurado().name())));
        }

        ExpedienteDocenteDTO expediente = new ExpedienteDocenteDTO();
        expediente.setDocente(docente);
        expediente.setSeminarios(new ArrayList<>(seminarios));
        expediente.setProyectosTutor(new ArrayList<>(proyectosTutor.values()));
        expediente.setDefensasJurado(new ArrayList<>(defensasJurado.values()));
        return expediente;
    }

    private DefensaJuradoDTO toDefensaJurado(Defensa d, RolJurado rolJurado) {
        Proyecto p = d.getProyecto();
        String tesista = (p.getEstudiante() != null)
                ? p.getEstudiante().getNombres() + " " + p.getEstudiante().getApellidos()
                : null;
        return new DefensaJuradoDTO(d.getId(), p.getId(), p.getTitulo(), tesista,
                d.getFecha(), d.getHoraInicio(), d.getHoraFin(), d.getEstatus(), rolJurado);
    }
}