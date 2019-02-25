package com.stemcell.swing.components;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Componente com funcionalidade de barra de status
 */
public class SStatusPanel extends JPanel {
    private static ImageIcon  errorIcon = new ImageIcon(SStatusPanel.class.getResource("/icons/error.png"));
    private static ImageIcon infoIcon = new ImageIcon(SStatusPanel.class.getResource("/icons/information.png"));
    private JLabel statusMessageLabel;

    /**
     * Construtor
     */
    public SStatusPanel() {
        final int defaultWidth = 4;
        final int defaultHeigth = 20;
        setBorder(javax.swing.BorderFactory.createEtchedBorder());
        setPreferredSize(new java.awt.Dimension(defaultWidth, defaultHeigth));
        setLayout(new BorderLayout());
        statusMessageLabel = new JLabel();
        statusMessageLabel.setText(" "); 
        statusMessageLabel.setBackground(Color.BLUE);
        this.add(statusMessageLabel, BorderLayout.WEST);
    }

    /**
     * Exibe mensagem informativa
     * @param message mensagem
     */
    public void showInfoStatus(String message){
        statusMessageLabel.setIcon(infoIcon);
        statusMessageLabel.setForeground(Color.BLUE);
        statusMessageLabel.setText(message);
    }
    
    /**
     * Exibe uma mensagem de erro
     * @param message mensagem
     */
    public void showErrorStatus(String message){
        statusMessageLabel.setIcon(errorIcon);
        statusMessageLabel.setForeground(Color.RED);
        statusMessageLabel.setText(message);
    }
    
}
