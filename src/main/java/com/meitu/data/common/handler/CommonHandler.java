package com.meitu.data.common.handler;

import com.meitu.rpc.netty4.http.HttpRequestHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zj
 * @since 2018/7/11
 */
public class CommonHandler extends HttpRequestHandler {
    private static final Logger LOG = LoggerFactory.getLogger(CommonHandler.class);

    private static final String BLANK = " ";

    private Map<String, Object> params = new HashMap<>();

    public CommonHandler(String uri, List<HttpMethod> httpMethods) {
        super(uri, httpMethods, true);
    }

    @Override
    protected DefaultFullHttpResponse handle(ChannelHandlerContext ctx, FullHttpRequest request) {
        HttpPostRequestDecoder decoder = null;
        try {
            final String uri = request.getUri();
            //request uri parameters
            this.putParams(new QueryStringDecoder(uri));
            if (request.getMethod() != HttpMethod.GET) {
                decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE), request);
                this.putParams(decoder);
            }

            /**
             * do anything you want
             */

        } catch (Exception e) {
            LOG.error(e.getMessage());
            return httpResponseError();
        } finally {
            if (decoder != null) {
                decoder.destroy();
            }
        }
        return null;
    }

    /**
     * post 请求的参数
     * @param decoder
     */
    protected void putParams(HttpPostRequestDecoder decoder) {
        if (decoder == null) {
            return;
        }

        for (InterfaceHttpData data : decoder.getBodyHttpDatas()) {
            putParams(data);
        }
    }

    /**
     * post 请求的参数
     * @param data
     */
    protected void putParams(InterfaceHttpData data) {
        final InterfaceHttpData.HttpDataType dataType = data.getHttpDataType();
        if (dataType == InterfaceHttpData.HttpDataType.Attribute) {
            Attribute attribute = (Attribute) data;

            try {
                this.putParams(attribute.getName(), attribute.getValue());
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    /**
     * post get 通用 填充请求的参数
     * @param key
     * @param values
     */
    protected void putParams(String key, Object values) {
        this.params.put(key, values);
    }

    /**
     * get 方法获取参数
     * @param decoder
     */
    protected void putParams(QueryStringDecoder decoder) {
        if (decoder != null) {
            List<String> valueList;
            for (Map.Entry<String, List<String>> entry : decoder.parameters().entrySet()) {
                valueList = entry.getValue();
                if (valueList != null) {
                    this.putParams(entry.getKey(), 1 == valueList.size() ? valueList.get(0) : valueList);
                }
            }
        }
    }

    private DefaultFullHttpResponse httpResponseError() {
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }
}
