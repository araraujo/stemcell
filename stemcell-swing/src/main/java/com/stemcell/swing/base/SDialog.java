package com.stemcell.swing.base;

import com.stemcell.swing.components.FinalizeOnDisposeDialog;
import com.stemcell.swing.components.action.GenericAction;
import java.awt.Dialog;
import java.awt.Dialog.ModalityType;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Window;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 * Classe base de diálogo com utilidades adicionais
 * 
 */
public class SDialog extends FinalizeOnDisposeDialog {
    private static final String ACTION_DISPOSE = "disposeAction";
    /**
     * Action de dispose padrao do diálogo
     */
    private Action disposeAction = new GenericAction(null, "", "dispose", this);

/**
     * @param owner The owner Dialog from which the dialog is displayed or null if this dialog has no owner
     * @param title the String to display in the dialog's title bar
     * @param gc the GraphicsConfiguration of the target screen device. If gc is null, the same GraphicsConfiguration as the owning Dialog is used.
     * @param modalityType specifies whether dialog blocks input to other windows when shown. null value and unsupported modality types are equivalent to MODELESS
     */
    public SDialog(Window owner, String title, ModalityType modalityType, GraphicsConfiguration gc) {
        super(owner, title, modalityType, gc);
    }

    /**
     * @param owner The owner Dialog from which the dialog is displayed or null if this dialog has no owner
     * @param title the String to display in the dialog's title bar
     * @param modalityType specifies whether dialog blocks input to other windows when shown. null value and unsupported modality types are equivalent to MODELESS
     */
    public SDialog(Window owner, String title, ModalityType modalityType) {
        super(owner, title, modalityType);
    }

    /**
     * @param owner The owner Dialog from which the dialog is displayed or null if this dialog has no owner
     * @param title the String to display in the dialog's title bar
     */
    public SDialog(Window owner, String title) {
        super(owner, title);
    }

    /**
     * @param owner The owner Dialog from which the dialog is displayed or null if this dialog has no owner
     * @param modalityType specifies whether dialog blocks input to other windows when shown. null value and unsupported modality types are equivalent to MODELESS
     */
    public SDialog(Window owner, ModalityType modalityType) {
        super(owner, modalityType);
    }

    /**
     * @param owner The owner Dialog from which the dialog is displayed or null if this dialog has no owner
     */
    public SDialog(Window owner) {
        super(owner);
    }

    /**
     * @param owner The owner Dialog from which the dialog is displayed or null if this dialog has no owner
     * @param modal Specifies whether dialog blocks user input to other top-level
     * @param title the String to display in the dialog's title bar
     * @param gc the GraphicsConfiguration of the target screen device. If gc is null, the same GraphicsConfiguration as the owning Dialog is used.
     */
    public SDialog(Dialog owner, String title, boolean modal, GraphicsConfiguration gc) {
        super(owner, title, modal, gc);
    }

    /**
     * @param owner The owner Dialog from which the dialog is displayed or null if this dialog has no owner
     * @param modal Specifies whether dialog blocks user input to other top-level
     * @param title the String to display in the dialog's title bar
     */
    public SDialog(Dialog owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    /**
     * @param owner The owner Dialog from which the dialog is displayed or null if this dialog has no owner
     * @param title the String to display in the dialog's title bar
     */
    public SDialog(Dialog owner, String title) {
        super(owner, title);
    }

    /**
     * @param owner The owner Dialog from which the dialog is displayed or null if this dialog has no owner
     * @param modal Specifies whether dialog blocks user input to other top-level
     */
    public SDialog(Dialog owner, boolean modal) {
        super(owner, modal);
    }

    /**
     * @param owner The owner Dialog from which the dialog is displayed or null if this dialog has no owner
     */
    public SDialog(Dialog owner) {
        super(owner);
    }

    /**
     * @param owner The owner Dialog from which the dialog is displayed or null if this dialog has no owner
     * @param modal Specifies whether dialog blocks user input to other top-level
     * @param title the String to display in the dialog's title bar
     * @param gc the GraphicsConfiguration of the target screen device. If gc is null, the same GraphicsConfiguration as the owning Dialog is used.
     */
    public SDialog(Frame owner, String title, boolean modal, GraphicsConfiguration gc) {
        super(owner, title, modal, gc);
    }

    /**
     * @param owner The owner Dialog from which the dialog is displayed or null if this dialog has no owner
     * @param modal Specifies whether dialog blocks user input to other top-level
     * @param title the String to display in the dialog's title bar
     */
    public SDialog(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    /**
     * @param owner The owner Dialog from which the dialog is displayed or null if this dialog has no owner
     * @param title the String to display in the dialog's title bar
     */
    public SDialog(Frame owner, String title) {
        super(owner, title);
    }

    /**
     * @param owner The owner Dialog from which the dialog is displayed or null if this dialog has no owner
     * @param modal Specifies whether dialog blocks user input to other top-level
     */
    public SDialog(Frame owner, boolean modal) {
        super(owner, modal);
    }

    /**
     * @param owner The owner Dialog from which the dialog is displayed or null if this dialog has no owner
     */
    public SDialog(Frame owner) {
        super(owner);
    }

    /**
     * Construtor
     */
    public SDialog() {
        super();
    }


    /**
     * Configura automaticamente o fechamento
     * do dialogo ao pressionar a tecla ESC
     * @param b True para habilitar
     */
    public void setDisposeOnEsc(boolean b) {
        if (b) {
            getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), ACTION_DISPOSE);
            getRootPane().getActionMap().put(ACTION_DISPOSE, disposeAction);
        } else {
            getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).remove(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
            getRootPane().getActionMap().remove(ACTION_DISPOSE);
        }
    }

    /**
     * Getter de disposeAction
     * @return disposeAction
     */
    public Action getDisposeAction() {
        return disposeAction;
    }

}
