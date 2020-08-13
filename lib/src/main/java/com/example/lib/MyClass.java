package com.example.lib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyClass {

    public static void main(String[] args) throws IOException {

        String savePath ="E:\\webdownload";
        String url ="https://www.privacypic.com/images/2020/07/31/GCiUsk.jpg";

        OkHttpClient client =new OkHttpClient();
        Request request =new Request.Builder().url(url).build();
        Response response =client.newCall(request).execute();

        InputStream is = response.body().byteStream();

        byte[] bs = new byte[1024];
        int len;
        File sf=new File(savePath);
        if(!sf.exists()){
            sf.mkdirs();
        }
        String extensionName = url.substring(url.lastIndexOf(".") + 1);
        String newFileName = url.substring(url.lastIndexOf("/") + 1,url.lastIndexOf("."))+ "." + extensionName;
        OutputStream os = new FileOutputStream(sf.getPath()+"\\"+newFileName);
        while ((len = is.read(bs)) != -1) {
            os.write(bs, 0, len);
        }
        os.close();
        is.close();

    }

}