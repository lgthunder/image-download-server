package test.java.com.github.monkeywie.proxyee;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;

public class CacheManager {

    public boolean hasCache(Channel clientChannel, Object msg, boolean isHttp) {
//        System.out.println(msg.getClass());
//        System.out.println(msg.toString());
        if (msg instanceof DefaultHttpRequest) {

            DefaultHttpRequest request = (DefaultHttpRequest) msg;
            if (request.method() == HttpMethod.GET) {
                System.out.println(" GET :" + request.uri());
            }

        }

        return false;

    }


    public void saveResponseAsCache(Channel clientChannel, ChannelHandlerContext ctx, Object msg) {

        if (msg instanceof DefaultHttpResponse) {
            DefaultHttpResponse response = (DefaultHttpResponse) msg;
//            if (response. == HttpMethod.GET) {
//                System.out.println(" GET :" + request.uri());
//            }


        }

        if (msg instanceof DefaultLastHttpContent) {
            DefaultLastHttpContent response = (DefaultLastHttpContent) msg;
        }

        System.out.println("RESPONSE: " + msg.getClass());
    }
}
