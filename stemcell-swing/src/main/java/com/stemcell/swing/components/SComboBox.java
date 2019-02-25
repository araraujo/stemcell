package com.stemcell.swing.components;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.ELProperty;
import org.jdesktop.beansbinding.Property;

public class SComboBox extends JComboBox {
    /**
     * Define se o combobox suporta digitação para seleção do valor
     */
    private boolean lookupEnabled;
    /**
     * Propriedade do objeto selecionado usada para renderizar o texto no combobox
     */
    private transient Property property;
    /**
     * Valor textual da propriedade do objeto selecionado usada para renderizar o texto no combobox
     */
    private String displayProperty;
    /**
     * Define se o combobox utilizara o redimensionamento automático do popup
     */
    private boolean popupResizeEnabled;
    /**
     * True enquanto estive em processo de layout
     */
    private boolean layingOut = false; 
    /**
     * Construtor
     */
    public SComboBox() {
        this((Object[])null);
    }

    /**
     * Construtor que inicializa combo com uma lista fixa
     * @param items items
     */
    public SComboBox(Object[] items) {
        lookupEnabled = false;
        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean selected, boolean hasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, selected, hasFocus);
                l.setText(getValueAsString(value));
                return l;
            }
        });
        if (items!=null) {
            setModel(new DefaultComboBoxModel(items));
        }
    }

    /**
     *  O JCombobox, quando sofre um exceção durante a mudança de seleção, parece para de enviar os eventos de mudança
     * após a action de seleção. Iste método é um Workaround para este problema
     */
    @Override
    protected void fireActionEvent() {
        final int modifiers=16;
        ActionEvent evt = new ActionEvent(this, (int) System.currentTimeMillis(), "comboBoxChanged", System.currentTimeMillis(), modifiers);
        for (ActionListener actionListener : this.getActionListeners()) {
            actionListener.actionPerformed(evt);
        }
    }

    public String getDisplayProperty() {
        return displayProperty;
    }

    /**
     * Setter de displayProperty
     * Tentar criar um BeanProperty ou ELProperty (se a String começa com "$") e associar
     * ao atributo property
     * @param displayProperty displayProperty
     */
    public void setDisplayProperty(String displayProperty) {
        Object old = this.displayProperty;
        this.displayProperty = displayProperty;
        if (displayProperty==null) {
            property = null;
        } else {
            if (displayProperty.contains("$")) {
                property = ELProperty.create(displayProperty);
            } else {
                property = BeanProperty.create(displayProperty);
            }
        }
        firePropertyChange("displayProperty", old, this.displayProperty);
    }

    public boolean isLookupEnabled() {
        return lookupEnabled;
    }

    /**
     * Ativa ou desativa a funcionalidade de lookup por digitação
     * @param lookupEnabled lookupEnabled
     */
    public void setLookupEnabled(boolean lookupEnabled) {
        Object old = this.lookupEnabled;
        this.lookupEnabled = lookupEnabled;

        setEditor(lookupEnabled ? new SComboboxEditor() : new BasicComboBoxEditor());
        setEditable(lookupEnabled);

        firePropertyChange("lookupEnabled", old, this.lookupEnabled);
    }

    public boolean isPopupResizeEnabled() {
        return popupResizeEnabled;
    }

    /**
     * Ativa o redimensionamento do popup do combo para o maior elemento da lista.
     * Dessa forma, os elementos exibidos no pop-up não serão cortados caso a
     * largura do combo seja menor do que o necessário. <p>
     * 
     * O texto do elemento selecionado, quando o combo estiver fechado, continuará 
     * aparecendo cortado, caso for maior do que a largura do componente. A
     * largura do combo continua sendo respeitada para evitar problemas no
     * layout da tela.
     * 
     * @param popupResizeEnabled true, para ativar o redimensionamento
     */
    public void setPopupResizeEnabled(boolean popupResizeEnabled) {
        Object old = this.popupResizeEnabled;
        this.popupResizeEnabled = popupResizeEnabled;
        firePropertyChange("popupResizeEnabled", old, this.popupResizeEnabled);
    }
    
    @Override
    public void doLayout() { 
        try { 
            layingOut = true; 
            super.doLayout(); 
        } finally {
            layingOut = false; 
        }
    }

    @Override
    public Dimension getSize() { 
        Dimension comboSize = super.getSize(); 
        if(popupResizeEnabled && !layingOut) {
            comboSize.width = Math.max(comboSize.width, getPreferredSize().width); 
        }
        return comboSize; 
    } 

    /**
     * Retorna o valor a ser exibido como texto para o valor passado como parâmetro
     * de acordo com o estado do <pre>displayProperty</pre> e <pre>property</pre>
     * @param value value
     * @return Valor em vorma de string
     */
    private String getValueAsString(Object value) {
        final String blank = "      ";
        if (value==null) {
            return blank;
        }
        
        if (property==null) {
            return value.toString();
        } else {
            try {
                Object pv = property.getValue(value);
                if (pv==null) {
                    return blank;
                }
                return pv.toString();
            } catch (UnsupportedOperationException uoe) {
                return uoe.getMessage();
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="ComboboxEditor usado quando o combobox suporta busca por texto">
    /**
     * Class SComboboxEditor
     */
    private class SComboboxEditor implements ComboBoxEditor {
        private STextField editor;
        /**
         * Construtor
         */
        public SComboboxEditor() {
            editor = new STextField();
            editor.setBorder(BorderFactory.createEmptyBorder(1,1,1,0));
            editor.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent ke) {
                    if (!ke.isActionKey() && ke.getKeyCode() != KeyEvent.VK_SHIFT && ke.getKeyCode() != KeyEvent.VK_BACK_SPACE) {
                        guessText();
                    }
                }
            });
        }

        /**
         * Usa o texto digitado no campo para objter o objeto de seleção
         */
        private void guessText() {
            String s = editor.getText();
            if (s.trim().equals("")) {
                return;
            }

            String v = null;
            Object value = null;
            for (int i = 0; i < getModel().getSize(); i++) {
                value = getModel().getElementAt(i);

                if (value == null) {
                    continue;
                }

                v = getValueAsString(value);

                if (v.toLowerCase().startsWith(s.toLowerCase())) {
                    editor.setText(v);
                    editor.setCaretPosition(s.length());
                    editor.setSelectionStart(s.length());
                    editor.setSelectionEnd(v.length());
                    return;
                }
            }
        }
        /**
         * {@inheritDoc}
         */
        public Component getEditorComponent() {
            return editor;
        }
        /**
         * {@inheritDoc}
         */
        public void setItem(Object value) {
            if (value != null) {
                editor.setText(getValueAsString(value));
            } else {
                editor.setText("");
            }
        }
        /**
         * {@inheritDoc}
         */
        public Object getItem() {
            Object value = null;
            for (int i = 0; i < getModel().getSize(); i++) {
                value = SComboBox.this.getModel().getElementAt(i);
                if (value == null && editor.getText().equals("")) {
                    return null;
                } else if (getValueAsString(value).equals(editor.getText())) {
                    return value;
                }
            }
            editor.setText("");
            return null;
        }
        /**
         * {@inheritDoc}
         */
        public void selectAll() {
            editor.selectAll();
            editor.requestFocus();
        }
        /**
         * {@inheritDoc}
         */
        public void addActionListener(ActionListener l) {
            editor.addActionListener(l);
        }

        /**
         * {@inheritDoc}
         */
        public void removeActionListener(ActionListener l) {
            editor.removeActionListener(l);
        }
    }
    // </editor-fold>
}
