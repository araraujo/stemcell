package com.stemcell.swing.components.util;

import java.awt.Toolkit;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

/**
 * Document utilitário para limitar opções de digitação em campos de texto.
 */
public class GenericDocument extends PlainDocument {
    public static final int NO_MAX_SIZE_LIMIT = -1;
    public static final int OPTION_APPLY_UPPERCASE = 1;
    public static final int OPTION_REJECT_LETTERS = 2;
    public static final int OPTION_REJECT_NUMBERS = 4;
    public static final int OPTION_REJECT_SYMBOLS = 8;
    public static final int OPTION_REJECT_SPACE = 16;
    private int maxSize = NO_MAX_SIZE_LIMIT;
    private boolean applyUpperCase;
    private boolean rejectLetters;
    private boolean rejectNumbers;
    private boolean rejectSymbols;
    private boolean rejectSpace;

    /**
     * Este construtor pode ser usado para pasagem de parâmetros usando múltiplas opções
     * através de operações de bit.
     * Exemplo: <pre>new GenericDocument(10, GenericDocument.OPTION_REJECT_NUMBERS | GenericDocument.OPTION_REJECT_SPACE | GenericDocument.OPTION_REJECT_SYMBOLS )</pre>
     * @param maxSize Tamanho máximo da string
     * @param options Opções de limitação do texto
     */
    public GenericDocument(final int maxSize, int options) {
        this(maxSize);
        final int pos1 = 1;
        final int pos2 = 2;
        final int pos3 = 3;
        final int pos4 = 4;
        this.applyUpperCase = options % pos2 != 0;
        this.rejectLetters = (options >> pos1) % pos2 != 0;
        this.rejectNumbers = (options >> pos2) % pos2 != 0;
        this.rejectSymbols = (options >> pos3) % pos2 != 0;
        this.rejectSpace = (options >> pos4) % pos2 != 0;
    }

    /**
     * Construtor
     * @param maxSize Tamanho máximo da string
     * @param rejectNumbers Flag para rejeitar números
     */
    public GenericDocument(int maxSize, boolean rejectNumbers) {
        this(maxSize);
        this.rejectNumbers = rejectNumbers;
    }

    /**
     * Construtor
     * @param maxSize Tamanho máximo da string
     * @param rejectNumbers Flag para rejeitar números
     * @param rejectSpace Flag para rejeitar espaços
     */
    public GenericDocument(int maxSize, boolean rejectNumbers, boolean rejectSpace) {
        this(maxSize, rejectNumbers);
        this.rejectSpace = rejectSpace;
    }

    /**
     * Construtor
     * @param maxSize Tamanho máximo da string
     * @param rejectNumbers Flag para rejeitar números
     * @param rejectSpace Flag para rejeitar espaços
     * @param rejectSymbols Flag para rejeitar símbolos não textuais
     */
    public GenericDocument(int maxSize, boolean rejectNumbers, boolean rejectSpace, boolean rejectSymbols) {
        this(maxSize, rejectNumbers, rejectSpace);
        this.rejectSymbols = rejectSymbols;
    }

    /**
     * Construtor d
     * @param maxSize Tamanho máximo da string
     * @param rejectNumbers Flag para rejeitar números
     * @param rejectSpace para rejeitar espaços
     * @param rejectSymbols Flag para rejeitar símbolos não textuais
     * @param applyUpperCase  Flag para aplicar upper case
    /**
     * Construtor d
     * @param maxSize Tamanho máximo da string
     * @param rejectNumbers Flag para rejeitar números
     * @param rejectSpace para rejeitar espaços
     * @param rejectSymbols Flag para rejeitar símbolos não textuais
     * @param applyUpperCase  Flag para aplicar upper case
     */



    public GenericDocument(int maxSize, boolean rejectNumbers, boolean rejectSpace, boolean rejectSymbols, boolean applyUpperCase) {
        this(maxSize, rejectNumbers, rejectSpace, rejectSymbols);
        this.applyUpperCase = applyUpperCase;
    }

    /**
     * Construtor padrão
     * @param pMaxSize tamanho máximo
     */
    public GenericDocument(int pMaxSize) {
        this.maxSize = pMaxSize;
        
        setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string == null) {
                    super.insertString(fb, offset, null, attr);
                    return;
                }
                if (applyUpperCase) {
                    string = string.toUpperCase();
                }
                if (maxSize > -1 && string.length() + getLength() > maxSize) {
                    int size = (maxSize - getLength());
                    size = Math.min(size, string.length()) - 1;
                    // Este campo permite no máximo %s caracteres
                    reject();
                    return;
                }

