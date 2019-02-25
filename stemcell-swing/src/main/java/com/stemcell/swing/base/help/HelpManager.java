package com.stemcell.swing.base.help;

import com.stemcell.swing.base.BaseApp;
import com.stemcell.swing.base.SDialog;
import java.awt.Component;
import java.awt.event.ActionListener;
import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.swing.AbstractButton;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import org.slf4j.LoggerFactory;

/**
 * Helper de exibição do JavaHelp
 */
public final class HelpManager {
    /**
     * HelpSet da aplicação
     */
    private static HelpSet helpSet;
    /**
     * HelpBroker gerenciador do help
     */
    private static HelpBroker broker;

    /**
     * Construtor privado
     */
    private HelpManager() {
        
    }

    /**
     * Inicialização do sistema de help
     */
    static {
        try {
            helpSet = new HelpSet(BaseApp.getApplication().getClass().getClassLoader(), BaseApp.getApplication().getHelpSetURL());
            broker = helpSet.createHelpBroker(BaseApp.getApplication().getHelpBrokerId());
        } catch (HelpSetException ex) {
            LoggerFactory.getLogger(HelpManager.class).error("WARNING: Sistema de HELP não foi configurado corretamente", ex);
            helpSet = new HelpSet();
            broker = helpSet.createHelpBroker();
        }
    }

    /**
     * Habilita a ação de help para a tecla F1
     * @param frame Frame
     */
    public static void configureFrameHelp(JFrame frame) {
        String topic = frame.getClass().getSimpleName();
        broker.enableHelpKey(frame.getRootPane(), topic, helpSet);
    }

    /**
     * Habilita a ação de help para a tecla F1
     * @param frame Frame
     */
    public static void configureInternalFrameHelp(JInternalFrame frame) {
        String topic = frame.getClass().getSimpleName();
        broker.enableHelpKey(frame.getRootPane(), topic, helpSet);
    }

    /**
     * Habilita a ação de help para a tecla F1
     * @param dialog Dialog
     */
    public static void configureDialogHelp(SDialog dialog) {
        String topic = dialog.getClass().getSimpleName();
        broker.enableHelpKey(dialog.getRootPane(), topic, helpSet);
    }

    public static ActionListener getHelpContentsActionListener() {
        return new CSH.DisplayHelpFromSource(broker);
    }
    /**
     * Exibe o frame dehelp no id selecionado
     * @param helpID Id do tópico a ser aberto
     */
    public static void displayHelp(String helpID) {
        broker.setCurrentID(helpID);
        broker.setDisplayed(true);
    }

    /**
     * Habilita a ação de help para a tecla F1
     * @param c Component
     * @param helpId Id do tópico a ser aberto
     */
    public static void configureContextHelp(Component c, String helpId) {
        CSH.setHelpIDString(c, helpId);
        broker.enableHelpKey(c, helpId, helpSet);
    }

    /**
     * Habilita a ação de help para a tecla F1
     * @param button AbstractButton
     * @param helpId Id do tópico a ser aberto
     */
    public static void configureHelpComponent(AbstractButton button, String helpId) {
        CSH.setHelpIDString(button, helpId);
        button.addActionListener(new CSH.DisplayHelpFromSource(broker));
    }

}
