package com.stemcell.common.beans;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Value Object of Transporting User Data Security
 */
public class CredentialsBean implements Serializable {
    /**
     * Stores the logon of an authenticated user
     */
    private String logon;
    /**
     * Stores the name of an authenticated user
     */
    private String username;
    /**
     * Stores the access control features allowed to the user in order to 
     * transport between server and remote client
     * 
     */
    private Set<String> securityResources;

    /**
     * Construtor
     */
    public CredentialsBean() {
    }

    /**
     * Construtor
     * @param logon 
     * @param username 
     * @param securityResources 
     */
    public CredentialsBean(String logon, String username, Set securityResources) {
        this.logon = logon;
        this.username = username;
        this.securityResources = new HashSet<String>(securityResources);
    }

    /**
     * @return logon
     */
    public String getLogon() {
        return logon;
    }

    /**
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     *  
     */
    public boolean isAuthenticated() {
        return username != null;
    }

    /**
     * 
     */
    public Set<String> getSecurityResources() {
        return Collections.unmodifiableSet(securityResources);
    }

    public boolean verifyAnySecurityResource(Collection<String> asList) {
        for (String obj : asList) {
            if (securityResources.contains(obj)) {
                return true;
            }
        }
        return false;
    }

    public boolean verifySecurityResource(String resource) {
        return securityResources.contains(resource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return (username != null) ? username : "";
    }
}