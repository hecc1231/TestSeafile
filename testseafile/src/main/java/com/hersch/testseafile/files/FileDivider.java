package com.hersch.testseafile.files;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author hchong
 */
public class FileDivider {
    static Process process = null;
    static DataOutputStream dataOutputStream = null;
    static BufferedReader bufferedReader = null;
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
    public static void split(String strFilePath,int fileSize){
        File file = new File(strFilePath);
        //if(file.length()>)
        initProcess();
        //process = Runtime.getRuntime().exec("su");
        dataOutputStream = new DataOutputStream(process.getOutputStream());
    }
    public static void merge(List<String> subFileList){

    }
}
