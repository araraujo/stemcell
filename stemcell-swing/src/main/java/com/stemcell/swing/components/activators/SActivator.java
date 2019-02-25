package com.stemcell.swing.components.activators;

import com.stemcell.common.util.AssertUtils;
import com.stemcell.swing.components.DescribedComponent;
import com.stemcell.swing.components.SLabel;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;

/**
 * Label descritor usado como uma representação visual para um objeto de
 * negócio. A informação do objeto de negócio (userObject) é obrigatória. Caso 
 * contrário, o activator lança uma exceção do tipo BusinessException. <p>
 * 
 * Um objeto activator também armazena uma referência para um componente
 * Swing (userComponent). Idealmente, este seria o componente responsável por
 * exibir os dados do objeto de negócio na tela. Entretanto, o seu uso não é
 * predefinido pelo framework, cabendo ao desenvolvedor decidir a sua
 * aplicação.
 *
 */
public class SActivator extends SLabel {
    private static final String NULL_PROPERTY_MSG = "fcorp.swing.components.nullProperty";
    private Object userObject;
    private JComponent userComponent;

    /**
     * Construtor
     * @param userObject Objeto que identifica o componente
     * @param userComponent Componente a ser controlado
     */
    public SActivator(Object userObject, JComponent userComponent) {
        this.setUserObject(userObject);
        this.setUserComponent(userComponent);
        this.setOpaque(true);
    }

    public Object getUserObject() {
        return userObject;
    }

    /**
     * Define o objeto que identifica o componente.
     * @param userObject objeto que identifica o componente
     */
    public final void setUserObject(Object userObject) {
        AssertUtils.assertExpression(userObject != null, NULL_PROPERTY_MSG, "(userObject)");
        
        Object old = this.userObject;
        this.userObject = userObject;
        firePropertyChange("userObject", old, this.userObject);
    }

    public JComponent getUserComponent() {
        return userComponent;
    }

    /**
     * Define o component controlado pelo activator
     * @param userComponent userComponent
     */
    public final void setUserComponent(JComponent userComponent) {
        AssertUtils.assertExpression(userComponent != null, NULL_PROPERTY_MSG, "(userComponent)");

        Object old = this.userComponent;
        this.userComponent = userComponent;
        firePropertyChange("userComponent", old, this.userComponent);

        this.updateDescriptionFromComponent();
    }

    /**
     * Atualiza a descrição a partir do userComponent
     * @return true se foi atualizado
     */
    public boolean updateDescriptionFromComponent() {
        if (userComponent instanceof DescribedComponent) {
            DescribedComponent c = (DescribedComponent) userComponent;
            this.setText(c.getDescription());
            this.setIcon(c.getIcon());
            return true;
        } else if (userComponent instanceof JInternalFrame) {
            JInternalFrame f = (JInternalFrame) userComponent;
            this.setText(f.getTitle());
            this.setIcon(f.getFrameIcon());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (null == obj) {
            return false;
        } else if (!(obj instanceof SActivator)) {
            return false;
        }

        SActivator other = (SActivator) obj;

        if (null != this.getUserObject() && null != other.getUserObject()) {
            return this.getUserObject().equals(other.getUserObject());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        if (null != this.getUserObject()) {
            return this.getUserObject().hashCode();
        } else {
            return super.hashCode();
        }
    }
}
