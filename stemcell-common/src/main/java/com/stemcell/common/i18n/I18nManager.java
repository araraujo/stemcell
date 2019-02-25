package com.stemcell.common.i18n;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * <p>
 * Auxilia o uso de ResourceBundles.
 * </p>
 * <p>
 * É importante observar que esta é uma classe estática, e portanto gerencia a internacionalização para todas as classes dentro do mesmo classloader. Dependendo
 * do ambiente da aplicação (swing, linha de comando, web) isso pode significar que alguns cuidados devem ser tomado na sua configuração e utilização.
 * </p>
 * <p>
 * Para aplicações que rodam numa JVM própria (ex.: Swing), o mesmo I18nManager será usado para TODA a JVM. Já aplicações que rodam em containers (Webapps,
 * EJB), cada aplicação terá o seu próprio I18nManager (separação por classloader).
 * </p>
 */
public final class I18nManager {

    /**
     * Locale pt_BR
     */
    public static final Locale PORTUGUESE_LOCALE = new Locale("pt", "BR");

    private static ILocaleStore localeStore;

    private static IBundleNameStore bundleNameStore;

    private static Map<String, ResourceBundle> bundleCache = new HashMap<String, ResourceBundle>();

    private static final int EMPTY = 0;
    private static final String INTERROG = "???";
    private static final String ILLEGAL_BUNDLE_OR_LOCALE = "Parameters bundleName and locale cannot be null.";

    /**
     * Construtor necessário para injeção de dependências. Não deve ser utilizado programaticamente.
     * Esta classe deve ser utilizada de forma estática.
     */
    public I18nManager() {
        // Não faz nada. Instâncias dessa classe não devem ser utilizadas.
    }

    /**
     * Metodo para recuperar o {@link ILocaleStore} atualmente utilizado pela
     * aplicacao
     * @return retorna o ILocaleStore atual
     */
    public static ILocaleStore getCurrentLocaleStore() {
        if(localeStore == null){
            throw new RuntimeException("LocaleStore não configurado no I18nManager.");
        }
        return localeStore;
    }

    /**
     * Metodo para recuperar o {@link IBundleNameStore} atualmente utilizado
     * pela aplicacao
     * @return retorna o IBundleNameStore atual
     */
    public static IBundleNameStore getBundleNameStore() {
        if(bundleNameStore == null){
            throw new RuntimeException("BundleNameStore não configurado no I18nManager.");
        }
        return bundleNameStore;
    }

    /**
     * <p>Retorna o valor da respectiva chave formatada, pesquisada em todos os
     * bundles da aplicacao</p>
     * @param key
     *            Chave do mensagem que sera pesquisa nos bundles
     * @param values
     *            parametros com valores que serao formatados na mensagem
     *            (opcional)
     * @return mensagem internacionalizada com parametros formatados, ou ???<code>key</code>??? caso a chave não seja encontrada
     */
    public static String getString(String key, Object... values) {
        String result = getStringOrNull(key, values);
        if (result == null) {
            result = (new StringBuilder(INTERROG)).append(key)
                    .append(INTERROG).toString();
        }
        return result;
    }

    /**
     * <p>Retorna o valor da respectiva chave formatada, pesquisada em todos os
     * bundles da aplicacao, retornando <code>null</code> caso não encontre a chave em nenhum
     * bundle.</p>
     * @param key Chave do mensagem que sera pesquisa nos bundles
     * @param values parametros com valores que serao formatados na mensagem (opcional)
     * @return mensagem internacionalizada com parametros formatados ou <code>null</code>
     */
    static String getStringOrNull(String key, Object... values) {
        ResourceBundle bundle = null;
        Locale locale = getCurrentLocaleStore().getLocale();
        for (String bundleName : bundleNameStore.getBundleNames()) {
            bundle = getBundle(bundleName, locale);
            if (bundle.containsKey(key)) {
                return getValue(key, bundle, locale, values);
            }
        }
        return null;
    }

    /**
     * recurso utilizado para pegar valores internacionalizados de enums dos
     * sistemas
     * @param enumeration
     *            enumeracao que sera pesquisada no arquivo de
     *            internacionalizacao
     * @return valor internacionalizado no enum
     */
    public static String getString(Enum<?> enumeration) {
        StringBuilder key = (new StringBuilder()).append(
                enumeration.getClass().getSimpleName()).append("_").append(
                enumeration.name());
        return getString(key.toString());
    }

