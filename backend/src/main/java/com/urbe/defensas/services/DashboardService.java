package com.urbe.defensas.services;

import com.urbe.defensas.dtos.DashboardStatsDTO;
import com.urbe.defensas.models.Defensa;
import com.urbe.defensas.models.Proyecto;
import com.urbe.defensas.repositories.DefensaRepository;
import com.urbe.defensas.repositories.ProyectoRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Service
public class DashboardService {

    private final ProyectoRepository proyectoRepository;
    private final DefensaRepository defensaRepository;

    public DashboardService(ProyectoRepository proyectoRepository, DefensaRepository defensaRepository) {
        this.proyectoRepository = proyectoRepository;
        this.defensaRepository = defensaRepository;
    }

    public DashboardStatsDTO obtenerEstadisticas() {
        LocalDate hoy = LocalDate.now();
        LocalDate lunes = hoy.with(DayOfWeek.MONDAY);
        LocalDate domingo = lunes.plusDays(6);

        DashboardStatsDTO stats = new DashboardStatsDTO();
        stats.setProyectosPendientes(
                proyectoRepository.countByEstatus(Proyecto.EstatusProyecto.PENDIENTE));
        stats.setDefensasEstaSemana(
                defensaRepository.countByFechaBetween(lunes, domingo));
        stats.setReprogramaciones(
                defensaRepository.countByEstatus(Defensa.EstatusDefensa.REPROGRAMADA));
        return stats;
    }
}