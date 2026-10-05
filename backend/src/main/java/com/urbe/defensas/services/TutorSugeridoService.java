package com.urbe.defensas.services;

import com.urbe.defensas.exceptions.ConflictException;
import com.urbe.defensas.models.Docente;
import com.urbe.defensas.models.TutorSugerido;
import com.urbe.defensas.repositories.DocenteRepository;
import com.urbe.defensas.repositories.TutorSugeridoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TutorSugeridoService {

    private final TutorSugeridoRepository tutorSugeridoRepository;
    private final DocenteRepository docenteRepository;

    public TutorSugeridoService(TutorSugeridoRepository tutorSugeridoRepository, DocenteRepository docenteRepository) {
        this.tutorSugeridoRepository = tutorSugeridoRepository;
        this.docenteRepository = docenteRepository;
    }

    public TutorSugerido crear(TutorSugerido tutor) {
        tutor.setEstado("PENDIENTE");
        return tutorSugeridoRepository.save(tutor);
    }

    public List<TutorSugerido> listarPendientes() {
        return tutorSugeridoRepository.findByEstado("PENDIENTE");
    }

    public List<TutorSugerido> listarPorProyecto(UUID proyectoId) {
        return tutorSugeridoRepository.findByProyectoId(proyectoId);
    }

    public List<TutorSugerido> listarPendientesPorProyecto(UUID proyectoId) {
        return tutorSugeridoRepository.findByProyectoIdAndEstado(proyectoId, "PENDIENTE");
    }

    public TutorSugerido cambiarEstado(UUID id, String nuevoEstado) {
        TutorSugerido existente = tutorSugeridoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tutor sugerido no encontrado"));

        if (!"PENDIENTE".equalsIgnoreCase(existente.getEstado())) {
            throw new ConflictException("Solo se pueden modificar sugerencias en estado PENDIENTE");
        }

        existente.setEstado(nuevoEstado);

        if (nuevoEstado.equalsIgnoreCase("APROBADO")
                && existeCedula(existente) && !docenteRepository.existsByCodigoInstitucional(existente.getCedula())) {
            Docente nuevoDocente = new Docente();
            nuevoDocente.setCodigoInstitucional(existente.getCedula());
            nuevoDocente.setNombreCompleto(existente.getNombre() + " " + existente.getApellido());
            nuevoDocente.setDepartamento(existente.getAreaInvestigacion());
            docenteRepository.save(nuevoDocente);
        }

        return tutorSugeridoRepository.save(existente);
    }

    private boolean existeCedula(TutorSugerido tutor) {
        return tutor.getCedula() != null && !tutor.getCedula().trim().isEmpty();
    }
}
