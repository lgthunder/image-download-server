package test.java.com.github.monkeywie.proxyee;

import com.example.lib.ChatServer;
import com.example.lib.Log;
import com.example.lib.LogCacheServer;
import com.example.lib.ScanService;
import com.github.monkeywie.proxyee.server.HttpProxyServer;
import com.github.monkeywie.proxyee.util.BatUtil;

import org.java_websocket.WebSocket;

import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class BatteryBoot extends JFrame {

    HttpProxyServer server;

    ChatServer downLoad;
    TextField serverPort;
    TextField downLoadPort;
    CancelTask restartTask;
    public static BatteryBoot INSTACE;
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    public BatteryBoot(String title) {
        super(title);
        setSize(400, 200);
        Button connect = new IButton("Connect");
        Button restart = new IButton("Restart");
        Button stop = new IButton("Stop");
        Button proxy = new IButton("ConfigProxy");
        serverPort = new TextField("9999");
        downLoadPort = new TextField("8887");
        serverPort.setSize(20, 50);
        downLoadPort.setSize(20, 50);
        Label proxyPortLabel = new Label("Proxy port");
        Label downPortLabel = new Label("Down Port");
        JPanel proxyPanel = new JPanel();
        JPanel downPanel = new JPanel();
        proxyPanel.add(proxyPortLabel);
        proxyPanel.add(serverPort);
        add(connect);
        add(restart);
        add(stop);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        downPanel.add(downPortLabel);
        downPanel.add(downLoadPort);
        add(proxyPanel);
        add(downPanel);
        add(proxy);
        setLayout(new GridLayout(2, 1, 5, 5));
        ScanService.scanService.start();
        LogCacheServer.getInstance().start();
        connect.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (restartTask != null) {
                    restartTask.cancel();
                }

                restartTask = getStartTask();
                executorService.submit(restartTask);

            }
        });

        stop.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.out.println("stop");
                BatUtil.removeProxy();
                if (server != null) {
                    server.close();
                }
                if (downLoad != null) {
                    try {
                        downLoad.stop();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        restart.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.out.println("restart");
                if (restartTask != null) {
                    restartTask.cancel();
                }

                restartTask = getStartTask();
                executorService.submit(restartTask);
            }
        });
        proxy.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.out.println("proxy");
                InterceptHttpProxyServer.configProxy(Integer.valueOf(serverPort.getText()));
            }
        });
    }

    public static void showMessage(String message) {
        JOptionPane.showMessageDialog(INSTACE, message,"",JOptionPane.ERROR_MESSAGE);
    }

    public CancelTask getStartTask() {
        return new CancelTask(() -> {
            try {
                if (server != null) {
                    server.close();
                }
                if (downLoad != null) {
                    downLoad.stop();
                }
                InterceptHttpProxyServer.configProxy(Integer.valueOf(serverPort.getText()));
                downLoad = new ChatServer(Integer.valueOf(downLoadPort.getText())) {
                    @Override
                    public void onError(WebSocket conn, Exception ex) {
                        super.onError(conn, ex);
                    }
                };
                downLoad.start();
                server = InterceptHttpProxyServer.initServer();
                server.start(Integer.valueOf(serverPort.getText()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    class IButton extends Button {
        public IButton(String title) {
            super(title);
            setSize(20, 50);
        }
    }

    public static void main(String[] args) {
        INSTACE = new BatteryBoot("proxy");

        INSTACE.setLocationRelativeTo(null);
        INSTACE.setVisible(true);


    }

    public static class CancelTask implements Runnable {

        Runnable task;
        public volatile boolean flag = true;

        public CancelTask(Runnable task) {
            this.task = task;
        }


        public void cancel() {
            flag = false;
        }

        @Override
        public void run() {
            if (!flag) {
                return;
            }
            task.run();
        }
    }
}


