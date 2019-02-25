package com.stemcell.swing.base;

import com.stemcell.common.beans.CredentialsBean;
import com.stemcell.common.i18n.I18nManager;
import com.stemcell.common.i18n.SimpleBundleNameStore;
import com.stemcell.common.i18n.StandaloneClientLocaleStore;
import com.stemcell.common.util.ExceptionUtil;
import com.stemcell.swing.blocking.BlockingOperationListener;
import com.stemcell.swing.components.DialogMessages;
import com.stemcell.swing.components.SSplashScreen;
import java.awt.Image;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.slf4j.LoggerFactory;

/**
  * Base class representing the application.
  * The singleton instance of BaseApp is bound to a service represented by the parameter T
  */
public abstract class BaseApp implements BlockingOperationListener {
    public static final String ARGS_SERVICE_LOCAL = "serviceLocal";
    public static final String SYSTEM_PROPRERTY_FAT_CLIENT_PROPERTY = "swing.fatclient";

    private static List<String> argsList;
    private static SSplashScreen splash;
    private static BaseApp instance;

    /**
     * Dados de autenticação retornados do servidor durante o login.
     */
    private CredentialsBean credentialsBean;
    
    /**
     * Flag de habilitação da mensagem de sucesso após chamada de método do serviço
     */
    private boolean successMessageEnabled;
    /**
     * Frame principal
     */
    private SFrame mainFrame;

    /**
     * Construtor
     */
    protected BaseApp() {
        successMessageEnabled = true;
    }

    // <editor-fold defaultstate="collapsed" desc="Métodos estáticos">
    /**
     * Retorna a instância singleton da aplicação.
     * @return Instância singleton da aplicação
     */
    public static BaseApp getApplication() {
        if (null == instance) {
            // Se não foi provida uma instância, se assume que foi executada através do designer (sem chamar o showSplashAndlaunch),
            // assim utilizando um mock para evitar erros no designer de telas
            return new BaseAppMock();
        }
        return instance;
    }

    /**
     * Retorna true se a aplicação foi inicializado com o parâmetro
     * de linha de comando serviceLocal.
     * @return True se for rodado com parâmetros de fatClient
     */
    public static boolean isFatClient() {
        return argsList.contains(ARGS_SERVICE_LOCAL) || System.getProperty(ARGS_SERVICE_LOCAL) != null;
    }

