package com.urbe.defensas.dtos;

import com.urbe.defensas.models.Defensa;
import com.urbe.defensas.models.Docente;
import com.urbe.defensas.models.JuradoDefensa;
import com.urbe.defensas.models.Proyecto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class ExpedienteDocenteDTO {

    private Docente docente;
    private List<String> seminarios;
    private List<ProyectoTutoriaDTO> proyectosTutor;
    private List<DefensaJuradoDTO> defensasJurado;

    public Docente getDocente() { return docente; }
    public void setDocente(Docente docente) { this.docente = docente; }
    public List<String> getSeminarios() { return seminarios; }
    public void setSeminarios(List<String> seminarios) { this.seminarios = seminarios; }
    public List<ProyectoTutoriaDTO> getProyectosTutor() { return proyectosTutor; }
    public void setProyectosTutor(List<ProyectoTutoriaDTO> proyectosTutor) { this.proyectosTutor = proyectosTutor; }
    public List<DefensaJuradoDTO> getDefensasJurado() { return defensasJurado; }
    public void setDefensasJurado(List<DefensaJuradoDTO> defensasJurado) { this.defensasJurado = defensasJurado; }

    public enum RolTutoria {
        ACADEMICO, METODOLOGICO
    }

    public enum RolJurado {
        PRESIDENTE, PRINCIPAL, SUPLENTE
    }

    public static class ProyectoTutoriaDTO {
        private UUID proyectoId;
        private String titulo;
        private String escuela;
        private String tesista;
        private RolTutoria rol;
        private Proyecto.EstatusProyecto estatus;

        public ProyectoTutoriaDTO() {}

        public ProyectoTutoriaDTO(UUID proyectoId, String titulo, String escuela, String tesista,
                                  RolTutoria rol, Proyecto.EstatusProyecto estatus) {
            this.proyectoId = proyectoId;
            this.titulo = titulo;
            this.escuela = escuela;
            this.tesista = tesista;
            this.rol = rol;
            this.estatus = estatus;
        }

        public UUID getProyectoId() { return proyectoId; }
        public void setProyectoId(UUID proyectoId) { this.proyectoId = proyectoId; }
        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }
        public String getEscuela() { return escuela; }
        public void setEscuela(String escuela) { this.escuela = escuela; }
        public String getTesista() { return tesista; }
        public void setTesista(String tesista) { this.tesista = tesista; }
        public RolTutoria getRol() { return rol; }
        public void setRol(RolTutoria rol) { this.rol = rol; }
        public Proyecto.EstatusProyecto getEstatus() { return estatus; }
        public void setEstatus(Proyecto.EstatusProyecto estatus) { this.estatus = estatus; }
    }

    public static class DefensaJuradoDTO {
        private UUID defensaId;
        private UUID proyectoId;
        private String proyectoTitulo;
        private String tesista;
        private LocalDate fecha;
        private LocalTime horaInicio;
        private LocalTime horaFin;
        private Defensa.EstatusDefensa estatus;
        private RolJurado rolJurado;

        public DefensaJuradoDTO() {}

        public DefensaJuradoDTO(UUID defensaId, UUID proyectoId, String proyectoTitulo, String tesista,
                                LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                                Defensa.EstatusDefensa estatus, RolJurado rolJurado) {
            this.defensaId = defensaId;
            this.proyectoId = proyectoId;
            this.proyectoTitulo = proyectoTitulo;
            this.tesista = tesista;
            this.fecha = fecha;
            this.horaInicio = horaInicio;
            this.horaFin = horaFin;
            this.estatus = estatus;
            this.rolJurado = rolJurado;
        }

        public UUID getDefensaId() { return defensaId; }
        public void setDefensaId(UUID defensaId) { this.defensaId = defensaId; }
        public UUID getProyectoId() { return proyectoId; }
        public void setProyectoId(UUID proyectoId) { this.proyectoId = proyectoId; }
        public String getProyectoTitulo() { return proyectoTitulo; }
        public void setProyectoTitulo(String proyectoTitulo) { this.proyectoTitulo = proyectoTitulo; }
        public String getTesista() { return tesista; }
        public void setTesista(String tesista) { this.tesista = tesista; }
        public LocalDate getFecha() { return fecha; }
        public void setFecha(LocalDate fecha) { this.fecha = fecha; }
        public LocalTime getHoraInicio() { return horaInicio; }
        public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
        public LocalTime getHoraFin() { return horaFin; }
        public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
        public Defensa.EstatusDefensa getEstatus() { return estatus; }
        public void setEstatus(Defensa.EstatusDefensa estatus) { this.estatus = estatus; }
        public RolJurado getRolJurado() { return rolJurado; }
        public void setRolJurado(RolJurado rolJurado) { this.rolJurado = rolJurado; }
    }
}