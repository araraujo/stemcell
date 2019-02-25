package com.stemcell.common.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Window;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.swing.CellEditor;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.ELProperty;
import org.jdesktop.beansbinding.PropertyResolutionException;
import org.jdesktop.el.ELException;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.JTableBinding.ColumnBinding;
import org.slf4j.LoggerFactory;

/**
 * Utilitátios de tratamento de exceção para Swing
 */
public final class ExceptionUtil {
    private static final String BINDING_GROUP_PROPERTY = "bindingGroup";
    /**
     * Construtor privado
     */
    private ExceptionUtil() {

    }

    /**
     * Retorna a lista de mensagens
     * @param t Throwable
     * @return String
     */
    public static String getMessages(Throwable t) {
        StringBuilder sb = new StringBuilder();
        Throwable e = t;
        while (e != null) {
            sb.append(e.getMessage()).append(" [").append(e.getClass()).append("]\n");
            e = e.getCause();
        }
        return sb.toString();
    }
    /**
     * Retorna a Stack de uma exceção em forma de String HTML
     * @param t Throwable
     * @return Stack da exceção em forma de String
     */
    public static String getStackAsHTMLString(Throwable t) {
        StringBuilder sb = new StringBuilder();
        Throwable e = t;
        while (e != null) {
            sb.append("<br />");
            sb.append("<b>").append(e.toString()).append("</b><br>");
            for (StackTraceElement ste : e.getStackTrace()) {
                sb.append(ste.toString()).append("<br>");
            }
            e = e.getCause();
        }
        return sb.toString();
    }

    /**
     * Retorna a Stack de uma exceção em forma de String
     * @param t Throwable
     * @return Stack da exceção em forma de String
     */
    public static String getStackAsString(Throwable t) {
        StringBuilder sb = new StringBuilder();
        Throwable e = t;
        final String crlf = "\r\n";
        while (e != null) {
            sb.append("\r\n####################################################################\r\n");
            sb.append(e.toString()).append(crlf);
            for (StackTraceElement ste : e.getStackTrace()) {
                sb.append(ste.toString()).append(crlf);
            }
            e = e.getCause();
        }
        return sb.toString();
    }

    /**
     * Elimina as exceções encapsuladas até encontrar a exceção original
     * @param t exceção original
     * @return Exceção original
     */
    public static Throwable cleanException(Throwable t) {
        return cleanException(t, true);
    }

    /**
     * Elimina as exceções encapsuladas até encontrar a exceção original
     * @param t exceção original
     * @param tryRefreshBinding flag para tentar dar refresh nos bindings em caso de PropertyResolutionException
     * @return Exceção original
     */
    public static Throwable cleanException(Throwable t, boolean tryRefreshBinding) {
        Throwable ex = t;
        while (ex != null && ex.getCause() != null
                && ((ex instanceof InvocationTargetException)
                || (ex instanceof UndeclaredThrowableException)
                || (ex instanceof ELException)
                || (ex instanceof ExecutionException)
                || (ex instanceof PropertyResolutionException)
                || (ex instanceof Error))) {
            if (ex instanceof PropertyResolutionException && tryRefreshBinding) {
                // Hack que tenta reverter o bind em janela que causou a PropertyResolutionException
                tryRefreshBindingsOnException((PropertyResolutionException) ex);
            }
            ex = ex.getCause();
        }
        return ex;
    }

    /**
     * Busca um JDesktopPane na árvore de componentes
     * @param c container
     * @return JDesktopPane
     */
    private static JDesktopPane findJDesktopPane(Container c) {
        JDesktopPane pane = null;
        for (Component component : c.getComponents()) {
            if (component instanceof JDesktopPane) {
                return (JDesktopPane) component;
            }
            if (component instanceof Container) {
                pane = findJDesktopPane((Container) component);
                if (pane != null) {
                    return pane;
                }
            }
        }
        return null;
    }

