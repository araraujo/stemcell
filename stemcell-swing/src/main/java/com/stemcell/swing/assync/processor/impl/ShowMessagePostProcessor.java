package com.stemcell.swing.assync.processor.impl;

import com.stemcell.swing.assync.processor.PostProcessor;
import java.awt.Component;
import javax.swing.JOptionPane;

public class ShowMessagePostProcessor implements PostProcessor {
    private Component parent;
    private String message;

    public ShowMessagePostProcessor(Component parent, String message) {
        this.parent = parent;
        this.message = message;
    }

    @Override
    public Object postProccess(Object result) {
        if (message.contains("%s")) {
            message = String.format(message, result+"");
        }
        JOptionPane.showMessageDialog(parent, message);
        return result;
    }
}
