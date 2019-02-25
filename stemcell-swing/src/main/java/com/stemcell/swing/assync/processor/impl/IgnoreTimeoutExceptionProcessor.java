package com.stemcell.swing.assync.processor.impl;

import com.stemcell.swing.assync.processor.ExceptionProcessor;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.SocketTimeoutException;

/**
 * ExceptionProcessor que abafa exeções de timeout (<code>java.net.SocketTimeoutException</code>). Se vier qualquer outra
 * exceção, ela será relançada.
 */
public class IgnoreTimeoutExceptionProcessor implements ExceptionProcessor {

    @Override
    public void exceptionProcess(Exception ex) {
        Throwable t = ex;
        do {
            if (t instanceof SocketTimeoutException) {
                return;
            }
            t = t.getCause();
        } while (t != null);
        
        throw (ex instanceof RuntimeException) ? (RuntimeException) ex : new UndeclaredThrowableException(ex);
    }

}
