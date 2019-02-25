package com.stemcell.swing.components.activators;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe de suporte a mudança em objetos de controle dos activators
 */
public class UserObjectChangeSupport {

    private List<UserObjectChangeListener> listeners;

    /**
     * Adiciona um listener
     * @param listener listener a ser adicionado
     * @return true se foi adicionado
     */
    public synchronized boolean addUserObjectChangeListener(UserObjectChangeListener listener) {
        if (listener == null) {
            return false;
        }
        if (listeners == null) {
            listeners = new ArrayList<UserObjectChangeListener>();
        }
        return this.listeners.add(listener);
    }

    /**
     * Remove um listener
     * @param listener listener a ser removido
     * @return true se foi adicionado
     */
    public synchronized boolean removeUserObjectChangeListener(UserObjectChangeListener listener) {
        if (listener == null) {
            return false;
        }
        if (listeners == null) {
            return false;
        }
        return this.listeners.remove(listener);
    }

    /**
     * Remove todos os listeners
     */
    public synchronized void clearUserObjectChangeListeners() {
        this.listeners.clear();
    }

    /**
     * Notifica alterações no objeto de controle
     * @param source Fonte da alteração
     * @param oldValue valor antigo
     * @param newValue valor novo
     */
    public void fireUserObjectChange(Object source, Object oldValue, Object newValue) {
        if (oldValue == newValue) {
            return;
        }
        UserObjectChangeEvent event = new UserObjectChangeEvent(source, oldValue, newValue);
        for (UserObjectChangeListener listener : listeners) {
            listener.userObjectChanged(event);
        }
    }
}
