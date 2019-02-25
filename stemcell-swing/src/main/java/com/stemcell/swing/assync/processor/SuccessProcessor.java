package com.stemcell.swing.assync.processor;

/**
 * Interface para criação de pós-processadores apenas em caso de sucesso
 */
public interface SuccessProcessor extends Processor {

    /**
     * Método chamado após finalização de uma chamada sem erros
     *
     * @param result resultado retornado pela chamada
     * @return Deve retornar o objeto de resultado para ser passadp ao próximo
     * SuccessProcessor
     */
    Object proccessSuccess(Object result);
}
