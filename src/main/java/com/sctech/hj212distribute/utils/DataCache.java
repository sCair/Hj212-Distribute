package com.sctech.hj212distribute.utils;

import com.sctech.hj212distribute.SocketClientModel;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class DataCache {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String MAP_KEY_PREFIX = "data_cache_map:";
    private static final String CLIENT_LIST_KEY = "data_cache_client_list";

    public void putToMap(String key, String value) {
        redisTemplate.opsForValue().set(MAP_KEY_PREFIX + key, value);
    }

    public String getFromMap(String key) {
        return (String) redisTemplate.opsForValue().get(MAP_KEY_PREFIX + key);
    }

    public void removeFromMap(String key) {
        redisTemplate.delete(MAP_KEY_PREFIX + key);
    }

    public Set<String> getMapKeys() {
        return redisTemplate.keys(MAP_KEY_PREFIX + "*");
    }

    public void addToClientList(SocketClientModel scm) {
        redisTemplate.opsForList().rightPush(CLIENT_LIST_KEY, scm);
    }

    public List<SocketClientModel> getClientList() {
        List<SocketClientModel> clientList = new ArrayList<>();
        long size = redisTemplate.opsForList().size(CLIENT_LIST_KEY);
        for (long i = 0; i < size; i++) {
            clientList.add((SocketClientModel) redisTemplate.opsForList().index(CLIENT_LIST_KEY, i));
        }
        return clientList;
    }
}