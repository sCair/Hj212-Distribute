package com.sctech.hj212distribute.socket;

import com.sctech.hj212distribute.utils.HJ212;
import org.apache.log4j.Logger;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

public class StringClient implements Runnable {

    private static Logger logger = Logger.getLogger(StringClient.class);
    private WriteBuffer writeBuffer;
    private boolean isServerConnected;
    private int socketSleepCount;
    private RedisTemplate<String, Object> redisTemplate;

    public StringClient(WriteBuffer writeBuffer, boolean isServerOk, RedisTemplate<String, Object> redisTemplate) {
        this.writeBuffer = writeBuffer;
        this.isServerConnected = isServerOk;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run() {
        if (isServerConnected) {
            ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
            Set<String> keys = redisTemplate.keys("*");
            if (keys != null) {
                Iterator<String> iterator = keys.iterator();
                //logger.info("当前待报文数量："+keys.size());
                if (iterator.hasNext()) {
                    //System.out.println("DataCache:" + keys.size());
                    String key = iterator.next();
                    String value = (String) valueOps.get(key);
                    if (value != null) {
                        logger.info("处理报文：" + value);
                        //System.out.println("处理报文：" + value);
                        try {
                            String rData = HJ212.For2011To2051(value);
                            logger.info("发送报文：" + rData);
                            writeBuffer.write(rData.getBytes());
                            writeBuffer.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    redisTemplate.delete(key);
                }
            }
        } else {
            socketSleepCount++;
            if (socketSleepCount > 1000) {
                socketSleepCount = 0;
                // loadServer();
            } else {
                if (socketSleepCount % 100 == 0) {
                    logger.info("服务器 " + (10 - socketSleepCount / 100) + " 秒后重新连接");
                }
            }
        }
    }
}