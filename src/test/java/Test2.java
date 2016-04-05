import com.cmd.dos.hw.util.CMDUtil;
import com.common.util.SystemHWUtil;
import com.kunlunsoft.app.ServerChatApp;
import com.kunlunsoft.dict.Constants;
import com.kunlunsoft.thread.ClientThread;
import com.kunlunsoft.util.HandshakeUtil;
import junit.framework.Assert;
import org.junit.Test;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Test2 {
    //	@Test
    public void test_002() throws UnknownHostException, IOException,
            InterruptedException, ClassNotFoundException {
        Socket server = new Socket("localhost", 8888);
        InputStream ins2 = server.getInputStream();
        ins2.read();
        OutputStream out = server.getOutputStream();
        PrintWriter writer = new PrintWriter(out);
        String sendMsg = "comm list d:\\Temp\\bb";
        writer.write(sendMsg);
        writer.flush();
        Thread.sleep(1000);
        InputStream ins = server.getInputStream();
        byte[] bytes = new byte[10];
        ins.read(bytes);
        System.out.println("result:" + new String(bytes));
        ObjectInputStream objectIn = new ObjectInputStream(ins);
        Object obj = objectIn.readObject();
        System.out.println(obj);
        Thread.sleep(5500000);
    }

    //	@Test
    public void test_003() {
        try {
            Socket server = new Socket("localhost", 8888);
            OutputStream out = server.getOutputStream();
            PrintWriter writer = new PrintWriter(out);
            String sendMsg = "comm list d:\\Temp\\bb";
            writer.write(sendMsg);
            writer.flush();
            Thread.sleep(1000);
            InputStream ins = server.getInputStream();
            int prefixReturnLength = 10;

            char[] returnMarkBytes = new char[prefixReturnLength];
            InputStreamReader bis = new InputStreamReader(ins);
            if (bis.ready()) {
                int readLength = 0;
                while ((readLength = bis.read(returnMarkBytes,
                        readLength, prefixReturnLength - readLength)) > 0) {
                    System.out.println("readLength:" + readLength);
                    if (prefixReturnLength == readLength) {
                        break;
                    }
                }
            }
//			bis.close();
//			ins.skip(prefixReturnLength);
            System.out.println("result:" + new String(returnMarkBytes));
            ObjectInputStream objectIn = new ObjectInputStream(ins);
            Object obj = objectIn.readObject();
            System.out.println(obj);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//		Thread.sleep(5500000);
    }

    @Test
    public void test_cut() {
        String sendMsg = "comm list d:\\Temp\\bb";
        sendMsg = CMDUtil.getRealMessage(sendMsg, Constants.TYPE_COMMAND);
        Assert.assertEquals("list d:\\Temp\\bb", sendMsg);
//		System.out.println(sendMsg);
    }

    //	@Test
    public void test_blank() {
        String input = " ";
        System.out.println(input.getBytes()[0]);
    }

    //	@Test
    public void test_get5Bytes() {
        String mesg = "suc";
        byte[] resultBytes = ClientThread.get5Bytes(mesg);
        SystemHWUtil.printBytes(resultBytes);
    }

    //	@Test
    public void test_cd() {
        String input = "cd abc";
        System.out.println(input.replaceAll("^cd ", "cwd "));
    }

    //	@Test
    public void test_hand() throws UnknownHostException, IOException, InterruptedException {
        Socket server = new Socket("localhost", ServerChatApp.clientDaemonPort);
        OutputStream out = server.getOutputStream();
        PrintWriter writer = new PrintWriter(out);
        String sendMsg = "hand helo";
        writer.write(sendMsg);
        writer.flush();
        Thread.sleep(1000);
        InputStream ins = server.getInputStream();
        int prefixReturnLength = 5;

        char[] returnMarkBytes = new char[prefixReturnLength];
        InputStreamReader bis = new InputStreamReader(ins);
        if (bis.ready()) {
            int readLength = 0;
            while ((readLength = bis.read(returnMarkBytes,
                    readLength, prefixReturnLength - readLength)) > 0) {
                System.out.println("readLength:" + readLength);
                System.out.println("result:" + new String(returnMarkBytes));
                if (prefixReturnLength == readLength) {
                    break;
                }
            }
        }
        bis.close();
    }

    //	@Test
    public void test_isRightClient() {
        try {
            System.out.println(HandshakeUtil.isRightClient("172.16.15.210", ServerChatApp.clientDaemonPort));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
