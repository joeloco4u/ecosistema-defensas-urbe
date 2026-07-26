package com.urbe.defensas.services;

import com.urbe.defensas.models.Defensa;
import com.urbe.defensas.repositories.DefensaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DefensaService {

    private final DefensaRepository defensaRepository;

    public DefensaService(DefensaRepository defensaRepository) {
        this.defensaRepository = defensaRepository;
    }

    public Defensa crear(Defensa defensa) {
        return defensaRepository.save(defensa);
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
}
