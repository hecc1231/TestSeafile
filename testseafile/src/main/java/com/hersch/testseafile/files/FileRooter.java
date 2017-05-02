package com.hersch.testseafile.files;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.hersch.testseafile.ui.SecondActivity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Hersch.
 */
public class FileRooter {
    static Process process = null;
    static DataOutputStream dataOutputStream = null;
    static BufferedReader bufferedReader = null;
    /**
     * chmod单个目录或者文件
     * @param accessNum
     * @param filePath
     * @param context
     */
    public static void chmodRootDirectory(Context context, int accessNum, String strPrefsName, String filePath){
        initProcess();
        cmdRootDirectory(context, accessNum, strPrefsName, filePath);
    }
    public static void requestRoot(){
        initProcess();
        try{
            process = Runtime.getRuntime().exec("su");
            process.destroy();
        }catch (Exception e){
            e.printStackTrace();
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
    public static void chmodFile(int accessNum,String strFilePath){
        initProcess();
        try {
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            dataOutputStream.writeBytes("chmod " + accessNum + " " + strFilePath + "\n");
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            process.waitFor();
        } catch (Exception e){
        }finally {
            try {
                if(dataOutputStream!=null){
                    dataOutputStream.close();
                }
                process.destroy();
            }catch (IOException e){
                System.out.println("文件chmod失败");
            }
        }
    }
    public static int getFileAccessNum(Context context,String strFilePath){
        initProcess();
        String s = null;
        try {
            process = Runtime.getRuntime().exec("su");
            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            dataOutputStream.writeBytes("ls -l -d " + strFilePath + "\n");
            s = bufferedReader.readLine();
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            process.waitFor();
        } catch (IOException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if(dataOutputStream!=null){
                    dataOutputStream.close();
                }
                process.destroy();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return sumChmodAccess(s);
    }
    /**
     * 获取文件的权限位信息
     * 该函数dataOutputStream和process均未关闭记得调用后关闭dataOutputStream和关闭process
     * @return
     */
    public static List<Integer> getFilesAccessNum(Context context,Map<String,?>maps){
        List<Integer>chmodIntList = new ArrayList<>();
        initProcess();
        try {
            process = Runtime.getRuntime().exec("su");
            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            for(String key:maps.keySet()) {
                dataOutputStream.writeBytes("ls -l -d " + key + "\n");
                String str = null;
                str = bufferedReader.readLine();
                System.out.println(str);
                chmodIntList.add(sumChmodAccess(str));
            }
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            process.waitFor();
        } catch (IOException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if(dataOutputStream!=null){
                    dataOutputStream.close();
                }
                process.destroy();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return chmodIntList;
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
            process = Runtime.getRuntime().exec("su");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            dataOutputStream.writeBytes("ls -l -d " + strCurrentDir+"\n");
            String str = null;
            str = bufferedReader.readLine();
            System.out.println(str);
            storeToChmodAccessPrefs(context, strPrefsName, srcFile.getAbsolutePath(), sumChmodAccess(str));
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
    public static void deleteZipsOfFiles(List<String> fileList){
            initProcess();
            try {
                process = Runtime.getRuntime().exec("su");
                dataOutputStream = new DataOutputStream(process.getOutputStream());
                for(String strFilePath:fileList) {
                    //dataOutputStream.writeBytes("rm " + f.getAbsolutePath() + ".tar" + "\n");
                    dataOutputStream.writeBytes("rm " + strFilePath + "\n");
                }
                dataOutputStream.writeBytes("exit\n");
                dataOutputStream.flush();
                process.waitFor();
            } catch (Exception e) {
            } finally {
                try {
                    if (dataOutputStream != null) {
                        dataOutputStream.close();
                    }
                    process.destroy();
                } catch (IOException e) {
                    System.out.println("文件刪除失败");
                }
            }
    }
    public static List<Integer> cmdZipsAndChmod(List<String>files){
        List<Integer>integers = new ArrayList<>();
        initProcess();
        try {
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            for(String strFilePath:files) {
                dataOutputStream.writeBytes("gzip -c " + strFilePath + ">" + strFilePath
                        + ".gz" + "\n");//保证先压缩到当前app文件夹下记得创建父级目录
                dataOutputStream.writeBytes("ls -l -d " + strFilePath + "\n");
                String str = bufferedReader.readLine();
                int chmodValue = sumChmodAccess(str);
                integers.add(chmodValue);//记录文件权限位
                System.out.println("权限字符:" + str + " " + strFilePath + "权限为" + chmodValue);
                dataOutputStream.writeBytes("chmod 777 " + strFilePath + "\n");
                dataOutputStream.writeBytes("chmod 777 "+strFilePath+".gz"+"\n");
            }
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            process.waitFor();
        } catch (Exception e){
        }finally {
            try {
                if(dataOutputStream!=null){
                    dataOutputStream.close();
                }
                process.destroy();
            }catch (IOException e){
                System.out.println("文件压缩失败");
            }
        }
        return integers;
    }

    /**
     * chmod /data/data/com.tencent.mm 需要把之前的/data和/data/data都chmod
     * @param files
     * @return
     */
    public static void chmodPreDirPath(List<String>files){
        initProcess();
        try {
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            for(String strFilePath:files) {
                dataOutputStream.writeBytes("ls -l -d " + strFilePath + "\n");
                String str = bufferedReader.readLine();
                if(str.charAt(10)!=' '){//即当前目录不存在
                    continue;
                }
                int chmodValue = sumChmodAccess(str);
                SecondActivity.chmodIntList.add(chmodValue);//记录文件权限位
                SecondActivity.chmodFileList.add(strFilePath);
                System.out.println("权限字符:" + str + " " + strFilePath + "权限为" + chmodValue);
                dataOutputStream.writeBytes("chmod 777 " + strFilePath + "\n");
            }
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            process.waitFor();
        } catch (Exception e){
        }finally {
            try {
                if(dataOutputStream!=null){
                    dataOutputStream.close();
                }
                process.destroy();
            }catch (IOException e){
                System.out.println("文件压缩失败");
            }
        }
    }
    public static void createDir(List<String>dirList) {
        initProcess();
        try {
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            for (String strFilePath : dirList) {
                dataOutputStream.writeBytes("mkdir " + strFilePath + "\n");
            }
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            process.waitFor();
        } catch (Exception e) {
        } finally {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                process.destroy();
            } catch (IOException e) {
                System.out.println("文件压缩失败");
            }
        }
    }
    public static void cmdUnzips(List<String>srcFileList,List<String>desFileList){
        initProcess();
        try {
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            for(int i=0;i<srcFileList.size();i++) {
                String srcFilePath = srcFileList.get(i);
                String desFilePath = desFileList.get(i);
                dataOutputStream.writeBytes("gzip -c -d " + srcFilePath+ ">" + desFilePath + "\n");
                System.out.println(srcFilePath + "---- unzip to ----" + desFilePath);
            }
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            process.waitFor();
        } catch (Exception e){
        }finally {
            try {
                if(dataOutputStream!=null){
                    dataOutputStream.close();
                }
                process.destroy();
                System.out.println("文件解压成功");
            }catch (IOException e){
                System.out.println("文件解压失败");
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
    public static List<Integer> getAccessFromFiles(List<String>files){
        List<Integer>integers = new ArrayList<>();
        initProcess();
        try {
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            for(int i=0;i<files.size();i++) {
                dataOutputStream.writeBytes("ls -l -d " + files.get(i) + "\n");
                String str = bufferedReader.readLine();
                int accessNum = sumChmodAccess(str);
                integers.add(accessNum);
                System.out.println(files.get(i)+"权限:"+accessNum);
                dataOutputStream.writeBytes("chmod 777 "+files.get(i)+"\n");
            }
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            process.waitFor();
        } catch (Exception e){
        }finally {
            try {
                if(dataOutputStream!=null){
                    dataOutputStream.close();
                }
                process.destroy();
                System.out.println("获取权限成功");
            }catch (IOException e){
                System.out.println("获取权限失败");
            }
        }
        return integers;
    }
    public static void rollBackChmodFiles(List<Integer>integers,List<String>fileList){
        initProcess();
        try {
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            for(int i=0;i<integers.size();i++){
                dataOutputStream.writeBytes("chmod "+integers.get(i)+" "+fileList.get(i)+"\n");
                System.out.println(fileList.get(i)+"还原权限: "+integers.get(i));
            }
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            process.waitFor();
        } catch (Exception e){
        }finally {
            try {
                if(dataOutputStream!=null){
                    dataOutputStream.close();
                }
                process.destroy();
            }catch (IOException e){
                System.out.println("文件还原权限失败");
            }
        }
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
