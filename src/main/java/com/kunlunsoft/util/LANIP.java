package com.kunlunsoft.util;

import com.common.util.SystemHWUtil;
import com.string.widget.util.ValueWidget;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.Vector;

public class LANIP {
    public static InetAddress[] getAllOnline(String charset) {
        return getAllOnline(charset, null);
    }

    public static InetAddress[] getAllOnline(String charset, String includedIp) {
        Vector v = new Vector(50);
        try {
            // Process process1 =
            // Runtime.getRuntime().exec("ping -w 2 -n 1 192.168.1.%i");
            // process1.destroy();
            Process process = Runtime.getRuntime().exec("arp -a");
            InputStreamReader inputStr = new InputStreamReader(
                    process.getInputStream(), charset);
            BufferedReader br = new BufferedReader(inputStr);
            String temp = "";
            br.readLine();
            br.readLine();
            br.readLine();// 此后开始读取IP地址，之前为描述信息，忽略。
            while ((temp = br.readLine()) != null) {
//				System.out.println(temp);
                if (!ValueWidget.isNullOrEmpty(temp)) {
                    StringTokenizer tokens = new StringTokenizer(temp);
                    String x;
                    InetAddress add = null;
                    try {
                        add = InetAddress.getByName(x = tokens
                                .nextToken());
                    } catch (java.net.UnknownHostException e) {
                        continue;
                    }
                    // System.out.println(x);
                    v.add(add);
                    // System.out.println(add);
                }
            }
            System.out.println(SystemHWUtil.DIVIDING_LINE);
            v.add(InetAddress.getLocalHost());
            process.destroy();
            br.close();
            inputStr.close();
        } catch (Exception e) {
            System.out.println("可能是网络不可用。");
            e.printStackTrace();
        }
        int cap = v.size();
        int arrLength = cap;
        if (!ValueWidget.isNullOrEmpty(includedIp)) {
            arrLength = cap + 1;
        }
        InetAddress[] addrs = new InetAddress[arrLength];
        for (int i = 0; i < cap; i++) {
            addrs[i] = (InetAddress) v.elementAt(i);
//			 System.out.println(addrs[i]);
        }
        if (!ValueWidget.isNullOrEmpty(includedIp)) {
            try {
                addrs[cap] = InetAddress.getByName(includedIp);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        return addrs;

    }

    public static void main(String args[]) {
        InetAddress[] i = LANIP.getAllOnline("GBK");

    }
}
