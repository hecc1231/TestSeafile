package com.hersch.testseafile;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Hersch on 2017/3/28.
 */
public class FileRooter {
    static Process process=null;
    public static void getFileList(String filePath) {
        chmod(filePath);
        File srcFile = new File(filePath);
        if (srcFile.canRead()) {
            System.out.println(srcFile.getAbsolutePath());
            if (srcFile.isDirectory()) {
                File[] files = srcFile.listFiles();
                for (int i = 0; i < files.length; i++) {
                    getFileList(files[i].getAbsolutePath());
                }
            } else {
                System.out.println(srcFile.getAbsolutePath());
            }
        }
    }

    /**
     * 初始化进程
     */
    private static void initProcess() {
        if (process == null)
            try {
                process = Runtime.getRuntime().exec("su");
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
    public static void chmod(String filePath){
          initProcess();
          chmodFile(filePath);
          close();
    }
    /**
     * 结束进程
     */
    private static void chmodFile(String filePath) {
        OutputStream out = process.getOutputStream();
        String cmd = "chmod 777 " + filePath + " \n";
        try {
            out.write(cmd.getBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭输出流
     */
    private static void close() {
        if (process != null)
            try {
                process.getOutputStream().close();
                process = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
}
