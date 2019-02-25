package com.stemcell.swing.base;

import com.stemcell.common.exception.BusinessException;
import com.stemcell.common.exception.SystemException;
import com.stemcell.common.i18n.I18nManager;
import com.stemcell.swing.components.SDesktop;
import com.stemcell.swing.components.action.GenericAction;
import com.stemcell.swing.components.activators.SActivator;
import com.stemcell.swing.components.activators.SActivatorBar;
import com.stemcell.swing.components.activators.UserObjectChangeListener;
import com.stemcell.swing.components.activators.UserObjectChangeSupport;
import com.stemcell.swing.exceptionhandler.SwingExceptionHandlerManager;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 * Janela com utilitários de controle de um SDesktopPane
 */
public abstract class SDesktopSupportFrame extends JFrame {
    public static final String HELP_ACTION_TEXT_KEY = "fcorp.swing.base.helpContents";
    private static final String FRAME_KEY = "FRAME_KEY";
    
    /**
     * Menu de controle das janelas abertas no sistema
     */
    private JMenu windowsMenu;
    
    /**
     * Activator usado no controle das janelas
     */
    private SActivatorBar openedFrames;
    
    /**
     * Escutador de mudança no estado dos objetos de controle das janelas
     */
    private UserObjectChangeSupport userObjectSupport;
    
    /**
     * Construtor
     */
    public SDesktopSupportFrame() {
        userObjectSupport = new UserObjectChangeSupport();
    }
    
    /**
     * @return Objeto <code>SDesktop</code> que o sistema deve usar para  exibir os frames.
     */
    public abstract SDesktop getDesktopPane();
    
    /**
     * @return GenericAction de Help
     */
    public abstract GenericAction getActionHelpContents();

    /**
     * Getter de windowsMenu
     * @return windowsMenu
     */
    public JMenu getWindowsMenu() {
        return windowsMenu;
    }
    
    /**
     * Retorna o componente que controla a lista de frames abertos no desktop
     * do sistema. A implementação default utiliza um <code>SActivatorBar</code>
     * não visível armazenado dentro da instância do <code>SFrame</code>.<p>
     *
     * <b>Atenção:</b> O método deve ser sobrescrito para o caso da subclasse
     * desejar utilizar a própria instância de <code>SActivatorBar</code>.
     * Exemplo:
     *
     * <blockquote><pre>
     *     private SActivatorBar myPanel = new SActivatorBar();
     *
     *     ...
     *
     *     &#64;Override
     *     public SActivatorBar getOpenedFrames() {
     *         return this.myPanel;
     *     }
     * </pre></blockquote>
     * @return SActivatorBar gerenciador das janelas abertas
     */
    public SActivatorBar getOpenedFrames() {
        if (null == openedFrames) {
            openedFrames = new SActivatorBar(SActivatorBar.UNLIMITED_MAX_ACTIVATORS);
        }
        return openedFrames;
    }
    
    /**
     * Adiciona um listener para escutar alterações de user object. O user
     * object é utilizado para identificar um internal frame aberto dentro
     * da tela principal do sistema. Listeners podem estar interessados em
     * escutar estas mudanças para se manterem atualizados com a instância
     * mais recente do user object em questão.
     * @param listener Escutador do objeto
     * @return True se foi adicionado
     */
    public boolean addUserObjectChangeListener(UserObjectChangeListener listener) {
        return userObjectSupport.addUserObjectChangeListener(listener);
    }

    /**
     * Remove um listener de user object que estava registrado no frame
     * principal da aplicação.
     * @param listener Escutador do objeto
     * @return True se foi removido
     */
    public boolean removeUserObjectChangeListener(UserObjectChangeListener listener) {
        return userObjectSupport.removeUserObjectChangeListener(listener);
    }

    /**
     * Notifica os listeners registrados de uma mudança de instância em um
     * user object.
     * @param source Objeto fonte
     * @param oldValue Valor antigo
     * @param newValue Valor novo
     */
    public void fireUserObjectChange(Object source, Object oldValue, Object newValue) {
        userObjectSupport.fireUserObjectChange(source, oldValue, newValue);
    }

