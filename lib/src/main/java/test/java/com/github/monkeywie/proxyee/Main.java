package test.java.com.github.monkeywie.proxyee;

public class Main {

    public static void main(String[] args) {
        try {
            InterceptHttpProxyServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
