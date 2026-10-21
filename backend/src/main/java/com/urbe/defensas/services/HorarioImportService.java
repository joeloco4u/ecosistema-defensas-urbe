package com.urbe.defensas.services;

import com.urbe.defensas.dtos.BloqueHorarioImportDTO;
import com.urbe.defensas.dtos.DocenteHorarioImportDTO;
import com.urbe.defensas.dtos.ImportacionHorariosResultadoDTO;
import com.urbe.defensas.models.Docente;
import com.urbe.defensas.models.HorarioClase;
import com.urbe.defensas.repositories.DocenteRepository;
import com.urbe.defensas.repositories.HorarioClaseRepository;
import com.urbe.defensas.util.CedulaUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class HorarioImportService {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("H:mm");

    private final DocenteRepository docenteRepository;
    private final HorarioClaseRepository horarioClaseRepository;

    public HorarioImportService(DocenteRepository docenteRepository,
                                HorarioClaseRepository horarioClaseRepository) {
        this.docenteRepository = docenteRepository;
        this.horarioClaseRepository = horarioClaseRepository;
    }

    public ImportacionHorariosResultadoDTO importar(List<DocenteHorarioImportDTO> docentes) {
        if (docentes == null || docentes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El JSON no contiene docentes para importar");
        }

        ImportacionHorariosResultadoDTO resultado = new ImportacionHorariosResultadoDTO();
        resultado.setDocentesProcesados(docentes.size());

        Map<String, Docente> docentesPorCedula = indexarDocentesPorCedula();
        List<HorarioClase> nuevosHorarios = new ArrayList<>();
        Set<Long> docentesImportados = new HashSet<>();
        Set<Long> reactivados = new HashSet<>();

        for (DocenteHorarioImportDTO dto : docentes) {
            Docente docente = docentesPorCedula.get(CedulaUtil.normalizar(dto.getCedula()));

            if (docente == null) {
                resultado.getDocentesNoEncontrados().add(
                        dto.getCedula() + " - " + dto.getNombre());
                continue;
            }

            resultado.setDocentesEncontrados(resultado.getDocentesEncontrados() + 1);
            docentesImportados.add(docente.getId());

            if (!Boolean.TRUE.equals(docente.getActivo())) {
                docente.setActivo(true);
                docenteRepository.save(docente);
                reactivados.add(docente.getId());
            }

            if (dto.getHorarioSemanal() == null) {
                continue;
            }

            for (Map.Entry<String, List<BloqueHorarioImportDTO>> entrada : dto.getHorarioSemanal().entrySet()) {
                String dia = normalizarDia(entrada.getKey());
                List<BloqueHorarioImportDTO> bloques = entrada.getValue();
                if (dia.isEmpty() || bloques == null) {
                    continue;
                }

                for (BloqueHorarioImportDTO bloque : bloques) {
                    if (bloque == null) {
                        continue;
                    }
                    RangoHorario rango = parsearRango(bloque.getBloqueHora());
                    if (rango == null) {
                        resultado.getErrores().add(describirError(dto, dia, bloque, "bloque_hora inválido"));
                        continue;
                    }

                    HorarioClase horario = new HorarioClase();
                    horario.setDocenteId(docente.getId());
                    horario.setDiaSemana(dia);
                    horario.setHoraInicio(rango.inicio());
                    horario.setHoraFin(rango.fin());
                    horario.setMateria(truncar(bloque.getMateria(), 200));
                    horario.setSeccion(truncar(bloque.getSeccion(), 50));
                    nuevosHorarios.add(horario);
                }
            }
        }

        if (!docentesImportados.isEmpty()) {
            horarioClaseRepository.deleteByDocenteIdIn(docentesImportados);
        }
        if (!nuevosHorarios.isEmpty()) {
            horarioClaseRepository.saveAll(nuevosHorarios);
        }

        resultado.setDocentesReactivados(reactivados.size());
        resultado.setBloquesInsertados(nuevosHorarios.size());
        return resultado;
    }

    private Map<String, Docente> indexarDocentesPorCedula() {
        Map<String, Docente> indice = new HashMap<>();
        for (Docente docente : docenteRepository.findAll()) {
            String cedulaNormalizada = CedulaUtil.normalizar(docente.getCedula());
            if (!cedulaNormalizada.isEmpty()) {
                indice.putIfAbsent(cedulaNormalizada, docente);
            }
        }
        return indice;
    }

    private static String normalizarDia(String dia) {
        if (dia == null) {
            return "";
        }
        String sinAcentos = Normalizer.normalize(dia, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinAcentos.trim().replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }

    private static RangoHorario parsearRango(String bloqueHora) {
        if (bloqueHora == null) {
            return null;
        }
        String[] partes = bloqueHora.split("\\s*-\\s*");
        if (partes.length != 2) {
            return null;
        }
        try {
            LocalTime inicio = LocalTime.parse(partes[0].trim(), FORMATO_HORA);
            LocalTime fin = LocalTime.parse(partes[1].trim(), FORMATO_HORA);
            if (!fin.isAfter(inicio)) {
                return null;
            }
            return new RangoHorario(inicio, fin);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private static String truncar(String valor, int maximo) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.length() <= maximo ? limpio : limpio.substring(0, maximo);
    }

    private static String describirError(DocenteHorarioImportDTO dto, String dia,
                                         BloqueHorarioImportDTO bloque, String detalle) {
        return dto.getCedula() + " - " + dia + " - " + bloque.getBloqueHora() + ": " + detalle;
    }

    private record RangoHorario(LocalTime inicio, LocalTime fin) {}
}
