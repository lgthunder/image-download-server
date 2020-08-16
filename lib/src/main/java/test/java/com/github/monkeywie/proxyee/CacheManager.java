package test.java.com.github.monkeywie.proxyee;

import com.example.lib.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;

public class CacheManager {

    private static final String CR = System.getProperty("line.separator");
    private boolean isCacheOpen = false;

    String contentType;

    public boolean hasCache(Channel ctx, Object msg, boolean isHttp) {
//        System.out.println(msg.getClass());
//        System.out.println(msg.toString());
        if (!isCacheOpen) return false;
        String filePath = FileUtils.DIR + File.separator + "test.jpg";
        if (msg instanceof DefaultHttpRequest) {

            DefaultHttpRequest request = (DefaultHttpRequest) msg;
            if (request.method() == HttpMethod.GET) {
                System.out.println(" GET :" + request.uri());
                System.out.println(" GET  REQUEST :" + request.toString());
                try {
                    File file = new File(filePath);
                    DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.OK);
                    response.headers().add("Content-Type", "image/jpg");
//                    ctx.write(file + " " + file.length() + CR);
                    ctx.write(response);
                    RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "r");
                    FileRegion region = new DefaultFileRegion(randomAccessFile.getChannel(), 0, randomAccessFile.length());
                    ctx.write(region);
                    ctx.writeAndFlush(CR);
                    randomAccessFile.close();
                    return true;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }

        return false;

    }


    public void saveResponseAsCache(Channel clientChannel, ChannelHandlerContext proxy, Object msg) {
        //client  L:/127.0.0.1:9999 - R:/127.0.0.1:59484

        if (msg instanceof DefaultHttpResponse) {
            DefaultHttpResponse response = (DefaultHttpResponse) msg;
            System.out.println(" GET RESPONSE :" + response.toString());
            String contentType = response.headers().get("Content-Type");

//            System.out.println("client: " + clientChannel.toString() + "| proxy :" + proxy.channel() + "RESPONSE: ");

        }

        if (msg instanceof DefaultHttpContent) {
            System.out.println(" GET RESPONSE_CONTENT :" + msg.toString());
//            DefaultHttpContent response = (DefaultHttpContent) msg;
        }

        if (msg instanceof LastHttpContent) {
//            System.out.println(proxy.channel()+ "RESPONSE: " + msg.getClass());

        }


    }
}
