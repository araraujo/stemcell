package com.stemcell.swing.components;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Window;
import java.lang.reflect.Field;
import java.util.List;
import javax.swing.JDialog;

/**
  * This dialog is used to prevent the consequences of this bug:
  http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6497929
  */
public class FinalizeOnDisposeDialog extends JDialog {
    /**
     * @param owner The owner Dialog from which the dialog is displayed or null if this dialog has no owner
     * @param title the String to display in the dialog's title bar
     * @param gc the GraphicsConfiguration of the target screen device. If gc is null, the same GraphicsConfiguration as the owning Dialog is used.
     * @param modalityType specifies whether dialog blocks input to other windows when shown. null value and unsupported modality types are equivalent to MODELESS
     */
    public FinalizeOnDisposeDialog(Window owner, String title, ModalityType modalityType, GraphicsConfiguration gc) {
        super(owner, title, modalityType, gc);
    }

    /**
     * @param owner The owner Dialog from which the dialog is displayed or null if this dialog has no owner
     * @param title the String to display in the dialog's title bar
     * @param modalityType specifies whether dialog blocks input to other windows when shown. null value and unsupported modality types are equivalent to MODELESS
     */
    public FinalizeOnDisposeDialog(Window owner, String title, ModalityType modalityType) {
        super(owner, title, modalityType);
    }

    /**
     * @param owner The owner Dialog from which the dialog is displayed or null if this dialog has no owner
     * @param title the String to display in the dialog's title bar
     */
    public FinalizeOnDisposeDialog(Window owner, String title) {
        super(owner, title);
    }

    /**
     * @param owner The owner Dialog from which the dialog is displayed or null if this dialog has no owner
     * @param modalityType specifies whether dialog blocks input to other windows when shown. null value and unsupported modality types are equivalent to MODELESS
     */
    public FinalizeOnDisposeDialog(Window owner, ModalityType modalityType) {
        super(owner, modalityType);
    }

    /**
     * @param owner The owner Dialog from which the dialog is displayed or null if this dialog has no owner
     */
    public FinalizeOnDisposeDialog(Window owner) {
        super(owner);
    }

    /**
     * @param owner The owner Dialog from which the dialog is displayed or null if this dialog has no owner
     * @param modal Specifies whether dialog blocks user input to other top-level
     * @param title the String to display in the dialog's title bar
     * @param gc the GraphicsConfiguration of the target screen device. If gc is null, the same GraphicsConfiguration as the owning Dialog is used.
     */
    public FinalizeOnDisposeDialog(Dialog owner, String title, boolean modal, GraphicsConfiguration gc) {
        super(owner, title, modal, gc);
    }

    /**
     * @param owner The owner Dialog from which the dialog is displayed or null if this dialog has no owner
     * @param modal Specifies whether dialog blocks user input to other top-level
     * @param title the String to display in the dialog's title bar
     */
    public FinalizeOnDisposeDialog(Dialog owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    /**
     * @param owner The owner Dialog from which the dialog is displayed or null if this dialog has no owner
     * @param title the String to display in the dialog's title bar
     */
    public FinalizeOnDisposeDialog(Dialog owner, String title) {
        super(owner, title);
    }

    /**
     * @param owner The owner Dialog from which the dialog is displayed or null if this dialog has no owner
     * @param modal Specifies whether dialog blocks user input to other top-level
     */
    public FinalizeOnDisposeDialog(Dialog owner, boolean modal) {
        super(owner, modal);
    }

    /**
     * @param owner The owner Dialog from which the dialog is displayed or null if this dialog has no owner
     */
    public FinalizeOnDisposeDialog(Dialog owner) {
        super(owner);
    }

    /**
     * @param owner The owner Dialog from which the dialog is displayed or null if this dialog has no owner
     * @param modal Specifies whether dialog blocks user input to other top-level
     * @param title the String to display in the dialog's title bar
     * @param gc the GraphicsConfiguration of the target screen device. If gc is null, the same GraphicsConfiguration as the owning Dialog is used.
     */
    public FinalizeOnDisposeDialog(Frame owner, String title, boolean modal, GraphicsConfiguration gc) {
        super(owner, title, modal, gc);
    }

    /**
     * @param owner The owner Dialog from which the dialog is displayed or null if this dialog has no owner
     * @param modal Specifies whether dialog blocks user input to other top-level
     * @param title the String to display in the dialog's title bar
     */
    public FinalizeOnDisposeDialog(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    /**
     * @param owner The owner Dialog from which the dialog is displayed or null if this dialog has no owner
     * @param title the String to display in the dialog's title bar
     */
    public FinalizeOnDisposeDialog(Frame owner, String title) {
        super(owner, title);
    }

    /**
     * @param owner The owner Dialog from which the dialog is displayed or null if this dialog has no owner
     * @param modal Specifies whether dialog blocks user input to other top-level
     */
    public FinalizeOnDisposeDialog(Frame owner, boolean modal) {
        super(owner, modal);
    }

    /**
     * @param owner The owner Dialog from which the dialog is displayed or null if this dialog has no owner
     */
    public FinalizeOnDisposeDialog(Frame owner) {
        super(owner);
    }

    /**
     * Construtor
     */
    public FinalizeOnDisposeDialog() {
        super();
    }

    @Override
    public void dispose() {
        super.dispose();
        try {
            final Field field = Dialog.class.getDeclaredField("modalDialogs");
            field.setAccessible(true);
            final List<?> list = (List<?>) field.get(null);
            list.remove(this);
        } catch (final Throwable ex) { // SUPPRESS CHECKSTYLE Illegal Catch - 
            ex.printStackTrace(System.out);
        }
    }
}