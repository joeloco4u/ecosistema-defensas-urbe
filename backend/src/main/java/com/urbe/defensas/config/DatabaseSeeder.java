package com.urbe.defensas.config;

import com.urbe.defensas.models.*;
import com.urbe.defensas.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final EspacioFisicoRepository espacioFisicoRepository;
    private final DocenteRepository docenteRepository;
    private final EstudianteRepository estudianteRepository;
    private final ProyectoRepository proyectoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(EspacioFisicoRepository espacioFisicoRepository,
                          DocenteRepository docenteRepository,
                          EstudianteRepository estudianteRepository,
                          ProyectoRepository proyectoRepository,
                          UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder) {
        this.espacioFisicoRepository = espacioFisicoRepository;
        this.docenteRepository = docenteRepository;
        this.estudianteRepository = estudianteRepository;
        this.proyectoRepository = proyectoRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        if (usuarioRepository.count() == 0) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setEmail("admin@urbe.edu");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setNombreCompleto("Coordinador General");
            admin.setRol("COORDINADOR");
            admin.setActivo(true);
            usuarioRepository.save(admin);
            log.info("Usuario administrador génesis creado con éxito.");
        }

        // 2. DESPUÉS: Validamos el resto de los datos (aulas, docentes, etc)
        if (espacioFisicoRepository.count() > 0) {
            log.info("Database already contains data – skipping seeder.");
            return;
        }
        if (espacioFisicoRepository.count() > 0) {
            log.info("Database already contains data – skipping seeder.");
            return;
        }

        log.info("Seeding database with initial data...");

        // ── Espacios Físicos ──
        EspacioFisico aula = new EspacioFisico();
        aula.setCodigoAula("C-412");
        aula.setTipo(EspacioFisico.TipoEspacio.AULA);
        aula.setCapacidad(35);
        aula.setEstatusOperativo(true);

        EspacioFisico salaConferencia = new EspacioFisico();
        salaConferencia.setCodigoAula("SALA-F");
        salaConferencia.setTipo(EspacioFisico.TipoEspacio.SALA_CONFERENCIA);
        salaConferencia.setCapacidad(15);
        salaConferencia.setEstatusOperativo(true);

        espacioFisicoRepository.saveAll(List.of(aula, salaConferencia));

        // ── Docentes ──
        Docente docente1 = new Docente();
        docente1.setCodigoInstitucional("V-12345678");
        docente1.setNombreCompleto("María Rodríguez");
        docente1.setEmail("maria.rodriguez@urbe.edu.ve");
        docente1.setDepartamento("Desarrollo de Software");
        docente1.setCargaMaximaSemanal(8);
        docente1.setActivo(true);

        Docente docente2 = new Docente();
        docente2.setCodigoInstitucional("V-23456789");
        docente2.setNombreCompleto("Carlos Mendoza");
        docente2.setEmail("carlos.mendoza@urbe.edu.ve");
        docente2.setDepartamento("Ciberseguridad");
        docente2.setCargaMaximaSemanal(8);
        docente2.setActivo(true);

        Docente docente3 = new Docente();
        docente3.setCodigoInstitucional("V-34567890");
        docente3.setNombreCompleto("Ana López");
        docente3.setEmail("ana.lopez@urbe.edu.ve");
        docente3.setDepartamento("Telecomunicaciones");
        docente3.setCargaMaximaSemanal(8);
        docente3.setActivo(true);

        docenteRepository.saveAll(List.of(docente1, docente2, docente3));

        // ── Estudiantes ──
        Estudiante est1 = new Estudiante();
        est1.setCedula("V-11111111");
        est1.setNombres("Viktor");
        est1.setApellidos("Gonzalez");

        Estudiante est2 = new Estudiante();
        est2.setCedula("V-22222222");
        est2.setNombres("Sebastián");
        est2.setApellidos("Cárdenas");

        Estudiante est3 = new Estudiante();
        est3.setCedula("V-33333333");
        est3.setNombres("Andreina");
        est3.setApellidos("Paredes");

        estudianteRepository.saveAll(List.of(est1, est2, est3));

        // ── Proyectos (Seminario III) ──
        Proyecto p1 = new Proyecto();
        p1.setTitulo("Aplicación Web para la Gestión de Tutorías Académicas usando Spring Boot y Angular");
        p1.setEstudiante(est1);
        p1.setTutor(docente1);
        p1.setEstatus(Proyecto.EstatusProyecto.PENDIENTE);

        Proyecto p2 = new Proyecto();
        p2.setTitulo("Sistema de Detección de Anomalías en Redes utilizando Algoritmos de Machine Learning");
        p2.setEstudiante(est2);
        p2.setTutor(docente2);
        p2.setEstatus(Proyecto.EstatusProyecto.PENDIENTE);

        Proyecto p3 = new Proyecto();
        p3.setTitulo("Análisis de Cobertura y Optimización de Redes 5G en Zonas Urbanas de Maracaibo");
        p3.setEstudiante(est3);
        p3.setTutor(docente3);
        p3.setEstatus(Proyecto.EstatusProyecto.PENDIENTE);

        proyectoRepository.saveAll(List.of(p1, p2, p3));

        // ── Usuario Génesis (Administrador inicial) ──
        /*Usuario admin = new Usuario();
        admin.setUsername("admin");
        admin.setEmail("admin@urbe.edu");
        admin.setPassword(passwordEncoder.encode("admin"));
        admin.setNombreCompleto("Coordinador General");
        admin.setRol("COORDINADOR");
        admin.setActivo(true);
        usuarioRepository.save(admin);

        log.info("Database seeding completed successfully.");*/
    }
}
