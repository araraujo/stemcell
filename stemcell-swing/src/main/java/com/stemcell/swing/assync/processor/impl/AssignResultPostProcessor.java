package com.stemcell.swing.assync.processor.impl;

import com.stemcell.swing.assync.processor.PostProcessor;
import com.stemcell.swing.assync.processor.PreScheduleProcessor;
import java.util.concurrent.Callable;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Property;

/**
 * PostProcessor que associa o resultado da chamada a uma propriedade de um
 * objeto
 */
public class AssignResultPostProcessor implements PostProcessor, PreScheduleProcessor {

    private static final String VALUE_NOT_SET = "___#_NOT_SET_#___";
    private final Object target;
    private Property beanProperty;
    private Object valueWhileInvoking;

    public AssignResultPostProcessor(Object target, String property) {
        this(target, property, VALUE_NOT_SET);
    }

    public AssignResultPostProcessor(Object target, String property, Object valueWhileInvoking) {
        this.beanProperty = BeanProperty.create(property);
        this.target = target;
        this.valueWhileInvoking = valueWhileInvoking;
        if (!beanProperty.isWriteable(target)) {
            throw new IllegalArgumentException("A propriedade "+property+" de "+target+" não pode ser escrita (não tem um setter publico)");
        }
    }

    @Override
    public Object postProccess(Object result) {
        beanProperty.setValue(target, result);
        return result;
    }

    @Override
    public void preSchedule(Callable call) {
        if (!VALUE_NOT_SET.equals(valueWhileInvoking)) {
            beanProperty.setValue(target, valueWhileInvoking);
        }
    }
}
