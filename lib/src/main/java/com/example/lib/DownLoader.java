package com.example.lib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.EventListener;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownLoader {
    static DownLoader INSTANCE = new DownLoader();

    public static DownLoader getInstance() {
        return INSTANCE;
    }

    private OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .readTimeout(20, TimeUnit.SECONDS)
            .eventListener(new EventListener() {
                @Override
                public void callEnd(Call call) {
                    requestCache.remove(call.request());
                    if (requestCache.size() % 10 == 0) {
                        Log.log("callEnd");
                    }
                }

                @Override
                public void callFailed(Call call, IOException ioe) {
                    requestCache.remove(call.request());
                    Log.log("callFailed" + call.request().url().toString());
                }
            })
            .build();

    private ConcurrentLinkedQueue<Request> requestCache = new ConcurrentLinkedQueue<>();

    public ConcurrentLinkedQueue<Request> getRequestCache() {
        return requestCache;
    }

    private static boolean isUrlAvailable(String extensionName) {
        if (!extensionName.equalsIgnoreCase("JPEG")
                && !extensionName.equalsIgnoreCase("jpg")
                && !extensionName.equalsIgnoreCase("PNG")
                && !extensionName.equalsIgnoreCase("GIF")
                && !extensionName.equalsIgnoreCase("bmp")
                && !extensionName.equalsIgnoreCase("tif")) {
            return false;
        }

        return true;
    }

    public OkHttpClient getClient() {
        return client;
    }

    public void load(final String savePath, final String url) {
//        String savePath ="E:\\webdownload";
//        String url ="https://www.privacypic.com/images/2020/07/31/GCiUsk.jpg";
        String extensionName = url.substring(url.lastIndexOf(".") + 1);
        if (!isUrlAvailable(extensionName)) {
            return;
        }
        final String reloadPath = savePath + File.separator + "reload.json";
        final File sf = new File(savePath);
        if (!sf.exists()) {
            sf.mkdirs();
        }
        final String newFileName = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf(".")) + "." + extensionName;
        final String tempFileName = "11aatemp" + url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf(".")) + "." + extensionName;
        final File file = new File(sf.getPath() + File.separator + newFileName);

        if (file.exists()) {
            FileUtils.delete(reloadPath, url);
            return;
        }
        final File tempFile = new File(sf.getPath() + File.separator + tempFileName);
        //todo tempfile
        client.dispatcher().setMaxRequestsPerHost(15);
        for (Request request : requestCache) {
            if (request.url().toString().equals(url)) {
                return;
            }
        }
        Request request = new Request.Builder().url(url).build();
        requestCache.add(request);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.log("failure" + savePath);
                if (tempFile.exists()) {
                    tempFile.delete();
                }
                FileUtils.addOrUpdate(reloadPath, url);
                ScanService.scanService.needWork(savePath);

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                OutputStream os = null;

                try {
                    if (tempFile.exists()) {
                        tempFile.delete();
                    }
                    is = response.body().byteStream();

                    byte[] bs = new byte[1024];
                    int len;

                    os = new FileOutputStream(sf.getPath() + File.separator + tempFileName);
                    while ((len = is.read(bs)) != -1) {
                        os.write(bs, 0, len);
                    }
                } finally {
                    if (os != null) {
                        os.close();
                    }
                    if (is != null) {
                        is.close();
                    }
                }

                double re = FileUtils.copyFile(sf.getPath() + File.separator + tempFileName, sf.getPath() + File.separator + newFileName);
                if (re > 0) {
                    if (tempFile.exists()) {
                        tempFile.delete();
                    }
                }
                FileUtils.delete(reloadPath, url);
            }
        });


    }

}
