package com.stemcell.common.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.observablecollections.ObservableListListener;

/**
 * Lista serializável e observável. Qualquer alteração em seus elementos é 
 * notificado aos listeners regitrados. É suportada pela API beansbinding pela
 * interface ObservableList
 * @param E Classe dos objetos da lista.
 */
public class SerializableObservableList<E> extends AbstractList<E> implements ObservableList<E>, Serializable {
    private static final long serialVersionUID = 1L;
    public static final String PROP_SIZE = "size";
    public static final String PROP_EMPTY = "empty";
    
    private List<E> list = new ArrayList<E>();
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private final boolean supportsElementPropertyChanged;

    private transient List<ObservableListListener> listeners;

    /**
     * Instancia uma lista observável com os elementos da lista passada por parâmetro.
     * @param list A lista que servirá de base para a observável.
     */
    public SerializableObservableList(List<E> list) {
        if (list == null) {
            list = new ArrayList<E>();
        } else if (list.getClass().getName().equals("org.hibernate.collection.PersistentBag") ||
                list.getClass().getName().equals("org.hibernate.collection.PersistentList")) {
            // Força inicialização de proxies hibernate
            list.size();
        }
        
        this.list = list;
        getListeners();
        this.supportsElementPropertyChanged = false;
    }
    
    /**
     * Definição de uma propriedade size que pode ser usada em bindings e escutadores
     * de properiedades
     * @return
     */
    public int getSize() {
        return list.size();
    }
    
    /**
     * Método padrão de adicição de PropertyChangeListener
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

     /**
     * Método padrão de remoção de PropertyChangeListener
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Construtor padrão
     */
    public SerializableObservableList() {
        this(null);
    }

    /**
     * Delegação para get da lista
     * @param index
     * @return
     */
    @Override
    public E get(int index) {
        return list.get(index);
    }

    /**
     * Delegação para size da lista
     * @return
     */
    @Override
    public int size() {
        return list.size();
    }

    /**
     * Delegação para set da lista e notificação
     * @param index
     * @param element
     * @return
     */
    @Override
    public E set(int index, E element) {
        int oldSize = list.size();
        E oldValue = list.set(index, element);
        
        for (ObservableListListener listener : getListeners()) {
            listener.listElementReplaced(this, index, oldValue);
        }
        propertyChangeSupport.firePropertyChange(PROP_SIZE, oldSize, list.size());
        propertyChangeSupport.firePropertyChange(PROP_EMPTY, oldSize==0, list.isEmpty());
        return oldValue;
    }

    /**
     * Delegação para add da lista e notificação
     * @param index
     * @param element
     */
    @Override
    public void add(int index, E element) {
        int oldSize = list.size();
        list.add(index, element);
        modCount++;
        for (ObservableListListener listener : getListeners()) {
            listener.listElementsAdded(this, index, 1);
        }
        propertyChangeSupport.firePropertyChange(PROP_SIZE, oldSize, list.size());
        propertyChangeSupport.firePropertyChange(PROP_EMPTY, oldSize==0, list.isEmpty());
    }

    /**
     * Delegação para remove da lista e notificação
     * @param index
     * @return
     */
    @Override
    public E remove(int index) {
        int oldSize = list.size();
        E oldValue = list.remove(index);
        modCount++;
        for (ObservableListListener listener : getListeners()) {
            listener.listElementsRemoved(this, index,
                    java.util.Collections.singletonList(oldValue));
        }
        propertyChangeSupport.firePropertyChange(PROP_SIZE, oldSize, list.size());
        propertyChangeSupport.firePropertyChange(PROP_EMPTY, oldSize==0, list.isEmpty());
        return oldValue;
    }

    /**
     * Delegação para addAll da lista e notificação
     * @param c
     * @return
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        return addAll(size(), c);
    }

    /**
     * Delegação para addAll da lista e notificação
     * @param index
     * @param c
     * @return
     */
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        int oldSize = list.size();
        if (list.addAll(index, c)) {
            modCount++;
            for (ObservableListListener listener : getListeners()) {
                listener.listElementsAdded(this, index, c.size());
            }
        }
        propertyChangeSupport.firePropertyChange(PROP_SIZE, oldSize, list.size());
        propertyChangeSupport.firePropertyChange(PROP_EMPTY, oldSize==0, list.isEmpty());
        return false;
    }

    /**
     * Delegação para clear da lista e notificação
     */
    @Override
    public void clear() {
        int oldSize = list.size();
        List<E> dup = new ArrayList<E>(list);
        list.clear();
        modCount++;
        for (ObservableListListener listener : getListeners()) {
            listener.listElementsRemoved(this, 0, dup);
        }
        propertyChangeSupport.firePropertyChange(PROP_SIZE, oldSize, list.size());
        propertyChangeSupport.firePropertyChange(PROP_EMPTY, oldSize==0, list.isEmpty());
    }

    /**
     * Delegação para containsAll da lista
     * @param c
     * @return
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    /**
     * Delegação para toArray da lista 
     * @param <T>
     * @param a
     * @return
     */
    @Override
    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }

    /**
     * Delegação para toArray da lista e notificação
     * @return
     */
    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    /**
     * Implementação da inteface ObservableList
     * @param listener
     */
    @Override
    public void addObservableListListener(ObservableListListener listener) {
        getListeners().add(listener);
    }

    /**
     * Implementação da inteface ObservableList
     * @param listener
     */
    @Override
    public void removeObservableListListener(ObservableListListener listener) {
        getListeners().remove(listener);
    }

    /**
     * Implementação da inteface ObservableList
     * @param listener
     */
    @Override
    public boolean supportsElementPropertyChanged() {
        return supportsElementPropertyChanged;
    }

    /**
     * Retorna a lista de listeners da lista
     * @return
     */
    private List<ObservableListListener> getListeners() {
        if (listeners == null) {
            listeners = new CopyOnWriteArrayList<ObservableListListener>();
        }
        return listeners;
    }
    
    /**
     * Verifica se existe algum listener da classe clazz escutando a lista
     * @param clazz
     * @return
     */
    public boolean hasListener(Class clazz) {
        for (ObservableListListener listener : getListeners()) {
            if (listener.getClass().getName().equals(clazz.getName())) {
                return true;
            }

        }
        return false;
    }

    /**
     * Implementação do equals
     * Retorna false sempre que <b>o</b> é null ou não é uma referência para
     * o este mesmo objeto 
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (o==null || o!=this ) {
            return false;
        } else {
            return super.equals(o);
        }
    }

    /**
     * Delega para hashCode da lista
     * @return
     */
    @Override
    public int hashCode() {
        return list.hashCode();
    }
    
    /**
     * Retorna a classe da lista base sendo observada
     * @return
     */
    public Class getObserverdListClass() {
        return list.getClass();
    }
   
}
