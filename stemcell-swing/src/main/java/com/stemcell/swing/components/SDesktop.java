package com.stemcell.swing.components;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.SystemColor;
import java.beans.PropertyVetoException;
import javax.swing.BorderFactory;
import javax.swing.DefaultDesktopManager;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.border.BevelBorder;

public class SDesktop extends JDesktopPane {
    /**
     * Posiciona os frames em cascata com um desnível de 20 pixels.
     */
    public static final FrameLocationStrategy CASCADE = new FrameLocationStrategy() {
        public void setLocation(SDesktop desktop, JInternalFrame frame) {
            final int offset = 20;
            JInternalFrame[] array = desktop.getAllFrames();
            Point p = null;
            desktop.checkDesktopSize();
            if (array.length - 1 > 0) {
                p = array[0].getLocation();
                p.x = p.x + offset;
                p.y = p.y + offset;
            } else {
                p = new Point(0, 0);
            }
            if (p.x < 0) {
                p.x = 0;
            }
            if (p.y < 0) {
                p.y = 0;
            }
            frame.setLocation(p.x, p.y);
        }
    };

    /**
     * Posiciona os frames centralizados no desktop.
     */
    public static final FrameLocationStrategy CENTER = new FrameLocationStrategy() {
        public void setLocation(SDesktop desktop, JInternalFrame frame) {
            final int half = 2;
            frame.setLocation((desktop.getWidth() - frame.getWidth()) / half, (desktop.getHeight() - frame.getHeight()) / half);
        }
    };

    private MDIDesktopManager manager;
    private FrameLocationStrategy frameLocationStrategy;
    private Image img;
    private boolean scaleImg;

    /**
     * Estratégia de posicionamento de frames. Define um método
     * <code>setLocation</code> que é chamado pelo desktop sempre que um novo
     * frame for adicionado. Deve ser implementada quando for preciso criar uma
     * nova forma de organizar os frames abertos no desktop.
     *
     * @see #CENTER
     * @see #CASCADE
     */
    public static interface FrameLocationStrategy {
        /**
         * Define a localização da janela no desktop
         * @param desktop desktop
         * @param frame frame
         */
        public void setLocation(SDesktop desktop, JInternalFrame frame);
    }

    /**
     * Construtor
     */
    public SDesktop() {
        manager = new MDIDesktopManager(this);
        frameLocationStrategy = CENTER;
        scaleImg = true;
        setDesktopManager(manager);
        setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        setBackground(SystemColor.controlShadow);
        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    }

    /**
     * Estratégia de posicionamento dos InternalFrames adicionados ao desktop.
     * A classe SDesktop trás duas implementações de estratégia pré-definidas:
     * {@link #CENTER} e {@link #CASCADE}. Por padrão, a estratégia utilizada
     * é CENTER. Um desenvolvedor pode criar a sua própria estrategia de
     * posicionamento implementando a interface {@link FrameLocationStrategy}.
     * @return frameLocationStrategy
     */
    public FrameLocationStrategy getFrameLocationStrategy() {
        return frameLocationStrategy;
    }

    /**
     * Define a nova estratégia de posicionamento dos frames adicionados ao
     * desktop. Isto afetará apenas os próximos frames inseridos através do
     * método <code>add(JInternalFrame)</code>. Os frames já contidos no
     * desktop não serão reposicionados.
     * @param frameLocationStrategy frameLocationStrategy
     */
    public void setFrameLocationStrategy(FrameLocationStrategy frameLocationStrategy) {
        Object old = this.frameLocationStrategy;
        this.frameLocationStrategy = frameLocationStrategy;
        firePropertyChange("frameLocationStrategy", old, this.frameLocationStrategy);
    }

    public Image getImg() {
        return img;
    }

    /**
     * @param img Imagem de background do desktop. Por padrão é <code>null</code>.
     */
    public void setImg(Image img) {
        this.img = img;
    }

    public boolean isScaleImg() {
        return scaleImg;
    }

    /**
     * Define se a imagem de fundo será redimensionada
     * @param scaleImg scaleImg
     */
    public void setScaleImg(boolean scaleImg) {
        Object old = scaleImg;
        this.scaleImg = scaleImg;
        firePropertyChange("scaleImg", old, this.scaleImg);
    }

