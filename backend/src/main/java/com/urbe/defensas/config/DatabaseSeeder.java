package com.urbe.defensas.config;

import com.urbe.defensas.models.*;
import com.urbe.defensas.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);
    private static final int CAMPO_EXPEDIENTE = 0;
    private static final int CAMPO_NOMBRE_TESISTA_1 = 14;
    private static final int CAMPO_CEDULA_TESISTA_1 = 16;
    private static final int CAMPO_NOMBRE_TESISTA_2 = 19;
    private static final int CAMPO_CEDULA_TESISTA_2 = 21;
    private static final int CAMPO_NOMBRE_TESISTA_3 = 24;
    private static final int CAMPO_CEDULA_TESISTA_3 = 26;
    private static final int CANTIDAD_CAMPOS_ESTUDIANTES = 27;

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
            log.info("Database already contains data – skipping initial data seed.");
            backfillTesistasDesdeAprobados();
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
        backfillTesistasDesdeAprobados();

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

    /**
     * Lee APROBADOS.txt (exportación del sistema legacy) y asigna los tutores de cada
     * proyecto ya existente en la base de datos:
     *   - Facilitador  (columna 2  del CSV) -> Tutor Académico   (setTutor)
     *   - Metodológico (columna 6  del CSV) -> Tutor Metodológico (setTutorMetodologico)
     * El emparejamiento con la tabla {@code docentes} se hace por cédula normalizada (solo dígitos).
     *
     * @return cantidad de proyectos actualizados
     */
    public int backfillTutoresDesdeAprobados() {
        Map<String, Docente> docentesPorCedula = new HashMap<>();
        for (Docente docente : docenteRepository.findAll()) {
            String clave = normalizarCedula(docente.getCodigoInstitucional());
            if (!clave.isEmpty()) {
                docentesPorCedula.putIfAbsent(clave, docente);
            }
        }

        InputStream in = getClass().getResourceAsStream("/APROBADOS.txt");
        if (in == null) {
            log.warn("APROBADOS.txt no encontrado en classpath.");
            return 0;
        }

        int actualizados = 0;
        int sinMatch = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String linea;
            int numeroLinea = 0;
            while ((linea = reader.readLine()) != null) {
                numeroLinea++;
                if (linea.isBlank()) continue;

                List<String> campos = parsearCsv(linea);
                if (campos.size() < 7) {
                    log.warn("Línea {} de APROBADOS.txt con formato inesperado ({} campos).", numeroLinea, campos.size());
                    continue;
                }

                String expediente = campos.get(0);
                String cedulaFacilitador = campos.get(2);
                String cedulaMetodologico = campos.get(6);

                Proyecto proyecto = proyectoRepository.findByExpediente(expediente).orElse(null);
                if (proyecto == null) {
                    log.warn("Proyecto con expediente '{}' no encontrado en la BD.", expediente);
                    continue;
                }

                Docente academico = docentesPorCedula.get(normalizarCedula(cedulaFacilitador));
                Docente metodologico = docentesPorCedula.get(normalizarCedula(cedulaMetodologico));

                if (academico == null) {
                    log.warn("Facilitador '{}' (CI {}) sin docente coincidente en la BD.", campos.get(1), cedulaFacilitador);
                    sinMatch++;
                }
                if (metodologico == null) {
                    log.warn("Metodológico '{}' (CI {}) sin docente coincidente en la BD.", campos.get(5), cedulaMetodologico);
                    sinMatch++;
                }
                if (academico == null && metodologico == null) continue;

                proyecto.setTutor(academico);
                proyecto.setTutorMetodologico(metodologico);
                proyectoRepository.save(proyecto);
                actualizados++;
                log.debug("Proyecto '{}': tutor={} (id={}), tutorMetodologico={} (id={}).",
                        expediente,
                        academico != null ? academico.getNombreCompleto() : null,
                        academico != null ? academico.getId() : null,
                        metodologico != null ? metodologico.getNombreCompleto() : null,
                        metodologico != null ? metodologico.getId() : null);
            }
        } catch (IOException e) {
            log.error("Error leyendo APROBADOS.txt.", e);
        }

        log.info("Backfill de tutores finalizado: {} proyectos actualizados, {} tutores sin correspondencia.", actualizados, sinMatch);
        return actualizados;
    }

    /**
     * Lee APROBADOS.txt (exportación del sistema legacy) y vincula los tesistas 2 y 3
     * de cada proyecto ya existente en la base de datos, creando los estudiantes que
     * falten en la tabla {@code estudiantes}:
     *   - Tesista 2 (columna 19 del CSV -> nombres, columna 21 -> cédula)
     *   - Tesista 3 (columna 24 del CSV -> nombres, columna 26 -> cédula)
     * La cédula se normaliza (solo dígitos) para el emparejamiento con la tabla {@code estudiantes}.
     *
     * @return cantidad de proyectos actualizados
     */
    public int backfillTesistasDesdeAprobados() {
        Map<String, Estudiante> estudiantesPorCedula = new HashMap<>();
        for (Estudiante estudiante : estudianteRepository.findAll()) {
            String clave = normalizarCedula(estudiante.getCedula());
            if (!clave.isEmpty()) {
                estudiantesPorCedula.putIfAbsent(clave, estudiante);
            }
        }

        InputStream in = getClass().getResourceAsStream("/APROBADOS.txt");
        if (in == null) {
            log.warn("APROBADOS.txt no encontrado en classpath.");
            return 0;
        }

        int actualizados = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String linea;
            int numeroLinea = 0;
            while ((linea = reader.readLine()) != null) {
                numeroLinea++;
                if (linea.isBlank()) continue;

                List<String> campos = parsearCsv(linea);
                if (campos.size() < CANTIDAD_CAMPOS_ESTUDIANTES) {
                    log.warn("Línea {} de APROBADOS.txt con formato inesperado ({} campos).", numeroLinea, campos.size());
                    continue;
                }

                String expediente = campos.get(CAMPO_EXPEDIENTE);
                Proyecto proyecto = proyectoRepository.findByExpediente(expediente).orElse(null);
                if (proyecto == null) {
                    log.warn("Proyecto con expediente '{}' no encontrado en la BD.", expediente);
                    continue;
                }

                Estudiante tesista1 = obtenerOCrearEstudiante(
                        campos.get(CAMPO_NOMBRE_TESISTA_1), campos.get(CAMPO_CEDULA_TESISTA_1), estudiantesPorCedula);
                Estudiante tesista2 = obtenerOCrearEstudiante(
                        campos.get(CAMPO_NOMBRE_TESISTA_2), campos.get(CAMPO_CEDULA_TESISTA_2), estudiantesPorCedula);
                Estudiante tesista3 = obtenerOCrearEstudiante(
                        campos.get(CAMPO_NOMBRE_TESISTA_3), campos.get(CAMPO_CEDULA_TESISTA_3), estudiantesPorCedula);

                boolean cambio = false;
                if (tesista1 != null && !sonElMismoEstudiante(proyecto.getEstudiante(), tesista1)) {
                    proyecto.setEstudiante(tesista1);
                    cambio = true;
                }
                if (!sonElMismoEstudiante(proyecto.getEstudiante2(), tesista2)) {
                    proyecto.setEstudiante2(tesista2);
                    cambio = true;
                }
                if (!sonElMismoEstudiante(proyecto.getEstudiante3(), tesista3)) {
                    proyecto.setEstudiante3(tesista3);
                    cambio = true;
                }
                if (!cambio) continue;

                proyectoRepository.save(proyecto);
                actualizados++;
                log.debug("Proyecto '{}': tesista1={} (id={}), tesista2={} (id={}), tesista3={} (id={}).",
                        expediente,
                        tesista1 != null ? tesista1.getNombres() : null,
                        tesista1 != null ? tesista1.getId() : null,
                        tesista2 != null ? tesista2.getNombres() : null,
                        tesista2 != null ? tesista2.getId() : null,
                        tesista3 != null ? tesista3.getNombres() : null,
                        tesista3 != null ? tesista3.getId() : null);
            }
        } catch (IOException e) {
            log.error("Error leyendo APROBADOS.txt.", e);
        }

        log.info("Backfill de tesistas finalizado: {} proyectos actualizados.", actualizados);
        return actualizados;
    }

    private static boolean sonElMismoEstudiante(Estudiante primero, Estudiante segundo) {
        if (primero == segundo) return true;
        if (primero == null || segundo == null || primero.getId() == null || segundo.getId() == null) return false;
        return primero.getId().equals(segundo.getId());
    }

    private Estudiante obtenerOCrearEstudiante(String nombreCompleto, String cedula,
                                               Map<String, Estudiante> cachePorCedula) {
        if (esVacio(cedula)) return null;
        String clave = normalizarCedula(cedula);
        if (clave.isEmpty()) return null;

        Estudiante existente = cachePorCedula.get(clave);
        if (existente != null) return existente;
        if (esVacio(nombreCompleto)) return null;

        Estudiante nuevo = new Estudiante();
        nuevo.setCedula(cedula.trim());
        asignarNombre(nuevo, normalizarEspacios(nombreCompleto));
        Estudiante guardado = estudianteRepository.save(nuevo);
        cachePorCedula.put(clave, guardado);
        return guardado;
    }

    private static void asignarNombre(Estudiante estudiante, String nombreCompleto) {
        String[] partes = nombreCompleto.split("\\s+");
        if (partes.length >= 3) {
            estudiante.setApellidos(String.join(" ", Arrays.copyOfRange(partes, 0, 2)));
            estudiante.setNombres(String.join(" ", Arrays.copyOfRange(partes, 2, partes.length)));
        } else {
            estudiante.setNombres(nombreCompleto);
            estudiante.setApellidos("");
        }
    }

    private static boolean esVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private static String normalizarEspacios(String valor) {
        return valor.trim().replaceAll("\\s+", " ");
    }

    private static String normalizarCedula(String cedula) {
        if (cedula == null) return "";
        StringBuilder soloDigitos = new StringBuilder();
        for (char c : cedula.toCharArray()) {
            if (Character.isDigit(c)) soloDigitos.append(c);
        }
        return soloDigitos.toString();
    }

    private static List<String> parsearCsv(String linea) {
        List<String> campos = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean dentroDeComillas = false;
        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (c == '"') {
                if (dentroDeComillas && i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                    actual.append('"');
                    i++;
                } else {
                    dentroDeComillas = !dentroDeComillas;
                }
            } else if (c == ',' && !dentroDeComillas) {
                campos.add(actual.toString().trim());
                actual.setLength(0);
            } else {
                actual.append(c);
            }
        }
        campos.add(actual.toString().trim());
        return campos;
    }
}
