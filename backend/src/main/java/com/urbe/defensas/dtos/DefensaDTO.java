package com.urbe.defensas.dtos;

import com.urbe.defensas.models.Defensa;
import com.urbe.defensas.models.EspacioFisico;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Proyección de lectura de {@link Defensa} para la API REST y el Módulo de Reportes.
 * Incrusta el {@link ProyectoDTO} completo (expediente + tesistas) y el espacio físico.
 */
public class DefensaDTO {

    private UUID id;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String estatus;
    private EspacioDTO espacioFisico;
    private ProyectoDTO proyecto;
    private Long juradoId;
    private Long tutorAcademicoId;
    private Long tutorMetodologicoId;

    public static DefensaDTO fromEntity(Defensa defensa) {
        if (defensa == null) return null;
        DefensaDTO dto = new DefensaDTO();
        dto.setId(defensa.getId());
        dto.setFecha(defensa.getFecha());
        dto.setHoraInicio(defensa.getHoraInicio());
        dto.setHoraFin(defensa.getHoraFin());
        dto.setEstatus(defensa.getEstatus() != null ? defensa.getEstatus().name() : null);
        dto.setEspacioFisico(EspacioDTO.fromEntity(defensa.getEspacioFisico()));
        dto.setProyecto(ProyectoDTO.fromEntity(defensa.getProyecto()));
        dto.setJuradoId(defensa.getJuradoId());
        dto.setTutorAcademicoId(defensa.getTutorAcademicoId());
        dto.setTutorMetodologicoId(defensa.getTutorMetodologicoId());
        return dto;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }
    public EspacioDTO getEspacioFisico() { return espacioFisico; }
    public void setEspacioFisico(EspacioDTO espacioFisico) { this.espacioFisico = espacioFisico; }
    public ProyectoDTO getProyecto() { return proyecto; }
    public void setProyecto(ProyectoDTO proyecto) { this.proyecto = proyecto; }
    public Long getJuradoId() { return juradoId; }
    public void setJuradoId(Long juradoId) { this.juradoId = juradoId; }
    public Long getTutorAcademicoId() { return tutorAcademicoId; }
    public void setTutorAcademicoId(Long tutorAcademicoId) { this.tutorAcademicoId = tutorAcademicoId; }
    public Long getTutorMetodologicoId() { return tutorMetodologicoId; }
    public void setTutorMetodologicoId(Long tutorMetodologicoId) { this.tutorMetodologicoId = tutorMetodologicoId; }

    public static class EspacioDTO {
        private UUID id;
        private String codigoAula;

        public static EspacioDTO fromEntity(EspacioFisico espacio) {
            if (espacio == null) return null;
            EspacioDTO dto = new EspacioDTO();
            dto.setId(espacio.getId());
            dto.setCodigoAula(espacio.getCodigoAula());
            return dto;
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getCodigoAula() { return codigoAula; }
        public void setCodigoAula(String codigoAula) { this.codigoAula = codigoAula; }
    }
}
