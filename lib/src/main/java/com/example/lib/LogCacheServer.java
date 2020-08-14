package com.example.lib;

import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LogCacheServer extends Thread {
    private int MAX_COUNT = 300;
    private List<ServerLogData> writeBuffOne = new ArrayList<>(MAX_COUNT);
    private List<ServerLogData> writeBuffTwo = new ArrayList<>(MAX_COUNT);
    private List<ServerLogData> writeBuffThree = new ArrayList<>(MAX_COUNT);
    private List<ServerLogData> currentRead = writeBuffOne;
    private List<ServerLogData> currentWrite = null;
    private static LogCacheServer INSTANCE = new LogCacheServer();
    private List<LinkedBlockingQueue<ServerLogData>> cacheList = new ArrayList<>();
    private LinkedBlockingQueue<ServerLogData> cache = new LinkedBlockingQueue<>();
    private Lock lock = new ReentrantLock();
    private volatile boolean flag = true;
    private boolean closing = false;

    public static LogCacheServer getInstance() {
        return INSTANCE;
    }


    @Override
    public void run() {

        while (flag) {
            try {
                ServerLogData data = LogCollection.getInstance().getQueue().take();
                if (data.equals(ServerLogData.emptyData())) {
                    closing = true;
                    flag = false;
                }
                if (currentRead.size() >= MAX_COUNT) {
                    currentWrite = currentRead;
                    writeLog();
                    currentRead = getEmptyBuff();
                    if (currentRead == null) {
                        Log.log("error get null buff");
                        currentRead = new ArrayList<>(MAX_COUNT);
//                        throw new RuntimeException("error get null buff");
                    }
                }
                currentRead.add(data);
                lock.lock();
                try {
                    cache.put(data);
                    if (cache.size() > MAX_COUNT * 3) {
                        cache.take();
                    }
                    for (LinkedBlockingQueue<ServerLogData> queue : cacheList) {
                        queue.put(data);
                    }
                } finally {
                    lock.unlock();
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void writeLog() {
        MessageExecutor.executorService.execute(new Runnable() {
            @Override
            public void run() {
                Log.log("write log to disk");
                FileUtils.writeFile(FileUtils.DIR + File.separator + System.currentTimeMillis() + ".log", new Gson().toJson(currentWrite), false);
            }
        });
    }

    public List<ServerLogData> getEmptyBuff() {
        if (writeBuffOne.size() == 0) return writeBuffOne;
        if (writeBuffTwo.size() == 0) return writeBuffTwo;
        if (writeBuffThree.size() == 0) return writeBuffThree;
        return null;
    }

    public LinkedBlockingQueue<ServerLogData> getCacheQueue() {
        LinkedBlockingQueue<ServerLogData> queue;
        lock.lock();
        try {
            queue = new LinkedBlockingQueue<>(cache);
            cacheList.add(queue);
        } finally {
            lock.unlock();
        }
        return queue;
    }

    public void finish() {
        flag = false;
        closing = true;
    }
}
