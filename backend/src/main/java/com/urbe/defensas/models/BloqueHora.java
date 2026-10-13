package com.urbe.defensas.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalTime;
import java.util.Objects;

/**
 * Bloque horario institucional.
 *
 * Es la fuente única de verdad para los rangos de tiempo: las defensas, las clases
 * y cualquier ocupación referencian un bloque por su id en lugar de guardar horas
 * sueltas (lo que evita datos corruptos como las horas PM persistidas como AM).
 *
 * El identificador es el código institucional ({@code id_bloque}); se usa como clave
 * natural porque proviene del sistema de horarios de la universidad.
 */
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "bloques_hora")
public class BloqueHora {

    @Id
    @Column(name = "id_bloque", length = 50, nullable = false, updatable = false)
    private String id;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(name = "turno", length = 20)
    private String turno;

    public BloqueHora() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
    public String getTurno() { return turno; }
    public void setTurno(String turno) { this.turno = turno; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BloqueHora)) return false;
        BloqueHora that = (BloqueHora) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
