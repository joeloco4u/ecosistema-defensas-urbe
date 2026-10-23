package com.urbe.defensas.services;

import com.urbe.defensas.dtos.DashboardStatsDTO;
import com.urbe.defensas.models.Defensa;
import com.urbe.defensas.models.Proyecto;
import com.urbe.defensas.repositories.DefensaRepository;
import com.urbe.defensas.repositories.ProyectoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DashboardService {

    private final ProyectoRepository proyectoRepository;
    private final DefensaRepository defensaRepository;

    public DashboardService(ProyectoRepository proyectoRepository, DefensaRepository defensaRepository) {
        this.proyectoRepository = proyectoRepository;
        this.defensaRepository = defensaRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDTO obtenerEstadisticas() {
        LocalDate hoy = LocalDate.now();
        LocalDate lunes = hoy.with(DayOfWeek.MONDAY);
        LocalDate domingo = lunes.plusDays(6);

        DashboardStatsDTO stats = new DashboardStatsDTO();
        stats.setTotalProyectos(proyectoRepository.count());
        stats.setProyectosPendientes(
                proyectoRepository.countByEstatus(Proyecto.EstatusProyecto.PENDIENTE));
        stats.setProyectosAgendados(
                proyectoRepository.countByEstatus(Proyecto.EstatusProyecto.AGENDADO));
        stats.setProyectosDefendidos(
                proyectoRepository.countByEstatus(Proyecto.EstatusProyecto.DEFENDIDO));
        stats.setDefensasEstaSemana(
                defensaRepository.countByFechaBetween(lunes, domingo));
        stats.setReprogramaciones(
                defensaRepository.countByEstatus(Defensa.EstatusDefensa.REPROGRAMADA));

        Map<String, Long> proyectosPorEscuela = new LinkedHashMap<>();
        for (Object[] fila : proyectoRepository.countByEscuela()) {
            String escuela = (String) fila[0];
            Long total = ((Number) fila[1]).longValue();
            proyectosPorEscuela.put(escuela, total);
        }
        stats.setProyectosPorEscuela(proyectosPorEscuela);
        return stats;
    }
}