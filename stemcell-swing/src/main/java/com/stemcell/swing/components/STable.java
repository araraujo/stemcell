package com.stemcell.swing.components;

import com.stemcell.swing.components.action.ExcelExportAction;
import com.stemcell.swing.components.table.MultiLineHeaderRendererProxy;
import com.stemcell.swing.components.util.TableListSorterMouseListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.html.HTMLEditorKit;

/**
 * JTable que implementa uma série de funcionalidades adicionais como:
 * <ul>
 *  <li>
 *      Copiar linhas para a área de transferência. Ctrl+C com a tabela em foco
 *      copia as linhas selecionadas. Ctrl+Shift+C copia todas as linhas. O
 *      formato da cópia é compatível para colar em aplicativos de planilha
 *  </li>
 *  <li>
 *      Exportação de dados para excel (Ver excelExportEnabled)
 *  </li>
 *  <li>
 *      Cabeçalhos com várias linhas. (Defina os títulos das colunas  com um '|'
 *      para quebrar linhas e defina a propriedade autoPopupColumns)
 *  </li>
 *  <li>
 *      Autopopup de células com conteúdos muito extensos (ver propriedade autoPopupColumns)
 * </li>
 *  <li>
 *      Ordenação de coluna ao clicar no título, baseada na ordenação de listas
 *      com binding para 'elements' da tabela
 *  </li>
 * </ul>
 */
public class STable extends JTable {
    private static final int DEFAULT_PACK_MARGIN = 4;
    
    private Action excelExportAction;
    private boolean excelExportEnabled;
    private TableListSorterMouseListener sortListener;
    private int packMargin = DEFAULT_PACK_MARGIN;
    private int[] autoPopupColumns;
    private Popup autoPopup;
    private int lastPopupColumn = -1;
    private int lastPopupRow = -1;

    /**
     * Construtor
     */
    public STable() {
        // Seta o multiline header
        getTableHeader().setDefaultRenderer(new MultiLineHeaderRendererProxy(getTableHeader().getDefaultRenderer()));
        ToolTipManager.sharedInstance().unregisterComponent(this);
        ToolTipManager.sharedInstance().unregisterComponent(getTableHeader());
        
        // Suporte a Control+C
        ConfigurableAction actionCopy = new ConfigurableAction(null,"Copiar Seleção", "copySelection", this);
        actionCopy.setShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        actionCopy.registerInto(this);

        // Suporte a Shift+Control+C
        ConfigurableAction actionCopyAll = new ConfigurableAction(null,"Copiar tudo", "copyAll", this);
        actionCopyAll.setShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        actionCopyAll.registerInto(this);

        // força o fim do modo de edição quando a tabela perde o foco
        this.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    }

    public boolean isExcelExportEnabled() {
        return excelExportEnabled;
    }

    public Action getExcelExportAction() {
        return excelExportAction;
    }

    /**
    * Define se a tabela suportará exportação para excel no menu popup
    * @param excelExportEnabled excelExportEnabled
    */
    public void setExcelExportEnabled(boolean excelExportEnabled) {
        if (excelExportEnabled) {
            excelExportAction = new ExcelExportAction(this);
            JPopupMenu menu = getComponentPopupMenu();
            if (menu==null) {
                menu = new JPopupMenu();
            }
            menu.add(excelExportAction);
            setComponentPopupMenu(menu);
        } else {
            throw new UnsupportedOperationException("Remover ação excel não implementado");
        }

        Object old = this.excelExportEnabled;
        this.excelExportEnabled = excelExportEnabled;
        firePropertyChange("excelExportEnabled", old, this.excelExportEnabled);
    }

    /**
     * Define se a tabela terá suporte a ordenação via binding
     * @param sortable sortable
     */
    public void setSortable(boolean sortable) {
        boolean old = this.isSortable();
        if (sortable) {
            if (sortListener==null) {
                sortListener = new TableListSorterMouseListener(this);
                getTableHeader().addMouseListener(sortListener);
            }
        } else {
            if (sortListener!=null){
                getTableHeader().removeMouseListener(sortListener);
            }
            sortListener = null;
        }
        firePropertyChange("sortable", old, sortable);
    }

    public boolean isSortable() {
        return sortListener != null;
    }

    /**
     * Copia as linhas selecionadas para a área de transferência
     */
    public void copySelection() {
        copy(selectionModel.getMinSelectionIndex(),  selectionModel.getMaxSelectionIndex());
    }

    /**
     * Copia todas as linhas para a área de transferência
     */
    public void copyAll() {
        copy(0,  dataModel.getRowCount()-1);
    }

    /**
     * Copia a faixa de linhas selecionadas para a área de transferência
     * @param rowIni linha inicial
     * @param rowFim linha final
     */
    public void copy(int rowIni, int rowFim) {
         StringBuilder sb = new StringBuilder();
         int j = 0;
         
         for (int i = rowIni; i <= rowFim; i++) {
             for (j = 0; j < dataModel.getColumnCount(); j++) {
                sb.append(dataModel.getValueAt(i, j));
                if (j<dataModel.getColumnCount()-1) {
                    sb.append("\t");
                }
             }
             sb.append("\r");
        }
        StringSelection selection = new StringSelection(sb.toString());

        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        cb.setContents(selection, selection);
    }

    /**
     * Redimensiona a largura das colunas e a altura das linhas para
     * atender ao tamanho preferido do conteúdo exibido na tabela.
     */
    public void packAll() {
        packColumns();
        packRows();
    }

