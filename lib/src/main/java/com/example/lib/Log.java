package com.example.lib;

public class Log {

    public static void logS(String content) {
        System.out.println(content);
    }

    public static void log(String content) {
        logS(content);
        ServerLogData data = ServerLogData.obtain();
        data.setContent(content);
        LogCollection.getInstance().sendLog(data);

    }
}
