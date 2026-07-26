package com.urbe.defensas.services;

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

    public TutorSugerido cambiarEstado(UUID id, String nuevoEstado) {
        TutorSugerido existente = tutorSugeridoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tutor sugerido no encontrado"));
        existente.setEstado(nuevoEstado);

        if (nuevoEstado.equalsIgnoreCase("APROBADO")) {
            Docente nuevoDocente = new Docente();
            nuevoDocente.setCodigoInstitucional(existente.getCedula());
            nuevoDocente.setNombreCompleto(existente.getNombre() + " " + existente.getApellido());
            nuevoDocente.setDepartamento(existente.getAreaInvestigacion());
            docenteRepository.save(nuevoDocente);
        }

        return tutorSugeridoRepository.save(existente);
    }
}
