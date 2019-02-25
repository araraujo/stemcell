package com.stemcell.swing.components.action;

import com.stemcell.common.exception.BusinessException;
import com.stemcell.common.exception.SystemException;
import com.stemcell.common.i18n.I18nManager;
import com.stemcell.swing.components.util.TableUtils;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableModel;


/**
 * Ação que exporta automaticamente o conteúdo de um JTable para um
 * arquivo xls em formato HTML
 */
public class ExcelExportAction extends AbstractAction {
    private static final String EXCEL_EXTENSION = ".xls";
    private JTable table;

    /**
     * Construtor
     * @param table tabela fonte
     */
    public ExcelExportAction(JTable table) {
        this.initializeProperties();
        this.table = table;
    }

    /**
     * Construtor
     */
    public ExcelExportAction() {
        this(null);
    }

    public JTable getTable() {
        return table;
    }

    /**
     * Define a tabela a ser exportada pela ação
     * @param table tabela a ser exportada pela ação
     */
    public void setTable(JTable table) {
        this.table = table;
        TableUtils.configureExcelExportPopup(table);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        JFileChooser dialog = new JFileChooser();
        dialog.setMultiSelectionEnabled(false);
        dialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
        dialog.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() || file.toString().toLowerCase().endsWith(EXCEL_EXTENSION);
            }
            @Override
            public String getDescription() {
                return ".xls (Excel)";
            }
        });

        if (dialog.showSaveDialog(table) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        
        FileOutputStream fout=null;
        String s = dialog.getSelectedFile().toString();
        if (!s.toLowerCase().endsWith(EXCEL_EXTENSION)) {
            s = String.format("%s%s", s, EXCEL_EXTENSION);
        }
        
        try {
            fout = new FileOutputStream(s);
            PrintStream out = new PrintStream(fout);
            TableModel model = table.getModel();

            out.println("<html><body><table border=\"1\"><tr>");
            for (int i = 0; i < model.getColumnCount(); i++) {
                out.println("<th>");
                out.println(table.getColumnModel().getColumn(i).getHeaderValue());
                out.println("</th>");
            }
            out.println(" </tr>");

            String format = null;
            String value = null;
            Object obj = null;
            int j = 0;
            for (int i = 0; i < model.getRowCount(); i++) {
                out.println("<tr>");
                for (j=0; j<model.getColumnCount(); j++) {
                    obj = model.getValueAt(i, j);
                    value  = (obj == null) ? "" : obj.toString();
                    format = "\\@";
                    if (obj instanceof Number) {
                        value = NumberFormat.getNumberInstance(Locale.getDefault()).format(obj);
                        format = "Fixed";
                    } else if (obj instanceof Date) {
                        value = dateFormat.format((Date)obj);
                        format = "'dd/mm/yyyy HH:mm'";
                    }
                    value = value.replaceAll("<html>", "")
                                    .replaceAll("<body>", "")
                                    .replaceAll("</html>", "")
                                    .replaceAll("</body>", "")
                                    .replaceAll("<b>", "")
                                    .replaceAll("</b>", "")
                                    .replaceAll("<", "&lt;")
                                    .replaceAll(">", "&gt;");

                    out.println("<td style=\"mso-number-format:");
                    out.println(format);
                    out.println("\">");
                    out.println(value);
                    out.println("</td>");
                }
                out.println("</tr>");
            }
            out.println("</table></body></html>");
            out.close();

            Desktop.getDesktop().open(new File(s));
        } catch (IOException ex) {
            throw new BusinessException(I18nManager.getString("swing.components.cannotGenerateFile"), ex);
        }
    }

    private void initializeProperties() {
        String name;
        try {
            name = I18nManager.getString("swing.components.excelExport");
        } catch(SystemException ex) {
            name = "<ExcelExportAction>";
        }
        putValue(Action.NAME, name);
        putValue(Action.SMALL_ICON, new ImageIcon(ExcelExportAction.class.getResource("/icons/page_excel.png")));
    }

}
