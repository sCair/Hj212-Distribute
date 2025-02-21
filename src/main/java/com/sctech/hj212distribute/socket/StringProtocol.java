package com.sctech.hj212distribute.socket;

import org.smartboot.socket.Protocol;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class StringProtocol implements Protocol<String> {

    /**
     * 从 ByteBuffer 中解码数据
     * @param readBuffer 读取数据的 ByteBuffer
     * @param session AioSession 实例
     * @return 解码后的字符串
     */
    @Override
    public String decode(ByteBuffer readBuffer, AioSession session) {
        try {
            // 跳过第一个字节
            readBuffer.get();
            int remaining = readBuffer.remaining();
            byte[] b = new byte[remaining];
            readBuffer.get(b);
            readBuffer.mark();
            return new String(b, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // 处理异常，记录日志等
            System.err.println("解码数据时出现异常：" + e.getMessage());
            return null;
        }
    }
}