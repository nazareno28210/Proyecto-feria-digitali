package com.mansilla_nazareno.feriadigital.feriadigital.services.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EdicionFeria;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EstadoEdicion;
import com.mansilla_nazareno.feriadigital.feriadigital.repositories.Admin.EdicionFeriaRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class EdicionScheduler {

    private static final Logger logger = LoggerFactory.getLogger(EdicionScheduler.class);

    @Autowired
    private EdicionFeriaRepository edicionFeriaRepository;

    @PostConstruct
    public void inicializarConPostConstruct() {
        logger.info("⚡ @PostConstruct: Verificando estados de ediciones al arrancar el servidor...");
        actualizarEstadosEdiciones();
    }

    // Ejecución cada hora
    @Scheduled(cron = "0 0 * * * *")
    public void actualizarEstadosEdiciones() {
        logger.info("⏰ Cron Job: Iniciando actualización automática de estados de ediciones...");
        LocalDateTime ahora = LocalDateTime.now();

        // 1. Ediciones PROXIMAS cuya fecha/hora inicio sea igual o anterior a ahora (+/- 60 min de margen) -> ACTIVA
        List<EdicionFeria> proximas = edicionFeriaRepository.findByEstado(EstadoEdicion.PROXIMA);
        int activadas = 0;
        for (EdicionFeria edicion : proximas) {
            if (edicion.getFechaInicio() != null) {
                LocalTime horaInicio = edicion.getHoraInicio() != null ? edicion.getHoraInicio() : LocalTime.MIN;
                LocalDateTime inicio = LocalDateTime.of(edicion.getFechaInicio(), horaInicio);
                if (!ahora.isBefore(inicio.minusMinutes(60))) {
                    edicion.setEstado(EstadoEdicion.ACTIVA);
                    edicionFeriaRepository.save(edicion);
                    activadas++;
                    logger.info("Edición id {} ('{}') pasó de PROXIMA a ACTIVA", edicion.getId(), edicion.getNombreEdicion());
                }
            }
        }

        // 2. Ediciones ACTIVAS cuya fecha/hora fin haya pasado -> FINALIZADA
        List<EdicionFeria> activas = edicionFeriaRepository.findByEstado(EstadoEdicion.ACTIVA);
        int finalizadas = 0;
        for (EdicionFeria edicion : activas) {
            if (edicion.getFechaFinal() != null || edicion.getFechaInicio() != null) {
                java.time.LocalDate fechaFin = edicion.getFechaFinal() != null ? edicion.getFechaFinal() : edicion.getFechaInicio();
                LocalTime horaFin = edicion.getHoraFin() != null ? edicion.getHoraFin() : LocalTime.MAX;
                LocalDateTime fin = LocalDateTime.of(fechaFin, horaFin);
                if (ahora.isAfter(fin)) {
                    edicion.setEstado(EstadoEdicion.FINALIZADA);
                    edicionFeriaRepository.save(edicion);
                    finalizadas++;
                    logger.info("Edición id {} ('{}') pasó de ACTIVA a FINALIZADA", edicion.getId(), edicion.getNombreEdicion());
                }
            }
        }

        logger.info("⏰ Cron Job finalizado: {} edición(es) pasadas a ACTIVA, {} edición(es) pasadas a FINALIZADA", activadas, finalizadas);
    }
}
