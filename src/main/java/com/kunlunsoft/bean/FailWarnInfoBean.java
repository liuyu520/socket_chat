package com.kunlunsoft.bean;

import java.io.Serializable;

public class FailWarnInfoBean implements Serializable {
    private static final long serialVersionUID = -2207138633427814709L;
    private String code = null;
    private String message = null;
    private String command = null;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }


}
