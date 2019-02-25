package com.stemcell.swing.exceptionhandler;

import com.stemcell.common.util.ExceptionUtil;
import com.stemcell.swing.components.DialogMessages;
import com.stemcell.swing.components.UnknownExceptionDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Toolkit;
import java.awt.EventQueue;
import java.awt.AWTEvent;
import java.util.ArrayList;

/**
 * Barreira de exceção aplicada como escutadora da thread EventDispatcher
 * do Swing, impedindo que exceções que sejam lançadas nesta thread sejam
 * simplesmente enviadas para o console
 */
public final class SwingExceptionHandlerManager extends ExceptionHandlerManager {
    /**
     * Instância singleton do handler manager
     */
    private static SwingExceptionHandlerManager instance;

    /**
     * Frame da aplicação usado na exibição de diálogo e atualizações
     * da barra de status
     */
    private JFrame frame;

    /**
     * Construtor privado da instância singleton que inicializa os
     * HandlerManagers padrão e se instala na fila de eventos do Swing
     */
    private SwingExceptionHandlerManager() {
        super(new ArrayList<ExceptionHandler>());
        // Adiciona os HandlerManagers padrão
        getHandlerList().add(new BusinessExceptionHandler());

        // Insere um novo tratador da fila de eventos do Swing
        Toolkit.getDefaultToolkit().getSystemEventQueue().push(new EventQueue() {
            @Override protected void dispatchEvent(AWTEvent event) {
                try {
                    // Delega o tratamento padrão do evento
                    super.dispatchEvent(event);
                } catch (final Throwable e) { // SUPPRESS CHECKSTYLE Illegal Catch - Barreira de excecao
                    // captura qualquer execeção vazada e redireciona para tratamento
                    doHandle(e);
                }
            }
        });
    }


    /**
     * Método para obtenção da instância singleton do
     * SwingExceptionHandlerManager. Quando a primeira chamada é efetuada, a
     * instância singleton inicializada cria um proxy da fila de eventos do
     * Swing que captura qualquer exceção vazada pelos tratamentos da thread
     * EventDispatcher.
     * @return Instância singleton do handler manager
     */
    public static SwingExceptionHandlerManager getHandlerManager() {
        if (instance==null) {
            instance = new SwingExceptionHandlerManager();
        }
        return instance;
    }

    /**
     * Setter da propriedade frame
     * @param frame Frame usado como partent dos diálogos de mensagem
     */
    public void setFrame(JFrame frame) {
        this.frame = frame;
    }

    /**
     * {@inheritDoc}
     * Caso nenhum tratador aceite a exceção, deve ser exibido o diálogo de
     * erro desconhecido.
     */
    @Override
    public HandlerResult doHandle(Throwable cause) {
        cause = ExceptionUtil.cleanException(cause);

        // Verifica se houve um tratamento adequado
        HandlerResult handlerResult =  super.doHandle(cause);

        if (handlerResult.isHandled()) {
            for (ExceptionMessage exceptionMessage : handlerResult.getMessages()) {
                SwingUtilities.invokeLater(new UserFriendlyExceptionMessageDialogCommand(exceptionMessage));
            }
        // Se não houve tratamento, exibe o diálogo detalhado
        // Primeiro de se checar se já existe um diálogo sendo exibido, para
        // evitar que múltiplos sejam abertos, inundando a tela
        } else {
            SwingUtilities.invokeLater(new UnhandledExceptionMessageDialogCommand(cause));
        }

        //retorna uma lista qualquer, a api exige, mas nao eh utilizado por ninguem
        return handlerResult;
    }

    /**
     * Runnable que exibe um diálogo de erro amigável ao usuário
     */
    class UserFriendlyExceptionMessageDialogCommand implements Runnable {
        private ExceptionMessage message;

        /**
         * Construtor padrão
         * @param message Mensagem a ser exibida
         */
        public UserFriendlyExceptionMessageDialogCommand(ExceptionMessage message) {
            this.message = message;
        }

        @Override
        public void run() {
            DialogMessages.error(frame, message.getKey());
        }

    }

    /**
     * Runnable que exibe um diálogo de erro não tratado ao usuário
     */
    class UnhandledExceptionMessageDialogCommand implements Runnable {
        private Throwable throwable;

        /**
         * Construtor padrão
         * @param throwable Exceção a ser reportada
         */
        public UnhandledExceptionMessageDialogCommand(Throwable throwable) {
            this.throwable = throwable;
        }

        @Override
        public void run() {
            if (UnknownExceptionDialog.getVisibleDialogsCount() <= 0) {
                UnknownExceptionDialog ed = new UnknownExceptionDialog(frame,
                                                    new StringBuilder(ExceptionUtil.getStackAsString(throwable)).toString());
                ed.setVisible(true);
            }
        }

    }



}