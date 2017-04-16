package com.hersch.testseafile.files;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.hersch.testseafile.ui.SecondActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hersch on 2017/2/9.
 */
public class FileSnapshot {
    public static int fileSize = 0;
    public static int differSize = 0;
    public static void traverseFileCy(Context context,String strFilePath,Handler handler) {
        //创建目录
        File file = new File(strFilePath);
        File[] files = file.listFiles();
        List<File> listFile = new ArrayList<>();
        List<File> listDir = new ArrayList<>();
        for (int j = 0; j < files.length; j++) {
            if (files[j].isDirectory()) {
                listDir.add(files[j]);
            } else {
                listFile.add(files[j]);
            }
        }
        if (listDir.size() > 0) {
            List<Integer> dirChmod = FileRooter.getAccessFromFiles(listDir);//获取当前目录下文件夹权限并将文件夹chmod为777
            for (int j = 0; j < listDir.size(); j++) {
                System.out.println(listDir.get(j).getAbsolutePath());
                createDirectory(listDir.get(j).getAbsolutePath());
                traverseFileCy(context, listDir.get(j).getAbsolutePath(), handler);
            }
            FileRooter.rollBackChmodFiles(dirChmod, listDir);
        }
        if (listFile.size() > 0) {
            List<Integer> fileChmod = FileRooter.cmdZipsAndChmod(listFile);
            for (int j = 0; j < listFile.size(); j++) {
                System.out.println(listFile.get(j).getAbsolutePath());
                storeToSharedPreference(context, listFile.get(j).getAbsolutePath());//存放并上传文件
            }
            FileRooter.rollBackChmodFiles(fileChmod, listFile);
            FileRooter.deleteZipsOfFiles(listFile);
        }
    }
    public static void rollBackChmodFile(Context context,String strFilePath){
        File srcFile = new File(strFilePath);
        SharedPreferences sharedPreferences = context.getSharedPreferences("chmodAccess", Context.MODE_PRIVATE);
        int accessNum = sharedPreferences.getInt(srcFile.getAbsolutePath(),-1);
        if(accessNum!=-1) {
            FileRooter.chmodFile(accessNum, srcFile.getAbsolutePath());
        }
        else{
        }
    }
    /**
     * 在云端建立目录
     * @param strFilePath
     */
    public static void createDirectory(String strFilePath) {
          File srcFile = new File(strFilePath);
          if(!SecondActivity.isFileExistOnCloud(srcFile.getAbsolutePath())){
              SecondActivity.createDirToCloud(srcFile.getAbsolutePath());
          }
    }
    /**
     * 将需要备份的文件以键值对形式存入SharedPrerence
     * @param context
     * @param strFilePath
     */
    public static void storeToSharedPreference(Context context,String strFilePath){
        String strMd5 = common.getFileMD5(strFilePath);
        SharedPreferences sharedPrefsBackupMd5 = context.getSharedPreferences("backupMd5", Context.MODE_PRIVATE);
        SharedPreferences sharedPrefsChange = context.getSharedPreferences("changeMd5",Context.MODE_PRIVATE);
        SharedPreferences.Editor editorBackup = sharedPrefsBackupMd5.edit();
        SharedPreferences.Editor editorChange = sharedPrefsChange.edit();
        editorChange.commit();
        editorBackup.commit();
        String strTempMd5= sharedPrefsBackupMd5.getString(strFilePath, "null");//null代表返回的缺省值
        if(strTempMd5.equals("null")||!strTempMd5.equals(strMd5)) {
            String desFilePath = strFilePath+".gz";//压缩文件存放在当前APP文件夹下
            File srcFile = new File(desFilePath);
            if (SecondActivity.isFileExistOnCloud(srcFile.getAbsolutePath())) {
                SecondActivity.uploadFile("update", srcFile.getAbsolutePath(),
                        srcFile.getName(), srcFile.getParent());
            } else {
                SecondActivity.uploadFile("upload", srcFile.getAbsolutePath(),
                        srcFile.getName(), srcFile.getParent());
            }
            if (strTempMd5.equals("null")) {
                //未在backupMd5中,第一次上传该文件
                strMd5 = common.getFileMD5(strFilePath);
                editorBackup.putString(strFilePath, strMd5);
                editorBackup.commit();
            } else if (!strTempMd5.equals(strMd5)) {
                strMd5 = common.getFileMD5(strFilePath);
                editorBackup.putString(strFilePath, strMd5);
                editorBackup.commit();
                editorChange.putString(strFilePath, strMd5);//将更改的文件列表存入changeMd5,,该文件用作以后同步的文件清单用
                editorChange.commit();
            }
        }
    }
    public static void gzipMsgFileToLocal(Context context, String strFilePath) {
        //创建目录
        File file = new File(SecondActivity.strCurrentPath + strFilePath);
        file.mkdirs();
        File[] files = new File(strFilePath).listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                //是文件将其chmod为777
                System.out.println(files[i].getAbsolutePath());
                int accessNum = FileRooter.getFileAccessNum(context, files[i].getAbsolutePath());
                System.out.println(accessNum);
                FileRooter.storeToChmodAccessPrefs(context, "chmodAccess", files[i].getAbsolutePath(), accessNum);
                FileRooter.chmodFile(777, files[i].getAbsolutePath());
                gzipMsgFileToLocal(context, files[i].getAbsolutePath());
                rollBackChmodFile(context, files[i].getAbsolutePath());//回滚文件夹的权限
            } else {
                //压缩到当前应用的文件夹
                System.out.println(files[i].getAbsolutePath());
                String srcFilePath = files[i].getAbsolutePath();
                String desFilePath = SecondActivity.strCurrentPath+srcFilePath + ".gz";
                FileRooter.cmdZip(srcFilePath,desFilePath);
            }
        }
    }
    public static void gzipLocalToMsg(Context context, String strFilePath) {
        File[] files = new File(strFilePath).listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                gzipLocalToMsg(context,files[i].getAbsolutePath());//将本地文件同步到微信应用下
            } else {
                //压缩到当前应用的文件夹
                String currentFilePath = files[i].getAbsolutePath();
                System.out.println(currentFilePath);
                String srcFilePath = currentFilePath;
                int i1 = srcFilePath.lastIndexOf(context.getPackageName());
                String desFilePath = srcFilePath.substring(i1+context.getPackageName().length(),srcFilePath.length()-3);
                FileRooter.cmdUnZip(srcFilePath, desFilePath);
                System.out.println(desFilePath);
            }
        }
    }
}
