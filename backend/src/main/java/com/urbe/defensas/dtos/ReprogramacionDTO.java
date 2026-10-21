package com.urbe.defensas.dtos;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class ReprogramacionDTO {
    @NotNull private UUID espacioId;
    @NotNull @FutureOrPresent private LocalDate fecha;
    @NotNull private LocalTime horaInicio;
    @NotNull private LocalTime horaFin;
    @NotNull @Size(min = 3, max = 3) private List<Long> juradosIds;

    public UUID getEspacioId() { return espacioId; }
    public void setEspacioId(UUID espacioId) { this.espacioId = espacioId; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
    public List<Long> getJuradosIds() { return juradosIds; }
    public void setJuradosIds(List<Long> juradosIds) { this.juradosIds = juradosIds; }
}