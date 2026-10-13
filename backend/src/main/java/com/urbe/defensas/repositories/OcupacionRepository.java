package com.urbe.defensas.repositories;

import com.urbe.defensas.models.Ocupacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Repositorio de ocupaciones unificadas (clases, defensas y otros eventos).
 *
 * Además del CRUD, ofrece la verificación puntual de conflicto usada al confirmar
 * o reprogramar una defensa (validación de escritura sobre un bloque concreto).
 */
@Repository
public interface OcupacionRepository extends JpaRepository<Ocupacion, UUID> {

    List<Ocupacion> findByFechaAndBloque_Id(LocalDate fecha, String bloqueId);

    List<Ocupacion> findByDetalle(String detalle);

    void deleteByDetalle(String detalle);

    /**
     * ¿Existe ya una ocupación que colisione con (fecha, bloque) para el espacio o
     * alguno de los actores? Devuelve {@code true} si el bloque NO está libre.
     *
     * Mismos guardas de colecciones vacías que en el catálogo de bloques.
     */
    @Query("""
            SELECT CASE WHEN COUNT(o) > 0 THEN TRUE ELSE FALSE END
            FROM Ocupacion o
            WHERE o.fecha = :fecha
              AND o.bloque.id = :bloqueId
              AND (
                    (:espacioId IS NOT NULL AND o.espacioId = :espacioId)
                 OR (:tieneDocentes = TRUE AND o.docenteId IN :docentesIds)
                 OR (:tieneEstudiantes = TRUE AND o.estudianteId IN :estudiantesIds)
              )
            """)
    boolean existeConflictoEnBloque(@Param("fecha") LocalDate fecha,
                                    @Param("bloqueId") String bloqueId,
                                    @Param("espacioId") UUID espacioId,
                                    @Param("docentesIds") Collection<Long> docentesIds,
                                    @Param("tieneDocentes") boolean tieneDocentes,
                                    @Param("estudiantesIds") Collection<UUID> estudiantesIds,
                                    @Param("tieneEstudiantes") boolean tieneEstudiantes);

    /** Ocupaciones existentes para un conjunto de bloques en una fecha (auditoría/depuración). */
    List<Ocupacion> findByFechaAndBloque_IdIn(LocalDate fecha, Collection<String> bloqueIds);
}
