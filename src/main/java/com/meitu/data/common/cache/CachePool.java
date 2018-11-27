package com.meitu.data.common.cache;

import redis.clients.jedis.Jedis;

/**
 * @author zj
 * @since 2018/7/11
 */
public interface CachePool {
    Jedis getResource();

    void returnResource(Jedis jedis);

    void destroy();
}
