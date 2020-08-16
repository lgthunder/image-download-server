package com.github.monkeywie.proxyee.handler;

import com.github.monkeywie.proxyee.util.ProtoUtil.RequestProto;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.proxy.ProxyHandler;
import test.java.com.github.monkeywie.proxyee.CacheManager;
import test.java.com.github.monkeywie.proxyee.UrlProvider;

/**
 * HTTP代理，转发解码后的HTTP报文
 */
public class HttpProxyInitializer extends ChannelInitializer {

  private Channel clientChannel;
  private RequestProto requestProto;
  private ProxyHandler proxyHandler;
  private CacheManager cacheManager;
  public HttpProxyInitializer(Channel clientChannel, RequestProto requestProto,
                              ProxyHandler proxyHandler, UrlProvider url ) {
    this.clientChannel = clientChannel;
    this.requestProto = requestProto;
    this.proxyHandler = proxyHandler;
    this.cacheManager = new CacheManager(url);
  }

  @Override
  protected void initChannel(Channel ch) throws Exception {
    if (proxyHandler != null) {
      ch.pipeline().addLast(proxyHandler);
    }
    if (requestProto.getSsl()) {
      ch.pipeline().addLast(
          ((HttpProxyServerHandle) clientChannel.pipeline().get("serverHandle")).getServerConfig()
              .getClientSslCtx()
              .newHandler(ch.alloc(), requestProto.getHost(), requestProto.getPort()));
    }
    ch.pipeline().addLast("httpCodec", new HttpClientCodec());
    ch.pipeline().addLast("proxyClientHandle", new HttpProxyClientHandle(clientChannel){
      @Override
      public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        cacheManager.saveResponseAsCache(clientChannel,ctx,msg);
        super.channelRead(ctx, msg);
      }
    });
  }
}
