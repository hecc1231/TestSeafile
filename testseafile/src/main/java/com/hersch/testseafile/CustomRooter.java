package com.hersch.testseafile;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * 获取Root权限以及撤销Root权限
 */
public class CustomRooter {
    public static void setFileMode(String cmd) {
        Process process=null;
        try {
            process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd+"\n");
            os.writeBytes("exit\n");
            os.flush();
            process.destroy();
            System.out.println("rooted");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
