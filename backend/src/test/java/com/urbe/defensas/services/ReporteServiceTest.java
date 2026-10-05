package com.urbe.defensas.services;

import com.urbe.defensas.dtos.DefensaDTO;
import com.urbe.defensas.dtos.FiltroReporteDTO;
import com.urbe.defensas.models.Defensa;
import com.urbe.defensas.models.EspacioFisico;
import com.urbe.defensas.models.Estudiante;
import com.urbe.defensas.models.Proyecto;
import com.urbe.defensas.repositories.DefensaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReporteServiceTest {

    @Test
    void generarReporteMapeaDefensasConProyectoCompleto() {
        DefensaRepository defensaRepository = mock(DefensaRepository.class);
        ReporteService service = new ReporteService(defensaRepository);

        Estudiante tesista1 = new Estudiante();
        tesista1.setId(UUID.randomUUID());
        tesista1.setCedula("30382684");
        tesista1.setNombres("Fabio");
        tesista1.setApellidos("Benavides");
        Estudiante tesista2 = new Estudiante();
        tesista2.setId(UUID.randomUUID());
        tesista2.setCedula("31547869");
        tesista2.setNombres("Jose");
        tesista2.setApellidos("Vilchez");

        Proyecto proyecto = new Proyecto();
        proyecto.setId(UUID.randomUUID());
        proyecto.setTitulo("Sistema B2B");
        proyecto.setExpediente("C17-0126");
        proyecto.setEscuela("COMPUTACIÓN");
        proyecto.setEstatus(Proyecto.EstatusProyecto.AGENDADO);
        proyecto.setEstudiante(tesista1);
        proyecto.setEstudiante2(tesista2);

        EspacioFisico espacio = new EspacioFisico();
        espacio.setCodigoAula("C-412");

        Defensa defensa = new Defensa();
        defensa.setId(UUID.randomUUID());
        defensa.setProyecto(proyecto);
        defensa.setEspacioFisico(espacio);
        defensa.setFecha(LocalDate.of(2026, 5, 25));
        defensa.setHoraInicio(LocalTime.of(8, 0));
        defensa.setHoraFin(LocalTime.of(9, 0));
        defensa.setEstatus(Defensa.EstatusDefensa.PROGRAMADA);
        defensa.setJuradoId(9L);

        when(defensaRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of(defensa));

        FiltroReporteDTO filtro = new FiltroReporteDTO();
        filtro.setFechaInicio(LocalDate.of(2026, 5, 1));
        filtro.setFechaFin(LocalDate.of(2026, 5, 31));
        filtro.setDocenteId(5L);
        filtro.setRol(FiltroReporteDTO.RolReporte.JURADO);
        filtro.setEscuela("COMPUTACIÓN");

        List<DefensaDTO> resultado = service.generarReporte(filtro);

        assertEquals(1, resultado.size());
        DefensaDTO dto = resultado.get(0);
        assertEquals(defensa.getId(), dto.getId());
        assertEquals("PROGRAMADA", dto.getEstatus());
        assertEquals(9L, dto.getJuradoId());
        assertNotNull(dto.getEspacioFisico());
        assertEquals("C-412", dto.getEspacioFisico().getCodigoAula());
        assertNotNull(dto.getProyecto());
        assertEquals("C17-0126", dto.getProyecto().getExpediente());
        assertNotNull(dto.getProyecto().getEstudiante());
        assertNotNull(dto.getProyecto().getEstudiante2());
        assertEquals("30382684", dto.getProyecto().getEstudiante().getCedula());
        assertEquals("31547869", dto.getProyecto().getEstudiante2().getCedula());
        verify(defensaRepository).findAll(any(Specification.class), any(org.springframework.data.domain.Sort.class));
    }

    @Test
    void generarReporteRechazaRangoDeFechasInvertido() {
        DefensaRepository defensaRepository = mock(DefensaRepository.class);
        ReporteService service = new ReporteService(defensaRepository);

        FiltroReporteDTO filtro = new FiltroReporteDTO();
        filtro.setFechaInicio(LocalDate.of(2026, 6, 1));
        filtro.setFechaFin(LocalDate.of(2026, 5, 1));

        IllegalArgumentException error =
                assertThrows(IllegalArgumentException.class, () -> service.generarReporte(filtro));
        assertTrue(error.getMessage().contains("fecha de inicio"));
    }

    @Test
    void generarReporteToleraFiltroNulo() {
        DefensaRepository defensaRepository = mock(DefensaRepository.class);
        ReporteService service = new ReporteService(defensaRepository);
        when(defensaRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of());

        assertTrue(service.generarReporte(null).isEmpty());
    }
}
