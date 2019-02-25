package com.stemcell.swing.base;

import com.stemcell.common.beans.AbstractBean;
import com.stemcell.common.beans.CredentialsBean;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;


/**
  * Base class of all presentation models.
  * @param <S> Service object type
*/
public abstract class BasePresentationModel extends AbstractBean implements PropertyChangeListener {

    /**
      * Current help message, which should be displayed on some component
      * on the screen represented by the model
    */
    private String helpMessage;


    /**
      * Default implementation of propertyChange
      * @param evt Event
    */    
    public void propertyChange(PropertyChangeEvent evt) {
        throw new UnsupportedOperationException(String.format("%s  não imlementa propertyChange", this.getClass().getSimpleName()));
    }

    /**
      * Shortcut to the instance of the credentials object in BaseApp
      * @return subject credentials
      */    
    public CredentialsBean getAuthenticationData() {
        return BaseApp.getApplication().getCredentialsBean();
    }

    /**
      * Default model status help message
      * @return helpMessage Default help message
      */    
    public String getHelpMessage() {
        return helpMessage;
    }

    /**
     * Setter de helpMessage
     * @param helpMessage
     */
    public void setHelpMessage(String helpMessage) {
        Object old = this.helpMessage;
        this.helpMessage = helpMessage;
        firePropertyChange("helpMessage", old, this.helpMessage);
    }

    /**
     * Notifies the main frame of a change in the userObject instance.
     * This method must be manually invoked by subclasses whenever
     * the object that identifies the frame associated with this presentation model for
     * changed. Example:
     *
     * <blockquote> <pre>
     * public class MyModel extends BasePresentationModel <MyService> {
     * private MyUserObject myObject; // injected by constructor
     *
     * ...
     *
     * public void save () {
     * MyUserObject old = this.myObject;
     * this.myObject = getService (). savingMyUserObject (this.myObject);
     * this.firePropertyChange ("myObject", null, this.myObject);
     * this.fireUserObjectChange (this, old, this.myObject);
     *}
     *}
     * </ pre> </ blockquote>
     * @param source Source object
     * @param oldValue Old value
     * @param newValue New value
     * @see SFrame # fireUserObjectChange (java.lang.Object, java.lang.Object, java.lang.Object)
     */
    protected void fireUserObjectChange(Object source, Object oldValue, Object newValue) {
        BaseApp.getApplication().getMainFrame().fireUserObjectChange(source, oldValue, newValue);
    } 

    /**
      * Utility that allows to apply property listeners throughout a graph
      * of objects from a root bean
      * @param bean bean to be observed
      */
    protected void deepObserve(AbstractBean bean) {
        Set context = new HashSet();
        recursiveDeepObserve(bean, context);
    }

    /**
      * Recursive call of the AbstractBean method
      * @see deepObserve (AbstractBean.class)
      * @param bean bean to be observed
      * @param context current context
      */
    private void recursiveDeepObserve(AbstractBean bean, Set context) {
        try {
            if (bean == null) {
                return;
            }
            bean.addPropertyChangeListener(this);
            AbstractBean property = null;
            for (PropertyDescriptor pd : Introspector.getBeanInfo(bean.getClass()).getPropertyDescriptors()) {
                if (AbstractBean.class.isAssignableFrom(pd.getPropertyType())) {
                    property = (AbstractBean) pd.getReadMethod().invoke(bean);
                    if (!context.contains(property)) {
                        context.add(property);
                        recursiveDeepObserve(property, context);
                    }
                }
            }
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex);
        } catch (IntrospectionException ex) {
            throw new RuntimeException(ex);
        }
    }
}