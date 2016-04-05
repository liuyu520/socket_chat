package com.kunlunsoft.bean;

import java.io.Serializable;

/***
 * used for retr and stat
 *
 * @author huangwei
 * @since 2013-10-23
 */
public class FileBean2 implements Serializable {
    private static final long serialVersionUID = 7216555000722969807L;
    private String fileName;
    /***
     * 文件有多大，1MB
     */
    private long sizeOfFile;
    /***
     * 有多少个文件
     */
    private int quantityOfFile;
    /***
     * 是否是文件夹
     */
    private boolean isFolder;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getSizeOfFile() {
        return sizeOfFile;
    }

    public void setSizeOfFile(long sizeOfFile) {
        this.sizeOfFile = sizeOfFile;
    }

    public int getQuantityOfFile() {
        return quantityOfFile;
    }

    public void setQuantityOfFile(int quantityOfFile) {
        this.quantityOfFile = quantityOfFile;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setFolder(boolean isFolder) {
        this.isFolder = isFolder;
    }


}
