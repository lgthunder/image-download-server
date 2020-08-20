package test.java.com.github.monkeywie.proxyee;

import com.example.lib.DownLoader;
import com.example.lib.Log;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;

import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;

public class ImageResponseHandle extends ImageCacheIntercept {


    String contentType;

    private long contentLength;


    List<byte[]> list = new LinkedList<>();

    @Override
    public void beforeRequest(Channel clientChannel, HttpRequest httpRequest, HttpProxyInterceptPipeline pipeline) throws Exception {
        super.beforeRequest(clientChannel, httpRequest, pipeline);
        pipeline.beforeRequest(clientChannel, httpRequest);
    }

    @Override
    public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) throws Exception {
        super.afterResponse(clientChannel, proxyChannel, httpResponse, pipeline);
        if (httpResponse instanceof DefaultHttpResponse) {
            DefaultHttpResponse response = (DefaultHttpResponse) httpResponse;
            contentType = response.headers().get("Content-Type");
            String length = response.headers().get("Content-Length");
            try {
                contentLength = Long.valueOf(length);
            } catch (Exception e) {
                contentLength = 0;
            }

//            host = ((InetSocketAddress) (proxy.pipeline().channel().remoteAddress())).getHostName();

//            System.out.println("client: " + clientChannel.toString() + "| proxy :" + proxy.channel() + "RESPONSE: ");

        }
    }


    @Override
    public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpContent httpContent, HttpProxyInterceptPipeline pipeline) throws Exception {
        saveResponseAsCache(httpContent);
        super.afterResponse(clientChannel, proxyChannel, httpContent, pipeline);
    }

    public void saveResponseAsCache(Object msg) {
        //client  L:/127.0.0.1:9999 - R:/127.0.0.1:59484

        if (contentLength < 1024 * 50) {
            return;
        }
        if (contentType == null || contentType.length() == 0) {
            return;
        }

        if (!contentType.contains("image")) {
            return;
        }
        if (msg instanceof DefaultHttpContent) {
            DefaultHttpContent response = (DefaultHttpContent) msg;
            ByteBuf chunk = response.content();
            if (chunk.isReadable()) {
//                chunk.retain();
                byte[] copy = new byte[chunk.readableBytes()];
                int readerIndex = chunk.readerIndex();
                chunk.getBytes(readerIndex, copy);
                list.add(copy);
            }
        }

        if (msg instanceof LastHttpContent) {
            Log.log("  RESPONSE |  url :" + host + "/" + url_);
            DownLoadExecutor.executor.execute(new Runnable() {
                @Override
                public void run() {
                    String name = CacheManager.getName(url_);
                    if (name.length() == 0) {
                        return;
                    }
                    File file = new File(CacheManager.getSavePath(host, url_), name);

                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    if (file.exists()) {
                        return;
                    }
                    try {
                        FileOutputStream os = new FileOutputStream(file.getPath());
                        for (byte[] byteBuf : list) {
                            os.write(byteBuf);
                        }
                        list.clear();
                        os.close();
                        Log.log("  RESPONSE | save url :" + url_ + "  to path : " + file.getPath());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
