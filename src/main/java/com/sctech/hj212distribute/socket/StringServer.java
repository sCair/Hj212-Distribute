package com.sctech.hj212distribute.socket;

import com.sctech.hj212distribute.SocketClientModel;
import com.sctech.hj212distribute.utils.HJ212;
import com.sctech.hj212distribute.utils.UUIDUtils;
import org.apache.log4j.Logger;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.transport.AioQuickServer;
import org.smartboot.socket.transport.AioSession;
import org.smartboot.socket.transport.WriteBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class StringServer {

    private static int port = 8310;
    private static Logger logger = Logger.getLogger(StringClient.class);
    private static RedisTemplate<String, Object> redisTemplate;

    public static void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        StringServer.redisTemplate = redisTemplate;
    }

    public static void startServer() throws IOException {
        MessageProcessor<String> processor = (session, msg) -> {
            try {
                String uuid = UUIDUtils.generateUUID();

                logger.info("收到客户端 " + session.getRemoteAddress().toString() + " 的消息: " + msg);
                if (!msg.isEmpty()) {
                    ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
                    valueOps.set(uuid, "#" + msg, 10, TimeUnit.MINUTES); // 设置10分钟过期时间，可根据需求调整
                } else {
                    return;
                }
                SocketClientModel scm = new SocketClientModel();
                String[] bw = msg.split(";");
                String mn = "";
                for (String bwi : bw) {
                    if (bwi.contains("MN=")) {
                        logger.info("8");
                        mn = bwi.split("=")[1];
                    }
                }
                if (!mn.isEmpty()) {
                    boolean isMn = false;
                    try {
                        for (String bwi : bw) {
                            if (bwi.contains("DataTime=")) {
                                String dateStr = bwi.split("&&")[1];
                                dateStr = dateStr.split("=")[1];

                                LocalDateTime now = LocalDateTime.now();
                                String nowDate = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                                long m = StringServer.getTime(dateStr, nowDate);
                                if (m > 30) {
                                    // 对新链接设备校时
                                    StringServer.clientTiming(session, mn);
                                }
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    // 从Redis获取客户端列表
                    List<SocketClientModel> clientList = (List<SocketClientModel>) redisTemplate.opsForValue().get("clientList");
                    if (clientList != null) {
                        for (SocketClientModel sc : clientList) {
                            //logger.info("sc:"+sc+"--"+sc.get_mn());
                            if (Objects.equals(sc.get_mn(), mn)) {
                                isMn = true;
                            }
                        }
                    }
                    if (!isMn) {
                        scm.set_mn(mn);
                        scm.set_session(session);
                        // 保存客户端信息到Redis
                        if (clientList == null) {
                            clientList = java.util.Collections.singletonList(scm);
                        } else {
                            clientList.add(scm);
                        }
                        redisTemplate.opsForValue().set("clientList", clientList);
                        //redisTemplate.opsForValue().set("station:" + mn, mn);
                        logger.info("保存了客户端：" + mn);

                    } else {
                        logger.info("客户端已存在：" + mn);
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
            System.out.println("socket server success,port:" + port);
        } catch (IOException e) {
            logger.info("服务端启动失败，可能已启动或被占用");
        }

    }

    private static void clientTiming(AioSession as, String mn) {
        String hj = HJ212.getDataHj212(mn);
        logger.info(mn + " 设备时间偏差超过30秒！");
        logger.info(mn + " 开始校时 " + hj);
        try {
            WriteBuffer outputStream = as.writeBuffer();
            byte[] bytes = hj.getBytes();
            outputStream.write(bytes);
        } catch (IOException e) {
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