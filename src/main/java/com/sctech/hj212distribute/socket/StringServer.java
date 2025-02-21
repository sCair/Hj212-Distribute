package com.sctech.hj212distribute.socket;

import com.sctech.hj212distribute.SocketClientModel;
import com.sctech.hj212distribute.utils.DataCache;
import com.sctech.hj212distribute.utils.HJ212;
import com.sctech.hj212distribute.utils.UUIDUtils;
import org.apache.log4j.Logger;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.transport.AioQuickServer;
import org.smartboot.socket.transport.AioSession;
import org.smartboot.socket.transport.WriteBuffer;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;


public class StringServer {

    private static int port = 8310;
    private static Logger logger = Logger.getLogger(StringClient.class);

    //private static RedisTemplate<String, String> redisTemplate;

    public static void startServer() throws IOException {
        MessageProcessor<String> processor = (session, msg) -> {
            try {
                String uuid = UUIDUtils.generateUUID();

                logger.info("收到客户端 "+session.getRemoteAddress().toString()+" 的消息: " + msg);
                if(!msg.isEmpty()) {
                    DataCache.map.put(uuid, "#" + msg);
                }else{
                    return;
                }
                SocketClientModel scm = new SocketClientModel();
                String[] bw = msg.split(";");
                String mn = "";
                for(String bwi : bw){
                    if(bwi.contains("MN=")){
                        logger.info("8");
                        mn = bwi.split("=")[1];
                    }
                }
                if(!mn.isEmpty()) {
                    boolean isMn = false;
                    try {
                        for(String bwi : bw){
                            if (bwi.contains("DataTime=")) {
                                String dateStr = bwi.split("&&")[1];
                                dateStr = dateStr.split("=")[1];

                                LocalDateTime now = LocalDateTime.now();
                                String nowDate = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                                long m = StringServer.getTime(dateStr,nowDate);
                                if(m>30){
                                    //对新链接设备校时
                                    StringServer.clientTiming(session, mn);
                                }
                            }
                        }
                    }catch (ParseException e) {
                        e.printStackTrace();
                    }
                    for (SocketClientModel sc : DataCache.clientList) {
                        //logger.info("sc:"+sc+"--"+sc.get_mn());
                        if (Objects.equals(sc.get_mn(), mn)) {
                            logger.info("12");
                            isMn = true;
                        }
                    }
                    logger.info("13");
                    if (!isMn) {
                        scm.set_mn(mn);
                        scm.set_session(session);
                        //保存客户端信息
                        DataCache.clientList.add(scm);
                        //BoundValueOperations ops = redisTemplate.boundValueOps("stu:user:1001");
                        //StringServer.redisTemplate.opsForValue().set("station:"+mn,mn);
                        logger.info("保存了客户端：" + mn);

                    }else{
                        logger.info("客户端已存在："+mn);
                    }
                }
            } catch (IOException e) {
                logger.info("*******");
                e.printStackTrace();
            }
        };
        try {
            AioQuickServer server = new AioQuickServer(port, new StringProtocol(), processor);
            server.start();
            System.out.println("socket server success,port:"+port);
        }catch (IOException e){
            logger.info("服务端启动失败，可能已启动或被占用");
        }

        /*
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                LocalDateTime now = LocalDateTime.now();
                if(now.getHour()==17&&now.getMinute()==40&&now.getSecond()==0) {
                    if (!DataCache.clientList.isEmpty()) {
                        for (SocketClientModel scm : DataCache.clientList) {
                            String hj = HJ212.getDataHj212(scm.get_mn());
                            try {
                                logger.info(scm.get_mn() + "开始校时" + hj);
                                WriteBuffer outputStream = scm.get_session().writeBuffer();
                                byte[] bytes = hj.getBytes();
                                outputStream.write(bytes);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        };
        timer.schedule(task,1000,1000);
        */
    }
    private static void clientTiming(AioSession as, String mn){
        String hj = HJ212.getDataHj212(mn);
        logger.info(mn+" 设备时间偏差超过30秒！");
        logger.info(mn+" 开始校时 " + hj);
        try {
            WriteBuffer outputStream = as.writeBuffer();
            byte[] bytes = hj.getBytes();
            outputStream.write(bytes);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public static long getTime(String startTime, String endTime) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        long eTime = df.parse(endTime).getTime();
        long sTime = df.parse(startTime).getTime();
        long diff = (eTime - sTime) / 1000;
        return diff;
    }
}
