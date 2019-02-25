package com.stemcell.swing.exceptionhandler;

import com.stemcell.common.exception.BusinessException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author alessandro
 */
public class BusinessExceptionHandler implements ExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BusinessExceptionHandler.class);
    
    @Override
    public HandlerResult doHandle(Throwable throwable) {
        
        List<ExceptionMessage> exceptionMessageList = new ArrayList<ExceptionMessage>(0);
        
        if (throwable instanceof BusinessException) {

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Caught a %s while processing.",
                   throwable.getClass()));
            }

            BusinessException exception = (BusinessException) throwable;
            exception.getMessage();

            exceptionMessageList.add(new ExceptionMessage(
                    exception.getMessage(), exception.getMessageParams()));

            return new HandlerResult(true, exceptionMessageList);
        }

        return new HandlerResult(false, new ArrayList<ExceptionMessage>());
    }
    
}
