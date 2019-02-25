package com.stemcell.swing.base;

import com.stemcell.common.i18n.I18nManager;
import com.stemcell.common.i18n.SimpleBundleNameStore;
import com.stemcell.common.i18n.StandaloneClientLocaleStore;
import java.awt.Image;
import java.net.URL;


/**
 * Mock implementado para evitar erros quando um componente usa elementos da instância
 * singleton do BaseApp quando aberto no designer de telas no Netbeans em tempo de desenvolvimento
 */
public class BaseAppMock extends BaseApp {

    /**
     * Construtor padrão
     */
    public BaseAppMock() {
        I18nManager i18nManagerConfigurer = new I18nManager();
        i18nManagerConfigurer.setBundleNameStore(new SimpleBundleNameStore());
        i18nManagerConfigurer.setLocaleStore(new StandaloneClientLocaleStore());
    }

    @Override
    public String getApplicationTitle() {
        return "";
    }

    @Override
    public Image getSplashScreenImage() {
        return null;
    }

    @Override
    public Image getApplicationIcon() {
        return null;
    }

    @Override
    public URL getHelpSetURL() {
        return null;
    }

    @Override
    public String getHelpBrokerId() {
        return "";
    }


    @Override
    public boolean sessionExpired(String descriptionMessage) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
