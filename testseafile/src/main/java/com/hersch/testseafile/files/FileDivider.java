package com.hersch.testseafile.files;

import com.hersch.testseafile.ui.SecondActivity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.FileHandler;

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
    public static void split(String strFilePath){
        initProcess();
        try {
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            dataOutputStream.writeBytes("split -b 1m "+strFilePath+" -a 1 "+strFilePath+"-"+"\n");
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            try {
                if(dataOutputStream!=null){
                    dataOutputStream.close();
                }
                process.destroy();
                System.out.println("文件划分成功");
            }catch (IOException e){
                System.out.println("文件划分失败");
            }
        }
    }
    public static void merge(List<String> subFileList){
        String cmd = " ";//记住空格 cat file1 file2 子文件格式为filename.gz-a
        String tempfileName = new File(subFileList.get(0)).getName();
        String zipfileName = tempfileName.substring(0, tempfileName.length() - 2);
        String targetFilePath = SecondActivity.strCurrentPath+"/out/"+zipfileName;
        for(String s:subFileList){
            cmd+=" "+s;
        }
        initProcess();
        try {
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            dataOutputStream.writeBytes("cat" + cmd + ">" + targetFilePath + "\n");
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            try {
                if(dataOutputStream!=null){
                    dataOutputStream.close();
                }
                process.destroy();
                System.out.println("合并成功");
            }catch (IOException e){
                System.out.println("合并失败");
            }
        }
    }
}
