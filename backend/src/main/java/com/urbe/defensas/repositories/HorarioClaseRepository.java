package com.urbe.defensas.repositories;

import com.urbe.defensas.models.HorarioClase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.Collection;
import java.util.UUID;

@Repository
public interface HorarioClaseRepository extends JpaRepository<HorarioClase, UUID> {

    /**
     * ¿Existe alguna clase que colisione con (día, aula, docentes, estudiantes, franja)?
     *
     * Se consideran tres fuentes de conflicto:
     *   - el aula ({@code espacioId}),
     *   - alguno de los docentes del jurado/tutorías,
     *   - alguno de los estudiantes del proyecto.
     *
     * Las colecciones vacías se protegen con los flags {@code tieneDocentes} /
     * {@code tieneEstudiantes} (mismo patrón que {@code OcupacionRepository}) para no
     * evaluar un {@code IN} vacío.
     */
    @Query("""
            SELECT CASE WHEN COUNT(h) > 0 THEN TRUE ELSE FALSE END
            FROM HorarioClase h
            WHERE h.diaSemana = :dia
              AND h.horaInicio < :fin
              AND h.horaFin > :inicio
              AND (
                    h.espacioId = :espacioId
                 OR (:tieneDocentes = TRUE AND h.docenteId IN :docentesIds)
                 OR (:tieneEstudiantes = TRUE AND h.estudianteId IN :estudianteIds)
              )
            """)
    boolean existeChoqueDeClases(@Param("dia") String dia,
                                 @Param("espacioId") UUID espacioId,
                                 @Param("docentesIds") Collection<Long> docentesIds,
                                 @Param("tieneDocentes") boolean tieneDocentes,
                                 @Param("estudianteIds") Collection<UUID> estudianteIds,
                                 @Param("tieneEstudiantes") boolean tieneEstudiantes,
                                 @Param("inicio") LocalTime inicio,
                                 @Param("fin") LocalTime fin);
}
