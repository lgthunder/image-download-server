package test.java.com.github.monkeywie.proxyee;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HTTPSTest {

    public static void main(String[] args) {
        InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 9999);

        OkHttpClient client = new OkHttpClient.Builder().proxy(new Proxy(Proxy.Type.HTTP,addr)).build();
        Request request =new Request.Builder().url("https://www.baidu.com").build();
        try {
           Response response= client.newCall(request).execute();
           System.out.println(response.body().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
