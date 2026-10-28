package com.urbe.defensas.services;

import com.urbe.defensas.dtos.ProyectoDTO;
import com.urbe.defensas.models.Defensa;
import com.urbe.defensas.models.Docente;
import com.urbe.defensas.models.Proyecto;
import com.urbe.defensas.repositories.DefensaRepository;
import com.urbe.defensas.repositories.DocenteRepository;
import com.urbe.defensas.repositories.ProyectoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class ProyectoService {

    private final ProyectoRepository proyectoRepository;
    private final DefensaRepository defensaRepository;
    private final DocenteRepository docenteRepository;

    public ProyectoService(ProyectoRepository proyectoRepository,
                           DefensaRepository defensaRepository,
                           DocenteRepository docenteRepository) {
        this.proyectoRepository = proyectoRepository;
        this.defensaRepository = defensaRepository;
        this.docenteRepository = docenteRepository;
    }

    public ProyectoDTO crear(Proyecto proyecto) {
        return toDTO(proyectoRepository.save(proyecto));
    }

    public ProyectoDTO actualizar(UUID id, Proyecto proyecto) {
        Proyecto existente = proyectoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
        existente.setTitulo(proyecto.getTitulo());
        existente.setExpediente(proyecto.getExpediente());
        existente.setEstudiante(proyecto.getEstudiante());
        existente.setEstudiante2(proyecto.getEstudiante2());
        existente.setEstudiante3(proyecto.getEstudiante3());
        existente.setTutor(proyecto.getTutor());
        existente.setTutorMetodologico(proyecto.getTutorMetodologico());
        existente.setEstatus(proyecto.getEstatus());
        return toDTO(proyectoRepository.save(existente));
    }

    @Transactional(readOnly = true)
    public ProyectoDTO obtenerPorId(UUID id) {
        Proyecto proyecto = proyectoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
        return toDTO(proyecto);
    }

    @Transactional(readOnly = true)
    public List<ProyectoDTO> listarTodos() {
        List<Proyecto> proyectos = proyectoRepository.findAllConRelaciones();
        Map<UUID, Defensa> defensasPorProyecto = new HashMap<>();
        Set<Long> juradoIds = new HashSet<>();

        for (Defensa defensa : defensaRepository.findAll()) {
            if (defensa.getProyecto() != null) {
                defensasPorProyecto.putIfAbsent(defensa.getProyecto().getId(), defensa);
            }
            if (defensa.getJuradoId() != null) {
                juradoIds.add(defensa.getJuradoId());
            }
        }

        Map<Long, Docente> jurados = new HashMap<>();
        for (Docente docente : docenteRepository.findAllById(juradoIds)) {
            jurados.put(docente.getId(), docente);
        }

        return proyectos.stream()
                .map(proyecto -> ProyectoDTO.fromEntity(
                        proyecto,
                        juradoDe(proyecto.getId(), defensasPorProyecto, jurados)))
                .toList();
    }

    private ProyectoDTO toDTO(Proyecto proyecto) {
        Defensa defensa = defensaRepository.findByProyectoId(proyecto.getId()).stream()
                .findFirst()
                .orElse(null);
        Docente jurado = null;
        if (defensa != null && defensa.getJuradoId() != null) {
            jurado = docenteRepository.findById(defensa.getJuradoId()).orElse(null);
        }
        return ProyectoDTO.fromEntity(proyecto, jurado);
    }

    private Docente juradoDe(UUID proyectoId,
                             Map<UUID, Defensa> defensasPorProyecto,
                             Map<Long, Docente> jurados) {
        Defensa defensa = defensasPorProyecto.get(proyectoId);
        if (defensa == null || defensa.getJuradoId() == null) {
            return null;
        }
        return jurados.get(defensa.getJuradoId());
    }

    public List<String> listarEscuelas() {
        return proyectoRepository.findDistinctEscuelas();
    }

    public List<Proyecto> listarPorEstatus(Proyecto.EstatusProyecto estatus) {
        return proyectoRepository.findByEstatus(estatus);
    }

    @Transactional
    public int ejecutarTransicionSeminarios() {
        List<Proyecto> nivel3 = new ArrayList<>();
        nivel3.addAll(proyectoRepository.findByNivelSeminario("SEMINARIO_3"));
        nivel3.addAll(proyectoRepository.findByNivelSeminarioIsNull());
        nivel3.forEach(p -> p.setNivelSeminario("ARCHIVADO"));

        List<Proyecto> nivel2 = proyectoRepository.findByNivelSeminario("SEMINARIO_2");
        nivel2.forEach(p -> p.setNivelSeminario("SEMINARIO_3"));

        List<Proyecto> nivel1 = proyectoRepository.findByNivelSeminario("SEMINARIO_1");
        nivel1.forEach(p -> p.setNivelSeminario("SEMINARIO_2"));

        return nivel3.size() + nivel2.size() + nivel1.size();
    }
}
