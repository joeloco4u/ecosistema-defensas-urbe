package com.urbe.defensas.dtos;

public class RegistroRequest {
    private String email;
    private String password;
    private String nombreCompleto;
    private String rol;

    public RegistroRequest() {}

    public RegistroRequest(String email, String password, String nombreCompleto, String rol) {
        this.email = email;
        this.password = password;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
