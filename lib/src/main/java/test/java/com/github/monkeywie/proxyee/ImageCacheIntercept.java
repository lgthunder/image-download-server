package test.java.com.github.monkeywie.proxyee;

import com.example.lib.Log;
import com.github.monkeywie.proxyee.intercept.HttpProxyIntercept;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.util.ProtoUtil;

import org.bouncycastle.cert.ocsp.Req;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

public abstract class ImageCacheIntercept extends HttpProxyIntercept {
    protected String url_;

    protected String host;

    protected String redirectHost = "";

    public ImageCacheIntercept() {
    }


    @Override
    public void beforeRequest(Channel clientChannel, HttpRequest httpRequest, HttpProxyInterceptPipeline pipeline) throws Exception {
        url_ = httpRequest.uri();
        host = ProtoUtil.getRequestProto(httpRequest).getHost();
        redirectHost = pipeline.getRedirectHost();
    }

    @Override
    public void beforeRequest(Channel clientChannel, HttpContent httpContent, HttpProxyInterceptPipeline pipeline) throws Exception {
        super.beforeRequest(clientChannel, httpContent, pipeline);
//        Log.log("beforeRequest  httpContent " + host + url_);
    }

    @Override
    public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpContent httpContent, HttpProxyInterceptPipeline pipeline) throws Exception {
        super.afterResponse(clientChannel, proxyChannel, httpContent, pipeline);
//        Log.log("afterResponse  httpContent" + host + url_);
    }

    @Override
    public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) throws Exception {
        super.afterResponse(clientChannel, proxyChannel, httpResponse, pipeline);
//        Log.log("afterResponse  httpResponse" + host + url_);
    }

}
