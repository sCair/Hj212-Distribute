package com.sctech.hj212distribute.socket;

import org.apache.log4j.Logger;
import com.sctech.hj212distribute.utils.DataCache;
import com.sctech.hj212distribute.utils.HJ212;
import org.smartboot.socket.transport.AioQuickClient;
import org.smartboot.socket.transport.AioSession;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class StringClient {
    private static String host = "127.0.0.1";
    private static int port = 8210;
    private static WriteBuffer writeBuffer;
    private static AioQuickClient client;
    public static boolean isServerOk = false;
    public static int socketSleepCount = 0;
    // 实例化Logger
    private static Logger logger = Logger.getLogger(StringClient.class);

    private static ProtocolMessageProcessor processor = new ProtocolMessageProcessor();

    private static boolean locke = false;

    public static void startClient() throws IOException {

        loadServer();
        locke = true;
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if(isServerOk) {
                    Iterator<String> iterator = DataCache.map.keySet().iterator();
                    //logger.info("当前待报文数量："+keys.size());
                    if (iterator.hasNext()) {
                        //System.out.println("DataCache:" + keys.size());
                        String key = iterator.next();
                        String value = DataCache.map.get(key);
                        if(value != null){
                            logger.info("处理报文："+value);
                            //System.out.println("处理报文：" + value);
                            try {
                                String rData = HJ212.For2011To2051(value);
                                logger.info("发送报文："+rData);
                                writeBuffer.write(rData.getBytes());
                                writeBuffer.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        DataCache.map.remove(key);
                    }
                }else{
                    socketSleepCount++;
                    if(socketSleepCount>1000){
                        socketSleepCount=0;
                        loadServer();
                    }else{
                        if(socketSleepCount % 100 == 0){
                            logger.info("服务器 "+(10-socketSleepCount/100)+" 秒后重新连接");
                        }

                    }
                }
            }
        };
        timer.schedule(task,1000,100);
    }
    public static void loadServer(){
        try {

            client = new AioQuickClient(host, port, new StringProtocol(), processor);
            AioSession session = client.start();
            writeBuffer = session.writeBuffer();
            System.out.println("socket client success,host:"+host+" port:"+port);
            if(locke){
                logger.info("重新连接服务器:"+host+" "+port);
            }
        }catch (IOException e){
            e.printStackTrace();
        }

    }
}
