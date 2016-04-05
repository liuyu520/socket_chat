package com.kunlunsoft.app;

import com.cmd.dos.hw.util.CMDUtil;
import com.common.util.SystemHWUtil;
import com.kunlunsoft.dict.Constants;
import com.kunlunsoft.thread.ServerThread;
import com.swing.component.UndoTextArea;
import com.swing.component.UndoTextField;
import com.swing.dialog.DialogUtil;
import com.swing.menu.MenuUtil2;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/***
 * @author huangwei
 * @since 2013-10-23
 */
public class ServerChatApp extends ServerGenericFrame {

    private static final long serialVersionUID = -4332189071650937851L;
    private JPanel contentPane;
    private JTextField commandTextField;
    private JTextArea statusTA;

    private JTextPane resultTP;
    private JButton executeBtn;
    private JComboBox<String> encodingComboBox;

    /**
     * Create the frame.
     */
    public ServerChatApp() {
        setTitle("Server 1.0");

        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    closeSocket();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                super.windowClosing(e);
            }
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 885, 448);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        contentPane.add(tabbedPane, BorderLayout.CENTER);

        JPanel panel = new JPanel();
        tabbedPane.addTab("chat tab", null, panel, null);

        JPanel panel_1 = new JPanel();
        tabbedPane.addTab("command tab", null, panel_1, null);
        panel_1.setLayout(new BorderLayout(0, 0));
        tabbedPane.setSelectedIndex(1);

        JPanel panel_2 = new JPanel();
        panel_2.setSize(200, 500);
        panel_1.add(panel_2, BorderLayout.NORTH);
        GridBagLayout gbl_panel_2 = new GridBagLayout();
        gbl_panel_2.columnWidths = new int[]{0, 0, 0, 0, 0};
        gbl_panel_2.rowHeights = new int[]{0, 0, 0};
        gbl_panel_2.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0,
                Double.MIN_VALUE};
        gbl_panel_2.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        panel_2.setLayout(gbl_panel_2);

        JLabel commandLabel = new JLabel("command:");
        GridBagConstraints gbc_commandLabel = new GridBagConstraints();
        gbc_commandLabel.insets = new Insets(0, 0, 5, 5);
        gbc_commandLabel.gridx = 1;
        gbc_commandLabel.gridy = 0;
        panel_2.add(commandLabel, gbc_commandLabel);

        commandTextField = new UndoTextField();
        MenuUtil2.setPopupMenu(commandTextField);
        commandTextField.setText("loca mvn package");// retr
        // D:\\bin\\divideFile-0.0.1-SNAPSHOT.jar
        GridBagConstraints gbc_commandTextField = new GridBagConstraints();
        gbc_commandTextField.insets = new Insets(0, 0, 5, 0);
        gbc_commandTextField.fill = GridBagConstraints.HORIZONTAL;
        gbc_commandTextField.gridx = 3;
        gbc_commandTextField.gridy = 0;
        panel_2.add(commandTextField, gbc_commandTextField);
        commandTextField.setColumns(10);
        commandTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendCmd_parseResult();
            }
        });

        JPanel panel_3 = new JPanel();
        GridBagConstraints gbc_panel_3 = new GridBagConstraints();
        gbc_panel_3.gridwidth = 3;
        gbc_panel_3.insets = new Insets(0, 0, 0, 5);
        gbc_panel_3.fill = GridBagConstraints.BOTH;
        gbc_panel_3.gridx = 1;
        gbc_panel_3.gridy = 1;
        panel_2.add(panel_3, gbc_panel_3);

        JButton previousCommandBtn = new JButton("previous command");
        panel_3.add(previousCommandBtn);

        executeBtn = new JButton("execute");
        executeBtn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int clickCount2 = e.getClickCount();
                if (clickCount2 == 1) {
                    sendCmd_parseResult();
                    // System.out.println("单击");
                } /*
                 * else if (clickCount2 > 1) { System.out.println("双击已经禁用!");
				 * new Thread(new Runnable() { public void run() { //
				 * executeBtn.setEnabled(false); } }).start(); }
				 */
            }
        });
        // executeBtn.addActionListener(new ActionListener() {
        // public void actionPerformed(ActionEvent e) {
        //
        //
        // }
        // });
        panel_3.add(executeBtn);

        encodingComboBox = new JComboBox<String>();
        encodingComboBox.addItem(SystemHWUtil.EMPTY);
        encodingComboBox.addItem(SystemHWUtil.CHARSET_UTF);
        encodingComboBox.addItem(SystemHWUtil.CHARSET_GBK);
        encodingComboBox.addItem(SystemHWUtil.CHARSET_GB2312);
        encodingComboBox.addItem(SystemHWUtil.CHARSET_ISO88591);
        //设置默认选中的项
        encodingComboBox.setSelectedIndex(1);

        panel_3.add(encodingComboBox);

        JPanel resultPanel = new JPanel();
        panel_1.add(resultPanel, BorderLayout.CENTER);
        GridBagLayout gbl_resultPanel = new GridBagLayout();
        gbl_resultPanel.columnWidths = new int[]{0, 0, 0, 0};
        gbl_resultPanel.rowHeights = new int[]{0, 0, 0, 0};
        gbl_resultPanel.columnWeights = new double[]{0.0, 1.0, 0.0,
                Double.MIN_VALUE};
        gbl_resultPanel.rowWeights = new double[]{0.0, 1.0, 1.0,
                Double.MIN_VALUE};
        resultPanel.setLayout(gbl_resultPanel);

        JLabel lblExecuteResult = new JLabel("execute result");
        GridBagConstraints gbc_lblExecuteResult = new GridBagConstraints();
        gbc_lblExecuteResult.insets = new Insets(0, 0, 5, 5);
        gbc_lblExecuteResult.gridx = 0;
        gbc_lblExecuteResult.gridy = 1;
        resultPanel.add(lblExecuteResult, gbc_lblExecuteResult);

        JScrollPane scrollPane_1 = new JScrollPane();
        GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
        gbc_scrollPane_1.insets = new Insets(0, 0, 5, 5);
        gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
        gbc_scrollPane_1.gridx = 1;
        gbc_scrollPane_1.gridy = 1;
        resultPanel.add(scrollPane_1, gbc_scrollPane_1);

        resultTP = new JTextPane();
        MenuUtil2.setPopupMenu(resultTP);
        resultTP.setEditable(false);
        DefaultCaret caret1 = (DefaultCaret) resultTP.getCaret();
        caret1.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        scrollPane_1.setViewportView(resultTP);

        JButton cleanButton = new JButton("clean");
        cleanButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resultTP.setText("");
            }
        });
        GridBagConstraints gbc_cleanButton = new GridBagConstraints();
        gbc_cleanButton.insets = new Insets(0, 0, 5, 0);
        gbc_cleanButton.gridx = 2;
        gbc_cleanButton.gridy = 1;
        resultPanel.add(cleanButton, gbc_cleanButton);

        JLabel lblStatus = new JLabel("status");
        GridBagConstraints gbc_lblStatus = new GridBagConstraints();
        gbc_lblStatus.insets = new Insets(0, 0, 0, 5);
        gbc_lblStatus.gridx = 0;
        gbc_lblStatus.gridy = 2;
        resultPanel.add(lblStatus, gbc_lblStatus);

        statusTA = new UndoTextArea();
        MenuUtil2.setPopupMenu(statusTA);
        statusTA.setEditable(false);
        DefaultCaret caret2 = (DefaultCaret) statusTA.getCaret();
        caret2.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollPane = new JScrollPane(statusTA);
        GridBagConstraints gbc_panel_4 = new GridBagConstraints();
        gbc_panel_4.insets = new Insets(0, 0, 0, 5);
        gbc_panel_4.fill = GridBagConstraints.BOTH;
        gbc_panel_4.gridx = 1;
        gbc_panel_4.gridy = 2;
        resultPanel.add(scrollPane, gbc_panel_4);

        JButton btnClean = new JButton("clean");
        btnClean.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusTA.setText("");
            }
        });
        GridBagConstraints gbc_btnClean = new GridBagConstraints();
        gbc_btnClean.gridx = 2;
        gbc_btnClean.gridy = 2;
        resultPanel.add(btnClean, gbc_btnClean);

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
                    ServerChatApp frame = new ServerChatApp();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void main33(String[] args) throws UnknownHostException,
            IOException {
        Socket client = new Socket("localhost", 8888);
        OutputStream socketOutput = client.getOutputStream();
        PrintWriter outputWrite = new PrintWriter(socketOutput);
        outputWrite.write("comm retr D:\\bin\\divideFile-0.0.1-SNAPSHOT.jar");
        outputWrite.flush();
        System.out.println();
        outputWrite.write("comm retr D:\\bin\\divideFile-0.0.1-SNAPSHOT.jar");
        outputWrite.flush();
        System.out.println("aa");
    }

    private void sendCmd_parseResult() {
        sendCmd_parseResult(commandTextField.getText());
    }

    /***
     * 发送命令，并且解析命令执行结果
     */
    private void sendCmd_parseResult(String command1) {
//		String command1 = commandTextField.getText();
        if (command1.matches("[ ]*cd[ ]+..[ ]*")) {
            command1 = Constants.COMMAND_CDUP;
        }
        command1 = command1.replaceAll("^cd ", "cwd ");
        System.out.println("command:" + command1);
        String realCommandAndArg = CMDUtil.getRealMessage(command1,
                Constants.TYPE_COMMAND);
        String[] realCommandAndArgs = realCommandAndArg.split(" ");
        String realCommand = realCommandAndArgs[0];
        System.out.println("server real command:" + realCommand);
		/*if(realCommand.equals(Constants.COMMAND_RETR)){//把本地文件传输到远端
			String localFilePath=realCommandAndArgs[1];
			if(!FileUtils.isFile(localFilePath)){
				ToastMessage toastMessage = new ToastMessage("文件不存在",3000,Color.red);
	            toastMessage.setVisible(true);
	            return;
			}
		}*/
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
            String charset = getSelectedItem4ComboBox(encodingComboBox);
            System.out.println("server charset:" + charset);
            serverThread = new ServerThread(client, ServerChatApp.this, charset);// JTextPane
            serverThread.setLatestRetrPath(new File("d:\\Temp\\a\\a\\b\\ab"
                    + count2 + ".jar"));
            // resultTP
            new Thread(serverThread).start();
            DialogUtil.focusSelectAllTF(commandTextField);
        }
    }

    public JTextArea getStatusTA() {
        return statusTA;
    }

    public JTextPane getResultTP() {
        return resultTP;
    }

    public JButton getExecuteBtn() {
        return executeBtn;
    }

}
