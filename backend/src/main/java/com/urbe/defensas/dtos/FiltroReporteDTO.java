package com.urbe.defensas.dtos;

import java.time.LocalDate;

/**
 * Parámetros de búsqueda del Módulo de Reportes.
 * Todos los campos son opcionales; los nulos simplemente no filtran.
 */
public class FiltroReporteDTO {

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Long docenteId;
    private RolReporte rol;
    private String escuela;

    public enum RolReporte {
        TUTOR, JURADO, AMBOS
    }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public Long getDocenteId() { return docenteId; }
    public void setDocenteId(Long docenteId) { this.docenteId = docenteId; }
    public RolReporte getRol() { return rol; }
    public void setRol(RolReporte rol) { this.rol = rol; }
    public String getEscuela() { return escuela; }
    public void setEscuela(String escuela) { this.escuela = escuela; }
}
