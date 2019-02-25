package com.stemcell.common.i18n;

/**
 * Interface de armazenamento de bundles da aplicacao
 * @author UP23
 */
public interface IBundleNameStore {

    /**
     * recupera os bundles da aplicacao
     * @return retorna todos os bundles da aplicacao
     */
    String[] getBundleNames();
    
    /**
     * Verifica se existe o bandle na lista de bundles da aplicacao
     * @param name
     *            bundle a ser pesquisado na lista de bundles da aplicacao
     * @return confirnacao da existencia ou nao do bundle
     */
    boolean containsBundle(String name);

}
