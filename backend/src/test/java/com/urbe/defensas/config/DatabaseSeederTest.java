package com.urbe.defensas.config;

import com.urbe.defensas.models.Estudiante;
import com.urbe.defensas.models.Proyecto;
import com.urbe.defensas.repositories.DocenteRepository;
import com.urbe.defensas.repositories.EstudianteRepository;
import com.urbe.defensas.repositories.EspacioFisicoRepository;
import com.urbe.defensas.repositories.ProyectoRepository;
import com.urbe.defensas.repositories.UsuarioRepository;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DatabaseSeederTest {

    @Test
    void backfillCreaYVinculaLosTresTesistas() {
        EspacioFisicoRepository espacioFisicoRepository = mock(EspacioFisicoRepository.class);
        DocenteRepository docenteRepository = mock(DocenteRepository.class);
        EstudianteRepository estudianteRepository = mock(EstudianteRepository.class);
        ProyectoRepository proyectoRepository = mock(ProyectoRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        Proyecto proyecto = new Proyecto();
        proyecto.setExpediente("O07-0126");

        when(estudianteRepository.findAll()).thenReturn(List.of());
        when(estudianteRepository.save(any(Estudiante.class))).thenAnswer(invocation -> {
            Estudiante estudiante = invocation.getArgument(0);
            estudiante.setId(UUID.randomUUID());
            return estudiante;
        });
        when(proyectoRepository.findByExpediente(anyString())).thenAnswer(invocation ->
                "O07-0126".equals(invocation.getArgument(0))
                        ? Optional.of(proyecto)
                        : Optional.empty());
        when(proyectoRepository.save(any(Proyecto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DatabaseSeeder seeder = new DatabaseSeeder(espacioFisicoRepository, docenteRepository,
                estudianteRepository, proyectoRepository, usuarioRepository, passwordEncoder);

        int actualizados = seeder.backfillTesistasDesdeAprobados();

        assertEquals(1, actualizados);
        assertNotNull(proyecto.getEstudiante());
        assertNotNull(proyecto.getEstudiante2());
        assertNotNull(proyecto.getEstudiante3());
        assertEquals("31211587", proyecto.getEstudiante().getCedula());
        assertEquals("30641788", proyecto.getEstudiante2().getCedula());
        assertEquals("28000295", proyecto.getEstudiante3().getCedula());
        verify(estudianteRepository, times(3)).save(any(Estudiante.class));
        verify(proyectoRepository).save(proyecto);
    }

    @Test
    void proyectoExponeLasRelacionesSecundariasComoColumnasUuid() throws NoSuchFieldException {
        Field estudiante2 = Proyecto.class.getDeclaredField("estudiante2");
        Field estudiante3 = Proyecto.class.getDeclaredField("estudiante3");

        assertNotNull(estudiante2.getAnnotation(ManyToOne.class));
        assertNotNull(estudiante3.getAnnotation(ManyToOne.class));
        assertTrue(estudiante2.getAnnotation(ManyToOne.class).optional());
        assertTrue(estudiante3.getAnnotation(ManyToOne.class).optional());
        assertEquals("estudiante2_id", estudiante2.getAnnotation(JoinColumn.class).name());
        assertEquals("estudiante3_id", estudiante3.getAnnotation(JoinColumn.class).name());
    }
}
