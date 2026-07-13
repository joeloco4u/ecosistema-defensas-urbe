package com.urbe.defensas.dtos;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class SugerenciaHorarioDTO {

    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private UUID espacioFisicoId;
    private String codigoAula;

    public SugerenciaHorarioDTO() {}

    public SugerenciaHorarioDTO(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                                 UUID espacioFisicoId, String codigoAula) {
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.espacioFisicoId = espacioFisicoId;
        this.codigoAula = codigoAula;
    }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
    public UUID getEspacioFisicoId() { return espacioFisicoId; }
    public void setEspacioFisicoId(UUID espacioFisicoId) { this.espacioFisicoId = espacioFisicoId; }
    public String getCodigoAula() { return codigoAula; }
    public void setCodigoAula(String codigoAula) { this.codigoAula = codigoAula; }
}
