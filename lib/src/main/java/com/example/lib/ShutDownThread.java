package com.example.lib;

import com.google.gson.Gson;

import org.java_websocket.WebSocket;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ShutDownThread extends Thread {

    private WebSocket webSocket;
    private boolean flag = true;
    private Gson gson = new Gson();


    public ShutDownThread(WebSocket ws) {
        webSocket = ws;
    }

    @Override
    public void run() {
        while (flag) {
            try {
                JVMData data = new JVMData();
                test4(data);
                test6(data);
                if (webSocket.isOpen()) {
                    webSocket.send(gson.toJson(data));
                }
                if (webSocket.isClosed() || webSocket.isClosing()) {
                    flag = false;
                }
                if (data.equals(ServerLogData.emptyData())) {
                    flag = false;
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void test6(JVMData data) {
        ThreadMXBean tmx = ManagementFactory.getThreadMXBean();
        data.setThreadCount(tmx.getThreadCount() + "");
        List<JVMThreadInfo> threadList = new ArrayList<>(tmx.getAllThreadIds().length);
        ThreadInfo[] threadInfoList = tmx.dumpAllThreads(true, true);
        for (ThreadInfo ti : threadInfoList) {
            JVMThreadInfo info = new JVMThreadInfo();
            List<String> detail = new LinkedList<>();

//            System.out.println(ti.toString().trim());
//            System.out.println("cpu time:" + tmx.getThreadCpuTime(id));
//            System.out.println("user time:" + tmx.getThreadUserTime(id));
            for (StackTraceElement element : ti.getStackTrace()) {
//                System.out.println(element.toString());
                detail.add(element.toString());
            }
            info.setTraceElement(detail);
            info.setId(ti.getThreadId() + "");
            info.setName(ti.getThreadName() +" | "+ ti.getThreadState().name());
            threadList.add(info);
//            System.out.println("-----------------");

        }
        data.setThreadInfo(threadList);

//        System.out.println("findDeadlockedThreads:");

        if (tmx.findDeadlockedThreads() != null) {
            threadList = new ArrayList<>(tmx.findDeadlockedThreads().length);
            for (long id : tmx.findDeadlockedThreads()) {
                JVMThreadInfo info = new JVMThreadInfo();
                List<String> detail = new LinkedList<>();
                ThreadInfo ti = tmx.getThreadInfo(id);
//                System.out.println(ti.toString().trim());

                for (StackTraceElement element : ti.getStackTrace()) {
//                    System.out.println(element.toString());
                    detail.add(element.toString());
                }
                info.setTraceElement(detail);
                info.setId(ti.getThreadId() + "");
                info.setName(ti.getThreadName() +" | "+ ti.getThreadState().name());
                threadList.add(info);
            }
            data.setDeadLockThreadInfo(threadList);
        }
    }


    public static void test4(JVMData data) {
        MemoryMXBean mxb = ManagementFactory.getMemoryMXBean();
        //Heap
//        System.out.println("Max:" + mxb.getHeapMemoryUsage().getMax() / 1024 / 1024 + "MB");    //Max:1776MB
//        System.out.println("Init:" + mxb.getHeapMemoryUsage().getInit() / 1024 / 1024 + "MB");  //Init:126MB
//        System.out.println("Committed:" + mxb.getHeapMemoryUsage().getCommitted() / 1024 / 1024 + "MB");   //Committed:121MB
//        System.out.println("Used:" + mxb.getHeapMemoryUsage().getUsed() / 1024 / 1024 + "MB");  //Used:7MB
//        System.out.println(mxb.getHeapMemoryUsage().toString());    //init = 132120576(129024K) used = 8076528(7887K) committed = 126877696(123904K) max = 1862270976(1818624K)
        data.setMaxHeap(mxb.getHeapMemoryUsage().getMax() / 1024 / 1024 + "MB");
        data.setInitHeap(mxb.getHeapMemoryUsage().getInit() / 1024 / 1024 + "MB");
        data.setCommittedHeap(mxb.getHeapMemoryUsage().getCommitted() / 1024 / 1024 + "MB");
        data.setUsedHeap(mxb.getHeapMemoryUsage().getUsed() / 1024 / 1024 + "MB");
        //Non heap
//        System.out.println("Max:" + mxb.getNonHeapMemoryUsage().getMax() / 1024 / 1024 + "MB");    //Max:0MB
//        System.out.println("Init:" + mxb.getNonHeapMemoryUsage().getInit() / 1024 / 1024 + "MB");  //Init:2MB
//        System.out.println("Committed:" + mxb.getNonHeapMemoryUsage().getCommitted() / 1024 / 1024 + "MB");   //Committed:8MB
//        System.out.println("Used:" + mxb.getNonHeapMemoryUsage().getUsed() / 1024 / 1024 + "MB");  //Used:7MB
//        System.out.println(mxb.getNonHeapMemoryUsage().toString());    //init = 2555904(2496K) used = 7802056(7619K) committed = 9109504(8896K) max = -1(-1K)
        data.setMaxNoHeap(mxb.getNonHeapMemoryUsage().getMax() / 1024 / 1024 + "MB");
        data.setInitNoHeap(mxb.getNonHeapMemoryUsage().getInit() / 1024 / 1024 + "MB");
        data.setCommittedNoHeap(mxb.getNonHeapMemoryUsage().getCommitted() / 1024 / 1024 + "MB");
        data.setUsedNoHeap(mxb.getNonHeapMemoryUsage().getUsed() / 1024 / 1024 + "MB");
    }
}
