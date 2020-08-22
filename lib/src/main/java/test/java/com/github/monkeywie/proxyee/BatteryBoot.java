package test.java.com.github.monkeywie.proxyee;

import com.example.lib.ChatServer;
import com.example.lib.Log;
import com.example.lib.LogCacheServer;
import com.example.lib.ScanService;
import com.github.monkeywie.proxyee.server.HttpProxyServer;
import com.github.monkeywie.proxyee.util.BatUtil;

import org.java_websocket.WebSocket;

import java.awt.AWTException;
import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
    private TrayIcon trayIcon;

    public BatteryBoot(String title) {
        super(title);
//        setType(Type.UTILITY);
        setSize(400, 200);
        Button connect = new IButton("Connect");
        Button restart = new IButton("Restart");
        Button stop = new IButton("Stop");
        Button proxy = new IButton("ConfigProxy");
        Button exit = new IButton("Exit");
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
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        add(exit);
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
        exit.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.exit(0);
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                super.windowClosing(windowEvent);
                minimizeToTray();
            }


        });
        initTrayIcon();
    }

    private void initTrayIcon() {
        Image image = Toolkit.getDefaultToolkit().getImage(Thread.currentThread().getContextClassLoader().getResource("photo.jpg"));
        PopupMenu popup = new PopupMenu();
        MenuItem exitItem = new MenuItem("Show");
        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(true);
                setExtendedState(Frame.NORMAL);
                SystemTray.getSystemTray().remove(trayIcon);
            }
        };
        exitItem.addActionListener(listener);
        popup.add(exitItem);
        //根据image、提示、菜单创建TrayIcon
        this.trayIcon = new TrayIcon(image, "proxy", popup);
        //给TrayIcon添加事件监听器
        this.trayIcon.addActionListener(listener);
    }

    public void minimizeToTray() {
        SystemTray tray = SystemTray.getSystemTray();
        try {
            tray.add(this.trayIcon);
        } catch (AWTException ex) {
            ex.printStackTrace();
        }
    }

    public static void showMessage(String message) {
        JOptionPane.showMessageDialog(INSTACE, message, "", JOptionPane.ERROR_MESSAGE);
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


