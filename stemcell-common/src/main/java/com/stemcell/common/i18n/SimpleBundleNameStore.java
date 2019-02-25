package com.stemcell.common.i18n;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>Implementação básica <code>br.com.petrobras.fcorp.i18n.IBundleNameStore</code>.</p>
 * <p>Armazena os bundle names em uma lista de <code>String</code>s, que pode ser
 * definida usando o acessor {@link #setBundleNames(String[])}.</p>
 *
 */
public class SimpleBundleNameStore implements IBundleNameStore {

    private List<String> bundleNames= new ArrayList<String>(0);

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getBundleNames() {
        return bundleNames.toArray(new String[bundleNames.size()]);
    }

   /**
    * {@inheritDoc}
    */
    @Override
    public boolean containsBundle(String name) {
        return bundleNames.contains(name);
    }

    /**
     * Injeta os nomes dos bundles como um Array
     * @param bundleNames String[] contendo os nomes dos bundles
     */
    public void setBundleNames(String[] bundleNames) {
        this.bundleNames = Arrays.asList(bundleNames);
    }
}