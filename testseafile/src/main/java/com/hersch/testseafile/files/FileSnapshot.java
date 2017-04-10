package com.hersch.testseafile.files;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.hersch.testseafile.ui.SecondActivity;

import java.io.File;

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
     * @param strFilePath
     */
    public static void getFileList(Context context, String strFilePath,Handler handler) {
        File srcFile = new File(strFilePath);
        if (srcFile.canRead()) {
            System.out.println(srcFile.getAbsolutePath());
            if (srcFile.isDirectory()) {
                //作为目录的文件把目录下的文件都chmod一遍
                createDirectory(srcFile.getAbsolutePath());//在云端创建目录
                FileRooter.chmodList(context,777,"chmodAccess",srcFile.getAbsolutePath());
                File[] files = srcFile.listFiles();
                for (int i = 0; i < files.length; i++) {
                    getFileList(context, files[i].getAbsolutePath(),handler);
                }
                //回滚之前的权限
                if(!rollBackChmodAccess(context,strFilePath)){
                    System.out.println("文件权限回滚失败!");
                }
            } else {
                storeToSharedPreference(context, srcFile);
                Message message = handler.obtainMessage();
                message.what = SecondActivity.MSG_BACKUP_FILE_INFO;
                Bundle bundle = new Bundle();
                bundle.putString("filePath", srcFile.getAbsolutePath());
                message.setData(bundle);
                handler.sendMessage(message);
                System.out.println(srcFile.getAbsolutePath());
            }
        }
    }
    public static boolean rollBackChmodAccess(Context context,String strFilePath){
        File srcFile = new File(strFilePath);
        SharedPreferences sharedPreferences = context.getSharedPreferences("chmodAccess", Context.MODE_PRIVATE);
        int accessNum = sharedPreferences.getInt(srcFile.getAbsolutePath(),-1);
        if(accessNum!=-1) {
            FileRooter.chmodList(context,accessNum,"chmodAccess",srcFile.getAbsolutePath());
            return true;
        }
        else{
            return false;
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
        editorBackup.commit();
        String strTempMd5= sharedPrefsBackupMd5.getString(strFilePath, "null");//null代表返回的缺省值
        if(strTempMd5.equals("null")){
            //未在backupMd5中,第一次上传该文件
            SecondActivity.uploadFile("upload", srcFile.getAbsolutePath(),
                    srcFile.getName(), srcFile.getParent());
            strMd5 = common.getFileMD5(srcFile);
            editorBackup.putString(strFilePath, strMd5);
            editorBackup.commit();
        }
        else if(!strTempMd5.equals(strMd5)){
            //之前上传过文件,覆盖云盘文件
            SecondActivity.uploadFile("update",srcFile.getAbsolutePath(),
                    srcFile.getName(),srcFile.getParent());
            strMd5 = common.getFileMD5(srcFile);
            editorBackup.putString(strFilePath, strMd5);
            editorBackup.commit();
            editorChange.putString(strFilePath, strMd5);//将更改的文件列表存入changeMd5,,该文件用作以后同步的文件清单用
            editorChange.commit();
        }
    }
}
