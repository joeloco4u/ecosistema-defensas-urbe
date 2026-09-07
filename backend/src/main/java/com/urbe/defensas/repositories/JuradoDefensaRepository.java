package com.urbe.defensas.repositories;

import com.urbe.defensas.models.JuradoDefensa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JuradoDefensaRepository extends JpaRepository<JuradoDefensa, UUID> {
    List<JuradoDefensa> findByDocenteId(Long docenteId);
}
