package com.stemcell.swing.components;

import com.stemcell.swing.components.util.ComponentRotationSupport;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.JLabel;

public class SLabel extends JLabel {
    /**
     * Decorador de campos obrigatórios padrão
     */
    private static RequiredFieldDecorator requiredFieldDecorator = new DefaultRequiredFieldDecorator();

    private boolean requiredField;
    private ComponentRotationSupport rotationSupport = new ComponentRotationSupport();

    /**
     * Construtor.
     */
    public SLabel() {
    }
    /**
     * Construtor.
     * @param string label
     */
    public SLabel(String string) {
        super(string);
    }
    /**
     * Define o field decorator para todos os labels
     * @param requiredFieldDecorator requiredFieldDecorator
     */
    public static void setRequiredFieldDecorator(RequiredFieldDecorator requiredFieldDecorator) {
        if (requiredFieldDecorator!=null){
            SLabel.requiredFieldDecorator = requiredFieldDecorator;
        }
    }

    public boolean isRequiredField() {
        return requiredField;
    }

    /**
     * Setter de requiredField
     * @param requiredField requiredField
     */
    public void setRequiredField(boolean requiredField) {
        Object old = this.requiredField;
        this.requiredField = requiredField;
        firePropertyChange("requiredField", old, this.requiredField);
        if (requiredField) {
            requiredFieldDecorator.decorateRequiredLabel(this);
        } else {
            requiredFieldDecorator.undecorateRequiredLabel(this);
        }
    }
    
    public boolean isVertical() {
        return rotationSupport.isVertical();
    }

    /**
     * Define se o label deve ser renderizado verticalmente. O default é false.
     * @param vertical true, se for vertical
     */
    public void setVertical(boolean vertical) {
        rotationSupport.setVertical(vertical);
        this.revalidate();
    }
    
    public boolean isClockwise() {
        return rotationSupport.isClockwise();
    }

    /**
     * Define o sentido da rotação do label. Só é utilizado se vertical estiver
     * definido para true.
     * @param clockwise true, se for no sentido horário
     */
    public void setClockwise(boolean clockwise) {
        rotationSupport.setClockwise(clockwise);
        this.revalidate();
    }
    
    /*
     * (non-Javadoc)
     *
     * @see javax.swing.JLabel#setText(String)
     */
    @Override
    public void setText(String text) {
        super.setText(text);
        if (requiredField) {
            requiredFieldDecorator.decorateRequiredLabel(this);
        }
    }
    
    /*
     * (non-Javadoc)
     *
     * @see javax.swing.JComponent#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize() {
        return rotationSupport.getRotatedSize(super.getPreferredSize());
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.Component#getSize()
     */
    @Override
    public Dimension getSize() {
        return rotationSupport.getRotatedSizeIfNeeded(super.getSize());
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.JComponent#getHeight()
     */
    @Override
    public int getHeight() {
        return getSize().height;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.JComponent#getWidth()
     */
    @Override
    public int getWidth() {
        return getSize().width;
    }
    
    /*
     * (non-Javadoc)
     *
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    protected void paintComponent(Graphics g) {
        rotationSupport.startPainting();
        super.paintComponent(rotationSupport.rotateGraphicsIfNeeded(super.getSize(), g));
        rotationSupport.stopPainting();
    }

    /**
     * Classe que decora um label marcado como requerido
     */
    public static abstract class RequiredFieldDecorator {
        /**
         * Remove decoração do label requerido
         * @param label label
         */
        public abstract void decorateRequiredLabel(SLabel label);
        /**
         * Aplica decoração do label requerido
         * @param label label
         */
        public abstract void undecorateRequiredLabel(SLabel label);
    }

    /**
     * Classe padrão que decora um label marcado como requerido
     */
    public static class DefaultRequiredFieldDecorator extends RequiredFieldDecorator {
        /**
         * {@inheritDoc}
         */
        public void decorateRequiredLabel(SLabel label) {
            if (!label.getFont().isBold()) {
                label.setFont(label.getFont().deriveFont(Font.BOLD));
            }
        }
        /**
         * {@inheritDoc}
         */
        public void undecorateRequiredLabel(SLabel label) {
            if (label.getFont().isBold()) {
                label.setFont(label.getFont().deriveFont(label.getFont().getStyle() ^ Font.BOLD));
            }
        }
    }

}
