package com.kunlunsoft.bean;

import java.io.Serializable;

/***
 * client environment status.
 *
 * @author huangwei
 * @since 2013-10-23
 */
public class ClientEnvStatusBean implements Serializable {
    private static final long serialVersionUID = -806064209174088982L;
    private String currentDirectory;
    /***
     * previous current directory
     */
    private String previousDirectory;


    public String getCurrentDirectory() {
        return currentDirectory;
    }

    public void setCurrentDirectory(String currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

    public String getPreviousDirectory() {
        return previousDirectory;
    }

    public void setPreviousDirectory(String previousDirectory) {
        this.previousDirectory = previousDirectory;
    }


}
