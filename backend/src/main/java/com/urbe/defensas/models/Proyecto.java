package com.urbe.defensas.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "proyectos")
public class Proyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(name = "expediente", length = 50)
    private String expediente;

    @Column(length = 100)
    private String escuela;

    @Column(name = "nivel_seminario", length = 30)
    private String nivelSeminario;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "estudiante_id", nullable = true)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "estudiante2_id", nullable = true)
    private Estudiante estudiante2;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "estudiante3_id", nullable = true)
    private Estudiante estudiante3;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id")
    private Docente tutor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_metodologico_id")
    private Docente tutorMetodologico;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstatusProyecto estatus;

    public enum EstatusProyecto {
        PENDIENTE, AGENDADO, DEFENDIDO
    }

    public Proyecto() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getExpediente() { return expediente; }
    public void setExpediente(String expediente) { this.expediente = expediente; }
    public String getEscuela() { return escuela; }
    public void setEscuela(String escuela) { this.escuela = escuela; }
    public String getNivelSeminario() { return nivelSeminario; }
    public void setNivelSeminario(String nivelSeminario) { this.nivelSeminario = nivelSeminario; }
    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }
    public Estudiante getEstudiante2() { return estudiante2; }
    public void setEstudiante2(Estudiante estudiante2) { this.estudiante2 = estudiante2; }
    public Estudiante getEstudiante3() { return estudiante3; }
    public void setEstudiante3(Estudiante estudiante3) { this.estudiante3 = estudiante3; }
    public Docente getTutor() { return tutor; }
    public void setTutor(Docente tutor) { this.tutor = tutor; }
    public Docente getTutorMetodologico() { return tutorMetodologico; }
    public void setTutorMetodologico(Docente tutorMetodologico) { this.tutorMetodologico = tutorMetodologico; }
    public EstatusProyecto getEstatus() { return estatus; }
    public void setEstatus(EstatusProyecto estatus) { this.estatus = estatus; }
}
