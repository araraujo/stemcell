package com.stemcell.swing.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.text.Format;
import java.text.MessageFormat;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

/**
 * Utilitário de monitoramento da heap no sistema
 */
public class GCLabel extends JLabel implements ActionListener, ComponentListener {
    
    private static final String TOOLTIP = "Click to force garbage collection";
    
    private static final boolean AUTOMATIC_REFRESH = true;
    private static final int TICK = 1000;
    private final Runtime r = Runtime.getRuntime();
    private final Format f = new MessageFormat("    {0,number,0.00}/{1,number,0.00}MB    ");
    private final Timer t;
    private double proportion = 0.0d;
    
    /** cyclic buffer of historical values of proportion, after every tick */
    private double[] graph = null;
    
    /** current index into graph: where next value will be placed (not yet usable) */
    private int graphPointer;
    
    /** start of usable data in graph */
    private int graphBase;
    private boolean containsMouse = false;

    /**
     * Construtor
     */
    public GCLabel() {
        final double formatterTemplate = 999.0d;
        final int defaultWidth = 200;
        t = new Timer(TICK, this);
        enableEvents(java.awt.AWTEvent.MOUSE_EVENT_MASK);
        
        // To get the size right:
        setText(f.format(new Object[] {new Double(formatterTemplate), new Double(formatterTemplate)}));
        setOpaque(false);
        
        setToolTipText(TOOLTIP);
        setHorizontalAlignment(JLabel.CENTER);
        setPreferredSize(new Dimension(defaultWidth,getPreferredSize().height));
    }
    
    @Override
    protected void paintComponent(Graphics g1) {
        final int one = 1;
        final int two = 2;
        final int three = 3;
        final int four = 4;
        Graphics2D g = (Graphics2D) g1;
        Dimension size = getSize();
        size.height -= two; 
        size.width -= two;
        g.translate(three, one);
        Color old = g.getColor();
        
        try {
            Color c = SystemColor.control;
            g.setColor(c);
            g.fillRect(0,0, getWidth(), getHeight());

            g.setPaint(new GradientPaint(getWidth()/two, getHeight(),  Color.green, getWidth()/two, 0, Color.yellow));
            int bufferLength = size.width - two; 
            
            if (graph == null) {
                graph = new double[bufferLength]; 
                graphPointer = 0;
                graphBase = 0;
            } else if (graph.length != bufferLength) {
                int oldLength = graph.length;
                
                // Resize the buffer.
                double[] nue = new double[bufferLength];
                
                // System.arraycopy would be slicker, but this is easier:
                int i = bufferLength;
                
                for (int j = graphPointer; (j != graphBase) && (i > 0);) {
                    j = ((j + oldLength) - 1) % oldLength;
                    nue[--i] = graph[j];
                }
                
                graph = nue;
                graphPointer = 0;
                graphBase = i % bufferLength;
            }
            
            // Now paint the graph.
            int x = size.width - three;
            assert graphBase >= 0 : String.format("graphBase=%d", graphBase);
            assert graphBase < bufferLength : String.format("graphBase=%d bufferLength=%d", graphBase, bufferLength);

            int drawnVal = 0;
            double val = 0;
            for (int i = graphPointer; i != graphBase;) {
                assert i >= 0 : String.format("i=%d", i);
                assert i < bufferLength : String.format("i=%d  bufferLength=%d", i, bufferLength);
                i = ((i + bufferLength) - one) % bufferLength;
                
                val = graph[i];
                drawnVal = (int) ((size.height - two) * val); 
                
                g.drawLine(x, size.height - one - drawnVal, x, size.height - one);
                x--;
            }
            
            // Paint a border.
            c = containsMouse ? getBackground().brighter() : getBackground().darker();
            g.setColor(c);
            g.drawRect(0, one, size.width - two, size.height - two); 
            
            if (containsMouse) {
                g.drawRect(one, two, size.width - four, size.height - four);
            }
        } finally {
            g.setColor(old);
            g.translate(-three, -one);
        }
        
        super.paintComponent(g);
    }
    @Override
    protected void processMouseEvent(MouseEvent me) {
        super.processMouseEvent(me);
        
        if (me.getID() == MouseEvent.MOUSE_CLICKED) {
            Graphics g = getGraphics();
            Color old = g.getColor();
            
            try {
                g.setColor(UIManager.getColor("info"));
                g.fillRect(0, 1, getWidth(), getHeight() - 1);
                g.setColor(UIManager.getColor("infoText"));
                g.setFont(getFont());
                
                FontMetrics fm = g.getFontMetrics();
                Rectangle textRect = new Rectangle();
                SwingUtilities.layoutCompoundLabel(
                        fm, TOOLTIP, null, SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.CENTER,
                        SwingConstants.LEFT, new Rectangle(), new Rectangle(), textRect, 0
                        );
                g.drawString("", textRect.x, textRect.y);
            } finally {
                g.setColor(old);
            }
            
            System.gc();
            System.runFinalization();
            System.gc();
            
            repaint();
        } else if (me.getID() == MouseEvent.MOUSE_ENTERED) {
            setToolTipText(String.format("%s [%s]", TOOLTIP,  getText()));
            containsMouse = true;
            repaint();
        } else if (me.getID() == MouseEvent.MOUSE_EXITED) {
            containsMouse = false;
            repaint();
        }
    }
    @Override
    public void paintBorder(Graphics g) {
    }
    @Override
    public Dimension getMaximumSize() {
        Dimension result = super.getMaximumSize();
        result.height = getParent().getHeight();
        
        return result;
    }
    @Override
    public void addNotify() {
        super.addNotify();
        getParent().addComponentListener(this);
        
        if (getParent().isVisible()) {
            if (AUTOMATIC_REFRESH) {
                t.start();
            }
            
            update(false);
        }
    }
    @Override
    public void removeNotify() {
        getParent().removeComponentListener(this);
        t.stop();
        super.removeNotify();
    }
    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e) {
        update(true);
    }
    /**
     * {@inheritDoc}
     */
    public void componentResized(ComponentEvent e) {
    }
    /**
     * {@inheritDoc}
     */
    public void componentMoved(ComponentEvent e) {
    }
    /**
     * {@inheritDoc}
     */
    public void componentShown(ComponentEvent e) {
        if (AUTOMATIC_REFRESH) {
            t.start();
        }
        
        update(false);
    }
    /**
     * {@inheritDoc}
     */
    public void componentHidden(ComponentEvent e) {
        t.stop();
    }
    
    /**
     * Executa atualização dos dados
     * @param ticked ticked
     */
    private void update(boolean ticked) {
        long total = r.totalMemory();
        long used = total - r.freeMemory();
        proportion = ((double) used) / total;
        
        if (ticked && (graph != null)) {
            graph[graphPointer] = proportion;
            graphPointer = (graphPointer + 1) % graph.length;
            
            if (graphPointer == graphBase) {
                graphBase = (graphPointer + 1) % graph.length;
            }
        }

        final int kilo = 1024;
        Double calcTotal = new Double(((double) total) / kilo / kilo);
        Double calcUsed = new Double(((double) used) / kilo / kilo);
        String text = f.format(new Object[] {calcUsed, calcTotal});
        setText(text);
    }
    
}