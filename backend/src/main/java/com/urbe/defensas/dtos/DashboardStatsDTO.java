package com.urbe.defensas.dtos;

public class DashboardStatsDTO {

    private long proyectosPendientes;
    private long defensasEstaSemana;
    private long reprogramaciones;

    public long getProyectosPendientes() { return proyectosPendientes; }
    public void setProyectosPendientes(long proyectosPendientes) { this.proyectosPendientes = proyectosPendientes; }
    public long getDefensasEstaSemana() { return defensasEstaSemana; }
    public void setDefensasEstaSemana(long defensasEstaSemana) { this.defensasEstaSemana = defensasEstaSemana; }
    public long getReprogramaciones() { return reprogramaciones; }
    public void setReprogramaciones(long reprogramaciones) { this.reprogramaciones = reprogramaciones; }
}