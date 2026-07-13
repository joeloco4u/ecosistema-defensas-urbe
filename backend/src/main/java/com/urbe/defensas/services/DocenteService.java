package com.urbe.defensas.services;

import com.urbe.defensas.models.Docente;
import com.urbe.defensas.repositories.DocenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DocenteService {

    private final DocenteRepository docenteRepository;

    public DocenteService(DocenteRepository docenteRepository) {
        this.docenteRepository = docenteRepository;
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

}
