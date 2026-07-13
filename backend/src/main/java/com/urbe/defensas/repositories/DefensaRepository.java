package com.urbe.defensas.repositories;

import com.urbe.defensas.models.Defensa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DefensaRepository extends JpaRepository<Defensa, UUID> {
    List<Defensa> findByEstatus(Defensa.EstatusDefensa estatus);
    List<Defensa> findByEspacioFisicoIdAndFecha(UUID espacioId, LocalDate fecha);
    List<Defensa> findByFecha(LocalDate fecha);
    List<Defensa> findByProyectoId(UUID proyectoId);
}
