package test.java.com.github.monkeywie.proxyee;

import com.example.lib.ChatServer;
import com.example.lib.LogCacheServer;
import com.example.lib.ScanService;
import com.github.monkeywie.proxyee.exception.HttpProxyExceptionHandle;
import com.github.monkeywie.proxyee.intercept.HttpProxyIntercept;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptInitializer;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.intercept.common.CertDownIntercept;
import com.github.monkeywie.proxyee.proxy.ProxyConfig;
import com.github.monkeywie.proxyee.proxy.ProxyType;
import com.github.monkeywie.proxyee.server.HttpProxyServer;
import com.github.monkeywie.proxyee.server.HttpProxyServerConfig;
import com.github.monkeywie.proxyee.util.BatUtil;

import java.net.UnknownHostException;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

public class InterceptHttpProxyServer {

    public static void start() throws Exception {
        configProxy();
        downLoadServer();
        HttpProxyServerConfig config = new HttpProxyServerConfig();
        ProxyConfig proxyConfig = new ProxyConfig(ProxyType.HTTP, "127.0.0.1", 1080);
//        ProxyConfig proxyConfig =new ProxyConfig(ProxyType.HTTP,"hk.01.muii.xyz",650);
//        proxyConfig.setPwd("110120asd");
//        proxyConfig.setUser("6062");
        config.setHandleSsl(true);
        new HttpProxyServer()
                .serverConfig(config)
                .proxyConfig(proxyConfig)
//        .proxyConfig(new ProxyConfig(ProxyType.SOCKS5, "127.0.0.1", 1085))  //使用socks5二级代理
                .proxyInterceptInitializer(new HttpProxyInterceptInitializer() {
                    @Override
                    public void init(HttpProxyInterceptPipeline pipeline) {
                        pipeline.addLast(new CertDownIntercept());  //处理证书下载
                        pipeline.addFirst(new HttpProxyIntercept() {

                        });
                        pipeline.addLast(new HttpProxyIntercept() {
                            @Override
                            public void beforeRequest(Channel clientChannel, HttpRequest httpRequest,
                                                      HttpProxyInterceptPipeline pipeline) throws Exception {

//                                System.out.println(httpRequest.toString());
                                //替换UA，伪装成手机浏览器
                /*httpRequest.headers().set(HttpHeaderNames.USER_AGENT,
                    "Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1");*/
                                //转到下一个拦截器处理
                                pipeline.beforeRequest(clientChannel, httpRequest);
                            }

                            @Override
                            public void afterResponse(Channel clientChannel, Channel proxyChannel,
                                                      HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) throws Exception {
//                                System.out.println(httpResponse.toString());
                                //拦截响应，添加一个响应头
                                httpResponse.headers().add("intercept", "test");

                                //转到下一个拦截器处理
                                pipeline.afterResponse(clientChannel, proxyChannel, httpResponse);
                            }
                        });
//                        pipeline.addFirst(new RedirectHandle());
                        pipeline.addLast(new ImageRequestHandle());
                        pipeline.addLast(new ImageResponseHandle());
                        pipeline.addLast(new VideoIntercept());


                    }
                })
                .httpProxyExceptionHandle(new HttpProxyExceptionHandle() {
                    @Override
                    public void beforeCatch(Channel clientChannel, Throwable cause) throws Exception {
                        cause.printStackTrace();
                    }

                    @Override
                    public void afterCatch(Channel clientChannel, Channel proxyChannel, Throwable cause)
                            throws Exception {
                        cause.printStackTrace();
                    }
                })
                .start(9999);


    }


    public static void downLoadServer() throws UnknownHostException {
        ScanService.scanService.start();
        LogCacheServer.getInstance().start();
        int port = 8887; // 843 flash policy port
        try {
//            port = Integer.parseInt(args[0]);
        } catch (Exception ex) {
        }
        ChatServer s = new ChatServer(port);
        s.start();
        System.out.println("ChatServer started on port: " + s.getPort());
    }

    public static void configProxy() {
        DownLoadExecutor.executor.execute(new Runnable() {

            @Override
            public void run() {
                BatUtil.setProxy("127.0.0.1", "9999");
            }
        });

    }

}