    /**
     * Busca um findingGroup usando o padrão de código gerado pelo netbeans
     * @param l Lista para adição do group
     * @param f Componente que contém o group
     */
    public static void findBindingGroupInComponent(List l, Container f) {
        findBindingGroup(l, f);
        for (int i = 0; i < f.getComponentCount(); i++) {
            if (f.getComponent(i) instanceof Container) {
                findBindingGroupInComponent(l, (Container) f.getComponent(i));
            } else {
                findBindingGroup(l, f.getComponent(i));
            }
        }
    }

    /**
     * Busca um findingGroup usando o padrão de código gerado pelo netbeans
     * @param f Objeto que contém o group
     * @param l lista de grupos na qual deve ser adicionado o grupo
     */
    public static void findBindingGroup(List l, Object f) {
        try {
            Field field = f.getClass().getDeclaredField(BINDING_GROUP_PROPERTY);
            field.setAccessible(true);
            l.add((BindingGroup) field.get(f));
        } catch (Exception e) { // SUPPRESS CHECKSTYLE Deve ignorar exceção
            LoggerFactory.getLogger(ExceptionUtil.class).debug("Error finding binding group",e);
            return;
        }
    }

    /**
     * Busca um findingGroup usando o padrão de código gerado pelo netbeans
     * @param f Objeto que contém o group
     * @return BindingGroup
     */
    public static BindingGroup findBindingGroup(Object f) {
        try {
            Field field = f.getClass().getDeclaredField(BINDING_GROUP_PROPERTY);
            field.setAccessible(true);
            return (BindingGroup) field.get(f);
        } catch (Exception e) { // SUPPRESS CHECKSTYLE Deve ignorar exceção
            return null;
        }
    }

