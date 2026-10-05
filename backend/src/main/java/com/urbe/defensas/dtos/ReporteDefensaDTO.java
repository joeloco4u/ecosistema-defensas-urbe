package com.urbe.defensas.dtos;

import java.time.LocalTime;

public class ReporteDefensaDTO {

    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String espacio;
    private String tesista;
    private String tesista2;
    private String tesista3;
    private String titulo;
    private String tutorAcademico;
    private String jurado;

    public ReporteDefensaDTO() {}

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
    public String getEspacio() { return espacio; }
    public void setEspacio(String espacio) { this.espacio = espacio; }
    public String getTesista() { return tesista; }
    public void setTesista(String tesista) { this.tesista = tesista; }
    public String getTesista2() { return tesista2; }
    public void setTesista2(String tesista2) { this.tesista2 = tesista2; }
    public String getTesista3() { return tesista3; }
    public void setTesista3(String tesista3) { this.tesista3 = tesista3; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getTutorAcademico() { return tutorAcademico; }
    public void setTutorAcademico(String tutorAcademico) { this.tutorAcademico = tutorAcademico; }
    public String getJurado() { return jurado; }
    public void setJurado(String jurado) { this.jurado = jurado; }
}