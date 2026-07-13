package com.urbe.defensas.services;

import com.urbe.defensas.models.EspacioFisico;
import com.urbe.defensas.repositories.EspacioFisicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class EspacioFisicoService {

    private final EspacioFisicoRepository espacioFisicoRepository;

    public EspacioFisicoService(EspacioFisicoRepository espacioFisicoRepository) {
        this.espacioFisicoRepository = espacioFisicoRepository;
    }

    public EspacioFisico crear(EspacioFisico espacio) {
        return espacioFisicoRepository.save(espacio);
    }

    public EspacioFisico actualizar(UUID id, EspacioFisico espacio) {
        EspacioFisico existente = espacioFisicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Espacio físico no encontrado"));
        existente.setCodigoAula(espacio.getCodigoAula());
        existente.setCapacidad(espacio.getCapacidad());
        existente.setTipo(espacio.getTipo());
        existente.setEstatusOperativo(espacio.getEstatusOperativo());
        return espacioFisicoRepository.save(existente);
    }

    public EspacioFisico obtenerPorId(UUID id) {
        return espacioFisicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Espacio físico no encontrado"));
    }

    public List<EspacioFisico> listarTodos() {
        return espacioFisicoRepository.findAll();
    }

    public List<EspacioFisico> listarDisponibles() {
        return espacioFisicoRepository.findByEstatusOperativoTrue();
    }
}
