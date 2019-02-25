package com.stemcell.swing.components;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JFrame;

/**
 * Janela de splash sem bordas
 */
public class SSplashScreen extends JFrame {
    private Image splash;
    /**
     * Construtor padrão
     */
    public SSplashScreen() {}

    /**
     * Construtor
     * @param splash imagem de splash
     */
    public SSplashScreen(Image splash) {
        final int defaultWidth = 400;
        final int defaultHeigth = 300;
        if (splash != null) {
            setSize(splash.getWidth(null), splash.getHeight(null));
        } else {
            setSize(defaultWidth, defaultHeigth);
        }
        setState(JFrame.MAXIMIZED_BOTH);
        this.splash = splash;
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        setUndecorated(true);
    }

    @Override
    public void paint(Graphics g) {
        final int posicao = 20;
        if (splash == null) {
            g.drawString("Splash image not set", posicao, posicao);
        } else {
            g.drawImage(splash, 0, 0, this);
        }
    }
}
