package com.urbe.defensas.repositories;

import com.urbe.defensas.dtos.FiltroReporteDTO;
import com.urbe.defensas.models.Defensa;
import com.urbe.defensas.models.Docente;
import com.urbe.defensas.models.JuradoDefensa;
import com.urbe.defensas.models.Proyecto;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Constructor de la consulta dinámica del Módulo de Reportes.
 *
 * Regla de docente + rol:
 *  - TUTOR  -> el docente es el tutor del proyecto (proyecto.tutor).
 *  - JURADO -> el docente figura en jurados_defensa para esa defensa.
 *  - AMBOS  (o rol nulo) -> cualquiera de las dos condiciones anteriores.
 *
 * Se usan joins explícitos con {@link JoinType#LEFT} para no descartar
 * defensas cuyas relaciones opcionales (proyecto o tutor) sean nulas, y
 * para que todas las combinaciones de filtros generen SQL válido.
 */
public final class DefensaSpecification {

    private DefensaSpecification() {}

    public static Specification<Defensa> conFiltros(FiltroReporteDTO filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            FiltroReporteDTO filtroSeguro = filtro != null ? filtro : new FiltroReporteDTO();

            if (filtroSeguro.getFechaInicio() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fecha"), filtroSeguro.getFechaInicio()));
            }
            if (filtroSeguro.getFechaFin() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fecha"), filtroSeguro.getFechaFin()));
            }

            boolean filtrarPorEscuela =
                    filtroSeguro.getEscuela() != null && !filtroSeguro.getEscuela().isBlank();
            boolean filtrarPorDocente = filtroSeguro.getDocenteId() != null;

            // Un único LEFT JOIN a proyecto, compartido por el filtro de escuela y el de tutor.
            Join<Defensa, Proyecto> proyecto = null;
            if (filtrarPorEscuela || filtrarPorDocente) {
                proyecto = root.join("proyecto", JoinType.LEFT);
            }

            if (filtrarPorEscuela) {
                predicates.add(cb.equal(proyecto.get("escuela"), filtroSeguro.getEscuela().trim()));
            }

            if (filtrarPorDocente) {
                // LEFT JOIN para no perder defensas cuyo proyecto no tenga tutor asignado.
                Join<Proyecto, Docente> tutor = proyecto.join("tutor", JoinType.LEFT);
                Predicate esTutor = cb.equal(tutor.get("id"), filtroSeguro.getDocenteId());

                Subquery<UUID> juradosDeDocente = query.subquery(UUID.class);
                Root<JuradoDefensa> jurado = juradosDeDocente.from(JuradoDefensa.class);
                Join<JuradoDefensa, Docente> docenteJurado = jurado.join("docente", JoinType.INNER);
                juradosDeDocente
                        .select(jurado.get("defensa").get("id"))
                        .where(cb.equal(docenteJurado.get("id"), filtroSeguro.getDocenteId()));
                Predicate esJurado = root.get("id").in(juradosDeDocente);

                FiltroReporteDTO.RolReporte rol =
                        filtroSeguro.getRol() != null ? filtroSeguro.getRol() : FiltroReporteDTO.RolReporte.AMBOS;
                switch (rol) {
                    case TUTOR -> predicates.add(esTutor);
                    case JURADO -> predicates.add(esJurado);
                    case AMBOS -> predicates.add(cb.or(esTutor, esJurado));
                }
            }

            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
