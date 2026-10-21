package com.urbe.defensas.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BloqueHorarioImportDTO {

    @JsonProperty("bloque_hora")
    private String bloqueHora;

    private String materia;
    private String seccion;

    public BloqueHorarioImportDTO() {}

    public String getBloqueHora() { return bloqueHora; }
    public void setBloqueHora(String bloqueHora) { this.bloqueHora = bloqueHora; }
    public String getMateria() { return materia; }
    public void setMateria(String materia) { this.materia = materia; }
    public String getSeccion() { return seccion; }
    public void setSeccion(String seccion) { this.seccion = seccion; }
}
