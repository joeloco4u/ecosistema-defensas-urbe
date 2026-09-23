package com.urbe.defensas.repositories;

import com.urbe.defensas.models.Defensa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DefensaRepository extends JpaRepository<Defensa, UUID> {
    List<Defensa> findByEstatus(Defensa.EstatusDefensa estatus);
    List<Defensa> findByEspacioFisicoIdAndFecha(UUID espacioId, LocalDate fecha);
    List<Defensa> findByFecha(LocalDate fecha);

    List<Defensa> findByFechaOrderByHoraInicioAsc(LocalDate fecha);

    List<Defensa> findByProyectoId(UUID proyectoId);

    @Query("SELECT d FROM Defensa d WHERE d.juradoId = :tutorId OR d.tutorAcademicoId = :tutorId OR d.tutorMetodologicoId = :tutorId")
    List<Defensa> findByTutorId(@Param("tutorId") Long tutorId);

    @Query("SELECT d FROM Defensa d WHERE d.proyecto.escuela = :escuela")
    List<Defensa> findByEscuela(@Param("escuela") String escuela);

    @Query("""
            SELECT d FROM Defensa d
            WHERE (:tutorId IS NULL OR d.juradoId = :tutorId OR d.tutorAcademicoId = :tutorId OR d.tutorMetodologicoId = :tutorId)
            AND (:proyectoId IS NULL OR d.proyecto.id = :proyectoId)
            AND (:escuela IS NULL OR d.proyecto.escuela = :escuela)
            """)
    List<Defensa> buscarConFiltros(@Param("tutorId") Long tutorId,
                                   @Param("proyectoId") UUID proyectoId,
                                   @Param("escuela") String escuela);

    @Query("""
            SELECT CASE WHEN COUNT(d) > 0 THEN TRUE ELSE FALSE END
            FROM Defensa d
            WHERE d.fecha = :fecha
              AND d.horaInicio < :fin
              AND d.horaFin > :inicio
              AND (d.espacioFisico.id = :espacioId
                   OR d.juradoId IN :docentesIds
                   OR d.tutorAcademicoId IN :docentesIds
                   OR d.tutorMetodologicoId IN :docentesIds)
            """)
    boolean existeDefensaEnHorario(@Param("fecha") LocalDate fecha,
                                   @Param("espacioId") UUID espacioId,
                                   @Param("docentesIds") List<Long> docentesIds,
                                   @Param("inicio") LocalTime inicio,
                                   @Param("fin") LocalTime fin);
}
