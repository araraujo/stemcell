package com.stemcell.swing.blocking;

import com.stemcell.common.i18n.I18nManager;
import com.stemcell.common.util.ExceptionUtil;
import com.stemcell.swing.base.BaseApp;
import com.stemcell.swing.components.dialog.BlockingDialog;
import java.awt.EventQueue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.LoggerFactory;

/**
 * Proxy que atua no lado cliente da aplicação com as seguintes funcionalidades:<br/>
 * <ul>
 *  <li>Exibe um diálogo bloqueante com a mensagem de "Processando" a cada chamada
 *    ao serviço</li>
 *  <li>Exibe na barra de status a descrição do método sendo executado, usando uma
 *    chave no I18nManager baseado na combinação de 
 *    <code>BaseApp.getApplication().getServiceInterface().getSimpleName()</code>.(nome do método)</li>
 *  <li>Checa o retorno de exceções de autenticação e delega o tratamento a um BlockingOperationListener
 *      retomando a execução de forma transparente após uma possível re-autenticação</li>
 * </ul>
 */
public final class BlockingProxy {
    public static final String OVERRIDE_ARGUMENT_SERIALIZATION_PATTERN_KEY = "br.com.petrobras.fcorp.swing.blocking.BlockingProxy.overrideArgumentSerializationPatternKey";
    private static final long serialVersionUID = 1L;
    /**
     * Diálogo de bloqueio exibido enquanto a operação é executada
     */
    private static BlockingDialog dialog;

    /**
     * Constructor padrão
     */
    private BlockingProxy() {
        super();
    }

    /**
     * Mantido para não quebrar compatibilidade
     * @deprecated Threads assincronas são agora detectadas automaticamente como aquelas fora
     *             da fila de eventos do Swing
     * @param thread Thread a ser registrada como assíncrona
     */
    @Deprecated
    public synchronized static void registerAssyncThread(Thread thread) {
    }
    
    /**
     * Mantido para não quebrar compatibilidade
     * @deprecated Threads assincronas são agora detectadas automaticamente como aquelas fora
     *             da fila de eventos do Swing
     * @param thread Thread a ser registrada
     */
    @Deprecated
    public synchronized static void unregisterAssyncThread(Thread thread) {
    }

