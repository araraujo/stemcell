package com.stemcell.swing.exceptionhandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Itera por uma lista de tratadores para
 * tratar a exceção correta. Se comportando com um tratador comum.
 * Atende a ordem da lista passada.
 *
 */
public class ExceptionHandlerManager implements ExceptionHandler {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ExceptionHandlerManager.class);

    private List<ExceptionHandler> handlerList;

    /**
     * Construtor que possibilita a inicialização da
     * lista de handlers
     * @param handlerList lista de handlers
     */
    public ExceptionHandlerManager(List<ExceptionHandler> handlerList) {
        super();
        this.handlerList = handlerList;
        LOGGER.debug(String.format("ExceptionHandlerManager initialized with %d handlers.",(handlerList!=null)?handlerList.size():0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HandlerResult doHandle(Throwable cause) {

        List<ExceptionMessage> exceptionMessageList = new ArrayList<ExceptionMessage>();

        for (ExceptionHandler handler : handlerList) {

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Calling handler %s.",handler.getClass().getName()));
            }

            HandlerResult handlerResult = handler.doHandle(cause);

            if (handlerResult.isHandled()) {
                return handlerResult;
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("Exception was %s.",!exceptionMessageList.isEmpty()?"handled":"not handled"));
        }

        return new HandlerResult(false, new ArrayList<ExceptionMessage>());

    }


    /**
     * Retorna a Lista de tratadores atual.
     * @return handlerList
     */
    public List<ExceptionHandler> getHandlerList() {
        return handlerList;
    }

    /**
     * Seta uma nova lista de tratadores.
     * @param handlerList the handlerList to set
     */
    public void setHandlerList(List<ExceptionHandler> handlerList) {
        this.handlerList = handlerList;
    }



}