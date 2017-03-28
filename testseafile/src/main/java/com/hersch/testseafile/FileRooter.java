package com.hersch.testseafile;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Hersch.
 */
public class FileRooter {
    static Process process=null;
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
        String cmd = "chmod -R 777 " + filePath + "\n";
        try {
            out.write(cmd.getBytes());
            //out.flush();
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
