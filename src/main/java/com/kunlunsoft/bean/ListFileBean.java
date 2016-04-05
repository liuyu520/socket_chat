package com.kunlunsoft.bean;

import java.io.File;
import java.io.Serializable;

public class ListFileBean implements Serializable {
    private static final long serialVersionUID = 4529136029973827277L;
    /***
     * current folder
     */
    private String path;
    private File[] files;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public File[] getFiles() {
        return files;
    }

    public void setFiles(File[] files) {
        this.files = files;
    }


}
