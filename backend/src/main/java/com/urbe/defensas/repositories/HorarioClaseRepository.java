package com.urbe.defensas.repositories;

import com.urbe.defensas.models.HorarioClase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface HorarioClaseRepository extends JpaRepository<HorarioClase, UUID> {

    @Query("""
            SELECT CASE WHEN COUNT(h) > 0 THEN TRUE ELSE FALSE END
            FROM HorarioClase h
            WHERE h.diaSemana = :dia
              AND h.horaInicio < :fin
              AND h.horaFin > :inicio
              AND (h.espacioId = :espacioId OR h.docenteId IN :docentesIds)
            """)
    boolean existeChoqueDeClases(@Param("dia") String dia,
                                @Param("espacioId") UUID espacioId,
                                @Param("docentesIds") List<Long> docentesIds,
                                @Param("inicio") LocalTime inicio,
                                @Param("fin") LocalTime fin);
}