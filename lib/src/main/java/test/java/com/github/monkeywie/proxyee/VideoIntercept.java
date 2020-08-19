package test.java.com.github.monkeywie.proxyee;

import com.example.lib.FileUtils;
import com.example.lib.Log;
import com.github.monkeywie.proxyee.intercept.HttpProxyIntercept;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.util.ProtoUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;

public class VideoIntercept extends HttpProxyIntercept {

    String YOUTUBE = "googlevideo.com";
    String host = "";
    String contentType;
    String extensionName = "";

    List<byte[]> list = new LinkedList<>();

    @Override
    public void beforeRequest(Channel clientChannel, HttpRequest httpRequest, HttpProxyInterceptPipeline pipeline) throws Exception {
        ProtoUtil.RequestProto proto = ProtoUtil.getRequestProto(httpRequest);
        host = proto.getHost();
        super.beforeRequest(clientChannel, httpRequest, pipeline);


    }

    @Override
    public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) throws Exception {
        contentType = httpResponse.headers().get(HttpHeaderNames.CONTENT_TYPE);
        super.afterResponse(clientChannel, proxyChannel, httpResponse, pipeline);
    }

    @Override
    public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpContent httpContent, HttpProxyInterceptPipeline pipeline) throws Exception {

        if (host.contains(YOUTUBE)) {

            if (contentType.contains("video/webm") || contentType.contains("video/mp4") || contentType.contains("video/flv")) {
                extensionName = contentType.split("/")[1];
//                saveVideo(httpContent);
            }

        }
        super.afterResponse(clientChannel, proxyChannel, httpContent, pipeline);
    }

    public void saveVideo(HttpContent httpContent) {
        if (httpContent instanceof DefaultHttpContent) {
            DefaultHttpContent response = (DefaultHttpContent) httpContent;
            ByteBuf chunk = response.content();
            if (chunk.isReadable()) {
//                chunk.retain();
                byte[] copy = new byte[chunk.readableBytes()];
                int readerIndex = chunk.readerIndex();
                chunk.getBytes(readerIndex, copy);
                list.add(copy);
            }

        }

        if (httpContent instanceof LastHttpContent) {
            File file = new File(FileUtils.DIR, System.currentTimeMillis() + "." + extensionName);
            ;
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
                Log.log("  RESPONSE | SAVE VIDEO :" + "  to path : " + file.getPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}