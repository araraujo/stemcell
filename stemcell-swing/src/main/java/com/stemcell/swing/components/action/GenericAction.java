package com.stemcell.swing.components.action;

import com.stemcell.swing.components.ConfigurableAction;
import javax.swing.Icon;

/**
 * ConfigurableAction com comportamento adicional de verificação de recursos
 * de segurança para habilitar/desabilitar
 *
 */
public class GenericAction extends ConfigurableAction {
    public static final String PROPERTY_NEEDED_RESOURCES = "neededSecurityResources";
    /**
     * Lista de recursos de segurança necessários para executar a ação, conferidos
     * com a obtenção de ICredentialsBean do BaseApp. A ação é desebilitada caso
     * o usuário não tenha os recursos
     */
    private String[] neededSecurityResources;
    /**
     * Assume a necessidade da existência de todos os neededSecurityResources
     * para liberar a execução da ação
     */
    private boolean allSecurityResourcesNeeded;

    /**
     * Construtor padrão
     */
    public GenericAction() {
    }

    /**
     * Construtor 
     * @param icon Icone
     * @param text Label
     * @param methodName Nome do método
     * @param target Objeto destino
     * @param params Parâmetros de chamada do método
     */
    public GenericAction(Icon icon, String text, String methodName, Object target, Object[] params) {
        super(icon, text, methodName, target, params);
        allSecurityResourcesNeeded = false;
    }

    /**
     * Construtor
     * @param icon Icone
     * @param text Label
     * @param methodName Nome do método
     * @param target Objeto destino
     */
    public GenericAction(Icon icon, String text, String methodName, Object target) {
        this(icon, text, methodName, target, null);
    }

    /**
     * Getter de neededSecurityResources
     * @return neededSecurityResources
     */
    public String[] getNeededSecurityResources() {
        if (neededSecurityResources==null) {
            return null;
        } else {
            String[] arrayCopy = new String[neededSecurityResources.length];
            System.arraycopy(neededSecurityResources, 0, arrayCopy, 0, arrayCopy.length);
            return arrayCopy;
        }
    }


    /**
     * Setter de neededSecurityResources
     * @param neededSecurityResources Lista de recursos de seguranças necessários
     */
    public void setNeededSecurityResources(String[] neededSecurityResources) {
        Object old = this.neededSecurityResources;
        if (neededSecurityResources==null) {
            this.neededSecurityResources = null;
            firePropertyChange(PROPERTY_NEEDED_RESOURCES, old, this.neededSecurityResources);
        } else {
            this.neededSecurityResources = new String[neededSecurityResources.length];
            System.arraycopy(neededSecurityResources, 0, this.neededSecurityResources, 0, this.neededSecurityResources.length);
            firePropertyChange(PROPERTY_NEEDED_RESOURCES, old, this.neededSecurityResources);
        }
    }

    /**
     * Força rechecagem das regras de segurança do baseapp
     */
    public void recheckSecurityResources() {
        boolean enabled = isEnabled();
        firePropertyChange("enabled", !enabled, enabled);
    }

    /**
     * Getter de allSecurityResourcesNeeded
     * @return allSecurityResourcesNeeded
     */
    public boolean isAllSecurityResourcesNeeded() {
        return allSecurityResourcesNeeded;
    }

    /**
     * Setter de allSecurityResourcesNeeded
     * @param allSecurityResourcesNeeded True  se todas as permissões são necessárias
     */
    public void setAllSecurityResourcesNeeded(boolean allSecurityResourcesNeeded) {
        Object old = this.allSecurityResourcesNeeded;
        this.allSecurityResourcesNeeded = allSecurityResourcesNeeded;
        firePropertyChange("allSecurityResourcesNeeded", old, this.allSecurityResourcesNeeded);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return super.isEnabled();
    }


}
