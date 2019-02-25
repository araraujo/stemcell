package com.stemcell.swing.base;

import javax.swing.Icon;
import javax.swing.JInternalFrame;


/**
 * InternalFrame pré-configurada
 */
public abstract class SInternalFrame extends JInternalFrame {
    private static Icon defaultIcon = null;

    /**
     * Construtor padrão
     */
    public SInternalFrame() {
        setFrameIcon(defaultIcon);
        setClosable(true);
        setResizable(true);
        setMaximizable(true);
        setIconifiable(true);
    }
    
    /**
     * Define o ícone padrão das próximas frames ciradas
     * @param defaultIcon Ícone
     */
    public static void setDefaultIcon(Icon defaultIcon) {
        SInternalFrame.defaultIcon = defaultIcon;
    }

    
}
