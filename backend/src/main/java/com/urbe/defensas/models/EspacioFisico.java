package com.urbe.defensas.models;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "espacios_fisicos")
public class EspacioFisico {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "codigo_aula", nullable = false, unique = true, length = 30)
    private String codigoAula;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoEspacio tipo;

    @Column(nullable = false)
    private Integer capacidad;

    @Column(name = "estatus_operativo", nullable = false)
    private Boolean estatusOperativo = true;

    public enum TipoEspacio {
        AULA, SALA_CONFERENCIA
    }

    public EspacioFisico() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCodigoAula() { return codigoAula; }
    public void setCodigoAula(String codigoAula) { this.codigoAula = codigoAula; }
    public TipoEspacio getTipo() { return tipo; }
    public void setTipo(TipoEspacio tipo) { this.tipo = tipo; }
    public Integer getCapacidad() { return capacidad; }
    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }
    public Boolean getEstatusOperativo() { return estatusOperativo; }
    public void setEstatusOperativo(Boolean estatusOperativo) { this.estatusOperativo = estatusOperativo; }
}
