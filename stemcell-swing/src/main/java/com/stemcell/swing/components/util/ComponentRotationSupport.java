package com.stemcell.swing.components.util;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

/**
 * Fornece a funcionalidade de rotação para os componentes do framework.
 * 
 */
public class ComponentRotationSupport {
    private boolean vertical = false;
    private boolean clockwise = false;
    private boolean painting = false;

    /**
     * Construtor.
     */
    public ComponentRotationSupport() {}
    
    public boolean isVertical() {
        return vertical;
    }

    /**
     * Define se o componente deve ser renderizado verticalmente. O default é false.
     * @param vertical true, se for vertical
     */
    public void setVertical(boolean vertical) {
        this.vertical = vertical;
    }
    
    public boolean isClockwise() {
        return clockwise;
    }

    /**
     * Define o sentido da rotação do componente. Só é utilizado se vertical estiver
     * definido para true.
     * @param clockwise true, se for no sentido horário
     */
    public void setClockwise(boolean clockwise) {
        this.clockwise = clockwise;
    }
    
    /**
     * Retorna um objeto Dimension rotacionado, caso vertical seja true.
     * @param size tamanho original
     * @return tamanho atualizado
     */
    public Dimension getRotatedSize(Dimension size) {
        if (vertical) {
            return new Dimension(size.height, size.width);
        } else {
            return size;
        }
    }
    
    /**
     * Retorna um objeto Dimension rotacionado, caso painting seja true.
     * @param size tamanho original
     * @return tamanho atualizado
     */
    public Dimension getRotatedSizeIfNeeded(Dimension size) {
        if (painting) {
            return getRotatedSize(size);
        } else {
            return size;
        }
    }
    
    /**
     * Deve ser chamado pelo método paintComponent da classe Swing que deseja a
     * funcionalidade de rotação. O objeto Graphics retornado deve ser passado
     * para super.paintComponent.
     * 
     * @param size tamanho atual do objeto sem considerar a rotação
     * @param g graphics recebido pelo método paintComponent
     * @return objeto Graphics atualizado com a rotação de seus eixos
     */
    public Graphics rotateGraphicsIfNeeded(Dimension size, Graphics g) {
        if (vertical) {
            Graphics2D gr = (Graphics2D) g.create();

            if (clockwise) {
                gr.transform(AffineTransform.getQuadrantRotateInstance(1));
                gr.translate(0, -size.getWidth());
            } else {
                gr.translate(0, size.getHeight());
                gr.transform(AffineTransform.getQuadrantRotateInstance(-1));
            }
            
            return gr;
        } else {
            return g;
        }
    }
    
    /**
     * Deve ser chamado pelo paintComponent antes de super.paintComponent.
     */
    public void startPainting() {
        painting = true;
    }
    
    /**
     * Deve ser chamado pelo paintComponent depois de super.paintComponent.
     */
    public void stopPainting() {
        painting = false;
    }
}
