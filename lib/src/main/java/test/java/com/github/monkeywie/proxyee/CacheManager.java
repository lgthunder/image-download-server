package test.java.com.github.monkeywie.proxyee;

import com.example.lib.DownLoader;
import com.example.lib.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;

public class CacheManager {

    private static final String CR = System.getProperty("line.separator");
    private boolean isCacheOpen = true;

    String contentType;

    String host;

    UrlProvider url;
    private long contentLength;

    public CacheManager(UrlProvider url) {
        this.url = url;
    }

    public boolean hasCache(Channel ctx, Object msg, boolean isHttp) {
//        System.out.println(msg.getClass());
//        System.out.println(msg.toString());
        if (!isCacheOpen) return false;

        if (msg instanceof DefaultHttpRequest) {
            DefaultHttpRequest request = (DefaultHttpRequest) msg;
            if (request.method() == HttpMethod.GET) {
//                System.out.println(" GET | host: " + url.getUrl().getHost() + " path: " + url.getUrl().getPath());
//                System.out.println(" GET  REQUEST :" + request.toString());
                try {
                    File file = new File(getSavePath(url.getUrl().getHost(), url.getUrl().getPath()), getName(url.getUrl().getPath()));
                    if (!file.exists()) {
                        return false;
                    }
                    String extensionName = getExtensionName(url.getUrl().getPath());
                    if (!DownLoader.isUrlAvailable(extensionName)) {
                        return false;
                    }
                    System.out.println("  GET | get url :"+url.getUrl().toString()+"  from path : " + file.getPath());
                    DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.OK);
                    response.headers().add("Content-Type", "image/" + getExtensionName(url.getUrl().getPath()));
                    response.headers().add("Content-Length", file.length());
                    response.headers().add("Cache-From-Local", "Local");
                    ctx.write(response);
                    writeFile(ctx, file.getPath());
//                    RandomAccessFile randomAccessFile = new RandomAccessFile(file.getPath(), "r");
//                    FileRegion region = new DefaultFileRegion(randomAccessFile.getChannel(), 0, randomAccessFile.length());
//                    ctx.write(region);
//                    ctx.writeAndFlush(CR);
//                    randomAccessFile.close();
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

    List<byte[]> list = new LinkedList<>();

    public void saveResponseAsCache(Channel clientChannel, ChannelHandlerContext proxy, Object msg) {
        //client  L:/127.0.0.1:9999 - R:/127.0.0.1:59484

        if (msg instanceof DefaultHttpResponse) {
            DefaultHttpResponse response = (DefaultHttpResponse) msg;
            contentType = response.headers().get("Content-Type");
            String length = response.headers().get("Content-Length");
            try {
                contentLength = Long.valueOf(length);
            } catch (Exception e) {
                contentLength = 0;
            }

            host = ((InetSocketAddress) (proxy.pipeline().channel().remoteAddress())).getHostName();

//            System.out.println("client: " + clientChannel.toString() + "| proxy :" + proxy.channel() + "RESPONSE: ");

        }

        if (contentLength < 1024 * 20) {
            return;
        }
        if (contentType == null || contentType.length() == 0) {
            return;
        }

        if (!contentType.contains("image")) {
            return;
        }
        if (msg instanceof DefaultHttpContent) {
//            System.out.println(" GET RESPONSE_CONTENT :" + msg.toString());

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
            LastHttpContent response = (LastHttpContent) msg;

            File file = new File(getSavePath(url.getUrl().getHost(), url.getUrl().getPath()), getName(url.getUrl().getPath()));
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (file.exists()) {
                return;
            }
//            System.out.println(Thread.currentThread().getName() + "|" + contentType + " : " + file.getPath());
            try {
                FileOutputStream os = new FileOutputStream(file.getPath());
                for (byte[] byteBuf : list) {
                    os.write(byteBuf);
                }
                list.clear();
                os.close();
                System.out.println("  RESPONSE | save url :"+url.getUrl().toString()+"  to path : " + file.getPath());
            } catch (Exception e) {
                e.printStackTrace();
            }


//            System.out.println(proxy.channel()+ "RESPONSE: " + response.content().nioBuffer(size());
        }
    }

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

    public void writeFile(Channel ctx, String path) throws IOException {
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
        if (ctx.pipeline().get(SslHandler.class) == null) {
            // SSL not enabled - can use zero-copy file transfer.
            ctx.write(new DefaultFileRegion(raf.getChannel(), 0, length));
        } else {
            // SSL enabled - cannot use zero-copy file transfer.
            ctx.write(new ChunkedFile(raf));
        }
        ctx.writeAndFlush("\n");
    }
}
