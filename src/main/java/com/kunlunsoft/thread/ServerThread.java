package com.kunlunsoft.thread;

import com.common.util.SystemHWUtil;
import com.common.util.TLVUtil;
import com.io.hw.file.util.FileUtils;
import com.kunlunsoft.app.ServerChatApp;
import com.kunlunsoft.bean.ClientEnvStatusBean;
import com.kunlunsoft.bean.FailWarnInfoBean;
import com.kunlunsoft.bean.FileBean2;
import com.kunlunsoft.bean.ListFileBean;
import com.kunlunsoft.dict.Constants;
import com.kunlunsoft.util.CommandUtils;
import com.string.widget.util.ValueWidget;
import com.swing.dialog.toast.ToastMessage;
import com.swing.messagebox.GUIUtil23;
import com.time.util.TimeHWUtil;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.Date;

public class ServerThread implements Runnable {
    public static final int prefixReturnLength = 10;
    private Socket client2;
    private JTextPane resultTP;
    private StringBuffer resultSbuf = new StringBuffer();
    private StringBuffer statusSbuf = new StringBuffer();
    private JTextArea statusTA;
    private File latestRetrPath;
    private JButton executeBtn;
    /***
     * 读取的时候的编码
     */
    private String readCharset;

    /***
     * 是否暂停
     */
    // private boolean pause = true;
    public ServerThread(Socket server2, ServerChatApp serverChatApp, String charset) {
        super();
        this.client2 = server2;
        this.resultTP = serverChatApp.getResultTP();
        this.statusTA = serverChatApp.getStatusTA();
        this.executeBtn = serverChatApp.getExecuteBtn();
        this.readCharset = charset;
    }

    public ServerThread(Socket server2, JTextPane resultTP, JButton executeBtn, String charset) {
        super();
        this.client2 = server2;
        this.resultTP = resultTP;
        this.executeBtn = executeBtn;
        this.readCharset = charset;
    }

    public static Object commandList(String path) {
        System.out.println("list:" + path);
        File[] files = FileUtils.getFilesByPathAndPrefix(path, null);
        ListFileBean fileBean = new ListFileBean();
        fileBean.setFiles(files);
        fileBean.setPath(path);
        return fileBean;
    }

    public static String getRealMessage(String message, String type) {
        message = message.replaceAll("^" + type + "[\\s]*\\b(.*)$", "$1");
        return message;
    }

