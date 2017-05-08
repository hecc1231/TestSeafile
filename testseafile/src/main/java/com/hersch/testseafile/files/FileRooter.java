package com.hersch.testseafile.files;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.hersch.testseafile.ui.SecondActivity;

import org.apache.commons.lang.StringEscapeUtils;

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
    public static void deleteZipsOfFiles(List<String> fileList){
            initProcess();
            try {
                process = Runtime.getRuntime().exec("su");
                dataOutputStream = new DataOutputStream(process.getOutputStream());
                for(String strFilePath:fileList) {
                    strFilePath = escapeString(strFilePath);
                    dataOutputStream.writeBytes("rm " + strFilePath + "\n");
                }
                dataOutputStream.writeBytes("exit\n");
                dataOutputStream.flush();
                //process.waitFor();
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
    public static List<Integer> cmdChmod(List<String>files){
        List<Integer>integers = new ArrayList<>();
        initProcess();
        try {
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            for(String strFilePath:files) {
                strFilePath = escapeString(strFilePath);
                System.out.println("转义字符"+strFilePath);
                dataOutputStream.writeBytes("ls -l -d " + strFilePath + "\n");
                String str = bufferedReader.readLine();
                int chmodValue = sumChmodAccess(str);
                integers.add(chmodValue);//记录文件权限位
                System.out.println("权限字符:" + str + " " + strFilePath + "权限为" + chmodValue);
                dataOutputStream.writeBytes("chmod 777 " + strFilePath + "\n");
            }
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            //process.waitFor();
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
    public static List<Integer> cmdZipsAndChmod(List<String>files){
        List<Integer>integers = new ArrayList<>();
        initProcess();
        try {
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            for(String strFilePath:files) {
                strFilePath = escapeString(strFilePath);
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
            //process.waitFor();
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
    public static List<Integer> chmodPreDirPath(List<String>files){
        List<Integer>integers = new ArrayList<>();
        initProcess();
        try {
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            for(String strFilePath:files) {
                strFilePath = escapeString(strFilePath);
                dataOutputStream.writeBytes("ls -l -d " + strFilePath + "\n");
                String str = bufferedReader.readLine();
                int chmodValue = sumChmodAccess(str);
                integers.add(chmodValue);//记录文件权限位
                System.out.println("权限字符:" + str + " " + strFilePath + "权限为" + chmodValue);
                dataOutputStream.writeBytes("chmod 777 " + strFilePath + "\n");
            }
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            //process.waitFor();
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
    public static void rollBackChmodFiles(List<Integer>integers,List<String>fileList){
        initProcess();
        try {
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            for(int i=0;i<integers.size();i++){
                String strFilePath = fileList.get(i);
                strFilePath = escapeString(strFilePath);
                dataOutputStream.writeBytes("chmod "+integers.get(i)+" "+strFilePath+"\n");
                System.out.println(strFilePath+"还原权限: "+integers.get(i));
            }
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            //process.waitFor();
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
    public static void createDir(List<String>dirList) {
        initProcess();
        try {
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            for (String strFilePath : dirList) {
                strFilePath = escapeString(strFilePath);
                dataOutputStream.writeBytes("mkdir " + strFilePath + "\n");
            }
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            //process.waitFor();
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
                srcFilePath = escapeString(srcFilePath);
                String desFilePath = desFileList.get(i);
                desFilePath = escapeString(desFilePath);//将转义字符转义
                dataOutputStream.writeBytes("gzip -c -d " + srcFilePath+ ">" + desFilePath + "\n");
                System.out.println(srcFilePath + "---- unzip to ----" + desFilePath);
            }
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            //process.waitFor();
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
                String strFilePath = files.get(i);
                strFilePath = escapeString(strFilePath);
                dataOutputStream.writeBytes("ls -l -d " + strFilePath + "\n");
                String str = bufferedReader.readLine();
                int accessNum = sumChmodAccess(str);
                integers.add(accessNum);
                System.out.println(files.get(i)+"权限:"+accessNum);
                dataOutputStream.writeBytes("chmod 777 "+strFilePath+"\n");
            }
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            //process.waitFor();
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
    static String escapeString(String s){
        s = s.replaceAll("\\ ", "\\\\\\ ");//前一个参数为正则表达式格式"\\ "表示空格
        s = s.replaceAll("\\$", "\\\\\\$");//正则表达式表示\$是\\\$再字符转义三个斜杠
        return s;
    }
}
