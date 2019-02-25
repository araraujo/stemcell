package com.stemcell.swing.components.activators;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import com.stemcell.swing.components.SPanel;

/**
 * Container para componentes <code>SActivator</code>. Gerencia a quantidade
 * máxima de activators e a quantidade de activators visíveis.
 *
 * @author x4rc
 * @see SActivator
 */
public class SActivatorBar extends SPanel implements UserObjectChangeListener {
    public static final int DEFAULT_MAX_ACTIVATORS = 10;
    public static final int UNLIMITED_MAX_ACTIVATORS = Integer.MAX_VALUE;
    public static final int EMPTY_INDEX = -1;

    // guarda a coleção de activators contidos no painel
    private List<SActivator> activators = new LinkedList<SActivator>();
    // indexa os activators pelo objeto de negócio
    private Map<Object, SActivator> activatorMap = new HashMap<Object, SActivator>();
    // activator selecionado pelo usuário
    private SActivator selectedActivator;
    // orientação que será atribuída aos activators que forem adicionados
    private boolean vertical = false;
    // sentido da rotação que será atribuída aos activators quando a orientação for vertical
    private boolean clockwise = false;
    // quantidade máxima de activators permitidos no painel
    private int maxActivators;
    // índice do primeiro activator visível no painel (referente a lista activators)
    private int firstVisibleActivatorIndex = EMPTY_INDEX;
    // índice do último descritor visível no painel (referente a lista activators)
    private int lastVisibleActivatorIndex = EMPTY_INDEX;
    
    // altera o activator selecionado para aquele que receber um evento de click.
    private transient MouseListener clickListener = new MouseAdapter() {
        @Override public void mouseClicked(MouseEvent e) {
            if (e.getComponent() instanceof SActivator) {
                setSelectedActivator((SActivator) e.getComponent());
            }
        }
    };

    /**
     * Construtor.
     */
    public SActivatorBar() {
        this(DEFAULT_MAX_ACTIVATORS);
    }

