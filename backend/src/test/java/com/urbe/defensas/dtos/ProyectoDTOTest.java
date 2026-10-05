package com.urbe.defensas.dtos;

import com.urbe.defensas.models.Docente;
import com.urbe.defensas.models.Estudiante;
import com.urbe.defensas.models.Proyecto;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProyectoDTOTest {

    @Test
    void fromEntityExponeExpedienteYLosTresTesistas() {
        Estudiante tesista1 = estudiante("V-11111111", "Ana", "Pérez");
        Estudiante tesista2 = estudiante("V-22222222", "Luis", "Gómez");
        Estudiante tesista3 = estudiante("V-33333333", "María", "Rojas");

        Docente tutor = new Docente();
        tutor.setId(7L);
        tutor.setNombreCompleto("Carlos Mendoza");

        Proyecto proyecto = new Proyecto();
        proyecto.setId(UUID.randomUUID());
        proyecto.setTitulo("Sistema de Gestión de Defensas");
        proyecto.setExpediente("C17-0126");
        proyecto.setEscuela("COMPUTACIÓN");
        proyecto.setNivelSeminario("SEMINARIO_3");
        proyecto.setEstatus(Proyecto.EstatusProyecto.PENDIENTE);
        proyecto.setEstudiante(tesista1);
        proyecto.setEstudiante2(tesista2);
        proyecto.setEstudiante3(tesista3);
        proyecto.setTutor(tutor);

        ProyectoDTO dto = ProyectoDTO.fromEntity(proyecto);

        assertEquals(proyecto.getId(), dto.getId());
        assertEquals("C17-0126", dto.getExpediente());
        assertEquals("COMPUTACIÓN", dto.getEscuela());
        assertEquals("SEMINARIO_3", dto.getNivelSeminario());
        assertEquals("PENDIENTE", dto.getEstatus());

        assertNotNull(dto.getEstudiante());
        assertNotNull(dto.getEstudiante2());
        assertNotNull(dto.getEstudiante3());
        assertEquals(tesista1.getId(), dto.getEstudiante().getId());
        assertEquals("Ana", dto.getEstudiante().getNombres());
        assertEquals("V-22222222", dto.getEstudiante2().getCedula());
        assertEquals("Rojas", dto.getEstudiante3().getApellidos());

        assertNotNull(dto.getTutor());
        assertEquals(7L, dto.getTutor().getId());
        assertEquals("Carlos Mendoza", dto.getTutor().getNombreCompleto());
        assertNull(dto.getTutorMetodologico());
    }

    @Test
    void fromEntityToleraProyectoSinEquipoNiExpediente() {
        Proyecto proyecto = new Proyecto();
        proyecto.setEstatus(Proyecto.EstatusProyecto.PENDIENTE);

        ProyectoDTO dto = ProyectoDTO.fromEntity(proyecto);

        assertNull(dto.getExpediente());
        assertNull(dto.getEstudiante());
        assertNull(dto.getEstudiante2());
        assertNull(dto.getEstudiante3());
        assertNull(dto.getTutor());
        assertNull(ProyectoDTO.fromEntity(null));
    }

    private static Estudiante estudiante(String cedula, String nombres, String apellidos) {
        Estudiante estudiante = new Estudiante();
        estudiante.setId(UUID.randomUUID());
        estudiante.setCedula(cedula);
        estudiante.setNombres(nombres);
        estudiante.setApellidos(apellidos);
        return estudiante;
    }
}
