package com.urbe.defensas.repositories;

import com.urbe.defensas.models.BloqueHora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Repositorio del catálogo de bloques horarios institucionales.
 *
 * Contiene la consulta central del algoritmo de intersección: devuelve los bloques
 * en los que NINGUNO de los actores requeridos (docentes: tutores + jurados, y
 * estudiantes: tesistas) ni el espacio solicitado tiene una ocupación registrada
 * para la fecha indicada.
 */
@Repository
public interface BloqueHoraRepository extends JpaRepository<BloqueHora, String> {

    List<BloqueHora> findAllByOrderByHoraInicioAsc();

    /**
     * Bloques libres para el conjunto de actores + espacio en una fecha dada.
     *
     * Intersección (NOT EXISTS): un bloque es candidato solo si NO existe ninguna
     * {@link com.urbe.defensas.models.Ocupacion} que, para esa fecha y ese bloque,
     * colisione con el espacio o con cualquiera de los actores.
     *
     * Los parámetros booleanos {@code tieneDocentes} / {@code tieneEstudiantes} se
     * pasan como {@code true} únicamente cuando la colección correspondiente NO está
     * vacía. Esto evita generar predicados {@code IN ()} y mantiene la consulta
     * portable entre proveedores JPA. ({@code :espacioId} puede ser {@code null}: en
     * ese caso el predicado de espacio simplemente no aplica.)
     */
    @Query("""
            SELECT b
            FROM BloqueHora b
            WHERE NOT EXISTS (
                SELECT 1
                FROM Ocupacion o
                WHERE o.fecha = :fecha
                  AND o.bloque.id = b.id
                  AND (
                        (:espacioId IS NOT NULL AND o.espacioId = :espacioId)
                     OR (:tieneDocentes = TRUE AND o.docenteId IN :docentesIds)
                     OR (:tieneEstudiantes = TRUE AND o.estudianteId IN :estudiantesIds)
                  )
            )
            ORDER BY b.horaInicio ASC
            """)
    List<BloqueHora> findBloquesDisponibles(@Param("fecha") LocalDate fecha,
                                            @Param("espacioId") UUID espacioId,
                                            @Param("docentesIds") Collection<Long> docentesIds,
                                            @Param("tieneDocentes") boolean tieneDocentes,
                                            @Param("estudiantesIds") Collection<UUID> estudiantesIds,
                                            @Param("tieneEstudiantes") boolean tieneEstudiantes);
}
