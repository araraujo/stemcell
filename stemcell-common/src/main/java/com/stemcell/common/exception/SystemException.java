package com.stemcell.common.exception;

/**
 * Excecao interna do sistema, do tipo unchecked. Utilizada para erros nao
 * relacionados a logica de negocio, como problemas de infra-estrutura.
 * 
 */
public class SystemException extends RuntimeException {

    private static final long serialVersionUID = 5435275058745576765L;

    private static final String EXCEPTION_SYSTEM_EXCEPTION_KEY = "systemException";

    /**
     * Constroi uma instancia desta classe enviando a mensagem generica
     * "petrobras.fcorp.systemException".
     */
    public SystemException() {
        super(EXCEPTION_SYSTEM_EXCEPTION_KEY);
    }

    /**
     * Constroi uma instancia desta classe.
     * 
     * @param message
     *            a mensagem de detalhe que pode ser recuperada com o metodo
     *            {@link #getMessage()}.
     * @param cause
     *            a causa que pode ser recuperada com o metodo
     *            {@link #getCause()}.
     */
    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constroi uma instancia desta classe.
     * 
     * @param message
     *            a mensagem de detalhe que pode ser recuperada com o metodo
     *            {@link #getMessage()}.
     */
    public SystemException(String message) {
        super(message);
    }

    /**
     * Constroi uma instancia desta classe enviando a mensagem generica
     * "petrobras.fcorp.systemException".
     * 
     * @param cause
     *            a causa que pode ser recuperada com o metodo
     *            {@link #getCause()}.
     */
    public SystemException(Throwable cause) {
        super(EXCEPTION_SYSTEM_EXCEPTION_KEY, cause);
    }

}
