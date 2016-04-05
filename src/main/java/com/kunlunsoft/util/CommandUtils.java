package com.kunlunsoft.util;


public class CommandUtils {

    /***
     * 判断是否是十六进制位串.
     * right :0f;
     * wrong:an;
     *
     * @param hex
     * @return
     */
    public static boolean isHex(String hex) {
        return hex.matches("^[0-9a-fA-F]*$");
    }

}
