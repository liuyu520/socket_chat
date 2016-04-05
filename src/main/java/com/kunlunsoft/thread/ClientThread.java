package com.kunlunsoft.thread;

import com.cmd.dos.hw.util.CMDUtil;
import com.common.dict.Constant2;
import com.common.util.SocketHWUtil;
import com.common.util.SystemHWUtil;
import com.common.util.TLVUtil;
import com.io.hw.file.util.FileUtils;
import com.kunlunsoft.bean.ClientEnvStatusBean;
import com.kunlunsoft.bean.FailWarnInfoBean;
import com.kunlunsoft.bean.FileBean2;
import com.kunlunsoft.bean.ListFileBean;
import com.kunlunsoft.dict.Constants;
import com.kunlunsoft.util.ShellSwingWorker;
import com.string.widget.util.ValueWidget;
import com.swing.messagebox.GUIUtil23;

import java.io.*;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;

/***
 * @author huangwei
 * @since 2013-10-23
 */
public class ClientThread implements Runnable {
    private Socket server2;
    /***
     * 表示客户端的状态.
     */
    private ClientEnvStatusBean clientEnvStatusBean;
    private ProcessBuilder pb;
    private String charset;

    public ClientThread(Socket server2, String charset2) {
        super();
        this.server2 = server2;
        if (ValueWidget.isNullOrEmpty(charset2)) {
            this.charset = SystemHWUtil.CURR_ENCODING;
        } else {
            this.charset = charset2;
        }
    }

    /***
     * 补齐至5个字节
     *
     * @param typeCommand
     * @return
     */
    public static byte[] get5Bytes(String typeCommand) {
        byte[] bytes = typeCommand.getBytes();
        if (bytes.length > Constants.MAX_LENGTH_COMMAND) {
            throw new RuntimeException(
                    "The length of command can not be longer than "
                            + Constants.MAX_LENGTH_COMMAND);
        }
        int offset = Constants.MAX_LENGTH_COMMAND - bytes.length;
        byte[] resultBytes = null;

        resultBytes = bytes;
        for (int i = 0; i < offset; i++) {
            resultBytes = SystemHWUtil.appandByte(resultBytes,
                    SystemHWUtil.BLANK_BYTE);
        }
        return resultBytes;
    }

    /***
     * execute command "list" .
     *
     * @param path
     * @return : ListFileBean
     */
    public static ListFileBean commandList(String path) {
        System.out.println("list:" + path);
        File[] files = FileUtils.getFilesByPathAndPrefix(path, null);
        ListFileBean fileBean = new ListFileBean();
        fileBean.setFiles(files);
        fileBean.setPath(path);
        return fileBean;
    }

    private void setCmdDirectory(File dir) {
        if (pb == null) {
            pb = new ProcessBuilder();
        }
        pb.directory(dir);
    }

    /***
     * 写入对象.
     *
     * @param returnObj
     * @param outs
     * @param isWriteObjFlag : 是否写入“是否包含对象”的标识
     * @throws IOException
     */
    private void writeObjec(Object returnObj, OutputStream outs, boolean isWriteObjFlag)
            throws IOException {
        if (!ValueWidget.isNullOrEmpty(returnObj)) {
            if (isWriteObjFlag)
                outs.write(Constants.CMD_RESULT_APPEND_OBJECT);
            ObjectOutputStream objectOut = new ObjectOutputStream(outs);
            objectOut.writeObject(returnObj);
            objectOut.flush();
        } else {// 不包含对象
            if (isWriteObjFlag) {/*是否写入“是否包含对象”的标识*/
                outs.write(Constants.CMD_RESULT_NONE_OBJECT);
            }
        }
    }

    /***
     * 在命令的执行结果中，标记是否成功.
     *
     * @param isSuccess
     * @param outs
     * @param realCommand
     * @throws IOException
     */
    private void writeReturnHeader(String isSuccess, OutputStream outs,
                                   String realCommand) throws IOException {
        outs.write((isSuccess + " ").getBytes());
        // eg "list "
        outs.write(get5Bytes(realCommand.split(" ", 2)[0]));
    }

