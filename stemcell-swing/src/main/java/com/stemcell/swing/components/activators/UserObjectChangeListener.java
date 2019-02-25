/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stemcell.swing.components.activators;

/**
 * Um evento de "UserObjectChange" é enviado sempre que ocorre uma alteração na
 * instância do <code>userObject</code> associado a um <code>DescribedComponent</code>.
 * Você pode registrar um <code>UserObjectChangeListener</code> através dos
 * métodos estáticos da classe <code>UserObjectChangeSupport</code> para ser
 * notificado de alterações no <code>userObject</code>.
 *
 */
public interface UserObjectChangeListener {

    /**
     * @param evt Notificação de mudança no userObject
     */
    public void userObjectChanged(UserObjectChangeEvent evt);

}
