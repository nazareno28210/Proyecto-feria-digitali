package com.mansilla_nazareno.feriadigital.feriadigital.models;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public enum TipoToken {
    CONFIRMACION_CUENTA(24, ChronoUnit.HOURS),
    RECUPERACION_CONTRASENA(15, ChronoUnit.MINUTES);

    private final long duracion;
    private final ChronoUnit unidadTiempo;

    TipoToken(long duracion, ChronoUnit unidadTiempo) {
        this.duracion = duracion;
        this.unidadTiempo = unidadTiempo;
    }

    public long getDuracion() {
        return duracion;
    }

    public ChronoUnit getUnidadTiempo() {
        return unidadTiempo;
    }

    public LocalDateTime calcularFechaExpiracion() {
        return LocalDateTime.now().plus(duracion, unidadTiempo);
    }
}
