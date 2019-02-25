package com.stemcell.swing.components;

import javax.swing.JTextField;

public class STextField extends JTextField {
    private String refreshingText;

    public String getRefreshingText() {
        return refreshingText;
    }

    /**
     * Define o texto forçando a atualização da propriedade text
     * @param refreshingText refreshingText
     */
    public void setRefreshingText(String refreshingText) {
        Object old = this.refreshingText;
        this.refreshingText = refreshingText;
        firePropertyChange("refreshingText", old, this.refreshingText);

        if (!checkEqualsNullSafe(getText(), refreshingText)) {
            setText(refreshingText);
        }
    }

     /**
     * Verifica se 2 objetos sÃ£o iguais. Considerando a possibilidade de serem null
     * @param o1 O primeiro objeto.
     * @param o2 O segundo objeto.
     * @return true, se ambos sÃ£o iguais.
     */
    private static boolean checkEqualsNullSafe(Object o1, Object o2) {
        if (o1 != null && o2 != null) {
            return o1.equals(o2);
        }
        return (o1 == null) == (o2 == null);
    }
}
