package com.sctech.hj212distribute.utils;

import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HJ212 {
    private static final int POLYNOMIAL = 0x1021;
    private static final String CN_2011 = "CN=2011";
    private static final String CN_2051 = "CN=2051";
    private static final String RTD = "-Rtd=";
    private static final String AVG = "-Avg=";

    public static String For2011To2051(String data) {
        String rDate = data;
        if (data.contains(CN_2011)) {
            String hj2011 = data;
            hj2011 = hj2011.replaceFirst(CN_2011, CN_2051);
            hj2011 = hj2011.replace(RTD, AVG);
            hj2011 = hj2011.substring(0, hj2011.length() - 6);
            String crc = CRC16(hj2011.substring(6));
            rDate = hj2011 + crc.toUpperCase() + "\r\n";
        }
        return rDate;
    }

    public static String getDataHj212(String _mn) {
        String hj = "QN=;ST=23;CN=1012;PW=123456;MN={mn};CP=&&SystemTime={datetime}&&";
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        hj = hj.replace("{mn}", _mn);
        hj = hj.replace("{datetime}", nowDate);
        String crc = CRC16(hj);
        hj = "##" + hj.length() + hj + crc.toUpperCase();
        return hj;
    }

    public static String CRC16(String temp) {
        try {
            Integer[] regs = new Integer[temp.length()];
            for (int i = 0; i < temp.length(); i++) {
                regs[i] = (int) temp.charAt(i);
            }
            int por = 0xFFFF;
            for (int j = 0; j < regs.length; j++) {
                por = por >> 8;
                por ^= regs[j];
                for (int i = 0; i < 8; i++) {
                    if ((por & 0x01) == 1) {
                        por = por >> 1;
                        por = por ^ 0xa001;
                    } else {
                        por = por >> 1;
                    }
                }
            }
            return String.format("%04x", por);
        } catch (Exception e) {
            System.err.println("计算 CRC16 时出现异常：" + e.getMessage());
            return "";
        }
    }
}