package com.meitu.data.common.handler;

import com.google.common.collect.Lists;
import com.meitu.data.common.cache.RedisPool;
import com.meitu.light.common.context.Context;
import com.meitu.rpc.netty4.RetryRejectExceptionHandler;
import com.meitu.rpc.netty4.http.HttpNettyNioServer;
import io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zj
 * @since 2018/7/11
 */
public class CommonServer {
    private static final Logger LOG = LoggerFactory.getLogger(CommonServer.class);

    private Context context;
    private RedisPool redisPool;

    /**
     * http 请求处理线程池
     */
    private ThreadPoolExecutor poolExecutor;
    private HttpNettyNioServer nettyNioServer;

    private AtomicBoolean isRuning = new AtomicBoolean(false);

    private CommonServer(Context context) throws IOException {
        this.context = context;
    }

    void start() throws Exception {
        if (isRuning.compareAndSet(false,  true)) {
            poolExecutor = new ThreadPoolExecutor(
                    context.getInt(HttpServerConstants.HTTP_SERVER_CORE_POOL_SIZE, 10),
                    context.getInt(HttpServerConstants.HTTP_SERVER_MAX_POOL_SIZE, 10),
                    context.getInt(HttpServerConstants.HTTP_SERVER_THREAD_MAX_ALIVE_TIME, 10000),
                    TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<Runnable>(
                            context.getInt(HttpServerConstants.HTTP_SERVER_TASK_QUEUE_CAPACITY, 1000)
                    ), new RetryRejectExceptionHandler(
                    context.getInt(HttpServerConstants.HTTP_SERVER_HANDLER_RETRY_TIMES, 2)
            ));

            List<HttpMethod> httpMethodList = Lists.newArrayList(HttpMethod.POST);
            httpMethodList = Lists.newArrayList(HttpMethod.GET, HttpMethod.POST);
            nettyNioServer = new HttpNettyNioServer.HttpNettyNioServerBuilder()
                    .setName("invoke-Http-Server")
                    .setVersion("1.0")
                    .setMaxContentLength(context.getInt(HttpServerConstants.HTTP_MAX_CONTENT_LENGTH,
                            5 * 1024 * 1024))
                    .setBossThreads(context.getInt(HttpServerConstants.HTTP_NETTY_BOSS_GROUP_THREAD_NUM, 10))
                    .setWorkerThreads(context.getInt(HttpServerConstants.HTTP_NETTY_WORKER_GROUP_THREAD_NUM, 10))
                    .setPort(context.getInt(HttpServerConstants.HTTP_PORT))
                    .setSoRcvbuf(context.getInt(HttpServerConstants.SO_RCVBUF_SIZE, 64 * 1024))
                    .setSoBacklog(context.getInt(HttpServerConstants.SO_BACKLOG, 1000))
                    .setTcpNoDelay(true)
                    .setSoKeepAlive(true)
                    .setAsyncHandlerThreadPool(poolExecutor)
                    .addHttpUrlRequest(new CommonHandler("/", httpMethodList))
                    .build();

            nettyNioServer.start();
        }
    }

    void stop() {
        if (isRuning.compareAndSet(true, false)) {
            if (nettyNioServer != null) {
                nettyNioServer.stop();
            }
            if (poolExecutor != null) {
                poolExecutor.shutdownNow();
            }
        }
    }
}
