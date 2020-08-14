package com.example.lib;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerLogData implements Serializable {

    private int call;
    private int running;
    private int waiting;
    private int logCache;
    private String time;
    private String content;
    /**
     * is in pool
     */
    @Expose(serialize = false, deserialize = false)
    private int flag = 0;
    public final static int FLAG_IN_POOL = 1 << 0;
    public final static int FLAG_OUT_POOL = 1 << 1;
    public final static int FLAG_RELEASE = 1 << 2;
    @Expose(serialize = false, deserialize = false)
    private ServerLogData next;

    private static ServerLogData sPool;
    private final static int MAX_POOL_SIZE = 12;
    private static int sPoolSize = 0;
    private static final Object sPoolSync = new Object();

    static AtomicInteger cont = new AtomicInteger(0);

    private ServerLogData() {
        cont.getAndIncrement();
    }

    public int getCall() {
        return call;
    }

    public ServerLogData setCall(int call) {
        this.call = call;
        return this;
    }

    public int getRunning() {
        return running;
    }

    public ServerLogData setRunning(int running) {
        this.running = running;
        return this;
    }

    public int getWaiting() {
        return waiting;
    }

    public ServerLogData setWaiting(int waiting) {
        this.waiting = waiting;
        return this;
    }

    public int getLogCache() {
        return logCache;
    }

    public ServerLogData setLogCache(int logCache) {
        this.logCache = logCache;
        return this;
    }

    public String getTime() {
        return time;
    }

    public ServerLogData setTime(String time) {
        this.time = time;
        return this;
    }

    public String getContent() {
        return content;
    }

    public ServerLogData setContent(String content) {
        this.content = content;
        return this;
    }


    public static ServerLogData obtain() {
        synchronized (sPoolSync) {
            if (sPool != null) {
                ServerLogData data = sPool;
                sPool = data.next;
                data.next = null;
                data.flag = FLAG_OUT_POOL;
                sPoolSize--;
                return data;
            }
        }
        return currentData(new ServerLogData());
    }

    public void recycle() {
        flag = FLAG_RELEASE;
        content = "";
        synchronized (sPoolSync) {
            if (sPoolSize < MAX_POOL_SIZE) {
                flag = FLAG_IN_POOL;
                next = sPool;
                sPool = this;
                sPoolSize++;
            }
        }

    }

    private static ServerLogData currentData(ServerLogData data) {
        int wait = DownLoader.getInstance().getClient().dispatcher().queuedCalls().size();
        int run = DownLoader.getInstance().getClient().dispatcher().runningCalls().size();
        int callCache = DownLoader.getInstance().getRequestCache().size();
        data.call = callCache;
        data.running = run;
        data.waiting = wait;
        Long timeStamp = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sd = sdf.format(new Date(Long.parseLong(String.valueOf(timeStamp))));
        data.time = sd;
        data.logCache = LogCollection.getInstance().getQueue().size();
        return data;
    }

    private static ServerLogData EMPTY_DATA = new ServerLogData();

    public static ServerLogData emptyData() {
        return EMPTY_DATA;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerLogData)) return false;
        ServerLogData data = (ServerLogData) o;
        return getTime().equals(data.getTime()) &&
                getContent().equals(data.getContent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTime(), getContent());
    }

    private static String getTimeFormatString(int second) {
        int hh = second / 3600;
        int mm = second % 3600 / 60;
        int ss = second % 60;
        String strTemp = null;
        if (0 != hh) {
            strTemp = String.format("%02d:%02d:%02d", hh, mm, ss);
        } else {
            strTemp = String.format("%02d:%02d", mm, ss);
        }
        return strTemp;
    }

    public static void main(String[] args) {

        final LinkedBlockingQueue<ServerLogData> list = new LinkedBlockingQueue<>();
        MessageExecutor.executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    ServerLogData data = ServerLogData.obtain();
                    try {
                        list.put(data);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        MessageExecutor.executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        ServerLogData data = list.take();
                        data.recycle();
                        System.out.println(ServerLogData.cont.intValue());
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        MessageExecutor.executorService.execute(new Runnable() {
            @Override
            public void run() {
                List<ServerLogData> temp = new ArrayList<>(10);
                while (true) {
                    try {
                        ServerLogData data = list.take();
                        temp.add(data);
                        if (temp.size() >= 10) {
                            for (ServerLogData serverLogData : temp) {
                                serverLogData.recycle();
                            }
                            temp.clear();
                        }
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

}
