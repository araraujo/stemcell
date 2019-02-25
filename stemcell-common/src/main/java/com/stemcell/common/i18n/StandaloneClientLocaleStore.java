package com.stemcell.common.i18n;

import java.util.List;
import java.util.Locale;

/**
 * <p>Implementação de <code>br.com.petrobras.fcorp.i18n.ILocaleStore</code>
 * usada apenas usado na configuração de clientes desktop.</p>
 * <p>Esta classe altera o locale default da máquina virtual, portanto
 * <b>não deve ser usado no ambiente de servidores de aplicação</b></p>
 */
public class StandaloneClientLocaleStore implements ILocaleStore {
    /**
     * Listeners de mudanças de locale
     */
    private List<ILocaleListener> localeListeners;
    /**
     * {@inheritDoc}
     */
    @Override
    public Locale getLocale() {
        return Locale.getDefault();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void setLocale(Locale locale) {
        Locale.setDefault(locale);
        if (localeListeners!=null) {
            for (ILocaleListener iLocaleListener : localeListeners) {
                iLocaleListener.changeLocale(locale);
            }
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void setLocaleListeners(List<ILocaleListener> localeListeners) {
        this.localeListeners = localeListeners;
    }
}