package com.urbe.defensas.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class DocenteHorarioImportDTO {

    @JsonProperty("id_urbe")
    private Long idUrbe;

    private String cedula;
    private String nombre;

    @JsonProperty("horario_semanal")
    private Map<String, List<BloqueHorarioImportDTO>> horarioSemanal;

    public DocenteHorarioImportDTO() {}

    public Long getIdUrbe() { return idUrbe; }
    public void setIdUrbe(Long idUrbe) { this.idUrbe = idUrbe; }
    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Map<String, List<BloqueHorarioImportDTO>> getHorarioSemanal() { return horarioSemanal; }
    public void setHorarioSemanal(Map<String, List<BloqueHorarioImportDTO>> horarioSemanal) {
        this.horarioSemanal = horarioSemanal;
    }
}
