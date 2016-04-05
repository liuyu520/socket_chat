package com.kunlunsoft.app;

import com.cmd.dos.hw.util.CMDUtil;
import com.common.util.SystemHWUtil;
import com.kunlunsoft.dict.Constants;
import com.kunlunsoft.thread.ServerThread;
import com.swing.component.AssistPopupTextPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

public class Build2TestApp extends ServerGenericFrame {

    private JPanel contentPane;
    private AssistPopupTextPane resultTextPane;
    private JButton webStaticButton_1;
    private JButton serverButton;

    /**
     * Create the frame.
     */
    public Build2TestApp() {
        setTitle("开发环境部署");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(0, 0));

        JPanel panel = new JPanel();
        contentPane.add(panel, BorderLayout.NORTH);

        serverButton = new JButton("官网后台");
        serverButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
//				sendCmd_parseResult("cd /home/oliangdd/bin");
                /*try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}*/
                sendCmd_parseResult("loca sudo ./update_server", serverButton);
            }
        });
        panel.add(serverButton);

        webStaticButton_1 = new JButton("官网前台");
        webStaticButton_1.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
//				sendCmd_parseResult("cd /home/oliangdd/bin");
				/*try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}*/
                sendCmd_parseResult("loca sudo ./update_web_js", webStaticButton_1);
            }
        });
        panel.add(webStaticButton_1);

        JScrollPane scrollPane = new JScrollPane();
        contentPane.add(scrollPane, BorderLayout.CENTER);

        resultTextPane = new AssistPopupTextPane();
        resultTextPane.setEditable(false);
        scrollPane.setViewportView(resultTextPane);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
					 /*while (!SocketHWUtil.isOcupy(clientDaemonPort)) {
					 Thread.sleep(1000);
					 }*/

                    boolean isConnected = establishConnection();
                    if (isConnected) {
                        System.out.println("connected client daemon successfully.");
                    }
                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                } catch (IOException e2) {
                    e2.printStackTrace();
                } /*
				 * catch (InterruptedException e) { e.printStackTrace(); }
				 */
            }
        }).start();
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Build2TestApp frame = new Build2TestApp();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /***
     * 发送命令，并且解析命令执行结果
     */
    protected void sendCmd_parseResult(String command1, JButton exeBtn) {
        // String command1 = commandTextField.getText();
        if (command1.matches("[ ]*cd[ ]+..[ ]*")) {
            command1 = Constants.COMMAND_CDUP;
        }
        command1 = command1.replaceAll("^cd ", "cwd ");
        System.out.println("command:" + command1);
        String realCommandAndArg = CMDUtil.getRealMessage(command1,
                Constants.TYPE_COMMAND);
        String realCommand = realCommandAndArg.split(" ")[0];
        System.out.println("server real command:" + realCommand);
        // if (realCommand.equals(Constants.COMMAND_RETR)) {
        // DialogBean dialogBean = DialogUtil.showSaveDialog(null,
        // null, null);
        // if (dialogBean.isSuccess()) {
        // System.out.println("save path:"
        // + dialogBean.getSelectedFile()
        // .getAbsolutePath());
        // serverThread.setLatestRetrPath(dialogBean.getSelectedFile());
        // } else {
        // return;
        // }
        // }
        // serverThread.continueListen();
        count2++;
        send(Constants.TYPE_COMMAND + " " + command1);
        if (client != null) {
            System.out.println("client.isClosed():" + client.isClosed());
            exeBtn.setEnabled(false);
            serverThread = new ServerThread(client, resultTextPane, exeBtn, SystemHWUtil.CHARSET_UTF);// JTextPane
            serverThread.setLatestRetrPath(new File("d:\\Temp\\a\\a\\b\\ab"
                    + count2 + ".jar"));
            // resultTP
            new Thread(serverThread).start();
            // DialogUtil.focusSelectAllTF(commandTextField);
        }
    }
}
