package com.stemcell.common.beans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * Superclass entity template, with several utilitarian features.
 */
public class AbstractBean implements Cloneable, Serializable {
    static final long serialVersionUID = 1L;
    
    private transient PropertyChangeSupport support = new PropertyChangeSupport(this);
    
    public AbstractBean() {
    }

    @Override
    public Object clone() {
        try {
            AbstractBean bean = (AbstractBean) super.clone();
            bean.support = new PropertyChangeSupport(bean);
            return bean;
        } catch (CloneNotSupportedException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
    
    public void removeAllPropertyChangeListeners() {
        for (PropertyChangeListener propertyChangeListener : support.getPropertyChangeListeners()) {
            support.removePropertyChangeListener(propertyChangeListener);
        }
    }

    protected void firePropertyChange(String prop, Object oldValue, Object newValue) {
        support.firePropertyChange(prop, oldValue, newValue);
    }


    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        support = new PropertyChangeSupport(this);
    }
}