    /**
     * Reseta os bindings que causaram a PropertyResolutionException
     * @param pre PropertyResolutionException
     */
    private static void tryRefreshBindingsOnException(PropertyResolutionException pre) {
        try {
            String expressaoComErro = pre.getMessage();
            if (!expressaoComErro.startsWith("Error evaluating EL expression ValueExpression[")) {
                return;
            }
            final int pos = 47;
            expressaoComErro = expressaoComErro.substring(pos, expressaoComErro.indexOf("]"));

            int i = 0;
            List<BindingGroup> listBindingGroup = null;
            JDesktopPane pane = null;

            Set<JTable> reloadedTables = null;
            Object bindingTarget = null;
            CellEditor cellEditor = null;
            int selectedRow = 0;
            int selectedCol = 0;
            boolean columnFound = false;
            JTableBinding tableBinding = null;
            List<ColumnBinding> columnBindings = null;


            Object[] colBindingArray = null;
            TableCellRenderer renderers[] = null;
            TableCellEditor editors[] = null;
            int prefWidth[] = null;
            int maxWidth[] = null;
            int minWidth[] = null;
            TableColumnModel columnModel = null;
            ColumnBinding columnBiding = null;
            ColumnBinding columnBiding2 = null;

            for (Frame f : JFrame.getFrames()) {
                listBindingGroup = new ArrayList<BindingGroup>();
                if (f instanceof JFrame) {
                    pane = findJDesktopPane(f);
                    if (pane != null) {
                        for (JInternalFrame jInternalFrame : pane.getAllFrames()) {
                            if (jInternalFrame.isSelected()) {
                                findBindingGroupInComponent(listBindingGroup, jInternalFrame);
                            }
                        }
                    }

                }
                for (Window w : f.getOwnedWindows()) {
                    if (w.isActive()) {
                        findBindingGroupInComponent(listBindingGroup, w);
                    }
                }

                for (BindingGroup bindingGroup : listBindingGroup) {
                    reloadedTables = new HashSet();
                    for (Binding binding : bindingGroup.getBindings()) {
                        if (binding.getSourceProperty() instanceof ELProperty) {
                            bindingTarget = binding.getTargetObject();
                            if ((bindingTarget instanceof JTable) && !reloadedTables.contains((JTable) bindingTarget)) {
                                selectedRow = ((JTable) bindingTarget).getSelectedRow();
                                selectedCol = ((JTable) bindingTarget).getSelectedColumn();
                                cellEditor = ((JTable) bindingTarget).getCellEditor();
                                if (cellEditor != null) {
                                    cellEditor.cancelCellEditing();
                                }
                                if (binding instanceof JTableBinding) {
                                    tableBinding = (JTableBinding) binding;
                                    columnBindings = tableBinding.getColumnBindings();

                                    columnFound = false;
                                    for (ColumnBinding columnBinding : columnBindings) {
                                        if (columnBinding.getSourceProperty().toString().contains(expressaoComErro)) {
                                            columnFound = true;
                                        }
                                    }
                                    if (!columnFound) {
                                        continue;
                                    }

                                    reloadedTables.add((JTable) bindingTarget);
                                    colBindingArray = new Object[columnBindings.size()];
                                    renderers = new TableCellRenderer[columnBindings.size()];
                                    editors = new TableCellEditor[columnBindings.size()];
                                    prefWidth = new int[columnBindings.size()];
                                    maxWidth = new int[columnBindings.size()];
                                    minWidth = new int[columnBindings.size()];
                                    columnModel = ((JTable) bindingTarget).getColumnModel();
                                    if (columnModel.getColumnCount() == columnBindings.size()) {
                                        for (i = columnBindings.size() - 1; i >= 0; i--) {
                                            colBindingArray[i] = columnModel.getColumn(i).getHeaderValue();
                                            renderers[i] = columnModel.getColumn(i).getCellRenderer();
                                            editors[i] = columnModel.getColumn(i).getCellEditor();
                                            prefWidth[i] = columnModel.getColumn(i).getPreferredWidth();
                                            maxWidth[i] = columnModel.getColumn(i).getMaxWidth();
                                            minWidth[i] = columnModel.getColumn(i).getMinWidth();
                                        }
                                    }
                                    tableBinding.unbind();
                                    for (i = columnBindings.size() - 1; i >= 0; i--) {
                                        columnBiding = tableBinding.getColumnBinding(i);
                                        tableBinding.removeColumnBinding(i);
                                        columnBiding2 = tableBinding.addColumnBinding(i, columnBiding.getSourceProperty());
                                        columnBiding2.setColumnClass(columnBiding.getColumnClass());
                                        columnBiding2.setColumnName(columnBiding.getColumnName());
                                        columnBiding2.setEditable(columnBiding.isEditable());
                                    }
                                    tableBinding.bind();
                                    if (columnModel.getColumnCount() == columnBindings.size()) {
                                        for (i = columnBindings.size() - 1; i >= 0; i--) {
                                            columnModel.getColumn(i).setHeaderValue(colBindingArray[i]);
                                            columnModel.getColumn(i).setCellRenderer(renderers[i]);
                                            columnModel.getColumn(i).setCellEditor(editors[i]);
                                            columnModel.getColumn(i).setPreferredWidth(prefWidth[i]);
                                            columnModel.getColumn(i).setMaxWidth(maxWidth[i]);
                                            columnModel.getColumn(i).setMinWidth(minWidth[i]);
                                        }
                                    }
                                } else {
                                    binding.refresh();
                                }
                                if (selectedRow != -1) {
                                    ((JTable) bindingTarget).setRowSelectionInterval(selectedRow, selectedRow);
                                }
                                if (selectedCol != -1) {
                                    ((JTable) bindingTarget).setColumnSelectionInterval(selectedCol, selectedCol);
                                }
                            //} else if (!binding.isManaged() && binding.getSourceProperty().toString().contains(expressaoComErro)
                               //     && bindingTarget instanceof SNumericTextField) {
                               // binding.unbind();
                               // ((SNumericTextField) bindingTarget).setNumberValue(null);
                               // binding.bind();
                            } else if (!binding.isManaged() && binding.getSourceProperty().toString().contains(expressaoComErro)) {
                                binding.refresh();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) { // SUPPRESS CHECKSTYLE Deve ignorar exceção
            LoggerFactory.getLogger(ExceptionUtil.class).error("Error refreshing bindings", e);
        }
    }

}