    /**
     * Redimensiona a altura das linhas para atender ao tamanho
     * preferido do conteúdo exibido na tabela.
     */
    public void packRows() {
        int j = 0;
        int h = 0;
        int height = 0;
        final int packMarginMultiplier = 2;
        TableCellRenderer renderer = null;
        Component comp = null;
        for (int i=0; i<getRowCount(); i++) {
            // Get the preferred height
            height = getRowHeight();

            // Determine highest cell in the row
            for (j=0; j<getColumnCount(); j++) {
                renderer = getCellRenderer(i, j);
                comp = prepareRenderer(renderer, i, j);
                h = comp.getPreferredSize().height + packMarginMultiplier*packMargin;
                height = Math.max(height, h);
            }

            // Now set the row height using the preferred height
            if (getRowHeight(i) != height) {
                setRowHeight(i, height);
            }
        }
    }

    /**
     * Redimensiona a largura das colunas para atender ao tamanho
     * preferido do conteúdo exibido na tabela.
     */
    public void packColumns() {
        for (int i = 0; i < getColumnCount(); i++) {
            packColumn(i);
        }
    }

    /**
     * Redimensiona a largura da coluna selecionada para atender ao tamanho
     * preferido do conteúdo exibido na tabela.
     * @param i indice da coluna
     */
    public void packColumn(int i) {
        TableColumn c = getColumnModel().getColumn(i);
        int column = convertColumnIndexToView(c.getModelIndex());
        int width = 0;

        TableCellRenderer headerRenderer = this.getTableHeader().getDefaultRenderer();
        if (headerRenderer != null) {
            Component comp = headerRenderer.getTableCellRendererComponent(this, c.getHeaderValue(), false, false, 0, column);
            width = comp.getPreferredSize().width;
        }
        TableCellRenderer renderer = this.getCellRenderer(c);
        Component comp = null;
        for (int j = 0; j < getRowCount(); j++) {
            comp = renderer.getTableCellRendererComponent(this, getValueAt(j, column), false, false, j, column);
            width = Math.max(width, comp.getPreferredSize().width);
        }

        final int margin = 2;
        width += margin * packMargin;
        c.setPreferredWidth(width);
    }

    /**
     * Retorna o cellRenderer da coluna
     * @param column column
     * @return cellRenderer
     */
    protected TableCellRenderer getCellRenderer(TableColumn column) {
        int viewIndex = this.convertColumnIndexToView(column.getModelIndex());
        if (viewIndex >= 0) {
            return this.getCellRenderer(0, viewIndex);
        }

        TableCellRenderer renderer = column.getCellRenderer();
        if (renderer == null) {
            renderer = this.getDefaultRenderer(this.getModel().getColumnClass(column.getModelIndex()));
        }
        return renderer;
    }

    /**
     * Define um conjunto de colunas que apresentarão popups com o conteúdo, quando
     * o mesmo não cabe completamente em uma linha da tabela
     * @param autoPopupColumns lista de colunas com popup habilitado
     */
    public void setAutoPopupColumns(int ... autoPopupColumns) {
        for (MouseMotionListener mouseMotionListener : getMouseMotionListeners()) {
            if (mouseMotionListener instanceof AutoPopupMouseMotionListener) {
                removeMouseMotionListener(mouseMotionListener);
            }
        }
        
        Object old = this.autoPopupColumns;
        this.autoPopupColumns = autoPopupColumns;
        firePropertyChange("autoPopupColumns", old, this.autoPopupColumns);

        if (this.autoPopupColumns!=null) {
            addMouseMotionListener(new AutoPopupMouseMotionListener());
        }
    }

    /**
     *  Classe AutoPopupMouseMotionListener
     */
    class AutoPopupMouseMotionListener extends MouseAdapter {

        private JPanel panel;
        private JTextPane text;

        /**
         * Construtor
         */
        public AutoPopupMouseMotionListener() {
            panel = new JPanel();
            panel.setForeground(SystemColor.infoText);
            panel.setBackground(SystemColor.info);
            panel.setOpaque(true);
            panel.setLayout(new BorderLayout());
            text = new JTextPane();
            text.setEditorKit(new HTMLEditorKit());
            text.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            text.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    if (autoPopup != null) {
                        autoPopup.hide();
                        autoPopup = null;
                    }
                }
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (autoPopup != null) {
                        autoPopup.hide();
                        autoPopup = null;
                    }
                }
            });
            text.setFont(UIManager.getDefaults().getFont("Label.font"));
            panel.add(new JScrollPane(text));
        }


        /**
         * {@inheritDoc}
         */
        public void mouseMoved(MouseEvent e) {
            if (autoPopupColumns == null) {
                return;
            } else {
                final int maxPopupWidth = 300;
                final int defaultPopupHeight = 300;
                int columnAtPoint = columnAtPoint(e.getPoint());
                int col = getColumnModel().getColumn(columnAtPoint).getModelIndex();
                int row = rowAtPoint(e.getPoint());
                if (autoPopup == null) {
                    Rectangle rect = null;
                    for (int i = 0; i < autoPopupColumns.length; i++) {
                        if (col == autoPopupColumns[i]) {
                            //Component comp = getCellRenderer(row, columnAtPoint).getTableCellRendererComponent(STable.this, getValueAt(row, col), false, false, row, col);
                            text.setText(String.valueOf(getModel().getValueAt(row, col)));
                            rect = getCellRect(row, columnAtPoint, true);
                            panel.setPreferredSize(new Dimension(Math.max(maxPopupWidth, rect.width), defaultPopupHeight));
                            autoPopup = PopupFactory.getSharedInstance().getPopup((Component)null, panel, getLocationOnScreen().x+rect.x, getLocationOnScreen().y+rect.y);
                            autoPopup.show();
                            lastPopupColumn = col;
                            lastPopupRow = row;
                        }
                    }
                } else {
                    if (col != lastPopupColumn || lastPopupRow != row) {
                        autoPopup.hide();
                        autoPopup = null;
                    }
                }
            }
        }
    }
}
