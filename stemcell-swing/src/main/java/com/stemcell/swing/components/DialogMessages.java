package com.stemcell.swing.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import org.omg.CORBA.SystemException;

public final class DialogMessages {

    /**
     * Construtor privado
     */
    private DialogMessages() {
    }

    /**
     * Mostra diálogo de cnfirmação (Sim|Não)
     * @param parent parent
     * @param question Pergunta
     * @return true se clicou em Sim
     */
    public static boolean confirm(Component parent, String question) {
        Object[] options = {"Sim", "Não"};
        JOptionPane opt = new JOptionPane(
            question,
            JOptionPane.QUESTION_MESSAGE,
            JOptionPane.YES_NO_OPTION,
            // Ícone padrão
            null,
            // botões
            options,
            //opção default
            options[0]);
        showDialog(parent, opt,"Confirmar", null);
        return (opt.getValue()==null) ? false : opt.getValue().equals("Sim");
    }

    /**
     * Mostra mensagem
     * @param parent parent
     * @param info mensagem
     * @param lengthContent lengthContent
     * @return Texto digitado
     */
    public static String input(Component parent, String info, Integer lengthContent) {
        JOptionPane opt = new JOptionPane(
            info,
            JOptionPane.INFORMATION_MESSAGE,
            JOptionPane.OK_CANCEL_OPTION);
        opt.setWantsInput(true);
        opt.selectInitialValue();
        showDialog(parent, opt,"", null);
        if(opt.getInputValue() == JOptionPane.UNINITIALIZED_VALUE || opt.getInputValue().equals("")) {
            return null;
        }

        return (opt.getInputValue()==null)
                    ? null
                    : (lengthContent > 0)
                            ? opt.getInputValue().toString().substring(0, opt.getInputValue().toString().trim().length())
                            : opt.getInputValue().toString();

    }

    /**
     * Mostra mensagem de erro
     * @param parent parent
     * @param info info
     */
    public static void info(Component parent, String info) {
        JOptionPane opt = new JOptionPane(
            info,
            JOptionPane.INFORMATION_MESSAGE,
            JOptionPane.DEFAULT_OPTION);
        showDialog(parent, opt, "Informação", null);
    }

    /**
     * Mostra mensagem de erro
     * @param parent parent
     * @param info mensagem
     */
    public static void error(Component parent, String info) {
        error(parent, info, null);
    }

    /**
     * Mostra mensagem de erro
     * @param parent parent
     * @param info info
     * @param hiddenString Detalhes técnicos
     */
    public static void error(Component parent, String info, String hiddenString) {
        JOptionPane opt = new JOptionPane(
            info,
            JOptionPane.ERROR_MESSAGE,
            JOptionPane.DEFAULT_OPTION);

        String message = null;
        try {
            message = "Erro";
        } catch (SystemException exception) {
            message = "";
        }
        showDialog(parent, opt, message, hiddenString);
    }

    /**
     * Mostra diálogo de entrada que só aceita números
     * @param parent parent
     * @param info info
     * @return Número digitado
     */
    public static Integer inputInteger(Component parent, String info) {
        String s = input(parent, info,0);
        if (s==null) {
            return null;
        } else {
            try {
                int i = Integer.parseInt(s);
                return i;
            }catch (NumberFormatException nfe) {
                info(parent, "Digite um número inteiro");
                return null;
            }
        }
    }

    /**
     * Mostra diálogo de entrada com um combobox
     * @param <T> Tipo de objeto da lista
     * @param parent parent
     * @param title título
     * @param label label
     * @param displayProperty Propriedade usada para exibir o objetos no combo
     * @param items lista de items
     * @param defaultOption item default
     * @return valor selecionado ou null
     */
    public static <T> T input(Component parent, String title, String label, String displayProperty, T[] items, T defaultOption) {
        SComboBox combo = new SComboBox(items);
        combo.setDisplayProperty(displayProperty);
        combo.setSelectedItem(defaultOption);
        if (input(parent, title, new SLabel(label), combo)) {
            return (T) combo.getSelectedItem();
        } else {
            return null;
        }
    }

