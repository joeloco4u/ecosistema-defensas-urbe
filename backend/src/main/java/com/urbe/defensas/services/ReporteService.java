package com.urbe.defensas.services;

import com.urbe.defensas.dtos.DefensaDTO;
import com.urbe.defensas.dtos.FiltroReporteDTO;
import com.urbe.defensas.repositories.DefensaRepository;
import com.urbe.defensas.repositories.DefensaSpecification;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReporteService {

    private static final Sort ORDEN_CRONOLOGICO =
            Sort.by("fecha").ascending().and(Sort.by("horaInicio").ascending());

    private final DefensaRepository defensaRepository;

    public ReporteService(DefensaRepository defensaRepository) {
        this.defensaRepository = defensaRepository;
    }

    public List<DefensaDTO> generarReporte(FiltroReporteDTO filtro) {
        if (filtro == null) {
            filtro = new FiltroReporteDTO();
        }
        if (filtro.getFechaInicio() != null && filtro.getFechaFin() != null
                && filtro.getFechaInicio().isAfter(filtro.getFechaFin())) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha fin.");
        }

        return defensaRepository
                .findAll(DefensaSpecification.conFiltros(filtro), ORDEN_CRONOLOGICO)
                .stream()
                .map(DefensaDTO::fromEntity)
                .toList();
    }
}
