package test.java.com.github.monkeywie.proxyee;

import com.example.lib.DownLoader;
import com.example.lib.FileUtils;
import com.example.lib.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URLEncoder;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class CacheManager {

    public static boolean isCacheOpen = false;





    public static String getSavePath(String host, String url) {


        String path = url.substring(0, url.lastIndexOf("/"));
        return FileUtils.DIR + "_cache" + File.separator + host + File.separator + URLEncoder.encode(path);
    }

    public static String getName(String url) {
        String extensionName = getExtensionName(url);
        if (DownLoader.isUrlAvailable(extensionName)) {
            return url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf(".")) + "." + extensionName;
        }
        return "";
    }

    public static String getExtensionName(String url) {
        return url.substring(url.lastIndexOf(".") + 1);
    }

    public static void writeFile(Channel ctx, String path) throws IOException {
        RandomAccessFile raf = null;
        long length = -1;
        try {
            raf = new RandomAccessFile(path, "r");
            length = raf.length();
        } catch (Exception e) {
            ctx.writeAndFlush("ERR: " + e.getClass().getSimpleName() + ": " + e.getMessage() + '\n');
            return;
        } finally {
            if (length < 0 && raf != null) {
                raf.close();
            }
        }

        ctx.write("OK: " + raf.length() + '\n');
        try {
            ChannelFuture futures;
            if (ctx.pipeline().get(SslHandler.class) == null) {
                // SSL not enabled - can use zero-copy file transfer.
                futures = ctx.writeAndFlush(new DefaultFileRegion(raf.getChannel(), 0, length)).await();
            } else {
                // SSL enabled - cannot use zero-copy file transfer.
                futures = ctx.writeAndFlush(new ChunkedFile(raf)).await();
            }
//           = ctx.writeAndFlush("\n").await();
            if (!futures.isSuccess()) {
                Log.log("send :" + path + " failure: " + futures.cause().toString());
            }
            futures.addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                }
            });
            if (futures.isDone()) {
                raf.close();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
