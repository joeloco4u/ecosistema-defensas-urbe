package com.urbe.defensas.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!prod")
public class ConsumidorInstitucionalTask {

    private static final Logger log = LoggerFactory.getLogger(ConsumidorInstitucionalTask.class);

    // La ejecución automática queda deshabilitada para evitar datos simulados.
    // @Scheduled(cron = "${app.scheduler.consumir-institucional}")
    public void consumirEndpointInstitucional() {
        log.warn("La sincronización institucional está deshabilitada; no se insertan docentes de prueba.");
    }
}