    /**
     * Mostra diálogo de entrada com um combobox padrão
     * @param parent parent
     * @param message messagem
     * @param title titulo
     * @param options valores do combo
     * @param defaultOption seleção default
     * @return valor selecionado ou null
     */
    public static Object input(Component parent, String message, String title, Object[] options, Object defaultOption) {
        JOptionPane opt = new JOptionPane(
            message,
            JOptionPane.INFORMATION_MESSAGE,
            JOptionPane.OK_CANCEL_OPTION);
        opt.setWantsInput(true);
        if (options!=null) {
            opt.setSelectionValues(options);
        }
        if (defaultOption!=null) {
            opt.setInitialSelectionValue(defaultOption);
        }
        opt.selectInitialValue();
        showDialog(parent, opt, title, null);
        if(opt.getInputValue() == JOptionPane.UNINITIALIZED_VALUE) {
            return null;
        }
        return opt.getInputValue();
    }
    /**
     * Mostra diálogo de entrada customizado
     * @param parent parent
     * @param title título
     * @param message componentes do diálogo
     * @return true se clicou em OK
 */
    public static boolean input(Frame parent, String title, Component ... message) {
        return input((Component)parent, title, message);
    }
    /**
     * Mostra diálogo de entrada customizado
     * @param parent parent
     * @param title título
     * @param message componentes do diálogo
     * @return true se clicou em OK
     */
    public static boolean input(Dialog parent, String title, Component ... message) {
        return input((Component)parent, title, message);
    }
    /**
     * Mostra diálogo de entrada customizado
     * @param parent parent
     * @param title título
     * @param message componentes do diálogo
     * @return true se clicou em OK
     */
    private static boolean input(Component parent, String title, Component ... message) {
        final boolean[] ret = new boolean[]{false};

        final FinalizeOnDisposeDialog d = (parent instanceof Dialog)
                ? new FinalizeOnDisposeDialog((Dialog)parent,title, true)
                : new FinalizeOnDisposeDialog((Frame)parent,title, true);

        for (Component component : message) {
            d.add(component);
        }

        d.setLayout(new FlowLayout());
        d.setResizable(false);
        ((SButton)d.add(new SButton())).setAction(new AbstractAction("OK") {
            public void actionPerformed(ActionEvent e) {
                ret[0] = true;
                d.dispose();
            }});
        d.pack();
        d.setLocationRelativeTo(d.getParent());
        d.setVisible(true);
        return ret[0];
    }

    /**
     * Retira a opacidade de uma árvore de componentes
     * @param comp raiz de uma árvore de componentes
     */
     private static void setNotOpaque(JComponent comp) {
        comp.setOpaque(false);
        for (Component component : comp.getComponents()) {
            if (component instanceof JPanel)  {
                setNotOpaque((JPanel)component);
            }
        }
    }

    /**
     * Exibe o dialog com as configurações solicitadas
     * @param parentComponent parent do dialog
     * @param opt OptionPane
     * @param title Título
     * @param hiddenHelp informações adicionais
     */
    private static void showDialog(final Component parentComponent, final JOptionPane opt,String title, final String hiddenHelp) {
        Frame f = JOptionPane.getFrameForComponent(parentComponent);
        final FinalizeOnDisposeDialog dialog = new FinalizeOnDisposeDialog(f , true);
        Container contentPane = dialog.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(opt, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(parentComponent);
        dialog.addWindowListener(new WindowAdapter() {
            private boolean gotFocus = false;
            @Override
            public void windowClosing(WindowEvent we) {
                opt.setValue(null);
            }
            @Override
            public void windowActivated(WindowEvent we) {
                // Once window gets focus, set initial focus
                if (!gotFocus) {
                    opt.selectInitialValue();
                    gotFocus = true;
                }
            }
        });
        opt.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if(dialog.isVisible() && event.getSource() == opt
                   && (event.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)
                   || event.getPropertyName().equals(JOptionPane.INPUT_VALUE_PROPERTY))) {
                    dialog.setVisible(false);
                    dialog.dispose();
                }
            }
        });

        // Configura ação de detalhes técnicos
        if (hiddenHelp!=null && !hiddenHelp.equals("")) {
            final String helpException = "helpException";
            opt.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F2"), helpException);
            opt.getActionMap().put(helpException, new AbstractAction() {
                public void actionPerformed(ActionEvent ae) {
                    try {
                        File f = File.createTempFile("rel", ".txt");
                        f.deleteOnExit();
                        PrintWriter p = new PrintWriter(f);
                        p.write(hiddenHelp);
                        p.close();
                        Desktop.getDesktop().open(f);
                    } catch (IOException ex) {
                        DialogMessages.info(parentComponent, hiddenHelp);
                    }
                }
            });

            final int borda = 2;
            JLabel label = new JLabel();
            label.setHorizontalAlignment(JLabel.RIGHT);
            label.setText("[F2] Obter detalhes técnicos");
            label.setFont(label.getFont().deriveFont(Font.ITALIC, label.getFont().getSize()-borda));
            contentPane.add(label, BorderLayout.SOUTH);
        }

        setNotOpaque(opt);
        dialog.setTitle(title);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }
}