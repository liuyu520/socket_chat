package com.kunlunsoft.bean;

public class ConfigBean {
    private String encoding;
    /***
     * 是否已经启动了socket 服务
     */
    private boolean isStarted;

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean isStarted) {
        this.isStarted = isStarted;
    }

}
