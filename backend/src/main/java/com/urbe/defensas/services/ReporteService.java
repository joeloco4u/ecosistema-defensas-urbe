package com.urbe.defensas.services;

import com.urbe.defensas.dtos.DefensaDTO;
import com.urbe.defensas.dtos.FiltroReporteDTO;
import com.urbe.defensas.dtos.ProyectoDTO;
import com.urbe.defensas.models.Docente;
import com.urbe.defensas.repositories.DefensaRepository;
import com.urbe.defensas.repositories.DefensaSpecification;
import com.urbe.defensas.repositories.DocenteRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReporteService {

    private static final Sort ORDEN_CRONOLOGICO =
            Sort.by("fecha").ascending().and(Sort.by("horaInicio").ascending());

    private final DefensaRepository defensaRepository;
    private final DocenteRepository docenteRepository;

    public ReporteService(DefensaRepository defensaRepository,
                          DocenteRepository docenteRepository) {
        this.defensaRepository = defensaRepository;
        this.docenteRepository = docenteRepository;
    }

    public List<DefensaDTO> generarReporte(FiltroReporteDTO filtro) {
        if (filtro == null) {
            filtro = new FiltroReporteDTO();
        }
        if (filtro.getFechaInicio() != null && filtro.getFechaFin() != null
                && filtro.getFechaInicio().isAfter(filtro.getFechaFin())) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha fin.");
        }

        List<DefensaDTO> defensas = defensaRepository
                .findAll(DefensaSpecification.conFiltros(filtro), ORDEN_CRONOLOGICO)
                .stream()
                .map(DefensaDTO::fromEntity)
                .toList();

        Set<Long> juradoIds = defensas.stream()
                .map(DefensaDTO::getJuradoId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, Docente> jurados = docenteRepository.findAllById(juradoIds).stream()
                .collect(Collectors.toMap(Docente::getId, Function.identity()));

        for (DefensaDTO defensa : defensas) {
            Docente jurado = jurados.get(defensa.getJuradoId());
            if (jurado == null || defensa.getProyecto() == null) continue;
            defensa.getProyecto().setJurado(ProyectoDTO.DocenteResumenDTO.fromEntity(jurado));
            defensa.getProyecto().setJuradoNombre(jurado.getNombreCompleto());
        }

        return defensas;
    }
}
