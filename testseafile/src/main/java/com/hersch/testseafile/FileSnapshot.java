package com.hersch.testseafile;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hersch on 2017/2/9.
 */
public class FileSnapshot {
    public static int fileSize = 0;
    public static int differSize = 0;
    /**
     * 遍历微信文件备份至app根目录下,计算对应的MD5值存入SharePreference
     *
     * @param context
     * @param srcFile
     */
    public static void getFileList(Context context, File srcFile) {
        if (srcFile.canRead()) {
            System.out.println(srcFile.getAbsolutePath());
            if (srcFile.isDirectory()) {
                createDirectory(context, srcFile);
                File[] files = srcFile.listFiles();
                for (int i = 0; i < files.length; i++) {
                    getFileList(context, files[i]);
                }
            } else {
                fileSize++;
                storeToSharedPreference(context, srcFile);
                System.out.println(srcFile.getAbsolutePath());
            }
        }
    }

    /**
     * 在云端建立目录
     * @param context
     * @param srcFile
     */
    public static void createDirectory(Context context,File srcFile) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("dirList", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String strTempMd5= sharedPreferences.getString(srcFile.getAbsolutePath(), "null");//null代表返回的缺省值
        if(strTempMd5.equals("null")) {
            MainActivity.createDirToCloud(srcFile.getAbsolutePath());
            editor.putString(srcFile.getAbsolutePath(),srcFile.getAbsolutePath());
            editor.commit();
        }
    }
    /**
     * 将需要备份的文件以键值对形式存入SharedPrerence
     * @param context
     * @param srcFile
     */
    public static void storeToSharedPreference(Context context,File srcFile){
        String strFilePath = srcFile.getAbsolutePath();
        String strMd5 = common.getFileMD5(srcFile);
        SharedPreferences sharedPrefsBackupMd5 = context.getSharedPreferences("backupMd5", Context.MODE_PRIVATE);
        SharedPreferences sharedPrefsChange = context.getSharedPreferences("changeMd5",Context.MODE_PRIVATE);
        SharedPreferences.Editor editorBackup = sharedPrefsBackupMd5.edit();
        SharedPreferences.Editor editorChange = sharedPrefsChange.edit();
        editorChange.commit();
        String strTempMd5= sharedPrefsBackupMd5.getString(strFilePath, "null");//null代表返回的缺省值
        if(strTempMd5.equals("null")){
            //upload File
            MainActivity.uploadFile("upload",srcFile.getAbsolutePath(),
                    srcFile.getName(),srcFile.getParent());
            strMd5 = common.getFileMD5(srcFile);
            editorBackup.putString(strFilePath, strMd5);
            editorBackup.commit();
            differSize++;
        }
        else if(!strTempMd5.equals(strMd5)){
            //update File
            MainActivity.uploadFile("update",srcFile.getAbsolutePath(),
                    srcFile.getName(),srcFile.getParent());
            strMd5 = common.getFileMD5(srcFile);
            editorBackup.putString(strFilePath, strMd5);
            editorBackup.commit();
            editorChange.putString(strFilePath, strMd5);//将更改的文件列表存入changeMd5,,该文件用作以后同步的文件清单用
            editorChange.commit();
            differSize++;
        }
    }
}
