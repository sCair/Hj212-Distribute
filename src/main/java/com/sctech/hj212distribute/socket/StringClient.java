package com.sctech.hj212distribute.socket;

import org.apache.log4j.Logger;
import com.sctech.hj212distribute.utils.DataCache;
import com.sctech.hj212distribute.utils.HJ212;
import org.smartboot.socket.transport.AioQuickClient;
import org.smartboot.socket.transport.AioSession;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StringClient {
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 8210;
    private static WriteBuffer writeBuffer;
    private static AioQuickClient client;
    public static boolean isServerOk = false;
    private static int socketSleepCount = 0;
    private static final Logger logger = Logger.getLogger(StringClient.class);
    private static final ProtocolMessageProcessor processor = new ProtocolMessageProcessor();
    private static boolean locke = false;

    private static String host = DEFAULT_HOST;
    private static int port = DEFAULT_PORT;

    public static String getHost() {
        return host;
    }

    public static void setHost(String host) {
        StringClient.host = host;
    }

    public static int getPort() {
        return port;
    }

    public static void setPort(int port) {
        StringClient.port = port;
    }

    public static void startClient() throws IOException {
        loadServer();
        locke = true;
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            try {
                if (isServerOk) {
                    Iterator<String> iterator = DataCache.map.keySet().iterator();
                    if (iterator.hasNext()) {
                        String key = iterator.next();
                        String value = DataCache.map.get(key);
                        if (value != null) {
                            logger.info("处理报文：" + value);
                            String rData = HJ212.For2011To2051(value);
                            logger.info("发送报文：" + rData);
                            writeBuffer.write(rData.getBytes());
                            writeBuffer.flush();
                        }
                        DataCache.map.remove(key);
                    }
                } else {
                    socketSleepCount++;
                    if (socketSleepCount > 1000) {
                        socketSleepCount = 0;
                        loadServer();
                    } else {
                        if (socketSleepCount % 100 == 0) {
                            logger.info("服务器 " + (10 - socketSleepCount / 100) + " 秒后重新连接");
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("处理报文时出现异常", e);
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public static void loadServer() {
        try {
            client = new AioQuickClient(host, port, new StringProtocol(), processor);
            AioSession session = client.start();
            writeBuffer = session.writeBuffer();
            System.out.println("socket client success,host:" + host + " port:" + port);
            if (locke) {
                logger.info("重新连接服务器:" + host + " " + port);
            }
        } catch (IOException e) {
            logger.error("连接服务器失败", e);
        }
    }
}