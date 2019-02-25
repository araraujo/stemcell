package com.stemcell.common.i18n;

import java.util.Locale;

/**
 * Interface de Listener para Locale
 * @author ZSQB
 */
public interface ILocaleListener {

    /**
     * Altera o locale para o listener
     * @param locale locale da aplicação
     */
    public void changeLocale(Locale locale);
}