                char c = ' ';
                for (int i = 0; i < string.length(); i++) {
                    c = string.charAt(i);
                    if (rejectLetters && Character.isLetter(c)) {
                        // Este campo não permite letras
                        reject();
                        return;
                    }
                    if (rejectNumbers && Character.isDigit(c)) {
                        // Este campo não permite números
                        reject();
                        return;
                    }
                    if (rejectSpace && Character.isWhitespace(c)) {
                        // Este campo não permite espaços
                        reject();
                        return;
                    }
                    if (rejectSymbols && (!Character.isLetter(c) && !Character.isDigit(c) && !Character.isWhitespace(c))) {
                        // Este campo não permite símbolos que não sejam caracteres textuais
                        reject();
                        return;
                    }
                }
                super.insertString(fb, offset, string, attr);
            }

            @Override
            public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String string, AttributeSet attrs) throws BadLocationException {
                if (string == null) {
                    super.replace(fb, offset, length, null, attrs);
                    return;
                }
                if (maxSize > -1 && string.length() + (getLength() - length) > maxSize) {
                    // Este campo permite no máximo %s caracteres
                    reject();
                    return;
                }

                char c = ' ';
                for (int i = 0; i < string.length(); i++) {
                    c = string.charAt(i);
                    if (rejectLetters && Character.isLetter(c)) {
                        // Este campo não permite letras
                        reject();
                        return;
                    }
                    if (rejectNumbers && Character.isDigit(c)) {
                        // Este campo não permite números
                        reject();
                        return;
                    }
                    if (rejectSpace && Character.isWhitespace(c)) {
                        // Este campo não permite espaços
                        reject();
                        return;
                    }
                    if (rejectSymbols && (!Character.isLetter(c) && !Character.isDigit(c) && !Character.isWhitespace(c))) {
                        // Este campo não permite símbolos que não sejam caracteres textuais
                        reject();
                        return;
                    }
                }

                if (applyUpperCase) {
                    super.replace(fb, offset, length, string.toUpperCase(), attrs);
                } else {
                    super.replace(fb, offset, length, string, attrs);
                }
            }
        });
    }

    /**
     * Factory que retorna uma nova instancia de document
     * com as opcoes padrao, aceitando apenas letras
     * @return uma nova instancia de document que aceita apenas letras
     */
    public static GenericDocument acceptOnlyLetters() {
        return new GenericDocument(NO_MAX_SIZE_LIMIT, OPTION_REJECT_NUMBERS | OPTION_REJECT_SYMBOLS | OPTION_REJECT_SPACE);
    }


    /**
     * Factory que retorna uma nova instancia de document
     * com definindo um tamanho maximo
     * @param maxSize definicao do tamanho maximo do documento
     * @return uma nova instancia de document com tamanho maximo definido
     */
    public static GenericDocument acceptOnlyLettersWithMaxSize(int maxSize) {
        return new GenericDocument(maxSize, OPTION_REJECT_NUMBERS | OPTION_REJECT_SYMBOLS | OPTION_REJECT_SPACE);
    }

    /**
     * Factory que retorna uma nova instancia de document
     * com as opcoes padrao, aceitando apenas numeros
     * @return uma nova instancia de document que aceita apenas numeros
     */
    public static GenericDocument acceptOnlyNumbers() {
        return new GenericDocument(NO_MAX_SIZE_LIMIT, OPTION_REJECT_LETTERS | OPTION_REJECT_SYMBOLS | OPTION_REJECT_SPACE);
    }

    /**
     * Factory que retorna uma nova instancia de document
     * aceitando apenas numeros e definindo um tamnho limite
     * @param maxSize  definicao do tamanho maximo do documento
     * @return uma nova instancia de document com tamanho maximo definido
     * formado apenas por numeros
     */
    public static GenericDocument acceptOnlyNumbersWithMaxSize(int maxSize) {
        return new GenericDocument(maxSize, OPTION_REJECT_LETTERS | OPTION_REJECT_SYMBOLS | OPTION_REJECT_SPACE);
    }

    /**
     * Código que roda na rejeição a uma string
     *
     */
    private void reject() {
        Toolkit.getDefaultToolkit().beep();
    }
}
