package com.stemcell.common.i18n;

import java.util.List;
import java.util.Locale;

/**
 * Interface de armazenamento do lacale da aplicacao
 * @author UP23
 */
public interface ILocaleStore {

    /**
     * Recupera o Locale da aplicacao
     * @return retorna o Locale da aplicacao
     */
    public Locale getLocale();

    /**
     * Seta o locale atual da aplicacao e invoca o metodo
     * de mudanca de locale, de todos os listeners injetados na
     * implementação do ILocaleStore.
     * @param locale
     *              Locale atual da aplicacao
     */
    void setLocale(Locale locale);

    /**
     * Seta uma lista de listeners que serão informados sobre
     * a mudança do locale
     * @param localeListeners
     *              lista com os listeners
     */
    public void setLocaleListeners(List<ILocaleListener> localeListeners);

}
