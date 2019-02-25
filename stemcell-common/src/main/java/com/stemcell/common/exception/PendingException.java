package com.stemcell.common.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Exceção que carrega uma lista de pendências.
 * 
 * @author x4rc
 */
public class PendingException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public static final String MESSAGE_SEPARATOR = ";";

    /**
     * Mensagens de erro armazenadas na exceção
     */
    private List<Pendency> pending = new ArrayList<Pendency>();

    /**
     * Cria uma exceção representando uma lista de erros esperados de
     * negócio ou de sistema.
     * @param pending lista de pendências
     * @return a exceção
     */
    public PendingException(List<Pendency> pending) {
        this(null, pending);
    }

    /**
     * @param type
     * @param msg
     * @param error
     * @param params
     */
    public PendingException(Throwable error, List<Pendency> pending) {
        super("snarf.exception.errorlist", error);
        this.pending.addAll(pending);
    }

    /**
     * Retorna a lista de pendências registradas na exceção.
     */
    public List<Pendency> getPending() {
        return Collections.unmodifiableList(pending);
    }

    /**
     * Retorna uma mensagem com o texto original de todas as pendências concatenados.
     */
    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder();
        for (Pendency pendency : pending) {
            if (builder.length() > 0) {
                builder.append(MESSAGE_SEPARATOR);
            }
            builder.append(pendency.getMessage());
        }
        return builder.toString();
    }

    /**
     * Retorna uma mensagem com o texto localizado de todas as pendências concatenados.
     */
    @Override
    public String getLocalizedMessage() {
        StringBuilder builder = new StringBuilder();
        for (Pendency pendency : pending) {
            if (builder.length() > 0) {
                builder.append(MESSAGE_SEPARATOR);
            }
            builder.append(pendency.getLocalizedMessage());
        }
        return builder.toString();
    }

}
