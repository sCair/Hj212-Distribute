package com.sctech.hj212distribute.utils;

import com.sctech.hj212distribute.SocketClientModel;
import org.smartboot.socket.transport.AioSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataCache {
    public static HashMap<String,String> map = new HashMap<String,String>();

    public static List<SocketClientModel> clientList = new ArrayList<SocketClientModel>();

}
