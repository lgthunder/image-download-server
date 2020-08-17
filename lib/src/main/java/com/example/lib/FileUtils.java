package com.example.lib;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public final class FileUtils {


    public static String DIR = "E:\\webdownload";

    public static boolean writeFile(String filePath, String content, boolean append) {
        FileWriter fileWriter = null;
        try {
            File file = new File(filePath);
            if (!new File(file.getParent()).exists()) {
                new File(file.getParent()).mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }

            fileWriter = new FileWriter(filePath, append);
            fileWriter.write(content);
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * read file
     *
     * @param filePath
     * @return if file not exist, return null, else return content of file
     * @throws IOException if an error occurs while operator BufferedReader
     */
    public static StringBuilder readFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        StringBuilder fileContent = new StringBuilder();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (!fileContent.toString().equals("")) {
                    fileContent.append("\r\n");
                }
                fileContent.append(line);
            }
            return fileContent;
        } catch (IOException e) {
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    public static  List<String> readFile(InputStream  inputStream) {

        List<String> fileContent = new LinkedList<>();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while ((line = reader.readLine()) != null) {
//                if (!fileContent.toString().equals("")) {
//                    fileContent.append("\r\n");
//                }
                fileContent.add(line);
            }
            return fileContent;
        } catch (IOException e) {
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    public static double copyFile(String sourceFile, String targetFile) {
        return copyFile(new File(sourceFile), new File(targetFile));
    }

    public static double copyFile(File sourceFile, File targetFile) {
        if (sourceFile != null && sourceFile.equals(targetFile)) return 1;

        long start = System.currentTimeMillis();
        BufferedInputStream inBuff = null;
        BufferedOutputStream outBuff = null;

        File targetFolder = new File(targetFile.getParent());
        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }
        try {
            targetFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            inBuff = new BufferedInputStream(new FileInputStream(sourceFile));
            outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));
            byte[] b = new byte[1024 * 5];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }
//            if (isStopCopy(sourceFile.getAbsolutePath())) {
//                stopCopy.remove(sourceFile.getAbsolutePath());
//                return -1;
//            }
            outBuff.flush();
            start = (System.currentTimeMillis() - start);
            double speed = targetFile.length() / 1000.0f / (start / 1000.0f);
//            stopCopy.remove(sourceFile.getAbsolutePath());
            return speed;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inBuff != null)
                    inBuff.close();
                if (outBuff != null)
                    outBuff.close();
            } catch (IOException e) {
            }
        }
        return -1;
    }

    public static String stringToMD5(String plainText) {
        byte[] secretBytes = null;
        try {
            secretBytes = MessageDigest.getInstance("md5").digest(
                    plainText.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("md5 not found ");
        }
        String md5code = new BigInteger(1, secretBytes).toString(16);
        for (int i = 0; i < 32 - md5code.length(); i++) {
            md5code = "0" + md5code;
        }
        return md5code;
    }

    public static void main(String[] args) {
//        String s = "hello world";
//        System.out.println(stringToMD5(s));
        String path = "E:\\webdownload\\762ed4e4ba61a47d734548a5c628a3a4\\%2Fhtm_data%2F2007%2F8%2F4006686" + File.separator + "reload.json";
        String[] urls = new String[]{"https://www.privacypic.com/images/2020/07/12/xUFI53.jpg", "https://www.privacypic.com/images/2020/07/12/xUFoM0.jpg",
                "https://www.privacypic.com/images/2020/07/12/xUFI53.jpg", "https://www.privacypic.com/images/2020/07/12/xUF667.jpg", "https://www.privacypic.com/images/2020/07/12/xUl5nZ.jpg"
                , "https://www.privacypic.com/images/2020/07/12/xUlxok.jpg", "https://www.privacypic.com/images/2020/07/12/xUlC9n.jpg"};
        for (String url : urls) {
            addOrUpdate(path, url);
        }
//            delete(path,"https://www.privacypic.com/images/2020/07/12/xUFI53.jpg");
    }

    public static void addOrUpdate(String file, String url) {
        addOrUpdate(file, new String[]{url});
    }

    public static void addOrUpdate(String file, String[] urls) {
        Gson gson = new Gson();
        String reloadPath = file;
        List<ImgData> list = new ArrayList<>();
        StringBuilder temp = readFile(reloadPath);
        if (temp != null) {
            try {
                list = gson.fromJson(temp.toString(), new TypeToken<ArrayList<ImgData>>() {
                }.getType());
            }catch (Exception e){
                Log.logS("addOrUpdate file : "+file);
                e.printStackTrace();
            }

        } else {
            list = new ArrayList<>();
        }
        final List<String> cache = new ArrayList<>(Arrays.asList(urls));

        for (String url : urls) {
            for (ImgData imgData : list) {
                if (imgData.url.equals(url)) {
                    imgData.count = imgData.count + 1;
                    cache.remove(url);
                    break;
                }
            }
        }
        for (String url : cache) {
            list.add(new ImgData(url, 0));
        }

        FileUtils.writeFile(reloadPath, gson.toJson(list), false);

    }

    public static void delete(String path, String url) {
        Gson gson = new Gson();
        String reloadPath = path;
        List<ImgData> list = new ArrayList<>();
        StringBuilder temp = readFile(reloadPath);
        if (temp != null) {
            list = gson.fromJson(temp.toString(), new TypeToken<ArrayList<ImgData>>() {
            }.getType());
        } else {
            return;
        }

        ImgData exits = null;
        if (list == null) return;
        for (ImgData imgData : list) {
            if (imgData.url.equals(url)) {
                exits = imgData;
                break;
            }
        }

        if (exits != null) {
            list.remove(exits);
            FileUtils.writeFile(reloadPath, gson.toJson(list), false);
        }
    }

}
