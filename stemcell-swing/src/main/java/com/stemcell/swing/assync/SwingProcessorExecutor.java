package com.stemcell.swing.assync;

import javax.swing.SwingUtilities;

/**
 *
 */
public class SwingProcessorExecutor implements ProcessorExecutor{

    @Override
    public void execute(Runnable processorsRunnable) {
        SwingUtilities.invokeLater(processorsRunnable);
    }

}
