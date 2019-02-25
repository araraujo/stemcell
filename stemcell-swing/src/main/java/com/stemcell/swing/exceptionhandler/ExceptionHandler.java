package com.stemcell.swing.exceptionhandler;

/**
 * Define como o ExceptionHandler
 * deve se comportar. Cabe a ExceptionHandler
 * identificar e tratar uma Exceção caso seja
 * de sua responsabilidade.
 *
 * @author Alessandro Araujo
 *  */
public interface ExceptionHandler {


    /**
     * Método tem a responsabilidade de tratar a
     * exceção que lhe cabe. É esperado que
     * ao tratar uma exceção, o doHandle adicione as
     * novas da mensagens de exeção na lista que
     * será retornada. Uma lista vazia significa que
     * o tratador não tratou nenhuma exception (a lista
     * não deve ser nula caso não trate a excecao).
     *
     *
     * @param throwable throwable
     * @return HandlerResult lista com as mensagens
     * de exceção encontradas. Uma lista com mais de um item
     * significa que um erro foi encontrado e tratado.
     */
    HandlerResult doHandle(Throwable throwable);



}