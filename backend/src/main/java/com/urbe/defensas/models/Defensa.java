package com.urbe.defensas.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "defensas")
public class Defensa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", nullable = false, unique = true)
    private Proyecto proyecto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "espacio_id")
    private EspacioFisico espacioFisico;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(name = "jurado_id")
    private Long juradoId;

    @Column(name = "tutor_academico_id")
    private Long tutorAcademicoId;

    @Column(name = "tutor_metodologico_id")
    private Long tutorMetodologicoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstatusDefensa estatus;

    public enum EstatusDefensa {
        PROGRAMADA, REPROGRAMADA, FINALIZADA
    }

    public Defensa() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Proyecto getProyecto() { return proyecto; }
    public void setProyecto(Proyecto proyecto) { this.proyecto = proyecto; }
    public EspacioFisico getEspacioFisico() { return espacioFisico; }
    public void setEspacioFisico(EspacioFisico espacioFisico) { this.espacioFisico = espacioFisico; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
    public Long getJuradoId() { return juradoId; }
    public void setJuradoId(Long juradoId) { this.juradoId = juradoId; }
    public Long getTutorAcademicoId() { return tutorAcademicoId; }
    public void setTutorAcademicoId(Long tutorAcademicoId) { this.tutorAcademicoId = tutorAcademicoId; }
    public Long getTutorMetodologicoId() { return tutorMetodologicoId; }
    public void setTutorMetodologicoId(Long tutorMetodologicoId) { this.tutorMetodologicoId = tutorMetodologicoId; }
    public EstatusDefensa getEstatus() { return estatus; }
    public void setEstatus(EstatusDefensa estatus) { this.estatus = estatus; }
}