    @Override
    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);
        checkDesktopSize();
    }

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        final float alpha = 0.1f;
        Graphics2D gr = (Graphics2D) g;
        Object rh = gr.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Composite c = gr.getComposite();
        gr.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        gr.setPaint(Color.BLACK);
        gr.fillRect(0, 0, getWidth(), getHeight());
        gr.setComposite(c);
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, rh);
        final int half = 2;
        if (img != null) {
            if (scaleImg) {
                gr.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), null);
            } else {
                gr.drawImage(img, (this.getWidth() - img.getWidth(null)) / half, (this.getHeight() - img.getHeight(null)) / half, img.getWidth(null), img.getHeight(null), null);
            }
        }
    }

    @Override
    public Component add(Component frame) {
        if (frame instanceof JInternalFrame) {
            return add((JInternalFrame) frame);
        }
        return super.add(frame);
    }

    /**
     * Adiciona um JInternalFrame
     * @param frame frame
     * @return componente adicionado
     */
    public Component add(JInternalFrame frame) {
        Component retval = super.add(frame);
        
        if (getFrameLocationStrategy() != null) {
            getFrameLocationStrategy().setLocation(this, frame);
        }
        
        moveToFront(frame);
        frame.setVisible(true);
        try {
            frame.setSelected(true);
        } catch (PropertyVetoException e) {
            frame.toBack();
        }
        return retval;
    }

    @Override
    public void remove(Component c) {
        super.remove(c);
        checkDesktopSize();
    }
    /**
     * Define o tamanho total
     * @param d dimensão
     */
    public void setAllSize(Dimension d) {
        setMinimumSize(d);
        setMaximumSize(d);
        setPreferredSize(d);
    }

    /**
     * Define o tamanho total
     * @param width altura
     * @param height  largura
     */
    public void setAllSize(int width, int height) {
        setAllSize(new Dimension(width, height));
    }

    /**
     * Verifica o tamanho do desktop
     */
    private void checkDesktopSize() {
        if (getParent() != null && isVisible()) {
            manager.resizeDesktop();
        }
    }

}

/**
 * Classe MDIDesktopManager
 * @author x4rb
 */
class MDIDesktopManager extends DefaultDesktopManager {
    private SDesktop desktop;

    /**
     * Construtor
     * @param desktop desktop
     */
    public MDIDesktopManager(SDesktop desktop) {
        this.desktop = desktop;
    }

    @Override
    public void endResizingFrame(JComponent f) {
        super.endResizingFrame(f);
        resizeDesktop();
    }

    @Override
    public void endDraggingFrame(JComponent f) {
        super.endDraggingFrame(f);
        resizeDesktop();
    }

    /**
     * Define o ajusta para o tamanho normal
     */
    public void setNormalSize() {
        JScrollPane scrollPane = getScrollPane();
        int x = 0;
        int y = 0;
        final int sizeAdjust = 20;
        Insets scrollInsets = getScrollPaneInsets();

        if (scrollPane != null) {
            Dimension d = scrollPane.getVisibleRect().getSize();
            if (scrollPane.getBorder() != null) {
                d.setSize(d.getWidth() - scrollInsets.left - scrollInsets.right,
                        d.getHeight() - scrollInsets.top - scrollInsets.bottom);
            }

            d.setSize(d.getWidth() - sizeAdjust, d.getHeight() - sizeAdjust);
            desktop.setAllSize(x, y);
            scrollPane.invalidate();
            scrollPane.validate();
        }
    }

    /**
     * @return ScrollPaneInsets
     */
    private Insets getScrollPaneInsets() {
        JScrollPane scrollPane = getScrollPane();
        if (scrollPane == null || getScrollPane().getBorder() == null) {
            return new Insets(0, 0, 0, 0);
        } else {
            return getScrollPane().getBorder().getBorderInsets(scrollPane);
        }
    }

    /**
     * @return JScrollPane
     */
    private JScrollPane getScrollPane() {
        if (desktop.getParent() instanceof JViewport) {
            JViewport viewPort = (JViewport) desktop.getParent();
            if (viewPort.getParent() instanceof JScrollPane) {
                return (JScrollPane) viewPort.getParent();
            }
        }
        return null;
    }

    /**
     * Reset desktop size
     */
    protected void resizeDesktop() {
        int x = 0;
        int y = 0;
        final int sizeAdjust = 20;
        JScrollPane scrollPane = getScrollPane();
        Insets scrollInsets = getScrollPaneInsets();

        if (scrollPane != null) {
            JInternalFrame allFrames[] = desktop.getAllFrames();
            for (int i = 0; i < allFrames.length; i++) {
                if (allFrames[i].getX() + allFrames[i].getWidth() > x) {
                    x = allFrames[i].getX() + allFrames[i].getWidth();
                }
                if (allFrames[i].getY() + allFrames[i].getHeight() > y) {
                    y = allFrames[i].getY() + allFrames[i].getHeight();
                }
            }
            Dimension d = scrollPane.getVisibleRect().getSize();
            if (scrollPane.getBorder() != null) {
                d.setSize(d.getWidth() - scrollInsets.left - scrollInsets.right,
                        d.getHeight() - scrollInsets.top - scrollInsets.bottom);
            }

            if (x <= d.getWidth()) {
                x = ((int) d.getWidth()) - sizeAdjust;
            }
            if (y <= d.getHeight()) {
                y = ((int) d.getHeight()) - sizeAdjust;
            }
            desktop.setAllSize(x, y);
            scrollPane.invalidate();
            scrollPane.validate();
        }
    }
}
