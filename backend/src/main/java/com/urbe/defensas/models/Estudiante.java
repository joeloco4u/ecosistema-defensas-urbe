package com.urbe.defensas.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "estudiantes")
public class Estudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String cedula;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(nullable = false)
    private Boolean condicionado = false;

    @Column(name = "nivel_seminario", nullable = false, length = 20)
    private String nivelSeminario = "SEMINARIO_3";

    @Column(nullable = false)
    private Boolean pendiente = false;

    @Column(name = "requiere_entrevista", nullable = false)
    private Boolean requiereEntrevista = false;

    @Column(name = "trimestre_inscripcion", nullable = false, length = 20)
    private String trimestreInscripcion = "ENERO_MARZO";

    public Estudiante() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public Boolean getCondicionado() { return condicionado; }
    public void setCondicionado(Boolean condicionado) { this.condicionado = condicionado; }
    public String getNivelSeminario() { return nivelSeminario; }
    public void setNivelSeminario(String nivelSeminario) { this.nivelSeminario = nivelSeminario; }
    public Boolean getPendiente() { return pendiente; }
    public void setPendiente(Boolean pendiente) { this.pendiente = pendiente; }
    public Boolean getRequiereEntrevista() { return requiereEntrevista; }
    public void setRequiereEntrevista(Boolean requiereEntrevista) { this.requiereEntrevista = requiereEntrevista; }
    public String getTrimestreInscripcion() { return trimestreInscripcion; }
    public void setTrimestreInscripcion(String trimestreInscripcion) { this.trimestreInscripcion = trimestreInscripcion; }
}
