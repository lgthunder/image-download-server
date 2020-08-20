package test.java.com.github.monkeywie.proxyee;

import com.github.monkeywie.proxyee.intercept.HttpProxyIntercept;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.util.ProtoUtil;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

public class RedirectHandle extends HttpProxyIntercept {
    public static final String REDIRECT = "-@ewe@-";
    public static boolean handleRedirect = true;


    public void beforeRequest(HttpRequest httpRequest, HttpProxyInterceptPipeline pipeline) {
        String redirectHost;
        String host = ProtoUtil.getRequestProto(httpRequest).getHost();
        if (httpRequest instanceof HttpRequest) {
            HttpRequest request = httpRequest;
            String url_ = request.uri();
            String[] s = url_.split(RedirectHandle.REDIRECT);
            if (s.length > 1) {
//                url_ = s[0];
                request.setUri(s[0]);
                redirectHost = s[1];
            } else {
                redirectHost = host;
            }

            pipeline.setRedirectHost(redirectHost);
        }
    }

    @Override
    public void beforeRequest(Channel clientChannel, HttpRequest httpRequest, HttpProxyInterceptPipeline pipeline) throws Exception {
        beforeRequest(httpRequest, pipeline);
        super.beforeRequest(clientChannel, httpRequest, pipeline);
    }

    @Override
    public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) throws Exception {

        String location = httpResponse.headers().get(HttpHeaderNames.LOCATION, "");
        if (location.length() != 0 && RedirectHandle.handleRedirect) {
            location = location + RedirectHandle.REDIRECT + pipeline.getHttpRequest().headers().get(HttpHeaderNames.HOST);
            httpResponse.headers().set(HttpHeaderNames.LOCATION, location);
        }
        super.afterResponse(clientChannel, proxyChannel, httpResponse, pipeline);
    }

    public boolean filter() {
        return false;
    }
}
