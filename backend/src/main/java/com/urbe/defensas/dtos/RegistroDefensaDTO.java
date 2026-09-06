package com.urbe.defensas.dtos;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class RegistroDefensaDTO {
    @NotNull private UUID proyectoId;
    @NotNull private UUID espacioId;
    @NotNull private LocalDate fecha;
    @NotNull private LocalTime horaInicio;
    @NotNull private LocalTime horaFin;
    private Long tutorAcademicoId;
    private Long tutorMetodologicoId;
    private Long juradoId;

    public UUID getProyectoId() { return proyectoId; }
    public void setProyectoId(UUID proyectoId) { this.proyectoId = proyectoId; }
    public UUID getEspacioId() { return espacioId; }
    public void setEspacioId(UUID espacioId) { this.espacioId = espacioId; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
    public Long getTutorAcademicoId() { return tutorAcademicoId; }
    public void setTutorAcademicoId(Long tutorAcademicoId) { this.tutorAcademicoId = tutorAcademicoId; }
    public Long getTutorMetodologicoId() { return tutorMetodologicoId; }
    public void setTutorMetodologicoId(Long tutorMetodologicoId) { this.tutorMetodologicoId = tutorMetodologicoId; }
    public Long getJuradoId() { return juradoId; }
    public void setJuradoId(Long juradoId) { this.juradoId = juradoId; }
}