package com.urbe.defensas.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.util.Set;

/**
 * Calendario de días feriados (inhábiles) para la programación de defensas.
 *
 * Incluye los feriados nacionales de Venezuela y los feriados regionales del
 * estado Zulia. Se manejan fechas fijas; los feriados móviles (Carnaval y
 * Semana Santa) pueden incorporarse aquí en el futuro sin cambiar a los
 * consumidores, ya que toda la lógica se consulta a través de {@link #esFeriado(LocalDate)}.
 */
public final class DiasFeriados {

    private DiasFeriados() {}

    /** Feriados nacionales de Venezuela (fecha fija). */
    private static final Set<MonthDay> FERIADOS_NACIONALES = Set.of(
            MonthDay.of(Month.JANUARY, 1),    // Año Nuevo
            MonthDay.of(Month.APRIL, 19),     // Declaración de la Independencia
            MonthDay.of(Month.MAY, 1),        // Día del Trabajador
            MonthDay.of(Month.JUNE, 24),      // Batalla de Carabobo
            MonthDay.of(Month.JULY, 5),       // Día de la Independencia
            MonthDay.of(Month.JULY, 24),      // Natalicio de Simón Bolívar
            MonthDay.of(Month.OCTOBER, 12),   // Resistencia Indígena
            MonthDay.of(Month.DECEMBER, 24),  // Nochebuena
            MonthDay.of(Month.DECEMBER, 25),  // Navidad
            MonthDay.of(Month.DECEMBER, 31)   // Fin de Año
    );

    /** Feriados regionales del estado Zulia (fecha fija). */
    private static final Set<MonthDay> FERIADOS_ZULIA = Set.of(
            MonthDay.of(Month.OCTOBER, 24),   // Natalicio de Rafael Urdaneta
            MonthDay.of(Month.NOVEMBER, 18)   // Día de la Virgen de Chiquinquirá
    );

    /** Indica si la fecha corresponde a un feriado (nacional o regional del Zulia). */
    public static boolean esFeriado(LocalDate fecha) {
        if (fecha == null) {
            return false;
        }
        MonthDay dia = MonthDay.from(fecha);
        return FERIADOS_NACIONALES.contains(dia) || FERIADOS_ZULIA.contains(dia);
    }

    /** Indica si la fecha cae en fin de semana (sábado o domingo). */
    public static boolean esFinDeSemana(LocalDate fecha) {
        if (fecha == null) {
            return false;
        }
        DayOfWeek dia = fecha.getDayOfWeek();
        return dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY;
    }

    /** Indica si la fecha es hábil para programar (no fin de semana y no feriada). */
    public static boolean esDiaHabil(LocalDate fecha) {
        return !esFinDeSemana(fecha) && !esFeriado(fecha);
    }
}
