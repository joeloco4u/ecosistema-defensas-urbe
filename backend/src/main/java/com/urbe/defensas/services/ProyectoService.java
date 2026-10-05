package com.urbe.defensas.services;

import com.urbe.defensas.dtos.ProyectoDTO;
import com.urbe.defensas.models.Proyecto;
import com.urbe.defensas.repositories.ProyectoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProyectoService {

    private final ProyectoRepository proyectoRepository;

    public ProyectoService(ProyectoRepository proyectoRepository) {
        this.proyectoRepository = proyectoRepository;
    }

    public ProyectoDTO crear(Proyecto proyecto) {
        return ProyectoDTO.fromEntity(proyectoRepository.save(proyecto));
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
        return ProyectoDTO.fromEntity(proyectoRepository.save(existente));
    }

    @Transactional(readOnly = true)
    public ProyectoDTO obtenerPorId(UUID id) {
        Proyecto proyecto = proyectoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
        return ProyectoDTO.fromEntity(proyecto);
    }

    @Transactional(readOnly = true)
    public List<ProyectoDTO> listarTodos() {
        return proyectoRepository.findAll().stream()
                .map(ProyectoDTO::fromEntity)
                .toList();
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