    /**
     * Inicializa um novo bundle para dentro do cache de bundles da aplicacao
     * @param bundleName
     *            nome do novo bandle a ser considerado pela aplicacao
     * @param locale
     *            Locale do bundle a ser considerado
     * @return incicializa e retorna o objeto de resource bundle informado
     */
    private static ResourceBundle initialize(String bundleName, Locale locale) {
        if (bundleName == null || locale == null) {
            throw new IllegalArgumentException(
                    ILLEGAL_BUNDLE_OR_LOCALE);
        }
        ResourceBundle bundle = null;
        try {
            bundle = ResourceBundle.getBundle(bundleName, locale);
            bundleCache.put(makeBundleKey(bundleName, locale), bundle);
        } catch (MissingResourceException e) {
            throw new RuntimeException((new StringBuilder()).append("Bundle '")
                    .append(bundleName)
                    .append("' não localizado no classpath.").toString(), e);
        }
        return bundle;
    }

    /**
     * Cria uma String para ser utilizada como chave para o bundleCache, a
     * partir do baseName (bundleName) e do locale.
     * O uso deste método garante uniformidade na forma das chaves utilizadas.
     * @param bundleName O baseName do bundle
     * @param locale O locale desejado
     * @return Uma chave para ser usada no bundleCache
     */
    private static String makeBundleKey(String bundleName, Locale locale) {
        return (new StringBuilder()).append(bundleName).append(
                locale.toString()).toString();
    }

    /**
     * Retorna um bundle do cache de bundles da aplicacao
     * @param bundleName
     *            o nome do bundle
     * @param locale
     *            o lacale do bundle que se deseja recuperar
     * @return o bundle desejado
     */
    private static ResourceBundle getBundle(String bundleName, Locale locale) {
        if (bundleName == null || locale == null) {
            throw new IllegalArgumentException(
                    ILLEGAL_BUNDLE_OR_LOCALE);
        }
        String bundleKey = makeBundleKey(bundleName, locale);
        if (!bundleCache.containsKey(bundleKey)) {
            initialize(bundleName, locale);
        }
        return bundleCache.get(bundleKey);
    }

    /**
     * Método local que retorna o valor de arquivo de propriedades dado uma
     * chave.
     * @param bundle
     *            Instancia do resource bundle
     * @param key
     *            nome da chave que se deseja comitar.
     * @param locale
     *            O locale atual
     * @param parameters
     *            array de valores que serao utilizados na internacionalizacao
     *            da mensage
     * @return mensagem referente a chave, internacionalizada ou a chava
     *         concatenado entrw interrogacoes caso nao seja encontrado (??? key
     *         ???).
     */
    private static String getValue(String key, ResourceBundle bundle,
            Locale locale, Object... parameters) {
        try {
            String result = null;
            if (parameters == null || parameters.length == EMPTY) {
                result = bundle.getString(key);
            } else {
                MessageFormat formatter = new MessageFormat(bundle
                        .getString(key), locale);
                result = formatter.format(parameters);
            }
            return result;
        } catch (MissingResourceException e) {
            return (new StringBuilder()).append(INTERROG).append(key)
                    .append(INTERROG).toString();
        }
    }

    /**
     * <p>Metodo para setar o {@link IBundleNameStore} que sera utilizado pela
     * aplicacao.</p>
     * <p>ATENÇÃO: Este método configura o I18nManager estaticamente. Não há estado nas instâncias!</p>
     * <p>Este método é não-estático unicamente para permitir a configuração do I18nManager via injeção de dependências.</p>
     * @param bundleNameStore
     *            Seta o IBundleNameStore que sera utilizado pela aplicacao
     */
    public void setBundleNameStore(IBundleNameStore bundleNameStore) {
        I18nManager.bundleNameStore = bundleNameStore;
    }

    /**
     * <p>Metodo para setar o novo {@link ILocaleStore} que sera utilizado pela
     * aplicacao</p>
     * <p>ATENÇÃO: Este método configura o I18nManager estaticamente. Não há estado nas instâncias!</p>
     * <p>Este método é não-estático unicamente para permitir a configuração do I18nManager via injeção de dependências.</p>
     * @param localeStore
     *            seta o ILocaleStore que sera utilizado pela apliacacao
     */
    public void setLocaleStore(ILocaleStore localeStore) {
        I18nManager.localeStore = localeStore;
    }
}
