package com.urbe.defensas.dtos;

import com.urbe.defensas.models.Docente;
import com.urbe.defensas.models.Estudiante;
import com.urbe.defensas.models.Proyecto;

import java.util.UUID;

/**
 * Proyección de lectura de {@link Proyecto} para la API REST.
 * Expone el expediente oficial y el equipo completo de tesistas
 * (tesista 1, 2 y 3) con sus IDs y nombres, sin depender de proxies
 * perezosos de Hibernate durante la serialización.
 */
public class ProyectoDTO {

    private UUID id;
    private String titulo;
    private String expediente;
    private String escuela;
    private String nivelSeminario;
    private String estatus;
    private TesistaDTO estudiante;
    private TesistaDTO estudiante2;
    private TesistaDTO estudiante3;
    private DocenteResumenDTO tutor;
    private DocenteResumenDTO tutorMetodologico;

    public static ProyectoDTO fromEntity(Proyecto proyecto) {
        if (proyecto == null) return null;
        ProyectoDTO dto = new ProyectoDTO();
        dto.setId(proyecto.getId());
        dto.setTitulo(proyecto.getTitulo());
        dto.setExpediente(proyecto.getExpediente());
        dto.setEscuela(proyecto.getEscuela());
        dto.setNivelSeminario(proyecto.getNivelSeminario());
        dto.setEstatus(proyecto.getEstatus() != null ? proyecto.getEstatus().name() : null);
        dto.setEstudiante(TesistaDTO.fromEntity(proyecto.getEstudiante()));
        dto.setEstudiante2(TesistaDTO.fromEntity(proyecto.getEstudiante2()));
        dto.setEstudiante3(TesistaDTO.fromEntity(proyecto.getEstudiante3()));
        dto.setTutor(DocenteResumenDTO.fromEntity(proyecto.getTutor()));
        dto.setTutorMetodologico(DocenteResumenDTO.fromEntity(proyecto.getTutorMetodologico()));
        return dto;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getExpediente() { return expediente; }
    public void setExpediente(String expediente) { this.expediente = expediente; }
    public String getEscuela() { return escuela; }
    public void setEscuela(String escuela) { this.escuela = escuela; }
    public String getNivelSeminario() { return nivelSeminario; }
    public void setNivelSeminario(String nivelSeminario) { this.nivelSeminario = nivelSeminario; }
    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }
    public TesistaDTO getEstudiante() { return estudiante; }
    public void setEstudiante(TesistaDTO estudiante) { this.estudiante = estudiante; }
    public TesistaDTO getEstudiante2() { return estudiante2; }
    public void setEstudiante2(TesistaDTO estudiante2) { this.estudiante2 = estudiante2; }
    public TesistaDTO getEstudiante3() { return estudiante3; }
    public void setEstudiante3(TesistaDTO estudiante3) { this.estudiante3 = estudiante3; }
    public DocenteResumenDTO getTutor() { return tutor; }
    public void setTutor(DocenteResumenDTO tutor) { this.tutor = tutor; }
    public DocenteResumenDTO getTutorMetodologico() { return tutorMetodologico; }
    public void setTutorMetodologico(DocenteResumenDTO tutorMetodologico) { this.tutorMetodologico = tutorMetodologico; }

    public static class TesistaDTO {
        private UUID id;
        private String cedula;
        private String nombres;
        private String apellidos;

        public static TesistaDTO fromEntity(Estudiante estudiante) {
            if (estudiante == null) return null;
            TesistaDTO dto = new TesistaDTO();
            dto.setId(estudiante.getId());
            dto.setCedula(estudiante.getCedula());
            dto.setNombres(estudiante.getNombres());
            dto.setApellidos(estudiante.getApellidos());
            return dto;
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getCedula() { return cedula; }
        public void setCedula(String cedula) { this.cedula = cedula; }
        public String getNombres() { return nombres; }
        public void setNombres(String nombres) { this.nombres = nombres; }
        public String getApellidos() { return apellidos; }
        public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    }

    public static class DocenteResumenDTO {
        private Long id;
        private String nombreCompleto;

        public static DocenteResumenDTO fromEntity(Docente docente) {
            if (docente == null) return null;
            DocenteResumenDTO dto = new DocenteResumenDTO();
            dto.setId(docente.getId());
            dto.setNombreCompleto(docente.getNombreCompleto());
            return dto;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNombreCompleto() { return nombreCompleto; }
        public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    }
}