    /**
     * Construtor.
     * @param maxActivators quantidade máxima permitida de activators
     */
    public SActivatorBar(int maxActivators) {
        initComponents();
        this.maxActivators = maxActivators;
        this.centerPane.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent evt) {
                updateVisibilityState();
            }
        });
    }

    /**
     * Retorna uma lista imutável com todos os activators contidos no componente.
     */
    public List<SActivator> getActivators() {
        return Collections.unmodifiableList(activators);
    }

    public boolean isClockwise() {
        return clockwise;
    }

    /**
     * Define o sentido da rotação dos activators. Só é utilizado se vertical estiver
     * definido para true.
     * @param clockwise true, se for no sentido horário
     */
    public void setClockwise(boolean clockwise) {
        Object old = this.clockwise;
        this.clockwise = clockwise;
        this.firePropertyChange("clockwise", old, clockwise);
        this.revalidate();
    }

    public boolean isVertical() {
        return vertical;
    }

    /**
     * Define se os activators devem ser renderizados verticalmente. O default é false.
     * @param vertical true, se for vertical
     */
    public void setVertical(boolean vertical) {
        boolean oldValue = this.vertical;

        if (vertical != oldValue) {
            this.vertical = vertical;
            this.remove(controlPane);
            if (vertical) {
                centerPane.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
                controlPane.setLayout(new BoxLayout(controlPane, BoxLayout.PAGE_AXIS));
                this.add(controlPane, BorderLayout.SOUTH);
            } else {
                centerPane.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
                controlPane.setLayout(new BoxLayout(controlPane, BoxLayout.LINE_AXIS));
                this.add(controlPane, BorderLayout.EAST);
            }
            btnPrevious.setVertical(vertical);
            btnNext.setVertical(vertical);
            firePropertyChange("vertical", oldValue, vertical);
            revalidate();
            repaint();
        }
    }

    public int getMaxActivators() {
        return maxActivators;
    }

    /**
     * Define a quantidade máxima permitida de activators. 
     * @param maxActivators quantidade máxima
     */
    public void setMaxActivators(int maxActivators) {
        Object old = this.maxActivators;
        this.maxActivators = maxActivators;
        firePropertyChange("maxActivators", old, this.maxActivators);
    }

    public SActivator getSelectedActivator() {
        return selectedActivator;
    }

    /**
     * Atualiza o activator selecionado. Caso ele não esteja visível no momento,
     * força que ele apareça movendo a área visível do componente até que o
     * activator seja incluído nela. Se o activator informado já estiver selecionado,
     * não faz nada. Se o activator informado for <code>null</code>, limpa a
     * seleção atual.
     */
    public void setSelectedActivator(SActivator selectedActivator) {
        Object oldValue = this.selectedActivator;

        if (selectedActivator != oldValue) {
            this.selectedActivator = selectedActivator;
            firePropertyChange("selectedActivator", oldValue, this.selectedActivator);

            // força que o activator fique visível no painel
            while (this.activators.indexOf(selectedActivator) < this.firstVisibleActivatorIndex) {
                this.moveBackwardInternal();
            }
            while (this.activators.indexOf(selectedActivator) > this.lastVisibleActivatorIndex) {
                this.moveForwardInternal();
            }
            
            this.updateUI();
        }
    }

    /**
     * Localiza o activator a partir de seu userObject. Caso nenhum seja 
     * encontrado, o método retorna <code>null</code>.
     * @param userObject objeto procurado
     * @return activator encontrato
     */
    public SActivator findActivator(Object userObject) {
        return this.activatorMap.get(userObject);
    }

    /**
     * Verifica se o activator existe dentro do componente.
     * @param activator activator procurado
     * @return true, se existir
     */
    public boolean containsActivator(SActivator activator) {
        return this.containsObject(activator.getUserObject());
    }

    /**
     * Verifica se o objeto de usuário existe dentro de algum activator.
     * @param userObject objeto procurado
     * @return true, se existir
     */
    public boolean containsObject(Object userObject) {
        return this.activatorMap.containsKey(userObject);
    }
    
    /**
     * Adiciona o activator ao componente. Se o ActivatorBar já conter uma
     * quantidade de activators igual ao máximo permitido, o elemento não será
     * adicionado e o método retorna <code>false</code>.
     * @param activator elemento a ser adicionado
     * @return true, se for adicionado com sucesso
     */
    public boolean addActivator(SActivator activator) {
        if (!this.containsObject(activator.getUserObject()) && this.activators.size() < this.getMaxActivators()) {
            activator.setVertical(vertical);
            activator.setClockwise(clockwise);
            activator.addMouseListener(this.clickListener);
            this.activatorMap.put(activator.getUserObject(), activator);
            this.activators.add(activator);
            this.centerPane.add(activator);

            this.updateVisibilityState();
            this.setSelectedActivator(activator);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Remove o activator do componente.
     * @param activator elemento procurado
     * @return elemento removido, ou null caso não seja removido
     */
    public SActivator removeActivator(SActivator activator) {
        if (this.containsObject(activator.getUserObject())) {
            SActivator previous = getPreviousActivator(activator);

            activator = this.activatorMap.remove(activator.getUserObject());
            activator.removeMouseListener(this.clickListener);
            this.activators.remove(activator);
            this.centerPane.remove(activator);

            this.updateVisibilityState();
            this.setSelectedActivator(previous);
            return activator;
        }
        return null;
    }

    /**
     * Move para trás a lista de activator visíveis no compomente.
     */
    public void moveBackward() {
        if (this.canMoveBackward()) {
            this.moveBackwardInternal();
            this.revalidate();
        }
    }

    /**
     * Move para frente a lista de activator visíveis no componente.
     */
    public void moveForward() {
        if (this.canMoveForward()) {
            this.moveForwardInternal();
            this.revalidate();
        }
    }

    /**
     * Quando ocorrer uma mudança na instância do userObject de algum activator
     * o painel precisa atualizar o activator.
     */
    @Override
    public void userObjectChanged(UserObjectChangeEvent evt) {
        if (evt.getOldValue() == null || evt.getNewValue() == null || evt.getOldValue() == evt.getNewValue()) {
            return;
        }

        SActivator activator = this.activatorMap.remove(evt.getOldValue());
        if (activator != null) {
            this.activatorMap.put(evt.getNewValue(), activator);
            activator.setUserObject(evt.getNewValue());
            activator.updateDescriptionFromComponent();
        }
    }

    /**
     * Atualiza os índices de visibilidade de acordo com a situação atual
     * do componente. Também atualiza o estado dos botões para habilitado
     * ou desabilitado.
     */
    private void updateVisibilityState() {
        if (this.centerPane.getComponentCount() == 0) {
            this.firstVisibleActivatorIndex = EMPTY_INDEX;
            this.lastVisibleActivatorIndex = EMPTY_INDEX;
        }
        else {
            int avaliableSpace = this.getVisibleSpace(this.centerPane);
            this.firstVisibleActivatorIndex = this.activators.indexOf(this.centerPane.getComponents()[0]);
            for (Component component : this.centerPane.getComponents()) {
                avaliableSpace -= this.getVisibleSpace(component);
                if (avaliableSpace >= 0) {
                    this.lastVisibleActivatorIndex = this.activators.indexOf(component);
                }
                else {
                    break;
                }
            }
        }
        this.actionLast.setEnabled(this.canMoveBackward());
        this.actionNext.setEnabled(this.canMoveForward());
    }

    private int getVisibleSpace(Component component) {
        if (vertical) {
            return ( component.getHeight() > 0 ) ? component.getHeight() : component.getPreferredSize().height;
        } else {
            return ( component.getWidth() > 0 ) ? component.getWidth() : component.getPreferredSize().width;
        }
    }

    private boolean canMoveBackward() {
        return this.firstVisibleActivatorIndex > 0;
    }

    private boolean canMoveForward() {
        return this.centerPane.getComponentCount() > 0 && this.lastVisibleActivatorIndex < this.activators.size() - 1;
    }

    private void moveBackwardInternal() {
        this.centerPane.add(this.activators.get(this.firstVisibleActivatorIndex - 1), 0);
        this.updateVisibilityState();
    }

    private void moveForwardInternal() {
        this.centerPane.remove(0);
        this.updateVisibilityState();
    }

    /**
     * Retorna o descritor que deve ficar como selecionado quando o descritor
     * informado for removido. O funcionamento do método deve seguir o seguinte
     * exemplo: <p>
     *
     * Descritores na lista = A1, A2, A3, A4 <br>
     *
     * <ul>
     * <li>Ao remover A1, o selecionado é A2</li>
     * <li>Ao remover A2, o selecionado é A3</li>
     * <li>Ao remover A3, o selecionado é A4</li>
     * <li>Ao remover A4, o selecionado é A3</li>
     * </ul>
     */
    private SActivator getPreviousActivator(SActivator activator) {
        int currentIndex = this.activators.indexOf(activator);
        if (currentIndex < this.activators.size() -1) {
            return this.activators.get(currentIndex + 1);
        }
        else if (currentIndex > 0) {
            return this.activators.get(currentIndex - 1);
        }
        else {
            return null;
        }
    }
    
    private void changeBackground(SActivator activator, Color background) {
        if ("Nimbus".equals(UIManager.getLookAndFeel().getName())) {
            UIDefaults overrides = new UIDefaults(new Object[] {"Label.background", background});
            activator.putClientProperty("Nimbus.Overrides.InheritDefaults", Boolean.TRUE);
            activator.putClientProperty("Nimbus.Overrides", overrides);
        } else {
            activator.setBackground(background);
        }
    }

    /** This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        actionLast = new com.stemcell.swing.components.ConfigurableAction();
        actionNext = new com.stemcell.swing.components.ConfigurableAction();
        centerPane = new com.stemcell.swing.components.SPanel();
        controlPane = new com.stemcell.swing.components.SPanel();
        btnPrevious = new com.stemcell.swing.components.SButton();
        btnNext = new com.stemcell.swing.components.SButton();

        actionLast.setEnabled(false);
        actionLast.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/arrowleft.png"))); // NOI18N
        actionLast.setMethodName("moveBackward");
        actionLast.setTarget(this);

        actionNext.setEnabled(false);
        actionNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/arrowright.png"))); // NOI18N
        actionNext.setMethodName("moveForward");
        actionNext.setTarget(this);

        setLayout(new java.awt.BorderLayout());

        centerPane.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        add(centerPane, java.awt.BorderLayout.CENTER);

        controlPane.setLayout(new javax.swing.BoxLayout(controlPane, javax.swing.BoxLayout.LINE_AXIS));

        btnPrevious.setAction(actionLast);
        btnPrevious.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        btnPrevious.setClockwise(true);
        btnPrevious.setAlignmentX(0.5F);
        btnPrevious.setBorderPainted(false);
        btnPrevious.setContentAreaFilled(false);
        btnPrevious.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/arrowleft_disabled.png"))); // NOI18N
        btnPrevious.setFocusPainted(false);
        btnPrevious.setFocusable(false);
        btnPrevious.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        controlPane.add(btnPrevious);

        btnNext.setAction(actionNext);
        btnNext.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        btnNext.setClockwise(true);
        btnNext.setAlignmentX(0.5F);
        btnNext.setBorderPainted(false);
        btnNext.setContentAreaFilled(false);
        btnNext.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/arrowright_disabled.png"))); // NOI18N
        btnNext.setFocusPainted(false);
        btnNext.setFocusable(false);
        btnNext.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        controlPane.add(btnNext);

        add(controlPane, java.awt.BorderLayout.EAST);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.stemcell.swing.components.ConfigurableAction actionLast;
    private com.stemcell.swing.components.ConfigurableAction actionNext;
    private com.stemcell.swing.components.SButton btnNext;
    private com.stemcell.swing.components.SButton btnPrevious;
    private com.stemcell.swing.components.SPanel centerPane;
    private com.stemcell.swing.components.SPanel controlPane;
    // End of variables declaration//GEN-END:variables
}
