package com.urbe.defensas.services;

import com.urbe.defensas.models.Proyecto;
import com.urbe.defensas.repositories.ProyectoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BackupService {

    private static final String[] ENCABEZADOS = {
            "ID_Proyecto", "Expediente",
            "Nombres_Tesista", "Apellidos_Tesista", "Cedula_Tesista",
            "Nombres_Tesista2", "Cedula_Tesista2",
            "Nombres_Tesista3", "Cedula_Tesista3",
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
            String cedula1 = proyecto.getEstudiante() != null ? proyecto.getEstudiante().getCedula() : null;
            String nombres1 = proyecto.getEstudiante() != null ? proyecto.getEstudiante().getNombres() : null;
            String apellidos1 = proyecto.getEstudiante() != null ? proyecto.getEstudiante().getApellidos() : null;
            String nombres2 = proyecto.getEstudiante2() != null ? proyecto.getEstudiante2().getNombres() : null;
            String cedula2 = proyecto.getEstudiante2() != null ? proyecto.getEstudiante2().getCedula() : null;
            String nombres3 = proyecto.getEstudiante3() != null ? proyecto.getEstudiante3().getNombres() : null;
            String cedula3 = proyecto.getEstudiante3() != null ? proyecto.getEstudiante3().getCedula() : null;
            String tutor = proyecto.getTutor() != null ? proyecto.getTutor().getNombreCompleto() : null;

            csv.append(campo(proyecto.getId() != null ? proyecto.getId().toString() : null)).append(',')
                    .append(campo(proyecto.getExpediente())).append(',')
                    .append(campo(nombres1)).append(',')
                    .append(campo(apellidos1)).append(',')
                    .append(campo(cedula1)).append(',')
                    .append(campo(nombres2)).append(',')
                    .append(campo(cedula2)).append(',')
                    .append(campo(nombres3)).append(',')
                    .append(campo(cedula3)).append(',')
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