package com.urbe.defensas.services;

import com.urbe.defensas.models.Proyecto;
import com.urbe.defensas.repositories.ProyectoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BackupService {

    private static final String[] ENCABEZADOS = {
            "ID_Proyecto", "Expediente", "Nombres_Tesista", "Apellidos_Tesista",
            "Titulo", "Escuela", "Tutor", "Estatus"
    };

    private final ProyectoRepository proyectoRepository;

    public BackupService(ProyectoRepository proyectoRepository) {
        this.proyectoRepository = proyectoRepository;
    }

    public String generarBackupProyectosCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append(String.join(",", ENCABEZADOS)).append('\n');

        for (Proyecto proyecto : proyectoRepository.findAll()) {
            String expediente = proyecto.getEstudiante() != null ? proyecto.getEstudiante().getCedula() : null;
            String nombres = proyecto.getEstudiante() != null ? proyecto.getEstudiante().getNombres() : null;
            String apellidos = proyecto.getEstudiante() != null ? proyecto.getEstudiante().getApellidos() : null;
            String tutor = proyecto.getTutor() != null ? proyecto.getTutor().getNombreCompleto() : null;

            csv.append(campo(proyecto.getId() != null ? proyecto.getId().toString() : null)).append(',')
                    .append(campo(expediente)).append(',')
                    .append(campo(nombres)).append(',')
                    .append(campo(apellidos)).append(',')
                    .append(campo(proyecto.getTitulo())).append(',')
                    .append(campo(proyecto.getEscuela())).append(',')
                    .append(campo(tutor)).append(',')
                    .append(campo(proyecto.getEstatus() != null ? proyecto.getEstatus().name() : null))
                    .append('\n');
        }

        return csv.toString();
    }

    private String campo(String valor) {
        if (valor == null) {
            return "";
        }
        if (valor.contains(",") || valor.contains("\"") || valor.contains("\n") || valor.contains("\r")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }
}