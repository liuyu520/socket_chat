package com.kunlunsoft.util;

import com.common.util.MyProcess;
import com.common.util.SystemHWUtil;
import com.common.util.TLVUtil;
import com.kunlunsoft.dict.Constants;
import com.string.widget.util.ValueWidget;
import com.swing.messagebox.GUIUtil23;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.io.*;
import java.util.List;

public class ShellSwingWorker extends SwingWorker<Boolean, Character> {
    public static final String FIX_PREFIX_COMMAND = "cmd /c ";
    /***
     * logging
     */
    protected Logger logger = Logger.getLogger(this.getClass());
    /***
     * 执行命令的正常输出（对程序来说是输入）
     */
    private BufferedReader br_right = null;
    /***
     * 执行命令的错误输出（对程序来说是输入）
     */
    private BufferedReader br_error = null;
    /***
     * 进程封装类
     */
    private MyProcess myprocess = null;
    private char word = ' ';
    private int tmp = 0;
    private boolean isPrintVerbose = false;
    private StringBuffer stringbuf = new StringBuffer();
    private JTextPane resultTP = null;
    /***
     * result textarea' document
     */
    private Document document;
    /***
     * charset of result
     */
    private String encoding;
    /***
     * 客户端socket 的OutputStream
     */
    private BufferedWriter clientSocketWriter = null;
    /***
     * 字符个数
     */
    private long charCount = 0;

    public ShellSwingWorker(MyProcess myprocess, BufferedReader br) {
        this.br_right = br;
        this.myprocess = myprocess;

    }

    /***
     * 构造方法.
     *
     * @param process
     * @param textPane
     * @param encoding
     */
    public ShellSwingWorker(Process process, OutputStream outs, String encoding) {
        MyProcess proc = null;
        proc = new MyProcess(process);
        construct(proc, null, encoding);

        try {
            clientSocketWriter = new BufferedWriter(new OutputStreamWriter(
                    outs, this.encoding));
            outs.write((byte) 1);
            outs.write(Constants.encodingMap.get(this.encoding));
//			clientSocketWriter.write("a");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /***
     * 构造方法.
     *
     * @param myprocess
     * @param textPane
     * @param encoding
     */
    public ShellSwingWorker(MyProcess myprocess, JTextPane textPane,
                            String encoding) {
        construct(myprocess, textPane, encoding);

    }

    public static void main(String[] args) {
        String lengthHex = TLVUtil.toHex(122);//长度占用的字符数
        System.out.println(lengthHex);
        int old = TLVUtil.hexToInt(lengthHex);
        System.out.println(old);
    }

    private void construct(MyProcess myprocess, JTextPane textPane,
                           String encoding) {
        this.myprocess = myprocess;
        if (ValueWidget.isNullOrEmpty(encoding)) {
            encoding = SystemHWUtil.CURR_ENCODING;
        }
        InputStream input_right = myprocess.getInputStream();
        InputStream input_error = myprocess.getErrorStream();
        int rigth_size = 0;
        int error_size = 0;

        this.encoding = encoding;
        try {
            br_right = new BufferedReader(new InputStreamReader(
                    input_right, this.encoding), 4096);
            rigth_size = input_right.available();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            br_error = new BufferedReader(new InputStreamReader(
                    input_error, this.encoding), 4096);
            error_size = input_error.available();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.resultTP = textPane;
        if (!ValueWidget.isNullOrEmpty(resultTP)) {
            document = this.resultTP.getDocument();
        }
        int charCount2 = rigth_size + error_size;
//		System.out.println("charCount2:"+charCount2);//output :0

    }

    @Override
    protected Boolean doInBackground() throws Exception {
        while ((tmp = br_right.read()) != -1) {
            word = (char) tmp;
            publish(word);
        }
        while ((tmp = br_error.read()) != -1) {
            word = (char) tmp;
            publish(word);
        }
        if (isPrintVerbose)// 是否打印详细信息
        {
            System.out.println("doInBackground() over");
        }
        return true;
    }

    @Override
    protected void process(List<Character> chunks) {
        for (char temp : chunks) {
            {
//				 System.out.print(temp);
                // this.resultTP.setText(this.stringbuf.toString());//效率低
                int leng2 = this.stringbuf.length();
                try {
                    if (!ValueWidget.isNullOrEmpty(document)) {
                        // 追加
                        document.insertString(leng2, String.valueOf(temp), null);
                    }
                    //打印命令执行结果的输出
                    System.out.print(temp);
                    clientSocketWriter.write(temp);
//					System.out.print(temp);
                    charCount++;//计数读取的char
                    if (charCount % 10 == 0) {
                        clientSocketWriter.flush();
                    }
                } catch (BadLocationException e) {
                    GUIUtil23.warningDialog(e.getMessage());
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                this.stringbuf.append(temp);
            }
        }
    }

    public StringBuffer getStringbuf() {
        return stringbuf;
    }

    /***
     * main thread can't execute next command(below waitFor()) until done() is
     * executed;if done() hasn't be executed,this.myprocess.waitFor() will wait
     */
    @Override
    protected void done() {
        if (isPrintVerbose) {
            System.out.println("done() is executed");
        }
        if (!ValueWidget.isNullOrEmpty(br_right)) {
            try {
                br_right.close();
                br_error.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String className = this.getClass().getSimpleName();
        String logMesg = className
                + ".done() is executed successfully.";
        logger.debug(logMesg);
        System.out.println(logMesg);
        try {
            //最后是"eof"表示完毕
            clientSocketWriter.write(Constants.TAG_END_OF_STREAM);//结束标记
//			String lengthTlv=TLVUtil.getTLVLengthHex(getCharCount());
            String dataLengthHex = Long.toHexString(getCharCount());//表示长度
//			System.out.println("hex:"+hex);
            String length_lengthHex = TLVUtil.toHex(dataLengthHex.length());//长度占用的字符数
//			System.out.println("lengthHex:"+lengthHex);
            if (length_lengthHex.length() != 2) {
                /*长度必须是2，这是我的规定*/
                throw new RuntimeException(this.getClass().getSimpleName() + " lengthHex must has two char");
            }
            clientSocketWriter.write(length_lengthHex);//hex 的长度
            clientSocketWriter.write(dataLengthHex);//表示传输的字符有多少个,十六进制形式
            clientSocketWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(className + "读取的字符（不是字节）个数：" + getCharCount());


        this.myprocess.stopLoop();
    }

    public MyProcess getProcess() {
        return myprocess;
    }

    public long getCharCount() {
        return charCount;
    }
}