    /**
     * Inicializa a aplicação.
     * @param args Argumentos de execução
     * @param appClass Classe de aplicação
     * @param appFrame Classe do frame principal
     */
    protected static void showSplashAndLaunch(String[] args, final Class<? extends BaseApp> appClass, final Class<? extends SFrame> appFrame) {
        try {
            if (instance != null) {
                throw new IllegalStateException("The application is already running");
            }

            // Processa argumentos de linha de comando
            argsList = Arrays.asList(args);
            if (isFatClient()) {
                System.setProperty("fatclient", "true");
            }

            // Desabilita possível security manager no Webstart
            System.setSecurityManager(null);

            // Instancia o objeto de aplicação
            BaseApp app = appClass.newInstance();
            instance = app;

            // Exibe splash screen
            splash = new SSplashScreen(instance.getSplashScreenImage());
            splash.setVisible(true);

            // Workaround para problema de não visualização da imagem do splash
            try {
                final int sleepTime = 20;
                Thread.sleep(sleepTime);
            } catch (InterruptedException ex) {
                ex.printStackTrace(System.out);
            }
        } catch (Exception e) { // SUPPRESS CHECKSTYLE Illegal Catch - Barreira de excecao
            DialogMessages.error(null, e.getMessage(), ExceptionUtil.getStackAsString(e));
            e.printStackTrace(System.out);
            System.exit(0);
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                instance.startup(appFrame);
            }
        });
    }
    // </editor-fold>

    /**
     * Obtém o objeto de serviço e exibe a tela principal da aplicação.
     * @param frameClass Classe do frame principal
     */
    private void startup(Class<? extends SFrame> frameClass) {
        try {
            if (isFatClient()) {
                System.setProperty(SYSTEM_PROPRERTY_FAT_CLIENT_PROPERTY, Boolean.TRUE.toString());
            }

            SimpleBundleNameStore store = createBundleNameStore();
            if (store==null) {
                throw new IllegalStateException("O SimpleBundleNameStore deve ser obrigatoriamente definido no método createBundleNameStore()");
            }
            
            // Configura o i18n manager
            I18nManager i18nManagerConfigurer = new I18nManager();
            i18nManagerConfigurer.setLocaleStore(new StandaloneClientLocaleStore());
            i18nManagerConfigurer.setBundleNameStore(store);

            // aplica as configurações de look and feel
            setupLookAndFeel();

            // Chama configuração customizada
            doBeforeOpenFrame();

            // Instancia o frame principal
            mainFrame = frameClass.newInstance();

            // Aplica o tratador de exceção global
            //SwingExceptionHandlerManager.getHandlerManager().setFrame(mainFrame);

            // Configura o frame principal
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            mainFrame.doAfterInit();

            // Exibe o frame principal
            mainFrame.setVisible(true);

            // Fecha o splash screen
            splash.dispose();
            splash = null;

            // Invoca o processo de logon
            SwingUtilities.invokeLater(new Runnable() {
                @Override public void run() {
                    //logon();
                }
            });
        } catch (Throwable t) { // SUPPRESS CHECKSTYLE Illegal Catch - Barreira de excecao
            LoggerFactory.getLogger(getClass()).error(t.getMessage(), t);
            DialogMessages.error(splash, t.getMessage(), ExceptionUtil.getStackAsString(t));
            if (splash!=null) {
                splash.dispose();
                splash = null;
                System.exit(1);
            }
        }
    }

    /**
     * Método invocado antes da criação do frame principal para definir o
     * look and feel do sistema. A implementação padrão aplica o look and
     * feel do sistema através de uma chamada à
     * <code>UIManager.getSystemLookAndFeelClassName()</code>.
     * @throws Exception Alguma exceção na carga do look and feel
     */
    protected void setupLookAndFeel() throws Exception {
        // Carrega o look and feel padrão do sistema
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }


    /**
     * Método disponível para configuração personalizada antes da abertura do Main Frame
     */
    protected void doBeforeOpenFrame() {
    }

    /**
     * Deve criar um SimpleBundleNameStore com as mensagens do sistema
     * @return SimpleBundleNameStore da aplicação
     */
    protected SimpleBundleNameStore createBundleNameStore() {
        SimpleBundleNameStore store = new SimpleBundleNameStore();
        store.setBundleNames(new String[]{"swing-base-messages",
                                          "components-messages",});
        return store;
    }
    
    /**
     * @return CredentialsBean do usuário logado
     */
    public CredentialsBean getCredentialsBean() {
        return credentialsBean;
    }

    /**
     * @return Janela principal do sistema
     */
    public SFrame getMainFrame() {
        return mainFrame;
    }

    /**
     * Desabilita a exibição da mensagem de sucesso ná próxima chamada do sistema
     */
    public void disableNextSuccessMessage() {
        this.successMessageEnabled = false;
    }

    /**
     * @return Título da aplicação exibida no topo da janela
     */
    public abstract String getApplicationTitle();
    /**
     * @return imagem de Splash da aplicação
     */
    public abstract Image getSplashScreenImage();
    /**
     * @return ícone da aplicação
     */
    public abstract Image getApplicationIcon();
    /**
     * @return URL do HelpSet da aplicação
     */
    public abstract URL getHelpSetURL();
    /**
     * @return id do Helpbroker da aplicação
     */
    public abstract String getHelpBrokerId();

    // <editor-fold defaultstate="collapsed" desc="ServiceOperationListener implementation">
    /**
     * {@inheritDoc}
     */
    public void processStart(String descriptionMessage) {
        if (mainFrame != null) {
            mainFrame.getStatusPanel().showInfoStatus(descriptionMessage);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void processSuccess(String descriptionMessage, String msgSucesso) {
        if (msgSucesso != null && !msgSucesso.trim().equals("") && mainFrame != null && successMessageEnabled) {
            DialogMessages.info(mainFrame, msgSucesso);
        }
        if (mainFrame != null) {
            mainFrame.getStatusPanel().showInfoStatus(descriptionMessage);
        }
        this.successMessageEnabled = true;
    }

    /**
     * {@inheritDoc}
     */
    public void processFailure(String descriptionMessage, Throwable e) {
        this.successMessageEnabled = true;
        if (mainFrame != null) {
            mainFrame.getStatusPanel().showErrorStatus(descriptionMessage);
        }
    }

    // </editor-fold>

}