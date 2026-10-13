package com.urbe.defensas.repositories;

import com.urbe.defensas.models.EspacioFisico;
import com.urbe.defensas.models.EspacioFisico.TipoEspacio;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EspacioFisicoRepository extends JpaRepository<EspacioFisico, UUID> {
    List<EspacioFisico> findByEstatusOperativoTrue();
    List<EspacioFisico> findByTipo(TipoEspacio tipo);

    /**
     * Carga el aula tomando un bloqueo pesimista de escritura (SELECT ... FOR UPDATE).
     * Mientras la transacción que agenda una defensa lo mantiene, ninguna otra puede
     * reservar ese mismo aula, haciendo atómica la validación check-then-save.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM EspacioFisico e WHERE e.id = :id")
    Optional<EspacioFisico> findByIdForUpdate(@Param("id") UUID id);
}
