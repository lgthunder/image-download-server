package com.example.lib;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ServerLogData implements Serializable {
    int call;
    int running;
    int waiting;
    String time;
    String content;

    public static ServerLogData currentData() {
        ServerLogData data = new ServerLogData();
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
        return data;
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
}
