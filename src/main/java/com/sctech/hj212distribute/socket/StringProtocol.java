package com.sctech.hj212distribute.socket;
import org.smartboot.socket.Protocol;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class StringProtocol implements Protocol<String> {

    @Override
    public String decode(ByteBuffer readBuffer, AioSession session) {
        readBuffer.get();
        int remaining = readBuffer.remaining();
        byte[] b = new byte[remaining];
        readBuffer.get(b);
        readBuffer.mark();
        //System.out.println("收到报文3："+new String(b, StandardCharsets.UTF_8));
        return new String(b,StandardCharsets.UTF_8);

        /*

        System.out.println("收到报文："+remaining+" - "+Integer.BYTES);
        if (remaining < Integer.BYTES) {
            System.out.println("收到报文1：");
            return null;
        }
        //readBuffer.mark();
        int length = readBuffer.getInt();
        System.out.println("收到报文2："+length +" - "+ readBuffer.remaining());
        /*if (length > readBuffer.remaining()) {
            readBuffer.reset();
            return null;
        }
        byte[] b = new byte[length];
        readBuffer.get(b);
        System.out.println("收到报文4："+length +" - "+ readBuffer.remaining());
        readBuffer.mark();
        System.out.println("收到报文3："+new String(b));
        return new String(b);*/
    }
}