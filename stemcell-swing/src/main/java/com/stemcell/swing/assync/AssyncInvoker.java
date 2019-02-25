package com.stemcell.swing.assync;

import com.stemcell.swing.assync.processor.ExceptionExtendedProcessor;
import com.stemcell.swing.assync.processor.ExceptionProcessor;
import com.stemcell.swing.assync.processor.PostProcessor;
import com.stemcell.swing.assync.processor.PreProcessor;
import com.stemcell.swing.assync.processor.PreScheduleProcessor;
import com.stemcell.swing.assync.processor.Processor;
import com.stemcell.swing.assync.processor.SuccessProcessor;
import com.stemcell.swing.assync.processor.impl.AddValueToListPostProcessor;
import com.stemcell.swing.assync.processor.impl.AssignResultPostProcessor;
import com.stemcell.swing.assync.processor.impl.IgnoreTimeoutExceptionProcessor;
import com.stemcell.swing.assync.processor.impl.ShowMessagePostProcessor;
import com.stemcell.swing.assync.processor.impl.SwapPropertyProcessor;
import java.awt.Component;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Utilitário para execução de chamadas assíncronas, visando maior agilidade na
 * interface e diminuição das esperas dos diálogos 'Processando ...' Deve ser
 * usado no formato fluent interface, e deve se observar que a ordem das chamdas
 * é importante pois determina a ordem de processamento das configurações
 * Exemplo:
 * <pre>
 *    AssyncInvoker.create(model.getService())              // Define o objeto do qual o método será invocado remotamente
 *          .assingingResultTo(model, "cursos")             // Associa o resultado à propriedade ao fim da execução
 *          .settingLoadingIconOn(sLabel1)                  // Adiciona o ícone de loading componentes enquanto espera
 *          .disabling(actionBuscar, sTable1)               // Desabilita componentes enquanto aguarda execução
 *          .invoke().buscarCursos(model.getCursoFiltro())  // Finaliza o agendamento da chamada
 * </pre>
 *
 */
public final class AssyncInvoker<R, T> implements Runnable {
    /*
     * Thread Model default para manter retrocompatibilidade.
     */
    public static ProcessorExecutor defaultProcessorExecutor = new SwingProcessorExecutor();
    /**
     * Chave do executor padrão
     */
    public static final String DEFAULT_EXECUTOR_KEY = "__DEFAULT_EXECUTOR__";
    /**
     * Icone default usado para demonstrar espera de chamada assíncrona
     */
    public static final Icon DEFAULT_LOADING_ICON = new ImageIcon(AssyncInvoker.class.getResource("/icons/loading.gif"));
    /**
     * Mapa de executores das chamdas
     */
    private static Map<Object, ExecutorService> executors = Collections.synchronizedMap(new HashMap<Object, ExecutorService>());
    /**
     * Mapa de lista de futures agendadas para execuçao
     */
    private static Map<Object, List<Future>> tasks = Collections.synchronizedMap(new HashMap<Object, List<Future>>());
    /**
     * Identificador do executor usado nesta instância
     */
    private Object executorKey = DEFAULT_EXECUTOR_KEY;
    /**
     * Lista de processadores de chamdas reigstrados na chamada atual
     */
    private List<Processor> processors = new ArrayList();
    /**
     * Referência para chamada a ser executada assincronamente
     */
    private Callable<R> call;
    
    private ProcessorExecutor processorExecutor;

    /**
     * Permite customizar o executor de chamadas assíncronas
     *
     * @param executor executor
     * @param executorKey chaveDoExecutor
     */
    public static synchronized void setExecutor(ExecutorService executor, Object executorKey) {
        executors.put(executorKey, executor);
        tasks.put(executorKey, new ArrayList<Future>());
    }

    /**
     * Obtém o executor de chamadas assíncronas Instanciado sob demanda com pool
     * de tamanho 2 se não definido
     *
     * @return executor
     */
    public static synchronized ExecutorService getExecutor(Object executorKey) {
        return executors.get(executorKey);
    }

