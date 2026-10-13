package com.urbe.defensas.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Ocupación unificada de un bloque horario.
 *
 * Una fila representa a UN actor (docente o estudiante) o a un espacio ocupando
 * exactamente un bloque ({@link BloqueHora}) en una fecha concreta. Las clases, las
 * defensas y otros eventos comparten esta misma tabla, de modo que el motor de
 * programación puede razonar sobre un solo conjunto de hechos.
 *
 * Convención de llenado (modelo de "fan-out", igual que el horario institucional):
 *  - Una clase con un docente en un aula  -> 1 fila (docenteId + espacioId).
 *  - Una defensa con 3 docentes + N tesistas en un aula -> N+3 filas, todas con la
 *    misma (fecha, bloque, espacioId) y tipo DEFENSA.
 *  - Un bloqueo de espacio sin actor -> 1 fila solo con espacioId.
 * Así, la simple existencia de cualquier fila que coincida es un conflicto.
 */
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "ocupaciones", indexes = {
        @Index(name = "idx_ocupacion_fecha_bloque", columnList = "fecha, bloque_id"),
        @Index(name = "idx_ocupacion_espacio", columnList = "espacio_id"),
        @Index(name = "idx_ocupacion_docente", columnList = "docente_id"),
        @Index(name = "idx_ocupacion_estudiante", columnList = "estudiante_id")
})
public class Ocupacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Naturaleza de la ocupación: clase, defensa u otro evento. */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoOcupacion tipo;

    /** Día de la ocupación (fecha institucional del bloque). */
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    /** Bloque horario ocupado (relación con id_bloque). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bloque_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ocupacion_bloque"))
    private BloqueHora bloque;

    /** Espacio/aula ocupado (referencia a espacios_fisicos.id), si aplica. */
    @Column(name = "espacio_id")
    private UUID espacioId;

    /** Docente ocupado (referencia a docentes.id), si aplica. */
    @Column(name = "docente_id")
    private Long docenteId;

    /** Estudiante ocupado (referencia a estudiantes.id), si aplica. */
    @Column(name = "estudiante_id")
    private UUID estudianteId;

    /** Referencia libre: expediente, código de asignatura, id de defensa, etc. */
    @Column(name = "detalle", length = 255)
    private String detalle;

    public enum TipoOcupacion {
        CLASE, DEFENSA, EVENTO
    }

    public Ocupacion() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public TipoOcupacion getTipo() { return tipo; }
    public void setTipo(TipoOcupacion tipo) { this.tipo = tipo; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public BloqueHora getBloque() { return bloque; }
    public void setBloque(BloqueHora bloque) { this.bloque = bloque; }
    public UUID getEspacioId() { return espacioId; }
    public void setEspacioId(UUID espacioId) { this.espacioId = espacioId; }
    public Long getDocenteId() { return docenteId; }
    public void setDocenteId(Long docenteId) { this.docenteId = docenteId; }
    public UUID getEstudianteId() { return estudianteId; }
    public void setEstudianteId(UUID estudianteId) { this.estudianteId = estudianteId; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
}
