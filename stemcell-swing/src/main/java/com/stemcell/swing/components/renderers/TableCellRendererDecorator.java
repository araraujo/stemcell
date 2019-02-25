package com.stemcell.swing.components.renderers;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * Decorador de tabela, que permite customizar a renderização de um elemento
 * de JTable sem alterar o renderer original
 */
public class TableCellRendererDecorator {

    /**
     * Aplica um decorator numa tabela
     * @param table Tabela
     * @param decorator Decorator
     */
    public static void applyTo(JTable table, TableCellRendererDecorator decorator) {
        if (decorator == null) {
            return;
        }

        TableCellRendererDecoratorProxy nullProxy = new TableCellRendererDecoratorProxy(decorator, null);
        TableCellRendererDecoratorProxy proxy = null;
        TableCellRenderer mainRenderer = null;

        for (int i = 0; i < table.getColumnCount(); i++) {
            mainRenderer = table.getCellRenderer(0, i);
            if (mainRenderer != null) {
                proxy = new TableCellRendererDecoratorProxy(decorator, mainRenderer);
                proxy.setMainRenderer(mainRenderer);
                table.getColumnModel().getColumn(i).setCellRenderer(proxy);
            } else {
                table.getColumnModel().getColumn(i).setCellRenderer(nullProxy);
            }

        }
    }

    /**
     * Método chamado para decoração de um label definido pelo renderer original
     * @param label Label definido pelo renderer
     * @param table Tabela
     * @param value Valor do campo na tabela
     * @param isSelected Flag de seleção
     * @param hasFocus Flag de foco
     * @param row Índice da linha
     * @param column Índice da coluna
     */
    protected void decorateLabel(JLabel label, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    }

    /**
     * Método chamado para decoração de um componente definido pelo renderer original
     * @param component Componente definido pelo renderer
     * @param table Tabela
     * @param value Valor do campo na tabela
     * @param isSelected Flag de seleção
     * @param hasFocus Flag de foco
     * @param row Índice da linha
     * @param column Índice da coluna
     */
    protected void decorateComponent(Component component, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    }
}

/**
 * Proxy executor do TableCellRendererDecorator
 * @author x4rb
 */
class TableCellRendererDecoratorProxy extends DefaultTableCellRenderer implements TableCellRenderer {
    private TableCellRenderer mainRenderer;
    private TableCellRendererDecorator decorator;

    /**
     * Construtor
     * @param decorator Decorador a ser aplicado
     * @param mainRenderer Renderer original
     */
    public TableCellRendererDecoratorProxy(TableCellRendererDecorator decorator, TableCellRenderer mainRenderer) {
        this.mainRenderer = mainRenderer;
        this.decorator = decorator;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = null;
        JLabel label = null;
        if (mainRenderer != null) {
            c = mainRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        } else {
            c = TableCellRendererDecoratorProxy.this;
            label = TableCellRendererDecoratorProxy.this;
            label.setText((value == null) ? "" : value.toString());
        }

        if (c instanceof JLabel) {
            label = (JLabel) c;
        }

        if (isSelected) {
            c.setForeground(table.getSelectionForeground());
            c.setBackground(table.getSelectionBackground());
        } else {
            c.setForeground(table.getForeground());
            c.setBackground(table.getBackground());
        }
        setFont(table.getFont());

        if (hasFocus) {
            c.setBackground(c.getBackground().darker().darker());
            if (label != null) {
                Border border = null;
                if (isSelected) {
                    border = UIManager.getBorder("Table.focusSelectedCellHighlightBorder");
                }
                if (border == null) {
                    border = UIManager.getBorder("Table.focusCellHighlightBorder");
                }
                label.setBorder(border);
            }

            if (!isSelected && table.isCellEditable(row, column)) {
                Color col = null;
                col = UIManager.getColor("Table.focusCellForeground");
                if (col != null) {
                    c.setForeground(col);
                }
                col = UIManager.getColor("Table.focusCellBackground");
                if (col != null) {
                    c.setBackground(col);
                }
            }
        } else {
            if (label != null) {
                label.setBorder(noFocusBorder);
            }
        }
        if (label != null) {
            decorator.decorateLabel(label, table, value, isSelected, hasFocus, row, column);
        }

        decorator.decorateComponent(c, table, value, isSelected, hasFocus, row, column);

        return c;
    }

    /**
     * Setter de mainRenderer
     * @param mainRenderer Renderer original
     */
    public void setMainRenderer(TableCellRenderer mainRenderer) {
        this.mainRenderer = mainRenderer;
    }
}
