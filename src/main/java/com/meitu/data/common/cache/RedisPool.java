package com.meitu.data.common.cache;

import com.meitu.light.common.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

/**
 * redis缓存控制
 * @author zj
 * @since 2018/7/2
 */
public class RedisPool implements CachePool {

    private static final Logger LOG = LoggerFactory.getLogger(RedisPool.class);
    private JedisPool jedisPool;
    private int redisDb;

    /**
     * 初始化连接池
     * @param context
     */
    public RedisPool(Context context) {
        try {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxIdle(context.getInt(RedisConstants.REDIS_MAX_IDLE,
                    10));
            config.setMaxTotal(context.getInt(RedisConstants.REDIS_MAX_TOTAL,
                    100));
            config.setMinIdle(context.getInt(RedisConstants.REDIS_MIN_IDLE,
                    4));
            config.setTestOnBorrow(context.getBoolean(RedisConstants.REDIS_TEST_ON_BORROW,
                    true));
            config.setMaxWaitMillis(context.getLong(RedisConstants.REDIS_MAX_WAIT,
                    10000));
            int port = context.getInt(RedisConstants.REDIS_PORT);
            String host = context.getParameter(RedisConstants.REDIS_HOST);
            String password = context.getParameter(RedisConstants.REDIS_PASSWORD);
            this.redisDb = context.getInt(RedisConstants.REDIS_DB);

            jedisPool = new JedisPool(config, host, port, Protocol.DEFAULT_TIMEOUT, password, redisDb);
            LOG.info("connect to redis node");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public Jedis getResource() {
        return jedisPool.getResource();
    }

    @Override
    public void returnResource(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

    @Override
    public void destroy() {
        jedisPool.destroy();
    }
}
