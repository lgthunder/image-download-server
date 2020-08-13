package com.example.lib;


import java.util.concurrent.LinkedBlockingQueue;

public class LogCollection {
    static LogCollection INSTANCE = new LogCollection();

    private LinkedBlockingQueue<ServerLogData> queue = new LinkedBlockingQueue();

    public static LogCollection getInstance() {
        return INSTANCE;
    }

    public LinkedBlockingQueue<ServerLogData> getQueue() {
        return queue;
    }

    public void sendLog(ServerLogData data) {
        try {
            queue.put(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
