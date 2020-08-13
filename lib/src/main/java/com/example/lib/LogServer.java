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
                ws.send(gson.toJson(data));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendLog(ServerLogData data) {
        try {
            queue.put(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
