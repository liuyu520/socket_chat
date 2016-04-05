package com.kunlunsoft.dict;

import com.common.util.SystemHWUtil;

import java.util.HashMap;
import java.util.Map;

public class Constants {
    /***
     * is command ,such list,loca
     */
    public static final String TYPE_COMMAND = "comm";
    /***
     * 握手的命令前缀
     */
    public static final String TYPE_HANDSHAKE = "hand";
    /***
     * is chat message
     */
    public static final String TYPE_CHAT = "chat";
    /***
     * 若有参数，则列出参数指定目录的所有文件；否则（未指定参数）列出当前目录的所有文件
     */
    public static final String COMMAND_LIST = "list";

    /***
     * 回到上一次的当前目录
     */
    public static final String COMMAND_CD_OLD = "cd-";
    /***
     * 改变到父目录
     */
    public static final String COMMAND_CDUP = "cdup";
    /***
     * 删除文件（不能删除目录）
     */
    public static final String COMMAND_DELE = "dele";
    /***
     * 创建目录
     */
    public static final String COMMAND_MKD = "mkd";
    /***
     * 打印当前工作目录
     */
    public static final String COMMAND_PWD = "pwd";
    /**
     * 断开连接
     */
    public static final String COMMAND_QUIT = "quit";
    /***
     * 传输文件副本（客户端把文件传送给服务器）
     */
    public static final String COMMAND_RETR = "retr";
    /***
     * 删除目录（也可以删除文件）
     */
    public static final String COMMAND_RMD = "rmd";
    /***
     * 返回文件大小（若是目录，报错）
     */
    public static final String COMMAND_SIZE = "size";
    /***
     * 服务器把文件上传到客户端（保存为客户端本地文件）
     */
    public static final String COMMAND_STOR = "stor";
    /***
     * 返回操作系统类型（xp,win7,还是win8，linux）
     */
    public static final String COMMAND_SYST = "syst";
    /***
     * 查看文件的大小
     */
    public static final String COMMAND_STAT = "stat";
    /***
     * 执行本地命令，如“loca net start mysql”表示启动mysql服务
     * （Local command的简称）
     */
    public static final String COMMAND_LOCA = "loca";
//	public static final String quit="quit";

    /***
     * return result
     */
    public static final String RETURN_IDENTIFICATION_SUCCESS = "succ";
    public static final String RETURN_IDENTIFICATION_FAIL = "fail";
    public static final String RETURN_IDENTIFICATION_WARN = "warn";
    /***
     * the max length of command
     */
    public static final int MAX_LENGTH_COMMAND = 5;

    /***
     * 文件不存在
     */
    public static final String RETURN_CODE_NOT_EXIST = "100";
    /***
     * cdup时，父目录不存在.
     */
    public static final String RETURN_CODE_FATHER_FOLDER_NOT_EXIST = "101";
    /***
     * 文件存在但删除失败
     */
    public static final String RETURN_CODE_FAIL2DEL = "102";

    /***
     * 不是文件，是目录
     */
    public static final String RETURN_CODE_IS_FOLDER = "103";

    /***
     * 创建目录失败
     */
    public static final String RETURN_CODE_FAIL2MKDIR = "105";

    /***
     * 保存文件失败
     */
    public static final String RETURN_CODE_FAIL2SAVE = "107";
    public static final String RETURN_CODE_LACK_PARAMETER = "104";
    /***
     * 命令不存在.
     */
    public static final String RETURN_CODE_INVALID_COMMAND = "108";
    /***
     * 命令执行结果中包含对象
     */
    public static final byte[] CMD_RESULT_APPEND_OBJECT = new byte[]{0, 1};
    /***
     * 命令执行结果中不包含对象
     */
    public static final byte[] CMD_RESULT_NONE_OBJECT = new byte[]{1, 0};

    public static final String TAG_END_OF_STREAM = "eof###";
    /***
     * (byte)1:GBK ;<br>(byte)2:UTF-8 ; <br>(byte)3:GB2312  ;
     * <br>(byte)4: GB18030  ;
     */
    public static Map<String, Byte> encodingMap = new HashMap<String, Byte>();

    static {
        encodingMap.put(SystemHWUtil.CHARSET_GBK, (byte) 1);
        encodingMap.put(SystemHWUtil.CHARSET_UTF, (byte) 2);
        encodingMap.put(SystemHWUtil.CHARSET_GB2312, (byte) 3);
        encodingMap.put(SystemHWUtil.CHARSET_GB18030, (byte) 4);
    }

}
