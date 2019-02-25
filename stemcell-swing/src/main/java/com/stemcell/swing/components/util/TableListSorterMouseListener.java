package com.stemcell.swing.components.util;

import com.stemcell.common.beans.AbstractBean;
import com.stemcell.common.exception.SystemException;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.jdesktop.swingbinding.impl.ListBindingManager;


/**
 * Classe TableSorterMouseListener
 * @author x4rb
 */
public class TableListSorterMouseListener extends MouseAdapter implements TableModelListener {
    private static ImageIcon icon = new ImageIcon(TableListSorterMouseListener.class.getResource("/icons/sort.png"));
    private static ImageIcon icon2 = new ImageIcon(TableListSorterMouseListener.class.getResource("/icons/sort2.png"));
    private TableColumn sortedColummn;
    private JTable table;
    private int lastColumnModelHash = -1;
    private int order;

    /**
     * Construtor
     * @param table Tabela
     */
    public TableListSorterMouseListener(JTable table) {
        for (MouseListener m : table.getTableHeader().getMouseListeners()) {
            if (m instanceof TableListSorterMouseListener) {
                table.getTableHeader().removeMouseListener(m);
            }
        }
        this.table = table;
        this.table.getModel().addTableModelListener(this);
    }

    /**
     * Instala o listener na(s) tabelas
     * @param tables tabelas
     */
    public static void install(JTable ... tables) {
        for (JTable jTable : tables) {
            jTable.getTableHeader().addMouseListener(new TableListSorterMouseListener(jTable));
        }
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        TableColumnModel columnModel = table.getColumnModel();

        if (lastColumnModelHash!=columnModel.hashCode()) {
            final TableCellRenderer originalRenderer = this.table.getTableHeader().getDefaultRenderer();

            DefaultTableCellRenderer newRenderer = null;
            for (int i=0; i< columnModel.getColumnCount(); i++) {
                newRenderer = new SortIconTableCellRenderer(table.getColumnModel().getColumn(i), originalRenderer);
                table.getColumnModel().getColumn(i).setHeaderRenderer(newRenderer);
            }
            lastColumnModelHash =  table.getColumnModel().hashCode();
        }


        int x = columnModel.getColumnIndexAtX(e.getX());
        if (x > -1) {
            final TableColumn tc = columnModel.getColumn(x);
            int sortedColummnIndex = tc.getModelIndex();
            if (sortedColummnIndex < 0) {
                    sortedColummn = null;
                    return;
            }
            if (!(table.getModel() instanceof ListBindingManager)) {
                throw new SystemException("A tabela não tem um ListBindingManager (não tem bind)");
            }
            final ListBindingManager model = ((ListBindingManager) table.getModel());
            order = (sortedColummn != tc) ? 1 : order*-1;
            table.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                    Collections.sort(model.getElements(), new GenericComparator(tc.getModelIndex(), order));
                    table.repaint();
                    table.getTableHeader().resizeAndRepaint();
                    } finally {
                        table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        sortedColummn = tc;
                    }
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    public void tableChanged(TableModelEvent tme) {
          if (sortedColummn != null) {
              if (tme.getColumn()==sortedColummn.getModelIndex() || tme.getColumn()==-1) {
                    sortedColummn = null;
                    table.getTableHeader().repaint();
              }
          }
    }

    /**
     * Classe SortIconTableCellRenderer
     */
    class SortIconTableCellRenderer extends DefaultTableCellRenderer {
        private TableColumn tableColumn;
        private TableCellRenderer originalRenderer;

        /**
         * Construtor
         * @param tableColumn Coluna
         * @param originalRenderer Renderer original
         */
        public SortIconTableCellRenderer(TableColumn tableColumn, TableCellRenderer originalRenderer) {
            this.tableColumn = tableColumn;
            this.originalRenderer = originalRenderer;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            DefaultTableCellRenderer c = (DefaultTableCellRenderer)originalRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (tableColumn == sortedColummn) {
                c.setIcon((order == 1) ? icon : icon2);
            } else {
                c.setIcon(null);
            }
            c.setHorizontalTextPosition(JLabel.LEFT);
            return c;
        }
    }

    /**
     * Classe de comparador genérico
     */
    private final class GenericComparator implements Comparator {
        private int columnIndex;
        private int ordem;
        // Evita o indexOf toda hora na lista
        private Map<Object, Integer> indexes; 

        /**
         * Construtor
         * @param modelIndex indice da coluna no modelo
         * @param order ordenação
         */
        private GenericComparator(int modelIndex, int order) {
            this.columnIndex = modelIndex;
            this.ordem = order;
            indexes = new HashMap<Object, Integer>();
        }

        /**
         * {@inheritDoc}
         */
        public int compare(Object o1, Object o2) {
            List l = ((ListBindingManager) table.getModel()).getElements();
            Integer p1 = indexes.get(o1);
            if (p1==null) {
                p1 = l.indexOf(o1);
                indexes.put(o1, p1);
            }
            Integer p2 = indexes.get(o2);
            if (p2==null) {
                p2 = l.indexOf(o2);
                indexes.put(o2, p2);
            }
            Object v1 = table.getModel().getValueAt(p1, columnIndex);
            Object v2 = table.getModel().getValueAt(p2, columnIndex);
            if (v1 == null && v2 == null) {
                return 0;
            }
            if (v1 == null && v2 != null) {
                return 1;
            }
            if (v1 != null && v2 == null) {
                return -1;
            }
            if (v1 instanceof Enum && v2 instanceof Enum){
                return v1.toString().compareTo(v2.toString()) * ordem;
            }
            if (v1 instanceof Comparable && v2 instanceof Comparable && !(v1 instanceof AbstractBean) && !(v2 instanceof AbstractBean)) {
                return ((Comparable) v1).compareTo(v2)  * ordem;
            }
            if (v1 != null && v2 != null) {
                return v1.toString().compareTo(v2.toString())  * ordem;
            }
            return 0;
        }
    }
}