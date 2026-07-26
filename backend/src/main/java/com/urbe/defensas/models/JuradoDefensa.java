package com.urbe.defensas.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "jurados_defensa")
public class JuradoDefensa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defensa_id", nullable = false)
    private Defensa defensa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_id", nullable = false)
    private Docente docente;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol_jurado", nullable = false, length = 20)
    private RolJurado rolJurado;

    public enum RolJurado {
        PRESIDENTE, PRINCIPAL, SUPLENTE
    }

    public JuradoDefensa() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Defensa getDefensa() { return defensa; }
    public void setDefensa(Defensa defensa) { this.defensa = defensa; }
    public Docente getDocente() { return docente; }
    public void setDocente(Docente docente) { this.docente = docente; }
    public RolJurado getRolJurado() { return rolJurado; }
    public void setRolJurado(RolJurado rolJurado) { this.rolJurado = rolJurado; }
}
