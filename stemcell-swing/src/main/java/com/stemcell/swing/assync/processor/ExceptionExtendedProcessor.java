package com.stemcell.swing.assync.processor;

import com.stemcell.swing.assync.AssyncInvoker;

/**
 * Interface para criação de tratador de exceção
 */
public interface ExceptionExtendedProcessor extends Processor {

    /**
     * Método chamado quando a chamada assíncrona levanta exceção
     *
     * @param ex
     * @param invoker
     */
    void exceptionProcess(AsyncExceptionData asyncExceptionData);

    class AsyncExceptionData {
        public final Exception exception;
        public final AssyncInvoker invoker;

        public AsyncExceptionData(Exception exception, AssyncInvoker invoker) {
            this.exception = exception;
            this.invoker = invoker;
        }
    }
}
