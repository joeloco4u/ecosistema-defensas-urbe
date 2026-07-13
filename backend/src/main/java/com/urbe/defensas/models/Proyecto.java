package com.urbe.defensas.models;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "proyectos")
public class Proyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String titulo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id")
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id")
    private Docente tutor;

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
    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }
    public Docente getTutor() { return tutor; }
    public void setTutor(Docente tutor) { this.tutor = tutor; }
    public EstatusProyecto getEstatus() { return estatus; }
    public void setEstatus(EstatusProyecto estatus) { this.estatus = estatus; }
}
