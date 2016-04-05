package com.kunlunsoft.listen;

import com.common.util.SystemHWUtil;
import com.kunlunsoft.app.ClientChatApp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/***
 * listen to menu.
 *
 * @author huangwei
 */
public class MyMenuBarActionListener implements ActionListener {
    private ClientChatApp swingApp = null;

    public MyMenuBarActionListener(ClientChatApp swingApp) {
        super();
        this.swingApp = swingApp;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();

        // System.out.println(command);
        if (command.equals(SystemHWUtil.CHARSET_UTF)
                || command.equals(SystemHWUtil.CHARSET_GBK)
                || command.equals(SystemHWUtil.CHARSET_GB2312)
                || command.equals(SystemHWUtil.CHARSET_GB18030)) {
            System.out.println(command);
            ClientChatApp.setEncoding(command);
        } else if (command.equals("启动socket服务")) {// clear pass data
            try {
                ClientChatApp.ftpServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (command.equals("复制配置内容")) {// clear pass data
        } else if (command.equals("修改请求ID")) {// clear pass data

        }
    }

}