    /**
     * Gerencia a abertura de frames no desktop do sistema, evitando telas
     * duplicadas e atualizando o menu Janelas. Usar este método para abrir
     * novos frames. <p>
     *
     * O objeto passado como parâmetro é usado para identificar unicamente cada
     * frame. A classe <code>SFrame</code> controla para que nunca existam dois
     * frames identificados pelo mesmo objeto. Caso já exista uma tela associada
     * ao objeto informado, o frame informado será ignorado e o outro
     * previamente existente é retornado pelo método. Dessa forma, é possível
     * que existam telas diferentes da mesma classe sendo exibidas
     * simultaneamente no desktop, desde que sejam identificadas por objetos
     * diferentes. <p>
     *
     * É importante lembrar que o método <code>openInternalFrame</code> utiliza
     * os métodos <code>equals</code> e <code>hashCode</code> do objeto
     * <code>key</code> para verificar a igualdade. Portanto, deve-se garantir
     * que a implementação de ambos estão corretas para evitar qualquer
     * comportamento imprevisto no sistema. <p>
     *
     * O desenvolvedor pode realizar algum processamento sobre a chave antes
     * que uma nova tela seja aberta e após SFrame ter realizado todas as
     * validações necessárias. Para isto, é necessário sobrescrever o método
     * <code>processKeyBeforeLoadInternalFrame</code> de <code>SFrame</code>.
     * Isto pode servir, por exemplo, para carregar dados adicionais da camada
     * de negócio que devem ser passados para a tela. Se isto for feito antes da
     * chamada a <code>openInternalFrame</code>, pode acontecer desta consulta
     * ser inútil caso a tela não possa ser aberta por algum motivo.
     *
     * @param key chave que identifica o frame
     * @param frameClass a classe do frame que deve ser adicionado ao desktop
     * @return Frame instanciado/ativado
     * @see #createInternalFrame(java.lang.Object, java.lang.Class)
     * @see #moveFocusToFrameIdentifiedBy(java.lang.Object)
     * @see #processKeyBeforeLoadInternalFrame(java.lang.Object)
     */
    public SInternalFrame openInternalFrame(final Object key, final Class<? extends SInternalFrame> frameClass) {
        if (key==null) {
            throw new SystemException("openInternalFrame: parâmetro key não pode retornar null");
        }
        SActivator activator = this.getOpenedFrames().findActivator(key);
        if (null == activator) {
            try {
                if (!this.canAddFrames()) {
                    throw new BusinessException("swing.base.maxFramesReached", this.getOpenedFrames().getMaxActivators());
                }

                Object processedKey = processKeyBeforeLoadInternalFrame(key);
                if (processedKey==null) {
                    throw new SystemException(String.format("openInternalFrame: %s.processKeyBeforeLoadInternalFrame() retornou null. Este método não pode retornar null",
                                                    getClass().getSimpleName()));
                }

                SInternalFrame frame = this.createInternalFrame(processedKey, frameClass);
                activator = new SActivator(processedKey, frame);
                if (this.getOpenedFrames().addActivator(activator)) {
                    getDesktopPane().add(frame);
                }
            } catch (Throwable e) { // SUPPRESS CHECKSTYLE Illegal Catch - Barreira de excecao
                e.printStackTrace();
                SwingExceptionHandlerManager.getHandlerManager().doHandle(e);
                return null;
            }
        }
        return this.moveFocusToFrameIdentifiedBy(activator);
    }
    
    /**
     * Gerencia a abertura de frames no desktop do sistema, permitindo a seleção
     * do estado de maximização da tela. 
     * 
     * @param key chave que identifica o frame
     * @param frameClass a classe do frame que deve ser adicionado ao desktop
     * @param maximized <code>true</code> se a tela deve aparecer maximizada
     * @return Frame instanciado/ativado
     * @see #openInternalFrame(java.lang.Object, java.lang.Class)
     */
    public SInternalFrame openInternalFrame(final Object key, final Class<? extends SInternalFrame> frameClass, boolean maximized) {
        SInternalFrame frame = openInternalFrame(key, frameClass);
        try {
            if (frame!=null) {
                frame.setMaximum(maximized);
            }
        } catch (PropertyVetoException ex) {
            throw new RuntimeException(ex);
        }
        return frame;
    }
        
