package com.hersch.testseafile.files;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Hersch.
 */
public class FileRooter {
    static Process process = null;
    static DataOutputStream dataOutputStream = null;
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

    /**
     * 多个根目录
     * @param accessNum
     * @param filePath
     * @param context
     */
    public static void chmodFile(int accessNum,String filePath,Context context){
        initProcess();
        cmdRootDirectory(accessNum,filePath,context);
    }

    /**
     *将传入的目录参数下的文件列表都chmod为777
     * @param accessNum
     * @param filePath(当前目录)
     * @param context
     */
    public static void chmodList(int accessNum,String filePath,Context context) {
        initProcess();
        cmd(accessNum, filePath, context);
    }
    /**
     * 用来获取根文件夹如MicroMsg和shared_prefs
     * @param accessNum
     * @param strCurrentDir
     * @param context
     */
    private static void cmdRootDirectory(int accessNum,String strCurrentDir,Context context){
        File srcFile = new File(strCurrentDir);
        try {
            process = Runtime.getRuntime().exec("su");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            dataOutputStream.writeBytes("ls -l -d " + strCurrentDir+"\n");
            String str = null;
            str = bufferedReader.readLine();
            System.out.println(str);
            storeToPreference(srcFile.getAbsolutePath(), sumChmodAccess(str), context);
            dataOutputStream.writeBytes("chmod " + accessNum + " " + srcFile.getAbsolutePath() + "\n");
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            bufferedReader.close();
            process.waitFor();
        } catch (Exception e) {
        } finally {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
    }
    private static void cmd(int accessNum,String strCurrentDir,Context context) {
        File srcFile = new File(strCurrentDir);
        File[] files = srcFile.listFiles();
        try {
            process = Runtime.getRuntime().exec("su");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            dataOutputStream.writeBytes("ls -al " + strCurrentDir+"\n");
            String str = null;
            int index = 0;
            while (index<files.length) {
                //读取到的正好是文件个数
                str = bufferedReader.readLine();
                System.out.println(str);
                storeToPreference(files[index].getAbsolutePath(),sumChmodAccess(str),context);
                dataOutputStream.writeBytes("chmod " + accessNum+" "+files[index].getAbsolutePath() + "\n");
                index++;
            }
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            bufferedReader.close();
            process.waitFor();
        } catch (Exception e) {
        } finally {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
    }
    private static int sumChmodAccess(String s){
        String s1 = s.substring(1,4);
        String s2 = s.substring(4,7);
        String s3 = s.substring(7,10);
        return sumSegment(s1)*100+sumSegment(s2)*10+sumSegment(s3);
    }
    private static int sumSegment(String s){
        int sum = 0;
        if(s.charAt(0)=='r'){
            sum+=4;
        }
        if(s.charAt(1)=='w'){
            sum+=2;
        }
        if(s.charAt(2)=='x'){
            sum+=1;
        }
        return sum;
    }
    private static void storeToPreference(String key,int content,Context context){
        SharedPreferences sharedPrefsChmod = context.getSharedPreferences("chmodAccess", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorChmod = sharedPrefsChmod.edit();
        int num = sharedPrefsChmod.getInt(key,-1);
        if(num==-1) {
            editorChmod.putInt(key, content);
            editorChmod.commit();
        }
    }
}
