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
        Runtime runtime = null;
        Process process=null;
        try {
            if (runtime==null) {
                runtime=Runtime.getRuntime();
                process = runtime.exec("su");
            }
            InputStream inputStream = process.getInputStream();
            OutputStream outputStream = process.getOutputStream();
            cmd = cmd + "\n";
            outputStream.write(cmd.getBytes());
            outputStream.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            reader.close();
            inputStream.close();
            process.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