    /**
     * Gerencia a abertura de frames no desktop do sistema. O próprio objeto
     * <code>frameClass</code> é usado como identificador do frame. Isto impede
     * que sejam abertas no desktop duas telas diferentes da mesma classe.
     * 
     * @param frameClass a classe do frame que deve ser adicionado ao desktop
     * @return Frame instanciado/ativado
     * @see #openInternalFrame(java.lang.Object, java.lang.Class)
     */
    public SInternalFrame openInternalFrame(final Class<? extends SInternalFrame> frameClass) {
        return this.openInternalFrame(frameClass, frameClass);
    }
    
    /**
     * Gerencia a abertura de frames no desktop do sistema, permitindo a seleção
     * do estado de maximização da tela. O próprio objeto <code>frameClass</code>
     * é usado como identificador do frame. Isto impede que sejam abertas no
     * desktop duas telas diferentes da mesma classe.
     *
     * @param frameClass a classe do frame que deve ser adicionado ao desktop
     * @param maximized <code>true</code> se a tela deve aparecer maximizada
     * @return Frame instanciado/ativado
     * @see #openInternalFrame(java.lang.Object, java.lang.Class, boolean)
     */
    public SInternalFrame openInternalFrame(final Class<? extends SInternalFrame> frameClass, boolean maximized) {
        return openInternalFrame(frameClass, frameClass, maximized);
    }

    /**
     * Fecha todas as janelas que estão abertas e atualiza o menu Janelas.
     */
    public void closeAllInternalFrames() {
        List<SActivator> list = getOpenedFrames().getActivators();
        for (int i = list.size() - 1; i >= 0; i--) {
            ((SInternalFrame) list.get(i).getUserComponent()).dispose();
        }
    }
    
