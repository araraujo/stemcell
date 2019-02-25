package com.stemcell.swing.components.table;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Render proxy de JTableHeader que permite múltiplas linhas
 */
public class MultiLineHeaderRendererProxy implements TableCellRenderer {
        private TableCellRenderer defaultRenderer;

        /**
         * Construtor
         * @param tcr TableCellRenderer
         */
        public MultiLineHeaderRendererProxy(TableCellRenderer tcr) {
            defaultRenderer = tcr;
        }

        /**
         * {@inheritDoc}
         */
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            String title = value.toString();
            final String pipe = "|";
            if (title.contains(pipe)) {
                    if (!title.toLowerCase().startsWith("<html>")) {
                        title = String.format("<html>%s</html>", title);
                    }

                    title = title.replace(pipe, "<br>");
                    table.getColumnModel().getColumn(column).setHeaderValue(title);
            }
            return defaultRenderer.getTableCellRendererComponent(table, title, isSelected, hasFocus, row, column);
        }

    }