package com.stemcell.swing.components.util;

import com.stemcell.common.beans.AbstractBean;
import com.stemcell.swing.components.action.ExcelExportAction;
import com.stemcell.swing.components.renderers.TableCellRendererDecorator;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.LineMetrics;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;


/**
 * Utilitários para JTables
 */
// Classe utilitária nomeada no plural
@SuppressWarnings("PMD.Regra 5") 
public abstract class TableUtils {
    /**
     * Construtor
     */
    private TableUtils() {

    }

    /**
     * Configura suporte a tabela com cabeçalhos multi-linha.
     * A quebra de linha é definida adicionando um '|' no texto do cabeçalho
     * @param table Tabela
     */
    public static void configureMultilineHeader(JTable table) {
        JTableHeader header = table.getTableHeader();
        TableColumnModel columnModel = header.getColumnModel();
        boolean multiline = false;
        String title = null;
        TableColumn column = null;
        final String separadorDeLinhas = "|";
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            column = columnModel.getColumn(i);
            title = column.getHeaderValue().toString();
            if (title.contains(separadorDeLinhas)) {
                multiline = true;
                column.setHeaderValue(String.format("<html>%s</html>", title.replace(separadorDeLinhas, "<br>")));
            }
            
        }

        if (multiline) {
            LineMetrics metrics = header.getFont().getLineMetrics("z", new java.awt.font.FontRenderContext(null, false, false));
            float height = metrics.getHeight();
            final int dobro = 2;
            final int espacamento = 5;
            Dimension dim = new Dimension((int)header.getSize().getWidth(), (int)(height * dobro + espacamento));
            header.setSize(dim);
            header.setPreferredSize(dim);
        }
    }

    /**
     * Aplica decorador de linhas numa tabela, sem remover comportamento do renderer original
     * @param table Tabela
     * @param decorator Decorator
     */
    public static void applyDecorator(JTable table, TableCellRendererDecorator decorator) {
        TableCellRendererDecorator.applyTo(table, decorator);
    }

    /**
     * Configura implementação de ordenamento baseado no binding de tabelas
     * @param tables Tabelas
     */
    public static void configureColumnSort(JTable ... tables) {
        TableListSorterMouseListener.install(tables);
    }

    /**
     * Cria ação de exportação excel
     * @param table Tabela
     * @return Ação de exportação
     */
    public static Action createExcelExportAction(JTable table) {
        return new ExcelExportAction(table);
    }

    /**
     * Configura ação de exportação excel
     * @param table Tabela
     */
    public static void configureExcelExportPopup(JTable table) {
        Action a = new ExcelExportAction(table);
        JPopupMenu menu = table.getComponentPopupMenu();
        if (menu==null) {
            menu = new JPopupMenu();
        }
        menu.add(a);
        table.setComponentPopupMenu(menu);
    }
    
    /**
     * Faz com que o clique com o botão direito selecione a linha correspondente na tabela
     * ATENÇÃO: O ComponentPopupMenu não deve ser setado após esta chamada, pois o evento não é repassado
     * @param table tabela
     */
    public static void configureRightClickSelectsRow(final JTable table) {
        final JPopupMenu popup = table.getComponentPopupMenu();
        table.setComponentPopupMenu(null);
        
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (SwingUtilities.isRightMouseButton(evt)) {
                    int index = table.rowAtPoint(evt.getPoint());
                    table.getSelectionModel().setSelectionInterval(index, index);
                    if (popup !=null) {
                        popup.show(table, evt.getX(), evt.getY());
                    }
                }
            }
        });
    }

    /**
     * Configura manutenção da última seleção em binds de tabela que são dinamicamente
     * carregadas
     * @param table tabela
     * @param model Objeto modelo
     * @param listProperty Propriedade que provê objetos da tabela no model
     * @param selectionProperty Propriedade que provê seleção da tabela no model
     */
    public static void configureMantainLastSingleSelection(final JTable table, final AbstractBean model, final String listProperty, final String selectionProperty) {
        model.addPropertyChangeListener(new PropertyChangeListener() {
            private Object ultimoSelecionado = null;
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(listProperty)) {
                    if (null != evt.getNewValue() && null == evt.getOldValue() && null != ultimoSelecionado) {
                        int index = ((List)evt.getNewValue()).indexOf(ultimoSelecionado);
                        table.getSelectionModel().setSelectionInterval(index, index);
                    }
                }
                if (evt.getPropertyName().equals(selectionProperty)) {
                    if (null != evt.getNewValue()) {
                        ultimoSelecionado = evt.getNewValue();
                    }
                }
            }
        });
    }

     /**
     * Aplica cores na célula selecionada/desselecionada
     * @param component Componente
     * @param color1 Cor 1
     * @param color2 Cor 2
     * @param isSelected indicador de seleção
     */
    public static void colorizeCell(JComponent component, Color color1, Color color2, boolean isSelected) {
        if (isSelected) {
            component.setBackground(color1);
            component.setForeground(color2);
        } else {
            component.setBackground(color2);
            component.setForeground(color1);
        }
    }

    /**
     * Aplica cores na célula selecionada/desselecionada
     * @param component Componente
     * @param color1 Cor 1
     * @param color2 Cor 2
     * @param bold Indicador de negrito
     * @param isSelected Indicador de seleção
     */
    public static void colorizeCell(JComponent component, Color color1, Color color2, boolean bold, boolean isSelected) {
        if (bold) {
            component.setFont(component.getFont().deriveFont(Font.BOLD));
        }
        if (isSelected) {
            component.setBackground(color1);
            component.setForeground(color2);
        } else {
            component.setBackground(color2);
            component.setForeground(color1);
        }
    }

}
