/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stemcell.common.exception;

import com.stemcell.common.i18n.I18nManager;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Uma mensagem de erro que será armazenada em uma PendingException.
 * O texto da mensagem pode estar internacionalizado ou não.
 */
public class Pendency implements Serializable {
    private static final long serialVersionUID = 1L;
    private String message;
    private Object[] additionalData;

    public Pendency(String message, Object... additionalData) {
        this.message = message;
        this.additionalData = additionalData;
    }

    public String getMessage() {
        return message;
    }

    public String getLocalizedMessage() {
        try {
            String localizedMsg = null;
            if (null != additionalData && additionalData.length > 0) {
                localizedMsg = I18nManager.getString(message, additionalData);
            } else {
                localizedMsg = I18nManager.getString(message);
            }

            if (null != localizedMsg && !localizedMsg.contains( message )) {
                return localizedMsg;
            } else {
                return message;
            }
        }
        catch (RuntimeException e) {
            return message;
        }
    }

    public Object[] getAdditionalData() {
        Object[] returnData = additionalData;
        if (returnData != null) {
            returnData = Arrays.copyOf(additionalData, additionalData.length);
        }
        return returnData;
    }
}
