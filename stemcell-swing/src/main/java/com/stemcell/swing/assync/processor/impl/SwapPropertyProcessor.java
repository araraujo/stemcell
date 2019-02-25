package com.stemcell.swing.assync.processor.impl;

import com.stemcell.swing.assync.processor.PostProcessor;
import com.stemcell.swing.assync.processor.PreProcessor;
import com.stemcell.swing.assync.processor.PreScheduleProcessor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Property;

/**
 * Processor que associa temporariamente o valor a uma propriedade enquanto
 * aguarda o fim da execução
 */
public class SwapPropertyProcessor implements PostProcessor, PreScheduleProcessor, PreProcessor {

    private static final String VALUE_NOT_SET = "___#_NOT_SET_#___";
    private final Object target;
    private Object originalValue;
    private Object swapValue;
    private Property beanProperty;

    public SwapPropertyProcessor(Object target, String property, Object swapValue, Object originalValue) {
        this.target = target;
        this.swapValue = swapValue;
        this.beanProperty = BeanProperty.create(property);
        this.originalValue = originalValue;
    }

    public SwapPropertyProcessor(Object target, String property, Object swapValue) {
        this(target, property, swapValue, VALUE_NOT_SET);
    }

    @Override
    public Object postProccess(Object result) {
        if (!VALUE_NOT_SET.equals(originalValue) && beanProperty.isReadable(target)) {
            beanProperty.setValue(target, originalValue);
        }
        return result;
    }

    @Override
    public void preSchedule(Callable call) {
        if (VALUE_NOT_SET.equals(originalValue) && beanProperty.isReadable(target)) {
            // Guarda o valor original
            originalValue = beanProperty.getValue(target);
        }

        swap();
    }

    @Override @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public void preProccess(Callable call) {
        if (beanProperty.isReadable(target)) {
            // Guarda o valor original
            Object value = beanProperty.getValue(target);
            if (value != swapValue) {
                swap();
            }
        }
    }

    private void swap() {
        if (beanProperty.isWriteable(target)) {
            // Automaticamente converte em lista se necessário
            if (beanProperty.getWriteType(target).isAssignableFrom(List.class) && !(swapValue instanceof List)) {
                List l = new ArrayList();
                l.add(swapValue);
                swapValue = l;
            }

            beanProperty.setValue(target, swapValue);
        }
    }
}