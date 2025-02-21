package com.sctech.hj212distribute.socket;
import org.apache.log4j.Logger;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.StateMachineEnum;
import org.smartboot.socket.transport.AioSession;

public class ProtocolMessageProcessor implements MessageProcessor<String> {

    private Logger logger = Logger.getLogger(StringClient.class);

    @Override
    public void process(AioSession aioSession, String s) {
        logger.info("客户端收到："+s);
    }

    /**
     * 状态机事件,当枚举事件发生时由框架触发该方法
     *
     * @param session          本次触发状态机的AioSession对象
     * @param stateMachineEnum 状态枚举
     * @param throwable        异常对象，如果存在的话
     * @see StateMachineEnum
     */
    @Override
    public void stateEvent(AioSession session, StateMachineEnum stateMachineEnum, Throwable throwable) {
        if (stateMachineEnum == StateMachineEnum.DECODE_EXCEPTION
                || stateMachineEnum == StateMachineEnum.PROCESS_EXCEPTION) {
            //logger.error(TcpConstant.DATA_PROCESS_ERROR, throwable);
        }

        //客户端断开连接
        if (StateMachineEnum.NEW_SESSION.equals(stateMachineEnum)) {
            StringClient.isServerOk = true;
            logger.info("服务器连接成功");
        }
        //客户端断开连接
        if (StateMachineEnum.INPUT_SHUTDOWN.equals(stateMachineEnum)) {
            StringClient.isServerOk = false;
            logger.info("读通道已被关闭，通会话正在关闭中。");
        }
        //客户端断开连接
        if (StateMachineEnum.SESSION_CLOSING.equals(stateMachineEnum)) {
            StringClient.isServerOk = false;
            logger.info("会话正在关闭中。");
        }
        //客户端断开连接
        if (StateMachineEnum.REJECT_ACCEPT.equals(stateMachineEnum)) {
            StringClient.isServerOk = false;
            logger.info("拒绝接受连接。");
        }

        //客户端断开连接
        if (StateMachineEnum.SESSION_CLOSED.equals(stateMachineEnum)) {
            //根据session监听客户端状态
            //List<String> strings= getKeysByValue(TcpServer.recvDataHashMap,session);
            //从Map中移除该客户端所有消息，TODO 可以向Controller反馈状态变化，进而反馈给前端。
            //for (String str: strings) {
              //  TcpServer.recvDataHashMap.remove(str,session);
            //}
            logger.info("服务器断开");
            StringClient.isServerOk = false;
            StringClient.loadServer();
        }
    }

}
