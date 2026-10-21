package com.urbe.defensas.dtos;

import java.util.ArrayList;
import java.util.List;

public class ImportacionHorariosResultadoDTO {

    private int docentesProcesados;
    private int docentesEncontrados;
    private int docentesReactivados;
    private int bloquesInsertados;
    private List<String> docentesNoEncontrados = new ArrayList<>();
    private List<String> errores = new ArrayList<>();

    public ImportacionHorariosResultadoDTO() {}

    public int getDocentesProcesados() { return docentesProcesados; }
    public void setDocentesProcesados(int docentesProcesados) { this.docentesProcesados = docentesProcesados; }
    public int getDocentesEncontrados() { return docentesEncontrados; }
    public void setDocentesEncontrados(int docentesEncontrados) { this.docentesEncontrados = docentesEncontrados; }
    public int getDocentesReactivados() { return docentesReactivados; }
    public void setDocentesReactivados(int docentesReactivados) { this.docentesReactivados = docentesReactivados; }
    public int getBloquesInsertados() { return bloquesInsertados; }
    public void setBloquesInsertados(int bloquesInsertados) { this.bloquesInsertados = bloquesInsertados; }
    public List<String> getDocentesNoEncontrados() { return docentesNoEncontrados; }
    public void setDocentesNoEncontrados(List<String> docentesNoEncontrados) {
        this.docentesNoEncontrados = docentesNoEncontrados;
    }
    public List<String> getErrores() { return errores; }
    public void setErrores(List<String> errores) { this.errores = errores; }
}
