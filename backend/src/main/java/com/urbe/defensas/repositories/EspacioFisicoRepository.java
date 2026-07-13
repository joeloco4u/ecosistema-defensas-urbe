package com.urbe.defensas.repositories;

import com.urbe.defensas.models.EspacioFisico;
import com.urbe.defensas.models.EspacioFisico.TipoEspacio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EspacioFisicoRepository extends JpaRepository<EspacioFisico, UUID> {
    List<EspacioFisico> findByEstatusOperativoTrue();
    List<EspacioFisico> findByTipo(TipoEspacio tipo);
}
