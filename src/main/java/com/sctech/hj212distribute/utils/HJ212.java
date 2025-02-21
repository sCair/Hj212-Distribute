package com.sctech.hj212distribute.utils;

import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HJ212 {
    private static final int POLYNOMIAL = 0x1021;

    //"##0215QN=202411123203230940;ST=39;CN=2011;PW=123456;MN=0507A010003197;Flag=4;CP=&&DataTime=2024112320423050;a01001-Rtd=19.3;a01006-Rtd=101.620;a01007-Rtd=0.0;a01008-Rtd=59;a34002-Rtd=19;a34004-Rtd=10;Flag=N&&3240"
    public static String For2011To2051(String data) {
        String rDate = data;
        if(data.contains("CN=2011")){
            String hj2011 = data;
            hj2011 = hj2011.replaceFirst("CN=2011", "CN=2051");
            hj2011 = hj2011.replace("-Rtd=","-Avg=");
            hj2011 = hj2011.substring(0, hj2011.length() - 6);
            String crc = CRC16(hj2011.substring(6));
            //System.out.println("crc:"+crc);
            rDate = hj2011+crc.toUpperCase()+"\r\n";
        }
        return rDate;

    }
    public static String getDataHj212(String _mn){
        String hj = "QN=;ST=23;CN=1012;PW=123456;MN={mn};CP=&&SystemTime={datetime}&&";
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        hj = hj.replace("{mn}",_mn);
        hj = hj.replace("{datetime}",nowDate);
        String crc = CRC16(hj);
        //crc = String.format("%04d",Integer.valueOf(crc));
        hj = "##"+hj.length()+hj+crc.toUpperCase();
        return hj;
    }
    public static String CRC16(String temp) {
        System.out.println("crc："+temp);
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
                } else
                    por = por >> 1;
            }
        }
        //return Integer.toHexString(por);
        //System.out.println(por);
        return String.format("%04x",por);
    }
}
