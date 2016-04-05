package com.kunlunsoft.app;

import com.common.util.SystemHWUtil;
import com.kunlunsoft.bean.ConfigBean;
import com.kunlunsoft.listen.MyMenuBarActionListener;
import com.kunlunsoft.thread.ClientThread;
import com.string.widget.util.ValueWidget;
import com.swing.dialog.GenericFrame;
import com.swing.dialog.toast.ToastMessage;
import com.swing.messagebox.GUIUtil23;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/***
 * @author huangwei
 * @since 2013-10-23
 */
public class ClientChatApp extends GenericFrame {

    private static final long serialVersionUID = 7867183035617184507L;
    private static ConfigBean configBean = new ConfigBean();
    private JPanel contentPane;

    /**
     * Create the frame.
     */
    public ClientChatApp() {
        setMenuBar2();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(0, 0));

        JPanel centPanel = new JPanel();
        contentPane.add(centPanel, BorderLayout.CENTER);
        GridBagLayout gbl_centPanel = new GridBagLayout();
        gbl_centPanel.columnWidths = new int[]{0, 0, 0, 0};
        gbl_centPanel.rowHeights = new int[]{0, 0, 0, 0};
        gbl_centPanel.columnWeights = new double[]{0.0, 1.0, 0.0,
                Double.MIN_VALUE};
        gbl_centPanel.rowWeights = new double[]{1.0, 1.0, 0.0,
                Double.MIN_VALUE};
        centPanel.setLayout(gbl_centPanel);

        JTextPane chatRecordPane = new JTextPane();
        GridBagConstraints gbc_chatRecordPane = new GridBagConstraints();
        gbc_chatRecordPane.gridwidth = 2;
        gbc_chatRecordPane.insets = new Insets(0, 0, 5, 0);
        gbc_chatRecordPane.fill = GridBagConstraints.BOTH;
        gbc_chatRecordPane.gridx = 1;
        gbc_chatRecordPane.gridy = 0;
        centPanel.add(chatRecordPane, gbc_chatRecordPane);

        JTextPane inputPane = new JTextPane();
        GridBagConstraints gbc_inputPane = new GridBagConstraints();
        gbc_inputPane.gridwidth = 2;
        gbc_inputPane.insets = new Insets(0, 0, 5, 0);
        gbc_inputPane.fill = GridBagConstraints.BOTH;
        gbc_inputPane.gridx = 1;
        gbc_inputPane.gridy = 1;
        centPanel.add(inputPane, gbc_inputPane);

        JPanel panel = new JPanel();
        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.anchor = GridBagConstraints.EAST;
        gbc_panel.gridwidth = 2;
        gbc_panel.insets = new Insets(0, 0, 0, 5);
        gbc_panel.fill = GridBagConstraints.VERTICAL;
        gbc_panel.gridx = 1;
        gbc_panel.gridy = 2;
        centPanel.add(panel, gbc_panel);

        JButton btnCancel = new JButton("cancel");
        panel.add(btnCancel);

        JButton btnSend = new JButton("send");
        panel.add(btnSend);

        JPanel rightPanel = new JPanel();
        contentPane.add(rightPanel, BorderLayout.EAST);
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        if (!ValueWidget.isNullOrEmpty(args) && args[0].equals("-i") && args[1].equals("console")) {
            System.out.println("命令行模式......");
            String charset = null;
            if (args.length > 2) {
                charset = args[2];
            }
            if (ValueWidget.isNullOrEmpty(charset)) {
                charset = configBean.getEncoding();
            }
            try {
                ClientChatApp.ftpServer(charset);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    try {
                        ClientChatApp frame = new ClientChatApp();
                        frame.setVisible(true);
//						ClientChatApp.ftpServer("GBK");
                    } catch (Exception e) {
                        e.printStackTrace();
                        GUIUtil23.errorDialog(e);
                    }
                }
            });
        }

    }

    public static void ftpServer() throws IOException {
        String encoding = configBean.getEncoding();
        if (ValueWidget.isNullOrEmpty(encoding)) {
            encoding = SystemHWUtil.CURR_ENCODING;
        }
        ftpServer(encoding);
    }

    /***
     * 启动一个socket 服务，用于提供ftp 服务
     *
     * @throws IOException
     */
    private static void ftpServer(final String charset) throws IOException {
//		if (SocketHWUtil.isOcupy(ServerChatApp.clientDaemonPort)) {
//			GUIUtil23.warningDialog("端口号 " + ServerChatApp.clientDaemonPort
//					+ " 已经被占用");
//			return;
//		}
        if (configBean.isStarted()) {
            ToastMessage.toast("socket服务已经启动", 3000, Color.red);
            return;
        }
        final ServerSocket server = new ServerSocket(
                ServerChatApp.clientDaemonPort);

        new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    try {
                        // 接收服务器端的请求
                        Socket server2 = server.accept();
                        server2.setSoTimeout(1000);
                        // 该线程仅用于处理服务器端的请求
                        ClientThread ftpThread = new ClientThread(server2, charset);
                        new Thread(ftpThread).start();

                    } catch (IOException e) {
                        System.out.println("error");
                        e.printStackTrace();
                        return;
                    }
                    // 为什么要睡1秒?
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        configBean.setStarted(true);
    }

    /***
     * 设置编码
     *
     * @param encoding
     */
    public static void setEncoding(String encoding) {
        System.out.println("encoding:" + encoding);
        configBean.setEncoding(encoding);
    }

    private void setMenuBar2() {
        MyMenuBarActionListener myMenuBarActionListener = new MyMenuBarActionListener(this);
        JMenuBar menuBar = new JMenuBar();
        JMenu fileM = new JMenu("文件");
        JMenuItem startSocketM = new JMenuItem("启动socket服务");
        startSocketM.addActionListener(myMenuBarActionListener);
        fileM.add(startSocketM);


        JMenu encodingM = new JMenu("设置编码");
        JMenuItem utfM = new JMenuItem(SystemHWUtil.CHARSET_UTF);

        utfM.addActionListener(myMenuBarActionListener);
        utfM.setActionCommand(SystemHWUtil.CHARSET_UTF);
        encodingM.add(utfM);

        JMenuItem gbkM = new JMenuItem(SystemHWUtil.CHARSET_GBK);
        gbkM.addActionListener(myMenuBarActionListener);
        gbkM.setActionCommand(SystemHWUtil.CHARSET_GBK);
        encodingM.add(gbkM);

        JMenuItem gb2312M = new JMenuItem(SystemHWUtil.CHARSET_GB2312);
        gb2312M.addActionListener(myMenuBarActionListener);
        gb2312M.setActionCommand(SystemHWUtil.CHARSET_GB2312);
        encodingM.add(gb2312M);

        menuBar.add(fileM);
        menuBar.add(encodingM);
        setJMenuBar(menuBar);
    }
}
