package com.stemcell.swing.assync.processor;

import java.util.concurrent.Callable;

/**
 * Interface para criação de pré-processadores customizados
 */
public interface PreProcessor extends Processor {

    /**
     * Chamado antes do momento de execução chamada
     */
    void preProccess(Callable call);
}
