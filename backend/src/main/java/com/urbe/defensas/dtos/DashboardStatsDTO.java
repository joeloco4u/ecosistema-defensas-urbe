package com.urbe.defensas.dtos;

import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardStatsDTO {

    private long totalProyectos;
    private long proyectosPendientes;
    private long proyectosAgendados;
    private long proyectosDefendidos;
    private long defensasEstaSemana;
    private long reprogramaciones;
    private Map<String, Long> proyectosPorEscuela = new LinkedHashMap<>();

    public long getTotalProyectos() { return totalProyectos; }
    public void setTotalProyectos(long totalProyectos) { this.totalProyectos = totalProyectos; }
    public long getProyectosPendientes() { return proyectosPendientes; }
    public void setProyectosPendientes(long proyectosPendientes) { this.proyectosPendientes = proyectosPendientes; }
    public long getProyectosAgendados() { return proyectosAgendados; }
    public void setProyectosAgendados(long proyectosAgendados) { this.proyectosAgendados = proyectosAgendados; }
    public long getProyectosDefendidos() { return proyectosDefendidos; }
    public void setProyectosDefendidos(long proyectosDefendidos) { this.proyectosDefendidos = proyectosDefendidos; }
    public long getDefensasEstaSemana() { return defensasEstaSemana; }
    public void setDefensasEstaSemana(long defensasEstaSemana) { this.defensasEstaSemana = defensasEstaSemana; }
    public long getReprogramaciones() { return reprogramaciones; }
    public void setReprogramaciones(long reprogramaciones) { this.reprogramaciones = reprogramaciones; }
    public Map<String, Long> getProyectosPorEscuela() { return proyectosPorEscuela; }
    public void setProyectosPorEscuela(Map<String, Long> proyectosPorEscuela) {
        this.proyectosPorEscuela = proyectosPorEscuela;
    }
}