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
    /**
     *微信文件备份至app根目录下,计算对应的MD5值存入SharePreference
     * @param kindCmd
     * @param context
     * @param arrayListFiles
     * @param srcFile
     * @param mHandler
     */
    public static void getFileList(int kindCmd,Context context,ArrayList<File> arrayListFiles,File srcFile,Handler mHandler) {
        if(!srcFile.canRead()||!srcFile.canWrite()||!srcFile.canExecute()) {
            CustomRooter.setFileMode("chmod 777 " + srcFile.getAbsolutePath());
        }
        if(srcFile.canRead()) {
            System.out.println(srcFile.getAbsolutePath());
            if (srcFile.isDirectory()) {
                File[] files = srcFile.listFiles();
                for (int i = 0; i < files.length; i++) {
                    getFileList(kindCmd,context,arrayListFiles, files[i], mHandler);
                }
            } else {
                arrayListFiles.add(srcFile);
                if(kindCmd==MainActivity.BACKUP_CMD) {
                    backUpFile(context, srcFile);
                    //storeToSharedPreference(context, srcFile.getAbsolutePath(), getFileMD5(srcFile));
                    Message msg = mHandler.obtainMessage();
                    System.out.println(srcFile.getAbsolutePath());
                    msg.what = MainActivity.FILE_UPDATE_MSG;
                    Bundle bundle = new Bundle();
                    bundle.putString("filename", srcFile.getPath());
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                }
                else{//比对指令

                }
            }
        }
        CustomRooter.setFileMode("chmod 771 "+srcFile.getAbsolutePath());
    }
    /**
     * 将当前浏览到的微信文件备份到app的目录下/data/data/packageName/fileRead.getAbsolutePath
     * @param context
     * @param srcFile
     */
    public static void backUpFile(Context context,File srcFile){
        String strParentPath = "/data/data/"+context.getPackageName()+srcFile.getParent();
        new File(strParentPath).mkdirs();//创建该文件以上的目录
        File destFile = new File(strParentPath+"/"+srcFile.getName());
        if(destFile.exists()){
            destFile.delete();
        }
        try {
            destFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.print("create File failed!");
        }
        InputStream inStream = null;
        try {
            inStream = new FileInputStream(srcFile);
            FileOutputStream outStream = new FileOutputStream(destFile);
            byte[] buf = new byte[1024];
            int byteRead = 0;
            while ((byteRead = inStream.read(buf)) != -1) {
                outStream.write(buf, 0, byteRead);
            }
            outStream.flush();
            outStream.close();
            inStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.print("file copy failed!");
        }

    }
    public static void storeToSharedPreference(Context context,String strFilePath,String strMd5){
        SharedPreferences sharedPreferences = context.getSharedPreferences("backupMd5",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String strTempMd5= sharedPreferences.getString(strFilePath, "null");//null代表返回的缺省值
        if(strTempMd5.equals("null")){
            //说明文件的MD5还未存入SharedPreference，用于初始化md5文件
            editor.putString(strFilePath,strMd5);
        }
        else if(!strTempMd5.equals(strMd5)){
            //说明文件被更改,需要做个记录比如存入另外一个文件

        }
    }
    /**
     * 判断当前微信进程是否运行
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isProcessRunning(Context context, String packageName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = activityManager.getRunningAppProcesses();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    public static String getFileMD5(File file) {
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, 1024);
            }
            in.close();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }

}
