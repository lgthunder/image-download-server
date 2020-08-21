package com.github.monkeywie.proxyee.util;

import com.example.lib.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Scanner;

public class BatUtil {


    public static void callCmd(InputStream in) {
        StringBuilder cmd = FileUtils.readFile(in);
        callCmd(cmd.toString());
    }


    public static void callCmd(String locationCmd) {
        try {
            Process child = Runtime.getRuntime().exec(locationCmd);
            InputStream in = child.getInputStream();
            int c;
            while ((c = in.read()) != -1) {
                System.out.println((char) c);
            }
            in.close();
            try {
                child.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("done");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
//        callCmd(Thread.currentThread().getContextClassLoader().getResourceAsStream("win_local_proxy.bat"));
//        callCmd(FileUtils.DIR + File.separator + "win_local_proxy.bat");
//        removeProxy();
        setProxy("127.0.0.1", "9999");

    }

    public static boolean setProxy(String ip, String port) {
        String code = "@echo off\r\n"
                + "set ip=" + ip + "\r\n"
                + "set port=" + port + "\r\n"
//                + "REG ADD \"HKCU\\SOFTWARE\\MICROSOFT\\Windows\\CURRENTVERSION\\Internet Settings\\Connections\" /v \"DefaultConnectionSettings\" /t  REG_BINARY /d \"3C000000AA0100000B0000000F000000\" /f\r\n"
                + "REG ADD \"HKCU\\SOFTWARE\\MICROSOFT\\Windows\\CURRENTVERSION\\Internet Settings\" /v \"ProxyEnable\" /t  REG_DWORD /d \"1\" /f\r\n"
                + "REG ADD \"HKCU\\SOFTWARE\\MICROSOFT\\Windows\\CURRENTVERSION\\Internet Settings\" /v \"ProxyServer\" /t  REG_SZ /d \"%ip%:%port%\" /f\r\n"
//                + "REG ADD \"HKLM\\System\\CurrentControlSet\\Hardware Profiles\\0001\\SOFTWARE\\MICROSOFT\\Windows\\CURRENTVERSION\\Internet Settings\" /v \"ProxyEnable\" /t  REG_DWORD /d \"1\" /f\r\n"
                + "REG ADD \"HKCU\\SOFTWARE\\MICROSOFT\\Windows\\CURRENTVERSION\\Internet Settings\\Connections\" /v \"SavedLegacySettings\" /t  REG_BINARY /d \"3C000000AE0100000B0000000F000000\" /f\r\n"
//                + "REG ADD \"HKCU\\SOFTWARE\\MICROSOFT\\Windows\\CURRENTVERSION\\Internet Settings\" /v \"ProxyOverride\" /t  REG_SZ /d \"<local>\" /f\r\n"
//                + "set rp=\"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\"\r\n"
                + "set rk=\"ProxyServer\"\r\n"
                + "for /f \"tokens=*\" %%a in ('reg query %rp% /v %rk%^|findstr %rk%') do (\r\n"
                + "call :doit %%a\r\n"
                + ")\r\n"
                + "goto :eof\r\n"
                + ":doit\r\n"
                + "echo %3\r\n"
                + "echo.&echo.\r\n"
                + "exit";
        return runBat(code, ip + ":" + port);
    }

    public static void removeProxy() {
        String code = "@echo off\r\n"
                + "REG ADD \"HKCU\\SOFTWARE\\MICROSOFT\\Windows\\CURRENTVERSION\\Internet Settings\\Connections\" /v \"DefaultConnectionSettings\" /t  REG_BINARY /d \"3C000000AA0100000B0000000F000000\" /f\r\n"
                + "REG ADD \"HKCU\\SOFTWARE\\MICROSOFT\\Windows\\CURRENTVERSION\\Internet Settings\" /v \"ProxyEnable\" /t  REG_DWORD /d \"0\" /f\r\n"
                + "REG ADD \"HKCU\\SOFTWARE\\MICROSOFT\\Windows\\CURRENTVERSION\\Internet Settings\" /v \"ProxyServer\" /t  REG_SZ /d \"\" /f\r\n"
                + "REG ADD \"HKLM\\System\\CurrentControlSet\\Hardware Profiles\\0001\\SOFTWARE\\MICROSOFT\\Windows\\CURRENTVERSION\\Internet Settings\" /v \"ProxyEnable\" /t  REG_DWORD /d \"1\" /f\r\n"
                + "REG ADD \"HKCU\\SOFTWARE\\MICROSOFT\\Windows\\CURRENTVERSION\\Internet Settings\\Connections\" /v \"SavedLegacySettings\" /t  REG_BINARY /d \"3C000000AE0100000B0000000F000000\" /f\r\n"
                + "REG ADD \"HKCU\\SOFTWARE\\MICROSOFT\\Windows\\CURRENTVERSION\\Internet Settings\" /v \"ProxyOverride\" /t  REG_SZ /d \"<local>\" /f\r\n"
                + "exit";
        runBat(code, "");
    }

    private static boolean runBat(String code, String flag) {
        File file = new File(FileUtils.DIR, "temp.bat");
        String str = "";
        try {
            if (!file.exists()) file.createNewFile();
            PrintWriter pw = new PrintWriter(file);
            pw.write(code);
            pw.flush();
            pw.close();
            Process child = Runtime.getRuntime().exec("cmd /c " + file.getPath());
            InputStream in = child.getInputStream();
            Scanner sc = new Scanner(in, "gbk");
            while (sc.hasNext()) {
                str += sc.nextLine();
            }
            try {
                child.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sc.close();
            Thread.sleep(1000);
            file.delete();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
//            e.printStackTrace();
        }
        System.out.println(file.getPath());
        if (str.contains(flag)) return true;
        else return false;
    }

}
