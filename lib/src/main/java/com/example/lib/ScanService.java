package com.example.lib;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScanService extends Thread {

    public static ScanService scanService = new ScanService();

    private ScanService() {
    }

    volatile int state = 0;

    boolean needReload = false;

    @Override
    public void run() {
        while (state == 0) {
            System.out.println("start to scan=======================");
            scanDir();
            System.out.println("finish scan sleep=====================");
          while (true){
              try {
                  System.out.println("running call :"+FileUtils.client.dispatcher().runningCalls().size());
                  System.out.println("awaiting call :"+FileUtils.client.dispatcher().queuedCalls().size());
                  Thread.sleep(3000);
              } catch (InterruptedException e) {
                  e.printStackTrace();
              }
          }
        }
    }

    private void scanDir() {
        try {
            find(FileUtils.DIR);
        } catch (IOException e) {
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

                find(file.getCanonicalPath());

            } else {
                if(name.equalsIgnoreCase("reloadss.json")){
                    reload(file);
                }
                if (name.equals("list.json")) {
//                    System.out.println(name);
                    String json = FileUtils.readFile(file.getPath()).toString();

                    Data data = new Gson().fromJson(json, Data.class);
                    if (data == null) continue;
                    if (data.data == null) continue;
                    File reload = new File(dirFile.getPath(), "reload.json");
                    int count =1;
                    if (reload.exists()) {
                        count=2;

                    }
                    int size = data.data.size();
                    int file_size = file.getParentFile().list().length;
                    if (size + count <= file_size) {

                    } else {
                        File parentFile = file.getParentFile();
                        String[] list = parentFile.list();
//                        List<String> reload = new ArrayList<>();
                        for (String url : data.data) {
                            boolean exits = false;
                            for (String s : list) {
                                if (url.contains(s)) {
                                    exits = true;
                                    break;
                                }
                            }
                            if (!exits) {
//                                reload.add(url);
                                System.out.println(url);
                                FileUtils.save(parentFile.getPath(), url);
                            }
                        }


                        System.out.println("need reload");
                        needReload = true;
                    }

                }
            }
        }
        if (needReload) {
            state = 0;
        } else {
            state = 1;
        }

    }

    private void reload(File file){
        Gson gson = new Gson();
        List<ImgData> list = new ArrayList<>();
        StringBuilder temp = FileUtils.readFile(file.getPath());
        if (temp != null) {
            list = gson.fromJson(temp.toString(), new TypeToken<ArrayList<ImgData>>() {
            }.getType());
            for (ImgData imgData : list) {
                FileUtils.save(file.getParentFile().getPath(), imgData.url);
            }
        }
    }

    public static void main(String[] args) {
        scanService.start();
    }
}
