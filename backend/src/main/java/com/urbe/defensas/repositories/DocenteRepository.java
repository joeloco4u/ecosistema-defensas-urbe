package com.urbe.defensas.repositories;

import com.urbe.defensas.models.Docente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocenteRepository extends JpaRepository<Docente, Long> {
    Optional<Docente> findByCodigoInstitucional(String codigoInstitucional);
    List<Docente> findByActivoTrue();
    List<Docente> findByDepartamento(String departamento);
}
