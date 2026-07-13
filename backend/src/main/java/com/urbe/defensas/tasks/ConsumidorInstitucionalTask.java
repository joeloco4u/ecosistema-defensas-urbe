package com.urbe.defensas.tasks;

import com.urbe.defensas.models.Docente;
import com.urbe.defensas.repositories.DocenteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ConsumidorInstitucionalTask {

    private static final Logger log = LoggerFactory.getLogger(ConsumidorInstitucionalTask.class);

    private final DocenteRepository docenteRepository;

    public ConsumidorInstitucionalTask(DocenteRepository docenteRepository) {
        this.docenteRepository = docenteRepository;
    }

    @Scheduled(cron = "${app.scheduler.consumir-institucional}")
    @Transactional
    public void consumirEndpointInstitucional() {
        log.info("Iniciando consumo del endpoint institucional de docentes...");

        try {
            Docente docenteSimulado = new Docente();
            docenteSimulado.setCodigoInstitucional("DOC-" + System.currentTimeMillis());
            docenteSimulado.setNombreCompleto("Docente Sincronizado");
            docenteSimulado.setEmail("docente@institucion.edu");
            docenteSimulado.setDepartamento("Ingeniería");
            docenteSimulado.setCargaMaximaSemanal(8);

            docenteRepository.save(docenteSimulado);
            log.info("Docente sincronizado exitosamente: {}", docenteSimulado.getCodigoInstitucional());
        } catch (Exception e) {
            log.error("Error al consumir endpoint institucional: {}", e.getMessage());
        }
    }

}
