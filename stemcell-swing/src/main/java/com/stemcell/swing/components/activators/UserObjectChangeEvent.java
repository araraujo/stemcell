package com.stemcell.swing.components.activators;

import java.util.EventObject;

/**
 * Um evento de "UserObjectChange" é enviado sempre que ocorre uma alteração na
 * instância do <code>userObject</code> associado a um <code>DescribedComponent</code>.
 * Um objeto <code>UserObjectChangeEvent</code> é enviado como argumento para
 * os métodos de um <code>UserObjectChangeListener</code>.
 *
 */
public class UserObjectChangeEvent extends EventObject {
    private Object oldValue;
    private Object newValue;

    /**
     * Construtor
     * @param source Fonte do evento
     * @param oldValue Valor antigo
     * @param newValue Valor novo
     */
    public UserObjectChangeEvent(Object source, Object oldValue, Object newValue) {
        super(source);
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }
}