    /**
     * Cria um AssyncInvoker que não executa. Usado apenas para enfileirar
     * processors
     *
     * @return instância para uso no formato fluent interface
     */
    public static <R> AssyncInvoker<R, Object> create() {
        AssyncInvoker ai = new AssyncInvoker(new Callable() {
            @Override
            public Object call() throws Exception {
                return null;
            }
        });
        return ai;
    }

    /**
     * Cria um AssyncInvoker cujo método é informado no momento do schedule
     *
     * @return instância para uso no formato fluent interface
     */
    public static <R, T> AssyncInvoker<R, T> create(T target) {
        return new AssyncInvoker<R, T>(target);
    }

    /**
     * Cria um AssyncInvoker para a chamada passada
     *
     * @param call chama a ser executada assincronamente
     * @return instância para uso no formato fluent interface
     */
    public static <R> AssyncInvoker<R, Object> create(Callable<R> call) {
        return new AssyncInvoker(call);
    }

    /**
     * Cria um AssyncInvoker para a chamada passada
     *
     * @param <T>
     * @param target
     * @param methodName
     * @param args
     * @return
     */
    public static AssyncInvoker<Object, Object> create(final Object target, final String methodName, final Object... args) {
        return new AssyncInvoker(new ReflectionCallable(target, methodName, args));
    }

    /**
     * Força o cancelamento de todas as chamadas pendentes para um determinado
     * executor
     *
     * @param executorKey
     * @return
     */
    public static boolean cancelAllPendingTasks(Object executorKey) {
        ExecutorService executor = executors.get(executorKey);
        if (executor != null) {
            List<Future> futureTasks = new ArrayList(tasks.get(executorKey));
            for (Future future : futureTasks) {
                if (future != null) {
                    future.cancel(false);
                }
            }
        }
        return executor != null;
    }

    /**
     * Cancela todas as tasks e finaliza o executor
     *
     * @param executorKey
     */
    public static void terminate(Object executorKey) {
        ExecutorService executor = executors.get(executorKey);
        if (executor != null) {
            executor.shutdownNow();
            executors.remove(executorKey);
            tasks.remove(executorKey);
        }
    }

    
    /**
     * Cancela todas as tasks e finaliza todos os executors (inclusive o executor default)
     *
     * @param executorKey
     */
    public static void terminate() {
        for (ExecutorService executor : executors.values()) {
            executor.shutdownNow();
        }
        executors.clear();
        tasks.clear();
    }
    
    /**
     * Construtor privado. Esta classe deve ser usada sob o formato de fluent
     * interface
     */
    private AssyncInvoker(T target) {
        this.call = new ProxyResolveCallable(target, this);
        this.processorExecutor = defaultProcessorExecutor;
    }

    /**
     * Construtor privado. Esta classe deve ser usada sob o formato de fluent
     * interface
     */
    private AssyncInvoker(Callable<R> call) {
        if (call == null) {
            throw new IllegalArgumentException("call não pode ser nulo");
        }
        this.call = call;
        this.processorExecutor = defaultProcessorExecutor;
    }
    
    public AssyncInvoker<R, T> usingProcessorExecutor(ProcessorExecutor processorExecutor) {
        this.processorExecutor = processorExecutor;
        return this;
    }

    /**
     * Solicita que o retorno da chamada seja setado na propriedade
     * <b>property</b>
     * do objeto <b>target</b>
     *
     * @param target target
     * @param property property
     * @return instância para uso no formato fluent interface
     * @see AssignResultPostProcessor
     */
    public AssyncInvoker<R, T> assigningResultTo(Object target, String property) {
        processing(new AssignResultPostProcessor(target, property));
        return this;
    }

    /**
     * Solicita que o retorno da chamada seja setado na propriedade
     * <b>property</b>
     * do objeto <b>target</b>
     *
     * @param target target
     * @param property property
     * @return instância para uso no formato fluent interface
     * @see AssignResultPostProcessor
     */
    public AssyncInvoker<R, T> assigningResultTo(Object target, String property, Object valueWhileInvoking) {
        processing(new AssignResultPostProcessor(target, property, valueWhileInvoking));
        return this;
    }