    /**
     *  Cria o proxy dinâmico para o serviço.
     * @param <T>
     *                  Tipo do objeto de destino
     * @param target
     *                  Destino do proxy
     * @param operationListener
     *                  Listener de operações do proxy
     * @param forceArgumentSerialization 
     *                  Habilita forçar a serialização dos argumento de entrada,
     *                  e o retorno do método, simulando uma chamada remota
     * @return Proxy para o service que bloqueia todas as janelas a cada chamada e gerencia cache local
     */
    public static <T> T applyProxy(final T target, final BlockingOperationListener operationListener, final boolean forceArgumentSerialization) {
        T corr = (T) Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(),
                new BlockingInvocationHandler(operationListener, forceArgumentSerialization, target));
        return corr;
    }

    /**
     * Exibe um diálogo de bloqueio enquanto invoca o método <i>method</i> no
     * objeto <i>target</i> usando os parâmetros <i>args</i>
     * @param msg Mensagem a ser exibida durante a exibição do método
     * @param progress Valor opcional de progresso a ser exibido
     * @param target Objeto no qual o método será executado
     * @param method Método a ser executado
     * @param args Parâmetros a serem passados na execução do método
     * @return Retorno da chamada ao método <i>method</i>
     * @throws InterruptedException InterruptedException
     * @throws ExecutionException ExecutionException
     */
    public static Object blockingInvoke(String msg, Integer progress, Object target, Method method, Object[] args) throws InterruptedException, ExecutionException  {
        if (target == null || method == null) {
            throw new IllegalArgumentException("Target e method não podem ser null");
        }

        getDialog().getLabelMensagem().setText(msg);
        
        if (progress == null) {
            getDialog().getProgress().setIndeterminate(true);
        } else {
            getDialog().getProgress().setIndeterminate(false);
            getDialog().getProgress().setValue(progress);
        }

        if (!getDialog().isVisible()) {
            getDialog().pack();
            getDialog().setModal(true);
            getDialog().setLocationRelativeTo(BaseApp.getApplication().getMainFrame());
        }
        
        return getDialog().blockingInvoke(target, method, args);
    }

    /**
     * Obtém uma String de descrição do método baseado na combinação NomeDaInterface.metodo
     * @param method Referência reflection para o método
     * @return descrição do método
     */
    private static String getDescription(Method method) {
        // procura uma mensagem no bundle com a chave ServiceInterfaceName.methodName
        //String key = String.format("%s.%s", BaseApp.getApplication().getServiceInterface().getSimpleName(), method.getName());
        //String description = I18nManager.getString(key);
        //if (description==null || description.contains(key)) {
            // Se o mensagem não for encontrada, volta o padrão ???key???
            // portanto, deixamos a descrição vazia
        //    description = "";
        //}
        return "";
    }

    /**
     * Obtém uma String com a mensagem de sucesso do método baseado na combinação
     * NomeDaInterface.metodo.success
     * @param method Referência reflection para o método
     * @return Mensagem de sucesso do método
     */
    private static String getSuccessMessage(Method method) {
        // procura uma mensagem no bundle com a chave ServiceInterface.methodName.success
        //String key = String.format("%s.%s.success", BaseApp.getApplication().getServiceInterface().getSimpleName(),  method.getName());
        //String successMessage = I18nManager.getString(key);
        //if (successMessage==null || successMessage.contains(key)) {
            // Se o mensagem não for encontrada, volta o padrão ???key???
            // portanto, deixamos a descrição vazia
        //    successMessage = "";
        //}
        return "";
    }

    /**
     * Inicializa o diálogo de bloqueio
     * @return Instância única do BlockingDialog
     */
    private static BlockingDialog getDialog() {
        if (dialog==null) {
            dialog = new BlockingDialog(BaseApp.getApplication().getMainFrame(), true);
        }
        return dialog;
    }

    /**
     *  InvocationHandler que faz o trabalho de bloqueio da interface a cada 
     * chamada ao serviço
     */
    private static class BlockingInvocationHandler implements InvocationHandler {
        /**
         * Objeto destino das chamadas
         */
        private Object target;
        /**
         * Resultado da última chamada a método
         */
        private BlockingOperationListener operationListener;
        /**
         * Flag
         */
        private boolean forceArgumentSerialization;

        /**
         * Construtor padrão
         * @param operationListener Listener de operações
         * @param forceArgumentSerialization
         *              Habilita forçar a serialização dos argumento de entrada,
         *              e o retorno do método, simulando uma chamada remota
         * @param target Objeto destino das invocações de método
         */
        public BlockingInvocationHandler(BlockingOperationListener operationListener, boolean forceArgumentSerialization, Object target) {
            this.operationListener = operationListener;
            this.forceArgumentSerialization = forceArgumentSerialization;
            this.target = target;
        }        
        
        /**
         * {@inheritDoc}
         */
        public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
            boolean assync = !EventQueue.isDispatchThread();
            LoggerFactory.getLogger(getClass()).info(String.format("Invoking: %s %s", method.getName(), assync?"(assync)":""));
            Object result = null;
            String description = getDescription(method);

            // Notifica o operation listener
            if (!assync) {
                operationListener.processStart(I18nManager.getString("fcorp.swing.base.processingObject", description));
            }

            Throwable handledThrowable = null;
            boolean tryAgain = true;

            while (tryAgain) {
                try {
                    result = null;
                    tryAgain = false;
                    
                    // Força a serizalização dos argumentos para simular ambiente servidor rodando local
                    if (forceArgumentSerialization && args != null && !overrideArgumentSerialization(method)) {
                        copyArgumentsThroughSerialization(args);
                    }

                    if (assync) {
                        result = method.invoke(target, args);
                    } else {
                        result = blockingInvoke(description, null, target, method, args);
                    }

                    if (!assync) {
                        operationListener.processSuccess(I18nManager.getString("fcorp.swing.base.processingSuccess"), getSuccessMessage(method));
                    }
                } catch (Exception e) { // SUPPRESS CHECKSTYLE Illegal Catch - Barreira de excecao
                    // O método lançou uma exceção. Tentamos descobrir o problema
                    handledThrowable = ExceptionUtil.cleanException(e);

                    if (!assync) {
                        operationListener.processFailure(I18nManager.getString("fcorp.swing.base.processingError", description), handledThrowable);
                    }

                    //if (handledThrowable instanceof InvalidOperationException) {
                        // Se o erro é porque não existe usuário logado, perguntamos ao operationListener se devemos executar de novo
                    //    tryAgain = operationListener.sessionExpired(handledThrowable.getMessage());
                    //} else {
                        // Esta é uma exceção desconhecida. Não podemos fazer nada :(
                    //    throw handledThrowable;
                    //}
                }
            }

            return result;
        }

        /**
         * Copia os argumentos do array informado atraves de um processo de serializacao
         * @param args argumentos que serao serializados
         * @throws Exception qualquer tipo de excacao que ocorra nesse processo
         */
        private void copyArgumentsThroughSerialization(final Object[] args) throws Exception {
            final float KBYTE = 1024f;
            int bufferSize = 0;
            int i = 0;
            ByteArrayOutputStream serializationBuffer = null;
            ObjectOutputStream objectOutputStream = null;
            
            for (i = 0; i < args.length; i++) {
                bufferSize = 0;
                serializationBuffer = new ByteArrayOutputStream();
                objectOutputStream = new ObjectOutputStream(serializationBuffer);
                objectOutputStream.writeObject(args[i]);
                objectOutputStream.close();
                bufferSize += serializationBuffer.size();
                args[i] = new ObjectInputStream(new ByteArrayInputStream(serializationBuffer.toByteArray())).readObject();
            }
            LoggerFactory.getLogger(getClass()).info(String.format("Serialized parameters size: %.3f kb\n", bufferSize / KBYTE));
        }

        private boolean overrideArgumentSerialization(Method method) {
            String pattern = System.getProperty(OVERRIDE_ARGUMENT_SERIALIZATION_PATTERN_KEY);
            return pattern != null && method.getName().matches(pattern);
        }
    }

    /**
     * Define manualmente o progresso do diálogo de bloqueio
     * @param progress Progresso definido manualmente
     */
    public static void setBlockProgress(Integer progress) {
        getDialog().getProgress().setValue(progress);
    }

    /**
     * Define manualmente a mensagem atual do diálogo de bloqueio
     * @param message Mensagem de bloquei
     */
    public static void setBlockMessage(String message) {
        getDialog().getLabelMensagem().setText(message);
    }

    /**
     * Carrega um dump de erro para um Map
     * @param dump String de dump
     * @return Mapa instanciado com o dump de erro
     * @throws IOException Quando ocorre um erro de IO
     * @throws ClassNotFoundException Quando uma das classes no dump não é localizada
     */
    public static Map loadErrorDump(String dump) throws IOException, ClassNotFoundException {
        ByteArrayInputStream b = new ByteArrayInputStream(Base64.decodeBase64(dump.getBytes()));
        ObjectInputStream o = new ObjectInputStream(b);
        return (Map) o.readObject();
    }

}