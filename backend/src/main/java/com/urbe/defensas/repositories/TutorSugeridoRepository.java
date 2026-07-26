package com.urbe.defensas.repositories;

import com.urbe.defensas.models.TutorSugerido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TutorSugeridoRepository extends JpaRepository<TutorSugerido, UUID> {
    List<TutorSugerido> findByEstado(String estado);
}
