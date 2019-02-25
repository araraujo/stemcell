package com.stemcell.swing.components;

import java.awt.BorderLayout;

public class DebugDialog extends FinalizeOnDisposeDialog {

    /** Creates new form DebugDialog
     * @param parent parent
     */
    public DebugDialog(java.awt.Frame parent) {
        super(parent, false);
        GCLabel gCLabel1 = new GCLabel();
        getContentPane().add(gCLabel1, BorderLayout.CENTER);
        getContentPane().setLayout(new BorderLayout());
        setLocationRelativeTo(parent);
    }

}
