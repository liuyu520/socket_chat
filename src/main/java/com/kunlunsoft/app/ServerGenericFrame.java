package com.kunlunsoft.app;

import com.common.util.SocketHWUtil;
import com.kunlunsoft.thread.ServerThread;
import com.kunlunsoft.util.HandshakeUtil;
import com.kunlunsoft.util.LANIP;
import com.string.widget.util.ValueWidget;
import com.swing.dialog.GenericFrame;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ServerGenericFrame extends GenericFrame {
    public static final int clientDaemonPort = 8088;
    /***
     * 客户端ftp 服务的ip
     */
    public static final String clientFtpIP = "localhost";
    protected OutputStream socketOutput;
    protected PrintWriter outputWrite;
    protected Socket client;
    protected ServerThread serverThread;
    protected int count2 = 0;

    protected void send(String message) {
        if (!ValueWidget.isNullOrEmpty(outputWrite)) {
            outputWrite.write(message);
            outputWrite.flush();
        }
    }

    protected void closeSocket() throws IOException {
        if (!ValueWidget.isNullOrEmpty(socketOutput)) {
            socketOutput.close();
            client.close();
        }
    }

    /***
     * 和服务器建立连接
     *
     * @return true:成功建立连接;<br>false:没有建立连接
     * @throws UnknownHostException
     * @throws IOException
     */
    protected boolean establishConnection() throws UnknownHostException, IOException {

        InetAddress[] ips = LANIP.getAllOnline("GBK", "172.18.22.170"/*"127.0.0.1"*/);
        boolean isFoundClient = false;
        for (int i = 0; i < ips.length; i++) {
            InetAddress singleIp = ips[i];
            String ipStr = singleIp.getHostAddress();
            if (ipStr.startsWith("172.16") || ipStr.startsWith("172.18") || ipStr.startsWith("10.1") || ipStr.equals("127.0.0.1")) {
                System.out.println(ipStr);
                if (SocketHWUtil.isOcupy(ipStr, clientDaemonPort)) {
                    if (HandshakeUtil.isRightClient(ipStr, clientDaemonPort)) {
                        client = new Socket(ipStr, clientDaemonPort);
                        socketOutput = client.getOutputStream();
                        outputWrite = new PrintWriter(socketOutput);
                        System.out.println(ipStr);
                        isFoundClient = true;
                        break;
                    }
                }

            }

        }
        if (!isFoundClient) {
            System.out.println("没有找到服务器");
        }
        return isFoundClient;
    }


}
