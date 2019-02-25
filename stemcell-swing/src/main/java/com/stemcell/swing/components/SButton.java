package com.stemcell.swing.components;

import com.stemcell.swing.components.util.ComponentRotationSupport;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

public class SButton extends JButton {
    private ComponentRotationSupport rotationSupport = new ComponentRotationSupport();

    /**
     * Construtor default.
     */
    public SButton() {}

    /**
     * Construtor.
     * @param text label
     */
    public SButton(String text) {
        super(text);
    }
    
    public boolean isVertical() {
        return rotationSupport.isVertical();
    }

    /**
     * Define se o botão deve ser renderizado verticalmente. O default é false.
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
     * Define o sentido da rotação do botão. Só é utilizado se vertical estiver
     * definido para true.
     * @param clockwise true, se for no sentido horário
     */
    public void setClockwise(boolean clockwise) {
        rotationSupport.setClockwise(clockwise);
        this.revalidate();
    }
    
    /**
     * Define a action e adiciona uma entrada no inputMap para que
     * ao teclar ENTER sobre o botão, esta action seja executada.
     * @param a ação
     */
    @Override
    public void setAction(Action a) {
        super.setAction(a);
        if (a!=null) {
            this.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), a.toString());
            this.getActionMap().put(a.toString(), a);
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
    
}