    private void writeSuccessHeader(OutputStream outs, String realCommand)
            throws IOException {
        writeReturnHeader(Constants.RETURN_IDENTIFICATION_SUCCESS, outs,
                realCommand);
    }

    private void writeFailHeader(OutputStream outs, String realCommand)
            throws IOException {
        writeReturnHeader(Constants.RETURN_IDENTIFICATION_FAIL, outs,
                realCommand);
    }

    @Override
    public void run() {
        clientEnvStatusBean = new ClientEnvStatusBean();
        clientEnvStatusBean.setCurrentDirectory(SystemHWUtil.USER_DIR);
        try {
            System.out.println("ClientThread server2.isClosed():" + server2.isClosed());
            while (!ValueWidget.isNullOrEmpty(server2) && !server2.isClosed()) {
//				 System.out.println("client....");
                InputStream ins = server2.getInputStream();
                OutputStream outs = server2.getOutputStream();

                String message = SocketHWUtil.readSocket(ins, SystemHWUtil.CURR_ENCODING);
                if (!ValueWidget.isNullOrEmpty(message)) {
                    System.out.println("客户端收到的命令:" + message);
                    Object returnObj = null;
                    if (message.startsWith(Constants.TYPE_COMMAND)) {
                        // eg:"list d:\Temp\a\a" , "retr
                        // d:\bin\path_tools-0.0.1-SNAPSHOT.jar"
                        String realCommand = CMDUtil.getRealMessage(
                                message, Constants.TYPE_COMMAND);
                        Object[] flag_obj = dealCommand(realCommand, outs);
                        //是否写入"是否包含对象的标识"
                        boolean isWriteObjFlag = false;
                        if (!ValueWidget.isNullOrEmpty(flag_obj)) {
                            isWriteObjFlag = (Boolean) flag_obj[0];
                            returnObj = flag_obj[1];
                        }


                        if ((!realCommand.startsWith(Constants.COMMAND_RETR)/*&&!realCommand.startsWith(Constants.COMMAND_STOR)*/)
                                || !ValueWidget.isNullOrEmpty(returnObj)) {
                            writeObjec(returnObj, outs, isWriteObjFlag);
                        }
                        outs.flush();
//						System.out.println(server2.isClosed()+"  "+server2.isConnected());
//						ins.close();//是不是要关闭？？？不能关闭
//						System.out.println(server2.isClosed()+"  "+server2.isConnected());
                    } else if (message.startsWith(Constants.TYPE_HANDSHAKE)) {
                        String realCommand = CMDUtil.getRealMessage(
                                message, Constants.TYPE_HANDSHAKE);
                        if (realCommand.equals("helo")) {
                            String retun = "ehlo";
                            outs.write(get5Bytes(retun));//约定：只写入5个字节，客户端读取之后要手动去掉空格
                            outs.flush();
                        }
                    }
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
                // 获取前
                // client get message from server

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getServer2() {
        return server2;
    }

    public void setServer2(Socket server2) {
        this.server2 = server2;
    }

    /***
     * @param command2
     * @param outs
     * @return [是否写入对象标示，对象]
     * @throws IOException
     */
    public Object[] dealCommand(String command2, OutputStream outs)
            throws IOException {
        String[] cmdAndArg = command2.split(" ", 2);
        String cmd = cmdAndArg[0];// eg. "list"
        String argument = null;
        if (cmdAndArg.length > 1) {
            argument = cmdAndArg[1];// e.g. "d:\bin"
            System.out.println("命令的参数:" + argument);
        }
        if (cmd.equals(Constants.COMMAND_LIST)) {// list
            writeSuccessHeader(outs, command2);
            String path = CMDUtil.getRealMessage(command2,
                    Constants.COMMAND_LIST);
            if (ValueWidget.isNullOrEmpty(path)) {
                path = clientEnvStatusBean.getCurrentDirectory();
            }
            ListFileBean obj = commandList(path);
            return new Object[]{true, obj};
        } else if (cmd.equals(Constants.COMMAND_PWD/* pwd */)) {// pwd
            writeSuccessHeader(outs, command2);
            return new Object[]{true, clientEnvStatusBean};
        } else if (cmd.equals(Constants.COMMAND_CDUP/* cdup,等价于 cd .. */)) {// cdup

			/* 要考虑到当前目录就是磁盘根目录的情况，比如d:\ 或 E:\ */
            String currentDir = clientEnvStatusBean.getCurrentDirectory();
            if (ValueWidget.isNullOrEmpty(currentDir)) {
                currentDir = System.getProperty("user.dir");
                clientEnvStatusBean.setCurrentDirectory(currentDir);
            }
            // 当前目录的父目录
            String parentDir = SystemHWUtil.getParentDir(currentDir);
            if (!ValueWidget.isNullOrEmpty(parentDir)) {// 存在父目录
                /* 只有这一种情况是正确的，其他情况都算作失败 */
                clientEnvStatusBean.setPreviousDirectory(currentDir);
                clientEnvStatusBean.setCurrentDirectory(parentDir);
                setCmdDirectory(new File(parentDir));
                writeSuccessHeader(outs, command2);
                return new Object[]{true, null};
            }

            // 失败的情况，包括当前目录不存在或父目录为空.
            writeFailHeader(outs, command2);
            FailWarnInfoBean failWarnInfo = new FailWarnInfoBean();
            failWarnInfo.setCode(Constants.RETURN_CODE_FATHER_FOLDER_NOT_EXIST);
            failWarnInfo.setCommand(command2);
            failWarnInfo.setMessage("父目录不存在.");
            return new Object[]{true, failWarnInfo};

            // return clientEnvStatusBean;
        } else if (cmd.equals(Constant2.COMMAND_CWD)) {// cwd==cd
            return new Object[]{true, cwdAction(command2, outs)};

            // return clientEnvStatusBean;
        } else if (cmd.equals(Constants.COMMAND_CD_OLD)) {// cd -

            String oldCurrentDir = clientEnvStatusBean.getPreviousDirectory();
            if (!ValueWidget.isNullOrEmpty(oldCurrentDir)) {
                clientEnvStatusBean.setPreviousDirectory(clientEnvStatusBean
                        .getCurrentDirectory());
                clientEnvStatusBean.setCurrentDirectory(oldCurrentDir);
            }// 没有返回对象
        } else if (cmd.equals(Constants.COMMAND_RETR)) {// retr
            return new Object[]{false, retrOrStatAction(command2, outs)};
        } else if (cmd.equals(Constants.COMMAND_STAT)) {// stat
			/* 若是文件，则返回文件路径和大小；若是目录，则返回目录中的文件个数 */
//			Object obj = ;
            return new Object[]{false, retrOrStatAction(command2, outs)};
        } else if (cmd.equals(Constants.COMMAND_LOCA)) {
			/*执行操作系统本地命令，比如cd，net start mysql,dir and so on*/
            return localCommand(argument, outs);
        } else if (cmd.equals(Constants.COMMAND_STOR)) {
			/*执行操作系统本地命令，比如cd，net start mysql,dir and so on*/
            File storeFile = new File(argument);
            if (!storeFile.exists()) {
                writeFailHeader(outs, command2);
                FailWarnInfoBean failWarnInfo = new FailWarnInfoBean();
                failWarnInfo.setCode("100");
                failWarnInfo.setCommand(command2);
                failWarnInfo.setMessage("文件不存在.");
                return new Object[]{true, failWarnInfo};
            } else if (storeFile.isDirectory()) {
                writeFailHeader(outs, command2);
                FailWarnInfoBean failWarnInfo = new FailWarnInfoBean();
                failWarnInfo.setCode("103");
                failWarnInfo.setCommand(command2);
                failWarnInfo.setMessage("不是文件，是目录.");
                return new Object[]{true, failWarnInfo};
            }
            return storeCommand(argument, outs);
        } else {// 命令不存在
            writeFailHeader(outs, command2);
            FailWarnInfoBean failWarnInfo = new FailWarnInfoBean();
            failWarnInfo.setCode(Constants.RETURN_CODE_INVALID_COMMAND);
            failWarnInfo.setCommand(command2);
            failWarnInfo.setMessage("command \"" + command2
                    + " \" does not exist.");
            return new Object[]{true, failWarnInfo};
        }
        return null;
    }

    private Object[] storeCommand(String input, OutputStream outs) {
        File storeFile = new File(input);
        if (storeFile.exists()) {
            try {
                writeSuccessHeader(outs, Constants.COMMAND_STOR);
                FileBean2 fileBean2 = new FileBean2();
                fileBean2.setFileName(storeFile.getName());
                writeObjec(fileBean2, outs, true);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            long length = storeFile.length();//文件大小
            String dataLengthHex = Long.toHexString(length);//表示长度
            System.out.println("length:" + length);
            System.out.println("dataLengthHex:" + dataLengthHex);
//			System.out.println("hex:"+hex);
            String length_lengthHex = TLVUtil.toHex(dataLengthHex.length());//长度占用的字符数
            System.out.println("length_lengthHex:" + length_lengthHex);
//			System.out.println("lengthHex:"+lengthHex);
            if (length_lengthHex.length() != 2) {
				/*长度必须是2，这是我的规定*/
                throw new RuntimeException(this.getClass().getSimpleName() + " lengthHex must has two char");
            }
            BufferedOutputStream bout = new BufferedOutputStream(outs);
            try {
                bout.write(length_lengthHex.getBytes());
                bout.write(dataLengthHex.getBytes());//表示传输的字符有多少个,十六进制形式
                FileUtils.writeFile2OutputStream(storeFile, bout, false);
            } catch (IOException e) {
                e.printStackTrace();
            }//hex 的长度

        } else {
            GUIUtil23.errorDialog(input + " 不存在");
        }
        return new Object[]{false, null};
    }

    /***
     * 执行操作系统本地命令.
     *
     * @param input : 本地命令
     * @return
     */
    private Object[] localCommand(String input, OutputStream outs) {
        if (pb == null) {
            pb = new ProcessBuilder();
        }
        //net start mysql
        System.out.println("localCmd:" + input);
        if (input.equals("pwd") && SystemHWUtil.isWindows) {/*windows 没有“pwd” 命令*/
            input = "cd";
        }
        if (SystemHWUtil.isWindows) {
            input = ShellSwingWorker.FIX_PREFIX_COMMAND + input;
        }
        System.out.println("执行的命令:" + input);
        String[] commands = input.split("[ \t]");
        pb.command(commands);
        Map env = pb.environment(); //获得进程的环境
        Iterator it = env.keySet().iterator();
        String sysatt = null;
        while (it.hasNext()) {
            sysatt = (String) it.next();
            System.out.println("System Attribute:" + sysatt + "=" + env.get(sysatt));
        }
        // if (directory != null)
        // {
        // pb.directory(directory);
        // }
        // Map<String, String> env = pb.environment();
        // env.put("$HOME", "/home/whuang2");
        if (!SystemHWUtil.isWindows) {
            pb.directory(new File("/home/oliangdd/bin"));
        }

        try {
            writeSuccessHeader(outs, Constants.COMMAND_LOCA);
            writeObjec(null, outs, true);
            ShellSwingWorker worker = new ShellSwingWorker(pb.start(), outs,
                    ClientThread.this.charset);
            worker.execute();
            Process proc = worker.getProcess();
            proc.waitFor();
            System.out.println("proc.waitFor() is executed.");
            // result2 = worker.getStringbuf().toString();
            int exitCode2 = proc.exitValue();
            long charCount = worker.getCharCount();
            System.out.println("read char:" + charCount);
            System.out.println("exitCode2:" + exitCode2);
//			return exitCode2;
        } catch (InterruptedException e1) {
            e1.printStackTrace();
//			resultTP.setText(SystemHWUtil.convertUTF2GBK(e1
//					.getLocalizedMessage()));
        } catch (IOException e2) {
            e2.printStackTrace();
//			resultTP.setText(e2.getMessage());
            System.out.println(SystemHWUtil.convertUTF2GBK(e2.getMessage()));
            System.out.println("---------------");
            System.out.println(SystemHWUtil.convertGBK2UTF(e2.getMessage()));
        }
        return new Object[]{false, null};
    }

    /***
     * 执行命令cwd.
     *
     * @param command2
     * @param outs
     * @return
     * @throws IOException
     */
    private Object cwdAction(String command2, OutputStream outs)
            throws IOException {
        String newPath = CMDUtil.getRealMessage(command2,
                Constant2.COMMAND_CWD);
        String currentDir = clientEnvStatusBean.getCurrentDirectory();

        boolean isAbsolutPaht = SystemHWUtil.isAbsolutePath(newPath);
        if (!isAbsolutPaht) {// is Relative path
            newPath = new File(currentDir, newPath).getAbsolutePath();
        }
        // 如果文件或目录不存在
        if (!new File(newPath).exists()) {// TODO 没有判断不是目录的情况
            return fail(command2, outs, Constants.RETURN_CODE_NOT_EXIST,
                    "File \"" + newPath + " \" does not exist.");
        } else {// 成功
            if (!ValueWidget.isNullOrEmpty(currentDir)) {
                clientEnvStatusBean.setPreviousDirectory(currentDir);
            }
            clientEnvStatusBean.setCurrentDirectory(newPath);
            setCmdDirectory(new File(newPath));
            writeSuccessHeader(outs, command2);
            // clientEnvStatusBean.setCurrentDirectory(newPath);
            return null;
        }

    }

    // private FailWarnInfoBean getFailWarnInfoBean(){
    //
    // }

    private FailWarnInfoBean fail(String command2, OutputStream outs,
                                  String code, String message) throws IOException {
        writeFailHeader(outs, command2);
        FailWarnInfoBean failWarnInfo = new FailWarnInfoBean();
        failWarnInfo.setCode(code);
        failWarnInfo.setCommand(command2);
        failWarnInfo.setMessage(message);
        return failWarnInfo;
    }

    /***
     * 执行命令 retr.
     *
     * @param command2
     * @param outs
     * @return
     * @throws IOException
     */
    private Object retrOrStatAction(String command2, OutputStream outs)
            throws IOException {
        String path = CMDUtil.getRealMessage(command2,
                command2.split(" ", 2)[0]);
        if (ValueWidget.isNullOrEmpty(path)) {
            return fail(command2, outs, Constants.RETURN_CODE_LACK_PARAMETER,
                    "命令 \"" + command2 + " \" 缺少参数.");
        }
        if (!SystemHWUtil.isAbsolutePath(path)) {
            path = new File(clientEnvStatusBean.getCurrentDirectory(), path)
                    .getAbsolutePath();
        }
        boolean isExist = FileUtils.isFile(path);
        if (!isExist) {// fail or warn
            return fail(command2, outs, Constants.RETURN_CODE_NOT_EXIST,
                    "File \"" + path + " \" does not exist.");
        } // success
        writeSuccessHeader(outs, command2);
        FileBean2 fileBean = new FileBean2();
        fileBean.setFolder(false);// 不是文件夹，而是普通的文件
        fileBean.setFileName(SystemHWUtil.getFileSimpleName(path));
        fileBean.setSizeOfFile(FileUtils.getFileSize2(path));

        String[] cmdAndArg = command2.split(" ", 2);
        String cmd = cmdAndArg[0];// eg. "list"
        if (cmd.equals(Constants.COMMAND_RETR)) {
            writeObjec(fileBean, outs, true);
            System.out.println("client :write " + path);
            InputStream fileIns = FileUtils.writeFile2OutputStream(path, outs,
                    false);
            fileIns.close();
            System.out.println("client:retr over");
        } else if (cmd.equals(Constants.COMMAND_STAT)) {
            return fileBean;
        }
        return null;

    }

}
