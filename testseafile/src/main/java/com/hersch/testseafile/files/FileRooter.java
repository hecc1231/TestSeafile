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

    /**
     * chmod单个目录或者文件
     * @param accessNum
     * @param filePath
     * @param context
     */
    public static void chmodDirectory(Context context,int accessNum,String strPrefsName,String filePath){
        initProcess();
        cmdRootDirectory(context,accessNum,strPrefsName,filePath);
    }

    /**
     *将传入的目录参数下的文件列表都chmod为777
     * @param accessNum
     * @param filePath(当前目录)
     * @param context
     */
    public static void chmodList(Context context,int accessNum,String strPrefsName,String filePath) {
        initProcess();
        cmd(context, accessNum, strPrefsName, filePath);
    }

    /**
     * 获取文件的权限位信息
     * @return
     */
    public static int getFileAccessNum(String strCurrentDir){
        String str = null;
        try {
            process = Runtime.getRuntime().exec("su");
            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            dataOutputStream.writeBytes("ls -l -d " + strCurrentDir+"\n");
            str = bufferedReader.readLine();
            System.out.println(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sumChmodAccess(str);
    }
    /**
     * 用来获取根文件夹权限位信息并存入prefs如MicroMsg和shared_prefs
     * @param context
     * @param accessNum
     * @param strCurrentDir
     */
    private static void cmdRootDirectory(Context context,int accessNum,String strPrefsName,String strCurrentDir){
        File srcFile = new File(strCurrentDir);
        try {
//            process = Runtime.getRuntime().exec("su");
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            dataOutputStream = new DataOutputStream(process.getOutputStream());
//            dataOutputStream.writeBytes("ls -l -d " + strCurrentDir+"\n");
//            String str = null;
//            str = bufferedReader.readLine();
//            System.out.println(str);
            int fileAccessNum = getFileAccessNum(strCurrentDir);
            storeToChmodAccessPrefs(context, strPrefsName, srcFile.getAbsolutePath(), fileAccessNum);
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

    /**
     * 调用adbshell实现目录下所有文件的777chmod
     * @param context
     * @param accessNum
     * @param strPrefsName
     * @param strCurrentDir
     */
    private static void cmd(Context context,int accessNum,String strPrefsName,String strCurrentDir) {
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
                storeToChmodAccessPrefs(context, strPrefsName, files[index].getAbsolutePath(), sumChmodAccess(str));
                dataOutputStream.writeBytes("chmod " + accessNum + " " + files[index].getAbsolutePath() + "\n");
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
    public static void storeToChmodAccessPrefs(Context context,String strPrefsName,String key,int content){
        SharedPreferences sharedPrefsChmod = context.getSharedPreferences(strPrefsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editorChmod = sharedPrefsChmod.edit();
        int num = sharedPrefsChmod.getInt(key,-1);
        if(num==-1) {
            editorChmod.putInt(key, content);
            editorChmod.commit();
        }
    }
}
