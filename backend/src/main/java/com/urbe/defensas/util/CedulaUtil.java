package com.urbe.defensas.util;

public final class CedulaUtil {

    private CedulaUtil() {}

    public static String soloDigitos(String cedula) {
        if (cedula == null) {
            return "";
        }
        return cedula.replaceAll("[^0-9]", "");
    }

    public static String normalizar(String cedula) {
        String soloDigitos = soloDigitos(cedula);
        return soloDigitos.replaceFirst("^0+", "");
    }

    public static boolean esValida(String cedula) {
        return !normalizar(cedula).isEmpty();
    }
}
