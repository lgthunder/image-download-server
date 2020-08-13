package com.example.lib;

import com.google.gson.Gson;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Collections;


public class ChatServer extends WebSocketServer {
    LogServer logServer;

    public ChatServer(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
    }

    public ChatServer(InetSocketAddress address) {
        super(address);
    }

    public ChatServer(int port, Draft_6455 draft) {
        super(new InetSocketAddress(port), Collections.<Draft>singletonList(draft));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        conn.send("Welcome to the server!"); //This method sends a message to the new client
        broadcast("new connection: " + handshake.getResourceDescriptor()); //This method sends a message to all clients connected
        System.out.println(conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        broadcast(conn + " has left the room!");
        System.out.println(conn + " has left the room!");
    }

    @Override
    public void onMessage(final WebSocket conn, final String message) {
        broadcast(message);
        if (message.equals("LogServer")) {
            logServer = new LogServer(conn, LogCollection.getInstance().getQueue());
            logServer.start();
            return;
        }

        MessageExecutor.executorService.execute(new Runnable() {
            @Override
            public void run() {
                Gson gson = new Gson();
                String json = URLDecoder.decode(message);
                Data data = gson.fromJson(json, Data.class);
                Log.log("onMessage: " + data.page);

                String filePath = FileUtils.DIR + File.separator + FileUtils.stringToMD5(data.host) + File.separator + URLEncoder.encode(data.page.substring(data.page.indexOf(data.host) + data.host.length(), data.page.lastIndexOf(".")));

                FileUtils.writeFile(filePath + File.separator + "list.json", json, false);
                ScanService.scanService.needWork(filePath);
//                for (String datum : data.data) {
//                    FileUtils.save(filePath, datum);
//                }
            }
        });

    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        broadcast(message.array());
        System.out.println(conn + "ByteBuffer : " + message);
    }


    public static void main(String[] args) throws InterruptedException, IOException {
        ScanService.scanService.start();
        int port = 8887; // 843 flash policy port
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception ex) {
        }
        ChatServer s = new ChatServer(port);
        s.start();
        System.out.println("ChatServer started on port: " + s.getPort());

        BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in, "utf-8"));
        while (true) {
            String in = sysin.readLine();
            if (in != null) {
                System.out.println(in);
            }
            if (("exit").equals(in)) {
                s.stop(1000);
                break;
            }
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        if (conn != null) {
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }

    @Override
    public void onStart() {
        System.out.println("Server started!");
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }

}
