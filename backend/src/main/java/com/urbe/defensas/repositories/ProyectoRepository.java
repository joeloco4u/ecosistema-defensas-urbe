package com.urbe.defensas.repositories;

import com.urbe.defensas.models.Proyecto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, UUID> {
    List<Proyecto> findByEstatus(Proyecto.EstatusProyecto estatus);
    long countByEstatus(Proyecto.EstatusProyecto estatus);
    List<Proyecto> findByTutorId(Long tutorId);
    List<Proyecto> findByEstudianteId(UUID estudianteId);
    List<Proyecto> findByNivelSeminario(String nivelSeminario);
    List<Proyecto> findByNivelSeminarioIsNull();
    Optional<Proyecto> findByExpediente(String expediente);

    @EntityGraph(attributePaths = {
            "estudiante",
            "estudiante2",
            "estudiante3",
            "tutor",
            "tutorMetodologico"
    })
    @Query("SELECT p FROM Proyecto p")
    List<Proyecto> findAllConRelaciones();

    @Query("SELECT DISTINCT p.escuela FROM Proyecto p WHERE p.escuela IS NOT NULL ORDER BY p.escuela")
    List<String> findDistinctEscuelas();

    @Query("""
            SELECT p.escuela, COUNT(p)
            FROM Proyecto p
            WHERE p.escuela IS NOT NULL AND p.escuela <> ''
            GROUP BY p.escuela
            ORDER BY p.escuela
            """)
    List<Object[]> countByEscuela();
}
