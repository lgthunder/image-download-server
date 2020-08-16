package test.java.com.github.monkeywie.proxyee;

import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptInitializer;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.intercept.common.CertDownIntercept;
import com.github.monkeywie.proxyee.intercept.common.FullRequestIntercept;
import com.github.monkeywie.proxyee.intercept.common.FullResponseIntercept;
import com.github.monkeywie.proxyee.server.HttpProxyServer;
import com.github.monkeywie.proxyee.server.HttpProxyServerConfig;
import com.github.monkeywie.proxyee.util.HttpUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;

import java.nio.charset.Charset;

public class InterceptFullHttpProxyServer {

  public static void main(String[] args) throws Exception {
    HttpProxyServerConfig config = new HttpProxyServerConfig();
    config.setHandleSsl(true);
    new HttpProxyServer()
            .serverConfig(config)
            .proxyInterceptInitializer(new HttpProxyInterceptInitializer() {
              @Override
              public void init(HttpProxyInterceptPipeline pipeline) {
                pipeline.addLast(new CertDownIntercept());

                pipeline.addLast(new FullRequestIntercept() {

                  @Override
                  public boolean match(HttpRequest httpRequest, HttpProxyInterceptPipeline pipeline) {
                    //�����json����
                    if(HttpUtil.checkHeader(httpRequest.headers(), HttpHeaderNames.CONTENT_TYPE,"^(?i)application/json.*$")){
                      return true;
                    }
                    return false;
                  }
                });
                pipeline.addLast(new FullResponseIntercept() {

                  @Override
                  public boolean match(HttpRequest httpRequest, HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) {
                    //�������а���user�ַ���
                    if(httpRequest instanceof FullHttpRequest){
                      FullHttpRequest fullHttpRequest = (FullHttpRequest) httpRequest;
                      String content = fullHttpRequest.content().toString(Charset.defaultCharset());
                      return content.matches("user");
                    }
                    return false;
                  }

                  @Override
                  public void handelResponse(HttpRequest httpRequest, FullHttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) {
                    //��ӡԭʼ��Ӧ��Ϣ
                    System.out.println(httpResponse.toString());
                    System.out.println(httpResponse.content().toString(Charset.defaultCharset()));
                    //�޸���Ӧͷ����Ӧ��
                    httpResponse.headers().set("handel", "edit head");
                    /*int index = ByteUtil.findText(httpResponse.content(), "<head>");
                    ByteUtil.insertText(httpResponse.content(), index, "<script>alert(1)</script>");*/
                    httpResponse.content().writeBytes("<script>alert('hello proxyee')</script>".getBytes());
                  }
                });

              }
            })
            .start(9999);
  }
}
