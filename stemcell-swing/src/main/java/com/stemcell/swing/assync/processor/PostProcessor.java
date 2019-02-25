package com.stemcell.swing.assync.processor;

/**
 * Interface para criação de pós-processadores customizados
 */
public interface PostProcessor extends Processor {

    /**
     * Método chamado após finalização da chamada
     *
     * @param result resultado retornado pela chamada
     * @return Deve retornar o objeto de resultado para ser passadp ao próximo
     * PostProcessor
     */
    Object postProccess(Object result);
}
