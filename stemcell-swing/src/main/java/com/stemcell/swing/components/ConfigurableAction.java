package com.stemcell.swing.components;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

/**
 * Action configurável de uso geral para botões e itens de menu.
 * As propriedades methodName e target indicam o método que deve
 * ser invocado quando um evento ActionPerformed ocorrer.
 *
 * @author x4rb
 */
public class ConfigurableAction extends AbstractAction {
    private static final String PROPERTY_TOOLTIP = "toolTipText";
    private static final String PROPERTY_METHOD_NAME = "methodName";
    private static final String PROPERTY_TARGET = "target";
    /**
     * Method resolvido a ser invocado pela ação
     */
    private Method method;
    /**
     * Nome do método configurado na interface para ser executado
     */
    private String methodName;
    /**
     * Objeto no qual o método é chamado pela ação
     */
    private Object target;
    /**
     * Mensagem de confirmação a ser exibida antes da execução da ação
     */
    private String confirm;
    /**
     * Component no qual o método repaint é chamado após execução da ação
     */
    private Component componentToRepaint;
    /**
     * Parâmetros passados ao método executado na chamada da ação
     */
    private Object[] params;

    /**
     * Construtor padrão
     */
    public ConfigurableAction() {
    }

    /**
     * Construtor
     * @param icon ícone
     * @param text label da ação
     * @param methodName nome do método a ser chamado
     * @param target Objeto destino do método
     * @param params parâmetros a ser passados para a chamada do método
     */
    public ConfigurableAction(Icon icon, String text, String methodName, Object target, Object[] params) {
        setIcon(icon);
        setText(text);
        setMethodName(methodName);
        setTarget(target);
        if (params!=null) {
            this.params = new Object[params.length];
            System.arraycopy(params, 0, this.params, 0, this.params.length);
        }
    }

    /**
     * Construtor
     * @param icon ícone
     * @param text label da ação
     * @param methodName nome do método a ser chamado
     * @param target Objeto destino do método
     */
    public ConfigurableAction(Icon icon, String text, String methodName, Object target) {
        this(icon, text, methodName, target, null);
    }

    public String getText() {
        return (getValue(NAME) != null) ? getValue(NAME).toString() : null;
    }

    /**
     * Define o texto da ação
     * @param text texto
     */
    public final void setText(String text) {
        Object old = getTarget();
        putValue(NAME, text);
        firePropertyChange("text", old, text);
        if (getToolTipText() == null) {
            putValue(SHORT_DESCRIPTION, text);
            firePropertyChange(PROPERTY_TOOLTIP, old, text);
        }
    }

    public String getToolTipText() {
        return (getValue(SHORT_DESCRIPTION) != null) ? getValue(SHORT_DESCRIPTION).toString() : null;
    }

    /**
     * Define um tooltip para a ação
     * @param text tooltip
     */
    public void setToolTipText(String text) {
        Object old = getTarget();
        putValue(SHORT_DESCRIPTION, text);
        firePropertyChange(PROPERTY_TOOLTIP, old, text);
    }

    public Icon getIcon() {
        return (Icon) getValue(SMALL_ICON);
    }

    /**
     * Define o ícone da ação
     * @param icon ícone
     */
    public final void setIcon(Icon icon) {
        Object old = getIcon();
        putValue(SMALL_ICON, icon);
        firePropertyChange("icon", old, getIcon());
    }

    public String getMethodName() {
        return methodName;
    }

    /**
     * Nome do método configurado na interface para ser executado
     * @param methodName Nome do método
     */
    public final void setMethodName(String methodName) {
        Object old = this.methodName;
        this.methodName = methodName;
        if (!findMethod()) {
            firePropertyChange(PROPERTY_METHOD_NAME, old, this.methodName);
        }
    }

    public Object getTarget() {
        return target;
    }

    /**
     * Objeto no qual o método é chamado pela ação
     * @param target Objeto target
     */
    public final void setTarget(Object target) {
        Object old = this.target;
        this.target = target;
        if (!findMethod()) {
            firePropertyChange(PROPERTY_TARGET, old, this.target);
        }
    }

    public Component getComponentToRepaint() {
        return componentToRepaint;
    }

    /**
     * Component no qual o método repaint é chamado após execução da ação
     * @param componentToRepaint componente a ser repintado
     */
    public void setComponentToRepaint(Component componentToRepaint) {
        Object old = this.componentToRepaint;
        this.componentToRepaint = componentToRepaint;
        firePropertyChange("componentToRepaint", old, this.componentToRepaint);
    }

    /**
     * Busca um método na classe do objeto targeta partir do methodName
     * @return true se um método foi encontrado
     */
    protected boolean findMethod() {
        if (target == null || methodName == null) {
            return false;
        }
        try {
            method = target.getClass().getMethod(methodName);
            return true;
        } catch (NoSuchMethodException ex) {
            return findMethodIgnoringArgs();
        } catch (SecurityException ex) {
            return findMethodIgnoringArgs();
        }
    }

    /**
     * Busca o método pelo methodName e ignora lista de argumentos
     * @return true se um método foi encontrado
     */
    private boolean findMethodIgnoringArgs() {
        for (Method m : target.getClass().getDeclaredMethods()) {
            if (m.getName().equals(methodName)) {
                method = m;
                return true;
            }
        }
        DialogMessages.error(JFrame.getFrames()[0], String.format("Metodo inválido (%s) em %s! Escolha o método!", methodName, target.getClass().getName()));
        this.methodName = null;
        firePropertyChange(PROPERTY_METHOD_NAME, this.methodName, null);
        this.target = null;
        firePropertyChange(PROPERTY_TARGET, this.target, null);
        method = null;

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent ae) {
        if (method == null) {
            DialogMessages.error(null, "Ação não configurada");
            return;
        }
        try {
            if (confirm != null && !DialogMessages.confirm(JFrame.getFrames()[0], confirm)) {
                return;
            }
            Object[] obj = new Object[method.getParameterTypes().length];
            if (this.params != null) {
                obj = params;
            } else if (obj.length > 0 && ActionEvent.class.isAssignableFrom(method.getParameterTypes()[0])) {
                obj[0] = ae;
            }
            method.setAccessible(true);
            method.invoke(target, obj);

            if (componentToRepaint != null) {
                componentToRepaint.repaint();
            }
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            throw new UndeclaredThrowableException(ex.getCause());
        }
        
    }

    @Override
    public void setEnabled(boolean arg0) {
        super.setEnabled(arg0);
    }

    public String getConfirm() {
        return confirm;
    }

    /**
     * Define uma mensagem de confirmação a ser exibida sempre antes da ação
     * ser executada
     * @param confirm mensagem de confirmação
     */
    public void setConfirm(String confirm) {
        Object old = this.confirm;
        this.confirm = confirm;
        firePropertyChange("confirm", old, this.confirm);
    }

    /**
     * Define um atalho de teclado para a ação
     * @param ks atalho
     */
    public void setShortcut(KeyStroke ks) {
        putValue(AbstractAction.ACCELERATOR_KEY, ks);
    }

    public KeyStroke getShortcut() {
        return (KeyStroke) getValue(AbstractAction.ACCELERATOR_KEY);
    }

    /**
     * Registra aação no mapa de entradas do componente
     * @param c componente
     */
    public void registerInto(JComponent c) {
        c.getActionMap().put(methodName, this);
        c.getInputMap().put(getShortcut(), methodName);
    }

    /**
     * Desregistra aação do mapa de entradas do componente
     * @param c componente
     */
    public void unregisterFrom(JComponent c) {
        c.getActionMap().remove(methodName);
        c.getInputMap().remove(getShortcut());
    }
}