    @Override
    public void run() {
        try {

            while (!ValueWidget.isNullOrEmpty(client2) && !client2.isClosed()) {
                InputStream ins = null;
                try {
                    ins = client2.getInputStream();
                } catch (IOException e1) {
                    // e1.printStackTrace();
                    GUIUtil23.warningDialog(e1.getMessage());
                    continue;
                }

                if (ValueWidget.isNullOrEmpty(ins)) {
                    continue;
                }
                InputStreamReader bis = new InputStreamReader(ins);
                // if (ValueWidget.isNullOrEmpty(bis)) {
                // continue;
                // }
                // String message = SocketHWUtil.readSocket(ins);
                // char[] returnMarkBytes = new char[prefixReturnLength];
                // if (bis.ready()) {
                // int readLength = 0;
                // while ((readLength = bis.read(returnMarkBytes,
                // readLength, prefixReturnLength - readLength)) > 0) {
                // System.out.println("readLength:" + readLength);
                // if(prefixReturnLength==readLength){
                // break;
                // }
                // }
                // } else {
                // continue;
                // }
                /* 如果没有可读的数据，则会一直等待。 */
                if (!bis.ready()) {
                    continue;
                }
                System.out.println(SystemHWUtil.DIVIDING_LINE);
                System.out.println("服务器开始读取客户端命令执行的结果...");
                if (this.executeBtn != null) {
                    this.executeBtn.setEnabled(false);
                }

                byte[] returnMarkBytes = new byte[prefixReturnLength];
                int readLength = 0;
                int readAlreadyLength = 0;
                while ((readLength = ins.read(returnMarkBytes,
                        readAlreadyLength, prefixReturnLength
                                - readAlreadyLength)) != -1) {
                    // System.out.println("read:" + new
                    // String(returnMarkBytes));
                    readAlreadyLength += readLength;
                    // System.out.println("readLength:" + readLength);
                    // System.out
                    // .println("readAlreadyLength:" + readAlreadyLength);
                    if (prefixReturnLength == readAlreadyLength) {
                        break;
                    }
                }

                String resultStr = resultTP.getText();
                if (ValueWidget.isNullOrEmpty(resultStr)) {
                    resultSbuf.setLength(0);
                }
                if (statusTA != null) {
                    String statusStr = statusTA.getText();
                    if (ValueWidget.isNullOrEmpty(statusStr)) {
                        statusSbuf.setLength(0);
                    }
                }


                // eg:"succ list "(10 bytes)
                String returnMarkStr = new String(returnMarkBytes);

                if (!ValueWidget.isNullOrEmpty(returnMarkStr)) {
                    System.out.println("命令执行结果:" + returnMarkStr);
                    Object returnObj = null;
                    String[] returnArr = returnMarkStr.split(" ");
                    // "succ"
                    String isSuccessLabel = returnArr[0];
                    // "list"
                    String command = returnArr[1];
                    byte[] isAppendObj = new byte[2];
                    ins.read(isAppendObj);
                    ObjectInputStream objectIn = null;
                    if (SystemHWUtil.isSame(isAppendObj,
                            Constants.CMD_RESULT_APPEND_OBJECT)) {
                        objectIn = new ObjectInputStream(ins);
                    }/*
					 * 此时绝对可以通过判断objectIn是否为null来得知后面是否包含对象，若包含对象，则可以调用objectIn
					 * .readObject() 读取对象
					 */
                    Document docment = resultTP.getDocument();
                    // 命令執行不成功
                    if (isSuccessLabel
                            .equals(Constants.RETURN_IDENTIFICATION_FAIL)
                            || isSuccessLabel
                            .equals(Constants.RETURN_IDENTIFICATION_WARN)) {
                        if (objectIn != null) {
                            FailWarnInfoBean failWarnInfo = (FailWarnInfoBean) objectIn
                                    .readObject();
                            statusSbuf.append(failWarnInfo.getMessage())
                                    .append(SystemHWUtil.CRLF);
                        }
                    } else {// 命令执行成功

                        if (command.equals(Constants.COMMAND_LIST/* list */)) {
                            listAction(isSuccessLabel, objectIn);
                        } else if (command
                                .equals(Constants.COMMAND_PWD/* pwd */)) {
                            // ObjectInputStream objectIn = new
                            // ObjectInputStream(ins);
                            Object obj = objectIn.readObject();
                            // System.out.println("obj:" + obj);
                            ClientEnvStatusBean clientEnvStatusBean = (ClientEnvStatusBean) obj;
                            String currentDirectory = clientEnvStatusBean
                                    .getCurrentDirectory();
                            resultSbuf.append(currentDirectory).append(
                                    SystemHWUtil.CRLF);
                        } else if (command
                                .equals(Constants.COMMAND_RETR/* retr */)) {
                            retrAction(isSuccessLabel, objectIn, ins);
                        } else if (command.equals(Constants.COMMAND_CD_OLD)) {

                        } else if (command.equals(Constants.COMMAND_STAT)) {
                            FileBean2 fileBean = (FileBean2) objectIn
                                    .readObject();
                            System.out.println("fileBean:" + fileBean);
                            long size = fileBean.getSizeOfFile();
                            resultSbuf.append(
                                    "File \"" + fileBean.getFileName()
                                            + "\" size is "
                                            + String.valueOf(size) + " KB")
                                    .append(SystemHWUtil.CRLF);
                        } else if (command.equals(Constants.COMMAND_LOCA)) {// 执行本地命令的结果，最后是"eof"表示完毕
                            //一个字节,(byte)0:没有编码  ; (byte)1:有编码
                            int isHasEncoding = ins.read();
                            System.out.println("isHasEncoding:" + isHasEncoding);
                            if (isHasEncoding == 1) {//设置了编码
                                int encodingType = ins.read();
                                System.out.println("encodingType:" + encodingType);
                                switch (encodingType) {
                                    case 1:
                                        readCharset = SystemHWUtil.CHARSET_GBK;
                                        break;
                                    case 2:
                                        readCharset = SystemHWUtil.CHARSET_UTF;
                                        break;
                                    case 3:
                                        readCharset = SystemHWUtil.CHARSET_GB2312;
                                        break;
                                    case 4:
                                        readCharset = SystemHWUtil.CHARSET_GB18030;
                                        break;
                                    default:
                                        break;
                                }
                            }
                            final BufferedReader br_right = new BufferedReader(
                                    new InputStreamReader(ins, readCharset), 4096);

                            int readChars;
                            int i = 0;
                            int length_eof_flag = Constants.TAG_END_OF_STREAM
                                    .length();/* 结束标记的字符长度 */
                            int currentDocLength = docment.getLength();
                            int docLength_tmp = currentDocLength;
                            while (!client2.isClosed() && ((readChars = br_right.read()) != -1)) {
                                try {
                                    if (!ValueWidget.isNullOrEmpty(docment)) {
                                        // 追加,注意：insertString 的第一个参数是从1开始的
                                        try {
                                            docment.insertString(
                                                    docLength_tmp++,
                                                    String.valueOf((char) readChars),
                                                    null);
                                            resultTP.setCaretPosition(docLength_tmp);
                                            i++;
                                        } catch (javax.swing.text.BadLocationException e) {
                                            e.printStackTrace();
                                            break;
                                        }
                                        // if (i == 19681) {
                                        // System.out.println(i);
                                        // }
                                        // System.out.print((char) readChars);
                                        if (i >= length_eof_flag) {
                                            String isEof = docment.getText(
                                                    docLength_tmp
                                                            - length_eof_flag,
                                                    length_eof_flag);
                                            if (Constants.TAG_END_OF_STREAM
                                                    .equalsIgnoreCase(isEof)) {
                                                // System.out.println("文本框内容:"
                                                // + resultTP.getText());
                                                char[] length_lengthChar = new char[2];
                                                br_right.read(length_lengthChar);
                                                String length_length_Hex = new String(
                                                        length_lengthChar/* 必须是2个字符 */);// 长度转化为十六进制后占有的字符个数
                                                if (!CommandUtils
                                                        .isHex(length_length_Hex))// java.lang.NumberFormatException:
                                                // For
                                                // input
                                                // string:
                                                // ".c"
                                                {
                                                    String str/* 错过的，没有显示到文本框中的字符串 */ = new String(
                                                            length_lengthChar);
                                                    docment.insertString(
                                                            docLength_tmp, str,
                                                            null);
                                                    i += str.length();
                                                    docLength_tmp = docLength_tmp
                                                            + str.length();
                                                    continue;
                                                }
                                                int length_length = TLVUtil
                                                        .hexToInt(length_length_Hex);
                                                if (length_length <= 0) {// 正常情况，长度应该大于0
                                                    String str/* 错过的，没有显示到文本框中的字符串 */ = new String(// TODO
                                                            // 与上面的代码重复
                                                            length_lengthChar);
                                                    docment.insertString(
                                                            docLength_tmp, str,
                                                            null);
                                                    i += str.length();
                                                    docLength_tmp = docLength_tmp
                                                            + str.length();
                                                    continue;
                                                }
                                                char[] lengthChars = new char[length_length];
                                                br_right.read(lengthChars);
                                                String lengHex = new String(
                                                        lengthChars);// 传输的长度
                                                if (!CommandUtils
                                                        .isHex(lengHex))// java.lang.NumberFormatException:
                                                // For
                                                // input
                                                // string:
                                                // ".c"
                                                {
                                                    String str/* 错过的，没有显示到文本框中的字符串 */ = new String(
                                                            length_lengthChar)
                                                            + new String(
                                                            lengthChars);
                                                    docment.insertString(
                                                            docLength_tmp, str,
                                                            null);
                                                    i += str.length();
                                                    docLength_tmp = docLength_tmp
                                                            + str.length();
                                                }
                                                // 获取真正传输的长度，十六进制格式
                                                long length2 = Long.parseLong(
                                                        lengHex, 16);
                                                if (length2
                                                        + Constants.TAG_END_OF_STREAM
                                                        .length() == i) {// 表示确实是读取完毕了。
                                                    System.out
                                                            .println("读取完毕");
                                                    break;
                                                } else if (length2
                                                        + Constants.TAG_END_OF_STREAM
                                                        .length() > i) { // 还没有读完
                                                    String str/* 错过的，没有显示到文本框中的字符串 */ = new String(
                                                            length_lengthChar)
                                                            + new String(
                                                            lengthChars);
                                                    docment.insertString(
                                                            docLength_tmp, str,
                                                            null);
                                                    i += str.length();
                                                    docLength_tmp = docLength_tmp
                                                            + str.length();
                                                } else {
                                                    int readByte;
                                                    while ((readByte = ins.read()) != -1) {
                                                        System.out
                                                                .println(readByte);
                                                    }
                                                }

                                            }
                                        }
                                    }
                                    // if (i > 50) {
                                    // System.out.println("获得的字符数xxxx:" + i);
                                    // }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            System.out.println("获得的字符数:" + i);
                        } else if (command.equals(Constants.COMMAND_STOR)) {
                            FileBean2 failWarnInfo = null;
                            if (objectIn != null) {
                                failWarnInfo = (FileBean2) objectIn
                                        .readObject();
                                statusSbuf.append(failWarnInfo.getFileName())
                                        .append(SystemHWUtil.CRLF);
                            }
                            byte[] length_lengthChar = new byte[2];
                            ins.read(length_lengthChar);
                            String length_length_Hex = new String(
                                    length_lengthChar/* 必须是2个字符 */);// 长度转化为十六进
                            int length_length = TLVUtil
                                    .hexToInt(length_length_Hex);
                            byte[] lengthChars = new byte[length_length];
                            ins.read(lengthChars);
                            String lengHex = new String(
                                    lengthChars);// 传输的长度
                            long length2 = Long.parseLong(
                                    lengHex, 16);
                            System.out.println("length2:" + length2);
                            //弹出对话框选择文件
                            JFileChooser chooser = new JFileChooser();
                            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                            File selectedFile = new File("C:\\Users\\Administrator\\Pictures\\" + TimeHWUtil.formatDate(new Date(), "yyyyMM")
                                    + File.separator + failWarnInfo.getFileName());
                            //home目录应该动态获取
//					        System.out.println(selectedFile.getAbsolutePath());
                            if (!ValueWidget.isNullOrEmpty(selectedFile)) {
                                chooser.setSelectedFile(selectedFile);
                            }
                            chooser.setName(failWarnInfo.getFileName());
					        /*FileNameExtensionFilter filter = new FileNameExtensionFilter(
					                "picture Files", picFormat, "二维码");
					            chooser.setFileFilter(filter);*/
                            chooser.setDialogType(JFileChooser.SAVE_DIALOG);
                            chooser.setControlButtonsAreShown(true);
                            chooser.setDialogTitle("保存远程文件");
                            //            chooser.setVisible(true);
                            int result = chooser.showSaveDialog(resultTP.getParent());
                            System.out.println("New file:" + result);
                            if (result == JOptionPane.OK_OPTION) {
                                selectedFile = chooser.getSelectedFile();
					           /* if(! SystemHWUtil.isHasSuffix(selectedFile)){
					            	selectedFile=new File(selectedFile.getAbsolutePath()+ SystemHWUtil.ENGLISH_PERIOD+picFormat);
					            }*/

                                if (!selectedFile.exists()) {
                                    selectedFile.createNewFile();
                                }
                                FileUtils.writeFromFile2File(ins, selectedFile, length2, false);

                                ToastMessage.toast("保存文件成功", 3000);
                                System.out.println("select file:" + selectedFile);
                            } else {
                                ToastMessage.toast("已取消", 2000, Color.red);
                                ins.skip(length2);//跳过没有读取的问题
                            }


                        }
                        statusSbuf.append(command + " successfully").append(
                                SystemHWUtil.CRLF);
                        statusSbuf.append(SystemHWUtil.DIVIDING_LINE).append(
                                SystemHWUtil.CRLF);
                        resultSbuf.append(SystemHWUtil.DIVIDING_LINE).append(
                                SystemHWUtil.CRLF);

                    }
                    if (statusTA != null) {
                        statusTA.setText(statusSbuf.toString());
                    }

                    // resultTP.setText();
                    docment.insertString(docment.getLength(),
                            resultSbuf.toString(), null);
                }
                // byte[]lastBytes=FileUtils.readBytes(ins);
                // System.out.println("last bytes:"+new
                // String(lastBytes));
                // if(returnMarkStr.startsWith(Constants.RETURN_IDENTIFICATION_SUCCESS)){
                // returnObj=dealCommand(getRealMessage(message,
                // Constants.TYPE_COMMAND));
                // // char[]bytes2="succ".toCharArray();
                System.out.println();
                System.out.println(SystemHWUtil.DIVIDING_LINE);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    GUIUtil23.errorDialog(e.getMessage());
                    e.printStackTrace();
                }
                if (this.executeBtn != null) {
                    this.executeBtn.setEnabled(true);
                }
                ToastMessage.toast("完成", 1000);
                break;

            }// while

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void listAction(String isSuccessLabel, ObjectInputStream objectIn)
            throws IOException, ClassNotFoundException {
        // ObjectInputStream objectIn = new
        // ObjectInputStream(ins);
        Object obj = objectIn.readObject();
        System.out.println("list 读取的obj:" + obj);
        ListFileBean fileBean = (ListFileBean) obj;
        File[] files = fileBean.getFiles();
        int fileLength = 0;
        if (ValueWidget.isNullOrEmpty(files)) {

        } else {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                resultSbuf.append(file.getName()).append(SystemHWUtil.CRLF);
            }
            fileLength = files.length;
        }

        statusSbuf.append("has " + fileLength + " files").append(
                SystemHWUtil.CRLF);

    }

    private void retrAction(String isSuccessLabel, ObjectInputStream objectIn,
                            InputStream ins) throws IOException, ClassNotFoundException {
        // ObjectInputStream objectIn = new
        // ObjectInputStream(ins);
        if (isSuccessLabel.equals(Constants.RETURN_IDENTIFICATION_SUCCESS)) {
            System.out.println("server: start retr.");

            FileBean2 fileBean = (FileBean2) objectIn.readObject();
            System.out.println("fileBean:" + fileBean);
            long size = fileBean.getSizeOfFile();
            // String fileName = fileBean.getFileName();
            System.out.println("server: start write "
                    + getLatestRetrPath().getAbsolutePath());
            FileUtils.writeInputStream2File(getLatestRetrPath(), ins, size,
                    true);

            // System.out.println("last:"+ins.read());
            System.out.println("server complete write "
                    + getLatestRetrPath().getAbsolutePath());
            statusSbuf.append(
                    "downloaded as: " + getLatestRetrPath().getAbsolutePath())
                    .append(SystemHWUtil.CRLF);
        } else {// fail or warn

        }
    }

    public Socket getServer2() {
        return client2;
    }

    public void setServer2(Socket server2) {
        this.client2 = server2;
    }

    public Object dealCommand(String command2) {
        if (command2.startsWith(Constants.COMMAND_LIST)) {
            Object obj = commandList(getRealMessage(command2,
                    Constants.COMMAND_LIST));
            return obj;
        }
        return null;
    }

    public File getLatestRetrPath() {
        return latestRetrPath;
    }

    public void setLatestRetrPath(File latestRetrPath) {
        this.latestRetrPath = latestRetrPath;
    }

}
