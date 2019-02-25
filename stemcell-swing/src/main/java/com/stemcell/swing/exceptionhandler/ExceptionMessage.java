package com.stemcell.swing.exceptionhandler;

import java.util.Arrays;

/**
 *
 * Classe que representa uma mensagem de exceção.
 *
 */
public class ExceptionMessage {

    private String key;
    private Object[] params;
    private Severity severity;

    /**
     * Construtor da classe.
     * @param key chave da mensagem
     * @param params parametros da mensagem
     */
    public ExceptionMessage(String key, Object...params) {
        this(key, Severity.ERROR, params);
    }

    /**
     * Construtor da classe.
     * @param key chave da mensagem
     * @param severity severidade da mensagem
     * @param params parametros da mensagem
     */
    public ExceptionMessage(String key, Severity severity, Object... params) {
        this.key = key;
        this.severity = severity;
        this.params = params;
    }


    /**
     * Retorna a chave da mensagem.
     * @return key
     */
    public String getKey() {
        return key;
    }

    /**
     * Retorna os parametros da mensagem.
     * @return params
     */
    public Object[] getParams() {
        return Arrays.copyOf(params, params.length);
    }

    /**
     * Retorna a severidade da mensagem.
     * @return severity
     */
    public Severity getSeverity() {
        return severity;
    }

    /**
     * Enum que representa os possíveis valores da severidade de uma mensagem.
     * @author Y3ZZ
     *
     */
    public enum Severity {
        /**
         * Severidade utilizada para mensagens de informação ao usuário
         */
        INFO,
        /**
         * Severidade utilizada para mensagens de aviso ao usuário
         */
        WARN,
        /**
         * Severidade utilizada para mensagens de erro ao usuário
         */
        ERROR;
    }
}