package com.urbe.defensas.services;

import com.urbe.defensas.models.Proyecto;
import com.urbe.defensas.repositories.ProyectoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProyectoService {

    private final ProyectoRepository proyectoRepository;

    public ProyectoService(ProyectoRepository proyectoRepository) {
        this.proyectoRepository = proyectoRepository;
    }

    public Proyecto crear(Proyecto proyecto) {
        return proyectoRepository.save(proyecto);
    }

    public Proyecto actualizar(UUID id, Proyecto proyecto) {
        Proyecto existente = proyectoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
        existente.setTitulo(proyecto.getTitulo());
        existente.setEstudiante(proyecto.getEstudiante());
        existente.setTutor(proyecto.getTutor());
        existente.setEstatus(proyecto.getEstatus());
        return proyectoRepository.save(existente);
    }

    public Proyecto obtenerPorId(UUID id) {
        return proyectoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
    }

    public List<Proyecto> listarTodos() {
        return proyectoRepository.findAll();
    }

    public List<Proyecto> listarPorEstatus(Proyecto.EstatusProyecto estatus) {
        return proyectoRepository.findByEstatus(estatus);
    }
}