    /**
     * Solicita que o ícone <b>icon</b> seja definido como valor da propriedade
     * "icon" do objeto <b>component</b>. O ícone original é restaurado após o
     * fim da chamada
     *
     * @param component component
     * @param icon icon
     * @return instância para uso no formato fluent interface
     * @see SwapPropertyProcessor
     */
    public AssyncInvoker<R, T> settingIconOn(Object component, Icon icon) {
        processing(new SwapPropertyProcessor(component, "icon", icon));
        return this;
    }

    /**
     * Solicita que um ícone sinalizador de carga seja definido como valor da
     * propriedade "icon" dos objetos <b>components</b>. O ícone original é
     * restaurado após o fim da chamada
     *
     * @param components components
     * @return instância para uso no formato fluent interface
     * @see settingIconOn(Object, Icon)
     */
    public AssyncInvoker<R, T> settingLoadingIconOn(Object... components) {
        for (Object component : components) {
            settingIconOn(component, DEFAULT_LOADING_ICON);
        }
        return this;
    }

    /**
     * Solicita que o valor da propriedade <b>property</b> do objeto
     * <b>target</b> seja alterado para <b>tempValue</b> enquanto a chamada não
     * finalizar. O valor original é restaurado após o fim da chamada
     *
     * @param target target
     * @param property property
     * @param tempValue tempValue
     * @return instância para uso no formato fluent interface
     * @see SwapPropertyProcessor
     */
    public AssyncInvoker<R, T> swapingTempValueOf(Object target, String property, Object tempValue) {
        processing(new SwapPropertyProcessor(target, property, tempValue));
        return this;
    }

    /**
     * Solicita que o valor da propriedade <b>enabled</b> de todos os objetos em
     * <b>components</b>
     * seja alterado para <b>false</b> enquanto a chamada não termina. O valor
     * original da propriedade <b>enabled</b> é restaurado após o fim da chamada
     *
     * @param components components
     * @return instância para uso no formato fluent interface
     * @see SwapPropertyProcessor
     */
    public AssyncInvoker<R, T> disabling(Object... components) {
        for (Object component : components) {
            processing(new SwapPropertyProcessor(component, "enabled", Boolean.FALSE));
        }
        return this;
    }

    /**
     * Solicita que o valor <b>object</b> seja adicionado na lista que retorna
     * da chamada assíncrona na posição <b>index</b>. O valor retornado da
     * chamada assíncrona deve ser uma lista, e esta solicitação deve ser feita
     * ANTES da chamada a assingingResultTo
     *
     * @param index index
     * @param object object
     * @return instância para uso no formato fluent interface
     * @see AddValueToListPostProcessor
     */
    public AssyncInvoker<R, T> addingToListResult(int index, Object object) {
        processing(new AddValueToListPostProcessor(index, object));
        return this;
    }

    /**
     * Solicita exibição de diálogo com mensagem de sucesso ao fim da chamada.
     *
     * @param parent
     * @param message
     * @return instância para uso no formato fluent interface
     * @see ShowMessagePostProcessor
     */
    public AssyncInvoker<R, T> showingSuccessDialog(Component parent, String message) {
        processing(new ShowMessagePostProcessor(parent, message));
        return this;
    }
    
    /**
     * Abafa erro de timeout, caso ocorra durante a execução do processo assíncrono.
     * 
     * @return  instância para uso no formato fluent interface
     * @see IgnoreTimeoutExceptionProcessor
     */
    public AssyncInvoker<R, T> ignoringTimeoutExceptions() {
        processing(new IgnoreTimeoutExceptionProcessor());
        return this;
    }

    /**
     * Solicita o processamento de um <b>processor</b> customizado
     *
     * @param processor processor
     * @return instância para uso no formato fluent interface
     */
    public AssyncInvoker<R, T> processing(Processor processor) {
        processors.add(processor);
        return this;
    }

    /**
     * Retorna um proxy para execução assíncrona
     *
     * @param executorKey executor
     * @return proxy para execução assíncrona
     */
    public T invoke(Object executorKey) {
        this.executorKey = executorKey;
        return invoke();
    }

    /**
     * Retorna um proxy para execução assíncrona
     *
     * @return proxy para execução assíncrona
     */
    public T invoke() {
        if (ProxyResolveCallable.class.isAssignableFrom(call.getClass())) {
            return (T) ((ProxyResolveCallable) call).getProxy();
        } else {
            throw new IllegalStateException("Não é possível usar invoke() quando o AssynInvoker é criado com o parâmetro callable");
        }
    }

