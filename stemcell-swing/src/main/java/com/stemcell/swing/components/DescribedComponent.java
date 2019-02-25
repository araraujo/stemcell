package com.stemcell.swing.components;

import javax.swing.Icon;

/**
 * Componente com uma descrição pré-determinada que será utilizada
 * automaticamente por um <code>SActivator</code>.
 *
 */
public interface DescribedComponent {

    /**
     * @return objeto que identifica este componente.
     */
    public Object getUserObject();

    /**
     * @return texto que deve ser exibido no descritor deste componente.
     */
    public String getDescription();

    /**
     * @return ícone que deve ser exibido no descritor deste componente.
     *
     */
    public Icon getIcon();

}
