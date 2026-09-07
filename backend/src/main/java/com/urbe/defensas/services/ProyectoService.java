package com.urbe.defensas.services;

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
