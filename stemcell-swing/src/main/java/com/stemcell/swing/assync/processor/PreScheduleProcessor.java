package com.stemcell.swing.assync.processor;

import java.util.concurrent.Callable;

/**
 * Interface PreScheduleProcessor. Chamado antes do momento de schedule da chamada
 */
public interface PreScheduleProcessor extends Processor {
    /**
     * Chamado antes do momento de schedule da chamada
     */
    void preSchedule(Callable call);
}