    /**
     * Adiciona um item para o frame no menu de janelas. Caso o menu ainda não
     * exista, ele é criado.
     * @param key Objeto chave do frame
     * @param frame Frame a ser adicionado no menu
     */
    private void addToWindowsMenu(final Object key, final SInternalFrame frame) {
        // cria o menu janelas no primeiro frame aberto
        if (windowsMenu == null) {
            getJMenuBar().add(windowsMenu = new JMenu(I18nManager.getString("swing.base.windows")), Math.max(getJMenuBar().getComponentCount()-1, 0));
        }

        // adiciona o item de menu
        final AbstractAction ac = new AbstractAction(frame.getTitle(), frame.getFrameIcon()) {
            public void actionPerformed(ActionEvent ae) {
                moveFocusToFrameIdentifiedBy(getOpenedFrames().findActivator(key));
            }
        };
        // propriedade usada para identificar a action posteriormente
        ac.putValue(FRAME_KEY, key);
        windowsMenu.add(ac);

        // notifica o item de menu sobre alterações no título ou ícone do frame
        frame.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(JInternalFrame.TITLE_PROPERTY)) {
                    ac.putValue(Action.NAME, evt.getNewValue());
                }
                if (evt.getPropertyName().equals(JInternalFrame.FRAME_ICON_PROPERTY)) {
                    ac.putValue(Action.SMALL_ICON, evt.getNewValue());
                }
            }
        });
    }
    /**
     * Remove o item do menu de janelas identificado pelo objeto passado como
     * parâmetro. Caso o menu fique vazio, ele é removido.
     * @param key Objeto chave do frame a ser removido do menu
     */
    private void removeFromWindowsMenu(Object key) {
        // remove o item de menu
        for (int i = 0; i < windowsMenu.getItemCount(); i++) {
            if (windowsMenu.getItem(i).getAction().getValue(FRAME_KEY).equals(key)) {
                windowsMenu.remove(i);
            }
        }
        // remove o menu após fechar o último frame
        if(windowsMenu.getItemCount()==0) {
            getJMenuBar().remove(windowsMenu);
            getJMenuBar().doLayout();
            windowsMenu = null;
        }
    }
    /**
     * Instancia o frame e adiciona o listener para gerenciamento do estado da
     * tela. Se a flag <code>windowsMenuEnabled</code> estiver habilitada, o
     * sistema cria automaticamente um menu para listar todos os frames abertos
     * no desktop.
     * @param key Objeto chave da janela
     * @param frameClass Classe da janela a ser instanciada/ativada
     * @return Janela instanciada/ativada
     */
    private SInternalFrame createInternalFrame(final Object key, final Class<? extends SInternalFrame> frameClass) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        SInternalFrame frame = null;
        for (Constructor c : frameClass.getConstructors()) {
            if (c.getParameterTypes() != null && c.getParameterTypes().length == 1 && c.getParameterTypes()[0].equals(key.getClass())) {
                    frame = (SInternalFrame) c.newInstance(key);
                break;
            }
        }
        if (null == frame) {
            frame = (SInternalFrame) frameClass.newInstance();
        }

        // adiciona o listener para controlar o fechamento do frame
        frame.addInternalFrameListener(new InternalFrameAdapter() {
            @Override public void internalFrameClosed(InternalFrameEvent ife) {
                getActionHelpContents().setText(I18nManager.getString(HELP_ACTION_TEXT_KEY));

                // remove o frame do desktop e o activator de openedFrames
                getDesktopPane().remove(ife.getInternalFrame());
                SActivator descriptor = getOpenedFrames().removeActivator(findActivatorByFrame(ife.getInternalFrame()));

                if (isWindowsMenuEnabled()) {
                    removeFromWindowsMenu(descriptor.getUserObject());
                }

                // remove o listener ao fechar a janela
                ife.getInternalFrame().removeInternalFrameListener(this);
            }
            @Override public void internalFrameActivated(InternalFrameEvent ife) {
                getActionHelpContents().setText(I18nManager.getString("fcorp.swing.base.helpTo", ife.getInternalFrame().getTitle()));
                getOpenedFrames().setSelectedActivator(findActivatorByFrame(ife.getInternalFrame()));
            }
            private SActivator findActivatorByFrame(JInternalFrame frame) {
                for(SActivator d : getOpenedFrames().getActivators()) {
                    if (d.getUserComponent().equals(frame)) {
                        return d;
                    }
                }
                return null;
            }
        });

        if (isWindowsMenuEnabled()) {
            addToWindowsMenu(key, frame);
        }
        return frame;
    }
    
    /**
     * Permite que subclasses realizem algum processamento sobre o objeto que
     * identifica o frame antes dele ser efetivamente carregado. A
     * implementaçao default apenas retorna o próprio objeto recebido como
     * parâmetro sem realizar nenhuma ação sobre ele.
     * @param key Objeto chave do frame
     * @return Objeto chave do frame
     */
    protected Object processKeyBeforeLoadInternalFrame(Object key) {
        return key;
    }
    /**
     * Retorna <code>true</code> se o <code>SFrame</code> deve exibir o menu
     * de janelas a medida que as instâncias de <code>SInternalFrame</code>
     * forem sendo adicionadas ao desktop. A implementação default retorna
     * <code>true</code>.<p>
     *
     * <b>Atenção:</b> O método deve ser sobrescrito para o caso da subclasse
     * desejar impedir a exibição do menu. Exemplo:
     *
     * <blockquote><pre>
     *     &#64;Override
     *     public boolean isWindowsMenuEnabled() {
     *         return false;
     *     }
     * </pre></blockquote>
     * @return True se o menu windows é habilitado
     */
    public boolean isWindowsMenuEnabled() {
        return true;
    }
    /**
     * Exibe, caso não esteja visível, e move o foco para o frame identificado
     * pelo objeto passado como parâmetro.
     * @param descriptor Activator a ser ativado
     * @return Janela que foi ativada
     */
    protected SInternalFrame moveFocusToFrameIdentifiedBy(final SActivator descriptor) {
        SInternalFrame f = (SInternalFrame) descriptor.getUserComponent();
        if (f!=null) {
            try {
                f.setIcon(false);
                f.setSelected(true);
                f.show();
            } catch (PropertyVetoException ex) {
                throw new RuntimeException(ex);
            }
        }
        return f;
    }
    /**
     * @return True se novos frames podem ser adicionados
     */
    public boolean canAddFrames() {
        return this.getOpenedFrames().getActivators().size() < this.getOpenedFrames().getMaxActivators();
    }
}