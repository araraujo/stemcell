package com.stemcell.swing.blocking;

/**
  * Event handler interface of a BlockingProxy
  */
public interface BlockingOperationListener {
    /**
      * Start operation execution event
      * @param descriptionMessage Description message published by operation
      */
    public void processStart(String descriptionMessage);
    
    /**
      * Event of successful completion of operation
      * @param descriptionMessage Message Description message published by the operation
      * @param successMessage Success message published by the operation
      */
    public void processSuccess(String descriptionMessage, String successMessage);
    
    /**
      * Failed operation execution finalization event
      * @param descriptionMessage Message Description message published by the operation
      * @param exception Exception raised by operation
      */
    public void processFailure(String descriptionMessage, Throwable exception);
    
    /**
      * Session expiration event raised when capturing
      * an AuthenticationException
      * @param descriptionMessage Message Description message published by the operation
      * @return True if you want the operation to be performed again
      */
    public boolean sessionExpired(String descriptionMessage);
}