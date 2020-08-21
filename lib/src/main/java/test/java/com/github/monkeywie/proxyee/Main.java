package test.java.com.github.monkeywie.proxyee;

public class Main {

    public static void main(String[] args) {

        BatteryBoot boot = new BatteryBoot("proxy");
        boot.setVisible(true);
//        startServer(9190);
    }


//    public static void startServer(int port) {
//        try {
//
//            InterceptHttpProxyServer.start(port);
//        } catch (Exception e) {
//            if (e instanceof BindException) {
//                port = port + 1;
//                startServer(port);
//            }
//            e.printStackTrace();
//        }
//    }
}
