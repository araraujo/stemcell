package com.stemcell.swing.assync.processor.impl;

import com.stemcell.swing.assync.processor.PostProcessor;
import java.util.List;

public class AddValueToListPostProcessor implements PostProcessor {

    private int index;
    private Object valueToAdd;

    public AddValueToListPostProcessor(int index, Object valueToAdd) {
        this.index = index;
        this.valueToAdd = valueToAdd;
    }

    @Override
    public Object postProccess(Object result) {
        if (result != null && (result instanceof List)) {
            ((List) result).add(index, valueToAdd);
        }
        return result;
    }
}