    /**
     * Confirma o agendamento da chamada assíncrona com Callable pré definido
     */
    public void schedule(Object executorKey) {
        this.executorKey = executorKey;

        preScheduleAll();

        // Busca ou inicializa executor solicitado
        ExecutorService queueExecutor = executors.get(executorKey);
        if (queueExecutor == null) {
            queueExecutor = Executors.newSingleThreadExecutor();
            setExecutor(queueExecutor, executorKey);
        }

        // Submete a task e a adiciona na lista de tasks do executor
        ManagedFutureTask task = new ManagedFutureTask(this);
        queueExecutor.submit(task);
        tasks.get(executorKey).add(task);
    }

    /**
     * Confirma o agendamento da chamada assíncrona com Callable pré definido
     */
    public void schedule() {
        schedule(executorKey);
    }

    /**
     * Implementação da chamada executada assincronamente Não deve ser chamado
     * diretamente
     */
    @Override
    public void run() {
        Object result = null;
        Exception ex = null;

        preProcessAll();

        try {
            result = call.call();
        } catch (Exception e) {
            ex = e;
        }

        postProcessAll(result, ex);
    }

    /**
     * Executa todos os PreScheduleProcessor agendados
     */
    private void preScheduleAll() {
        for (Object processor : processors) {
            if (processor instanceof PreScheduleProcessor) {
                ((PreScheduleProcessor) processor).preSchedule(call);
            }
        }
    }
    
    /**
     * Executa todos os PreProcessor agendados
     */
    private void preProcessAll() {
        Runnable preProcessExecution = new Runnable() {
            @Override
            public void run() {
                for (Object processor : processors) {
                    if (processor instanceof PreProcessor) {
                        ((PreProcessor) processor).preProccess(call);
                    }
                }
            }
        };
        
        processorExecutor.execute(preProcessExecution);
    }
    
    /**
     * Executa todos os PostProcessor agendados
     *
     * @param result
     * @param ex
     */
    private void postProcessAll(final Object result, final Exception ex) {
        Runnable posProccessRunnable = new Runnable() {
            @Override
            public void run() {
                if (getExecutor(executorKey) != null && !getExecutor(executorKey).isShutdown()) {
                    Object res = result;
                    for (Object processor : processors) {
                        if (processor instanceof PostProcessor) {
                            res = ((PostProcessor) processor).postProccess(res);
                        }
                    }

                    if (ex != null) {
                        boolean handled = false;
                        for (Object processor : processors) {
                            if (processor instanceof ExceptionProcessor) {
                                ((ExceptionProcessor) processor).exceptionProcess(ex);
                                handled = true;
                            } else if (processor instanceof ExceptionExtendedProcessor) {
                                ((ExceptionExtendedProcessor) processor).exceptionProcess(new ExceptionExtendedProcessor.AsyncExceptionData(ex, AssyncInvoker.this));
                                handled = true;
                            }
                        }
                        if (!handled) {
                            throw ex instanceof RuntimeException ? (RuntimeException) ex : new UndeclaredThrowableException(ex);
                        }
                    } else {
                        for (Object processor : processors) {
                            if (processor instanceof SuccessProcessor) {
                                res = ((SuccessProcessor) processor).proccessSuccess(res);
                            }
                        }
                    }
                }
            }
        };
        
        processorExecutor.execute(posProccessRunnable);
    }

    /**
     * Future task que limpa sua instância da lista de tasks ao terminar
     *
     * @param <T>
     */
    class ManagedFutureTask<T> extends FutureTask<T> {

        public ManagedFutureTask(Runnable runnable) {
            super(runnable, null);
        }

        @Override
        protected void done() {
            // Remove a task da lista de tasks do executor
            List<Future> l = tasks.get(AssyncInvoker.this.executorKey);
            if (l != null) {
                l.remove(this);
            }

            // Se a task foi cancelada, remove todos os pos processadores
            if (isCancelled()) {
                processors.clear();
            }
        }
    }
}