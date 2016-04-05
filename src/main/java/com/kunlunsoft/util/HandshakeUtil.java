package com.kunlunsoft.util;

import java.io.*;
import java.net.Socket;

public class HandshakeUtil {
    /***
     * 判断客户端ftp服务器是否已经开启.
     *
     * @param ip
     * @param port
     * @return
     * @throws IOException
     */
    public static boolean isRightClient(String ip, int port) throws IOException {
        Socket server = null;
        try {
            server = new Socket(ip, port);
            server.setSoTimeout(1000);
        } catch (java.net.ConnectException e1) {
            return false;
//			e1.printStackTrace();
        }
        OutputStream out = server.getOutputStream();
        PrintWriter writer = new PrintWriter(out);
        String sendMsg = "hand helo";
        writer.write(sendMsg);
        writer.flush();
        InputStream ins = server.getInputStream();
        int prefixReturnLength = 5;

        char[] returnMarkBytes = new char[prefixReturnLength];
        InputStreamReader bis = new InputStreamReader(ins);
        int timeoutTimes = 0;
//		if (!bis.ready()) {
//			try {
//				if(timeoutTimes>2){
//					return false;
//				}
//				Thread.sleep(10);
//				timeoutTimes++;
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
        int readLength = 0;
        boolean isRight = false;
        while ((readLength = bis.read(returnMarkBytes, readLength,
                prefixReturnLength - readLength)) > 0) {
            String returnStr = new String(returnMarkBytes).trim();
            System.out.println("result:" + returnStr);
            if (returnStr.equalsIgnoreCase("ehlo")) {
                isRight = true;
                break;
            }
        }
        bis.close();
        server.close();
        return isRight;
    }

}
