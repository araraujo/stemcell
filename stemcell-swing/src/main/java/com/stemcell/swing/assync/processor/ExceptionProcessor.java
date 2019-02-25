package com.stemcell.swing.assync.processor;

/**
 * Interface para criação de tratador de exceção
 */
public interface ExceptionProcessor extends Processor {

    /**
     * Método chamado quando a chamada assíncrona levanta exceção
     *
     * @param ex
     * @return
     */
    void exceptionProcess(Exception ex);
}
