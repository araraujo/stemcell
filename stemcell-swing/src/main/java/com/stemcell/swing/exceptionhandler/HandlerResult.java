package com.stemcell.swing.exceptionhandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Objeto que encapsula a resposta do tratador de excecao, possibilitando
 * avaliacao se a excecao foi tratada ou nao, e a possivel mas nao obrigatoria
 * obtencao de mensagens de erro.
 */
public final class HandlerResult  {

   private boolean handled;
   private List<ExceptionMessage> messages;

    /**
     * Contrutor de um resultado de um tratador de excecao. O objeto
     * eh imutavel e so obtem estado atraves desse construtor. Normalmente
     * eh criado apos a tentativa de tratamento da excecao
     * @param handled informa se a excecao foi tratada ou nao
     * @param messages lista com as messagens de excacao, caso existam
     */
    public HandlerResult(boolean handled, List<ExceptionMessage> messages) {
        this.handled = handled;
        this.messages = new ArrayList<ExceptionMessage>(messages);
    }

    /**
     * Informa se a excecao foi tratada ou nao
     * @return true se a excecao for tratada ou false se nao.
     */
    public boolean isHandled() {
        return this.handled;
    }

    /**
     * Retorna a lista de mensagens de excecao tratadas pelo
     * respectivo tratador de excecao
     * @return Lista de ExceptionMessages caso existam
     */
    public List<ExceptionMessage> getMessages() {
        return new ArrayList<ExceptionMessage>(this.messages);
    }
}