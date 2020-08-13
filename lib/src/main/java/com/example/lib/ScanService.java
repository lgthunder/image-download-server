package com.example.lib;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

public class ScanService extends Thread {

    public static ScanService scanService = new ScanService();

    private ScanService() {
    }

    private volatile int state = 0;

    boolean needReload = false;
    private Lock lock = new ReentrantLock();

    private String currentWorkPath;

    @Override
    public void run() {
        Log.log("start to scan " + FileUtils.DIR);
        scanDir();
        Log.log("finish scan " + FileUtils.DIR);
        while (true) {

            if (state == 0) {
                lock.lock();
                try {
                    Log.log("start to scan :" + currentWorkPath);
                    findListFile(currentWorkPath);
                    Log.log("finish scan :" + currentWorkPath);
                } catch (Exception e) {
                    e.printStackTrace();

                } finally {
                    lock.unlock();
                }
                state = 1;
            } else {
                Log.log("scanService park");
                LockSupport.park(scanService);
            }
        }
    }


    public void needWork(String workPath) {
        lock.lock();
        try {
            state = 0;
            currentWorkPath = workPath;
        } finally {
            lock.unlock();
        }
        Log.log("scanService unpark");
        LockSupport.unpark(scanService);

    }

    private void scanDir() {
        try {
            find(FileUtils.DIR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void find(String pathName) throws IOException {

        File dirFile = new File(pathName);

        if (!dirFile.exists()) {
            System.out.println("do not exit");
            return;
        }

        if (!dirFile.isDirectory()) {
            if (dirFile.isFile()) {
                System.out.println(dirFile.getCanonicalFile());
            }
            return;
        }

        String[] fileList = dirFile.list();

        for (int i = 0; i < fileList.length; i++) {

            String string = fileList[i];

            File file = new File(dirFile.getPath(), string);

            String name = file.getName();

            if (file.isDirectory()) {
                System.out.println(name);
                findListFile(file.getPath());
                find(file.getCanonicalPath());
            }
        }
    }


    public void findListFile(String parent) {
//        File parent = file.getParentFile();
//        String name = file.getName();
        if (parent == null) return;
        if (parent.length() == 0) return;
        checkDataComplete(parent);
        reload(parent);

    }

    private boolean checkDataComplete(String parent) {
        Gson gson = new Gson();
        File file = new File(getListFilePath(parent));
        if (file.exists()) {
//                    System.out.println(name);
            String json = FileUtils.readFile(file.getPath()).toString();

            Data data = gson.fromJson(json, Data.class);
            if (data == null) return false;
            if (data.data == null) return false;

            List<String> reload = new ArrayList<>();
            if (data.state == 0) {
                File parentFile = file.getParentFile();
                String[] list = parentFile.list();
                for (String url : data.data) {
                    boolean exits = false;
                    for (String s : list) {
                        if (url.contains(s)) {
                            exits = true;
                            break;
                        }
                    }
                    if (!exits) {
                        reload.add(url);
                        Log.log("found need load :" + url);
//                        FileUtils.save(parentFile.getPath(), url);
                    }
                }
                needReload = true;
            }
            if (reload.size() > 0) {
                System.out.println("need reload");
                data.state = 1;
                FileUtils.addOrUpdate(getReloadFilePath(parent), reload.toArray(new String[reload.size()]));
                FileUtils.writeFile(file.getPath(), gson.toJson(data), false);
            }

            if (reload.size() == 0 && data.state != 2) {
                data.state = 2;
                FileUtils.writeFile(file.getPath(), gson.toJson(data), false);
                System.out.println("complete");
            }

        }
        return false;
    }

    private void reload(String parent) {
        File file = new File(getReloadFilePath(parent));
        if (!file.exists()) {
            return;
        }
        Gson gson = new Gson();
        List<ImgData> list = new ArrayList<>();
        StringBuilder temp = FileUtils.readFile(file.getPath());
        if (temp != null) {
            list = gson.fromJson(temp.toString(), new TypeToken<ArrayList<ImgData>>() {
            }.getType());
            for (ImgData imgData : list) {
                DownLoader.getInstance().load(file.getParentFile().getPath(), imgData.url);
            }
        }
    }

    public String getReloadFilePath(String parent) {
        return parent + File.separator + "reload.json";
    }

    public String getListFilePath(String parent) {
        return parent + File.separator + "list.json";
    }

    public static void main(String[] args) {
        scanService.start();
    }
}
