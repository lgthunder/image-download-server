package test.java.com.github.monkeywie.proxyee;

import com.example.lib.DownLoader;
import com.example.lib.Log;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;

import java.io.File;
import java.io.IOException;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.ReferenceCountUtil;

public class ImageRequestHandle extends ImageCacheIntercept {


    @Override
    public void beforeRequest(Channel clientChannel, HttpRequest httpRequest, HttpProxyInterceptPipeline pipeline) throws Exception {
        initRequest(httpRequest);
        if (hasCache(clientChannel, httpRequest)) {
            return;
        }
        super.beforeRequest(clientChannel, httpRequest, pipeline);
    }


    public boolean hasCache(Channel ctx, Object msg) {
//        System.out.println(msg.getClass());
//        System.out.println(msg.toString());
        if (!CacheManager.isCacheOpen) return false;

        if (msg instanceof DefaultHttpRequest) {
            DefaultHttpRequest request = (DefaultHttpRequest) msg;
            if (request.method() == HttpMethod.GET) {
//                System.out.println(" GET | host: " + url.getUrl().getHost() + " path: " + url.getUrl().getPath());
//                System.out.println(" GET  REQUEST :" + request.toString());
                try {
                    File file = new File(CacheManager.getSavePath(host, url_), CacheManager.getName(url_));
//                    File file = new File(FileUtils.DIR + File.separator + "test.jpg");
                    if (!file.exists()) {
                        return false;
                    }
                    String extensionName = CacheManager.getExtensionName(url_);
                    if (!DownLoader.isUrlAvailable(extensionName)) {
                        return false;
                    }
                    Log.log("  GET CACHE:" + CacheManager.isCacheOpen + "| get url :" + url_ + "  from path : " + file.getPath());
                    DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.OK);
                    response.headers().add("Content-Type", "image/" + CacheManager.getExtensionName(url_));
                    response.headers().add("Content-Length", file.length());
                    response.headers().add("Cache-From-Local", "Local");
                    ctx.write(response);
                    DownLoadExecutor.executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                CacheManager.writeFile(ctx, file.getPath());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

//                    RandomAccessFile randomAccessFile = new RandomAccessFile(file.getPath(), "r");
//                    FileRegion region = new DefaultFileRegion(randomAccessFile.getChannel(), 0, randomAccessFile.length());
//                    ctx.write(region);
//                    ctx.writeAndFlush(CR);
//                    randomAccessFile.close();
                    ReferenceCountUtil.release(msg);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }

        return false;

    }
}
