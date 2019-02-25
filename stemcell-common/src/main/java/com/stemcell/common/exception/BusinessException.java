package com.stemcell.common.exception;

import java.util.Arrays;

public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = -6578431977912288874L;

    private Object[] params;

    /**
     * Constroi uma instancia desta classe.
     *
     * @param message
     *            a mensagem de detalhe que pode ser recuperada com o metodo
     *            {@link #getMessage()}.
     * @param cause
     *            a causa que pode ser recuperada com o metodo
     *            {@link #getCause()}.
     * @param messageParams
     *            parametros que podem ser utilizados na formatacao da mensagem,
     *            sua utilizacao eh opcional.
     */
    public BusinessException(String message, Throwable cause, Object... messageParams) {
        super(message, cause);
        this.params = messageParams;
    }

    /**
     * Constroi uma instancia desta classe.
     *
     * @param message
     *            a mensagem de detalhe que pode ser recuperada com o metodo
     *            {@link #getMessage()}.
     * @param messageParams
     *            parametros que podem ser utilizados na formatacao da mensagem,
     *            sua utilizacao eh opcional.
     */
    public BusinessException(String message, Object... messageParams) {
        super(message);
        this.params = messageParams;
    }

    /**
     * Getter dos parametros para formatacao da mensagem.
     *
     * @return array de object com parametros
     */
    public Object[] getMessageParams() {
        // evitando exposicao de representacao interna
        // previnido erro EI_EXPOSE_REP do findbugs
        Object[] copyParams = null;

        if (this.params != null) {
            copyParams = Arrays.copyOf(this.params, params.length);
        }

        return copyParams;
    }

    /**
     * Setter dos parametros para formatacao da mensagem.
     *
     * @param params
     *        array de object com parametros que serao utilizados na
     *        formatacao da mensagem
     */
    public void setMessageParams(Object[] params) {
        // evitando exposicao de representacao interna
        // previnido erro EI_EXPOSE_REP2 do findbugs
        if (params != null) {
            this.params = Arrays.copyOf(params, params.length);
        }
    }

}
