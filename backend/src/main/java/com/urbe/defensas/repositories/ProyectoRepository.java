package com.urbe.defensas.repositories;

import com.urbe.defensas.models.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, UUID> {
    List<Proyecto> findByEstatus(Proyecto.EstatusProyecto estatus);
    List<Proyecto> findByTutorId(Long tutorId);
    List<Proyecto> findByEstudianteId(UUID estudianteId);
}
