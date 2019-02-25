package com.stemcell.swing.base;

import com.stemcell.common.i18n.I18nManager;
import com.stemcell.swing.base.help.HelpManager;
import com.stemcell.swing.components.AboutBox;
import com.stemcell.swing.components.DebugDialog;
import com.stemcell.swing.components.SStatusPanel;
import com.stemcell.swing.components.action.GenericAction;
import com.stemcell.swing.components.activators.SActivator;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

/**
 * Classe base para o frame principal do sistema.
 * 
 */
public abstract class SFrame extends SDesktopSupportFrame {
    private static final String ABOUT_ACTION_TEXT_KEY = "fcorp.swing.base.about";
    /**
     * Action de exibição do help da aplicação
     */
    private GenericAction actionHelpContents;
    /**
     * Action de exibição da janela de About
     */
    private GenericAction actionAbout;
   

    /**
     * Construtor
     */
    public SFrame() {
        final int defaultWidth = 640;
        final int defaultHeight = 480;
        setIconImage(BaseApp.getApplication().getApplicationIcon());
        setSize(defaultWidth,defaultHeight);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        actionAbout = new GenericAction(null, I18nManager.getString(ABOUT_ACTION_TEXT_KEY), "showAbout", this);
        actionHelpContents = new GenericAction(null, I18nManager.getString(HELP_ACTION_TEXT_KEY), "showCurrentInternalFrameHelp", this);
        actionHelpContents.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("F1"));

        // Ação oculta de debug ao apertar F12
        final String debugActionName = "debug";
        this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F12"), debugActionName);
        this.getRootPane().getActionMap().put(debugActionName, new AbstractAction() {
            public void actionPerformed(ActionEvent ae) {
                DebugDialog debug = new DebugDialog(null);
                debug.setVisible(true);
            }
        });
    }


    /**
     * @return Ação de Sobre... da janela
     */
    public GenericAction getActionAbout() {
        return actionAbout;
    }

    /**
     * {@inheritDoc}
     */
    public GenericAction getActionHelpContents() {
        return actionHelpContents;
    }

    /**
     * Retorna o objeto <code>SStatusPanel</code> que o sistema deve usar para
     * exibir as mensagens de notificação. Template Method a ser definido pelas
     * subclasses.
     * @return SStatusPanel da janela principal
     */
    public abstract SStatusPanel getStatusPanel();

    /**
     * Exibe a tela de about padrão do sistema. Deve ser sobrescrito pelas
     * subclasses para exibição de uma tela customizada.
     */
    public void showAbout() {
      String s = null;
        try {
            s = I18nManager.getString("swing.base.aboutServer", null);
        } catch (Exception e) { // SUPPRESS CHECKSTYLE Illegal Catch - Deve capturar qualquer exceção em tempo de desenvolvimento
            s = I18nManager.getString("swing.base.fatClientMode", (e.getCause()!=null) ? e.getCause().toString() : "");
        }
        AboutBox about = new AboutBox(this, true, BaseApp.getApplication().getCredentialsBean(), s, I18nManager.getString(ABOUT_ACTION_TEXT_KEY), BaseApp.getApplication().getApplicationTitle());
        about.setVisible(true);
    }

    /**
     * Exibe a tela de help relativa ao internal frame ativo. Se nenhum frame
     * estiver aberto, o sistema exibe a tela padrão do sistema.
     */
    public void showCurrentInternalFrameHelp() {
        HelpManager.displayHelp((getDesktopPane().getSelectedFrame()!=null)
                                  ? getDesktopPane().getSelectedFrame().getClass().getSimpleName()
                                  : BaseApp.getApplication().getMainFrame().getClass().getSimpleName());
    }

    /**
     * <code>BaseApp</code> chama este método antes de exibir o <code>SFrame</code>.
     * Adiciona o openedFrames na lista de userObjectChangeListeners e adiciona
     * um listener em openedFrames para escutar alterações no activator
     * selecionado. O SFrame move o foco para o frame associado quando detecta
     * a mudança do activator selecionado. <p>
     *
     * Subclasses podem sobrescrever este método para adicionar outras 
     * atividades que precisam ser executadas após a instanciação e
     * configuração da tela inicial do sistema. É importante que as subclasses
     * façam uma chamada a <code>super.doAfterInit()</code> antes de adicionar
     * as suas customizações. Caso contrário, openedFrames não irá funcionar
     * corretamente.
     */
    public void doAfterInit() {
        this.addUserObjectChangeListener(getOpenedFrames());
        this.getOpenedFrames().addPropertyChangeListener("selectedActivator", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getNewValue() != null) {
                    moveFocusToFrameIdentifiedBy((SActivator) evt.getNewValue());
                }
            }
        });
    }

    /**
     * <code>BaseApp</code> chama este método após a execução de <code>logon</code>.
     * A implementação default não faz nada. Deve ser sobrescrito pelas subclasses
     * para realizar alguma ação necessária após a execução do login.
     */
    public void doAfterLogin() {}

}