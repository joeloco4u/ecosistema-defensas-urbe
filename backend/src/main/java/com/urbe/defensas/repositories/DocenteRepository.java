package com.urbe.defensas.repositories;

import com.urbe.defensas.models.Docente;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocenteRepository extends JpaRepository<Docente, Long> {
    Optional<Docente> findByCodigoInstitucional(String codigoInstitucional);
    boolean existsByCodigoInstitucional(String codigoInstitucional);
    List<Docente> findByActivoTrue();
    List<Docente> findByDepartamento(String departamento);
    List<Docente> findByEstado(String estado);

    @Query(value = """
            SELECT *
            FROM docentes d
            WHERE ltrim(regexp_replace(d.cedula, '[^0-9]', '', 'g'), '0')
                = ltrim(regexp_replace(:cedula, '[^0-9]', '', 'g'), '0')
            LIMIT 1
            """, nativeQuery = true)
    Optional<Docente> findByCedulaNormalizada(@Param("cedula") String cedula);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Docente d WHERE d.id IN :ids ORDER BY d.id")
    List<Docente> findByIdInForUpdate(@Param("ids") Collection<Long> ids);
}
