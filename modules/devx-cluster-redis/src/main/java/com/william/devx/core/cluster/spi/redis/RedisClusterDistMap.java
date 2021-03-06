package com.william.devx.core.cluster.spi.redis;

import com.william.devx.common.$;
import com.william.devx.core.cluster.ClusterDistMap;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;
import java.util.stream.Collectors;

public class RedisClusterDistMap<M> implements ClusterDistMap<M> {

    private String mapKey;
    private Class<M> clazz;
    private StringRedisTemplate redisTemplate;

    RedisClusterDistMap(String mapKey, Class<M> clazz, StringRedisTemplate redisTemplate) {
        this.mapKey = "devx:dist:map:" + mapKey;
        this.clazz = clazz;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void put(String key, M value) {
        redisTemplate.opsForHash().put(mapKey, key, $.json.toJsonString(value));
    }

    @Override
    public void putAsync(String key, M value) {
        new Thread(() -> put(key, value)).start();
    }

    @Override
    public void putIfAbsent(String key, M value) {
        redisTemplate.opsForHash().putIfAbsent(mapKey, key, value);
    }

    @Override
    public boolean containsKey(String key) {
        return redisTemplate.opsForHash().hasKey(mapKey, key);
    }

    @Override
    public Map<String, M> getAll() {
        Map<Object, Object> map = redisTemplate.opsForHash().entries(mapKey);
        if (map != null) {
            return map.entrySet().stream().collect(Collectors.toMap(i -> (String) (i.getKey()), i -> $.json.toObject(i.getValue(), clazz)));
        } else {
            return null;
        }
    }

    @Override
    public M get(String key) {
        Object result = redisTemplate.opsForHash().get(mapKey, key);
        if (result != null) {
            return $.json.toObject(result, clazz);
        } else {
            return null;
        }
    }

    @Override
    public void remove(String key) {
        redisTemplate.opsForHash().delete(mapKey, key);
    }

    @Override
    public void removeAsync(String key) {
        new Thread(() -> remove(key)).start();
    }

    @Override
    public void clear() {
        redisTemplate.delete(mapKey);
    }

}
