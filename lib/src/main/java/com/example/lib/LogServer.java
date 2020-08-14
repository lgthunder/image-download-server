package com.example.lib;

import com.google.gson.Gson;

import org.java_websocket.WebSocket;

import java.util.concurrent.LinkedBlockingQueue;

public class LogServer extends Thread {

    volatile boolean flag = true;
    private LinkedBlockingQueue<ServerLogData> queue;
    private WebSocket ws;
    private Gson gson = new Gson();

    public LogServer(WebSocket webSocket, LinkedBlockingQueue<ServerLogData> queue) {
        ws = webSocket;
        this.queue = queue;
    }

    @Override
    public void run() {
        while (flag) {
            try {
                ServerLogData data = queue.take();
                if (ws.isOpen()) {
                    ws.send(gson.toJson(data));
                }
                if (ws.isClosed() || ws.isClosing()) {
                    flag = false;
                }
                if (data.equals(ServerLogData.emptyData())) {
                    flag = false;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void finish() {
        flag = false;
    }
}
