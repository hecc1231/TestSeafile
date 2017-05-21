package com.hersch.testseafile.files;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import com.hersch.testseafile.ui.SecondActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Hersch
 */
public class FileBackup {
    public static void traverseFileCy(Context context,String strFilePath,Handler handler) {
        //创建目录
        String cloudPremix = "/"+SecondActivity.processName+"/version_"+SecondActivity.version;
        List<String> listFile = new ArrayList<>();
        List<String> listDir = new ArrayList<>();
        //List<String> requestList = ConfigList.getList(SecondActivity.processName);
        File file = new File(strFilePath);
        if(file.isDirectory()) {
            File[] files = file.listFiles();
            for (int j = 0; j < files.length; j++) {
                if (files[j].isDirectory()) {
                    //if (isValid(files[j].getAbsolutePath(), requestList)) {
                        listDir.add(files[j].getAbsolutePath());
                    //}
                } else {
                    //if (isValid(files[j].getAbsolutePath(), requestList)) {
                        listFile.add(files[j].getAbsolutePath());
                    //}
                }
            }
        }
        else{
            //if(isValid(file.getAbsolutePath(),requestList)){
                listFile.add(file.getAbsolutePath());
            //}
        }
        if (listDir.size() > 0) {
            FileRooter.getAccessFromFiles(listDir);//获取当前目录下文件夹权限并将文件夹chmod为777
            for (int j = 0; j < listDir.size(); j++) {
                File dir = new File(listDir.get(j));
                System.out.println(dir.getAbsolutePath()+"---文件夹大小---"+dir.getTotalSpace());
                if(dir.list().length>0) {//文件夹不空才记录
                    System.out.println(listDir.get(j));
                    createDirectory(cloudPremix+listDir.get(j));
                    traverseFileCy(context, listDir.get(j), handler);
                }
            }
        }
        if (listFile.size() > 0) {
            FileRooter.cmdZipsAndChmod(listFile);
            List<String>deleteList = new ArrayList<>();
            for (int j = 0; j < listFile.size(); j++) {
                File cFile = new File(listFile.get(j));//存在文件不能访问
                if(!cFile.canRead()){
                    continue;
                }
                System.out.println(listFile.get(j));
                storeToPrefsAndUpload(context, listFile.get(j),listFile.get(j)+".gz");//存放并上传文件
                deleteList.add(listFile.get(j)+".gz");
            }
            FileRooter.deleteZipsOfFiles(deleteList);
        }
    }
    /**
     * 判断遍历的文件或者文件夹是否是需要的文件
     * @param strFilePath
     * @param initList
     * @return
     */
    static boolean isValid(String strFilePath,List<String>initList){
        for(String s:initList){
            if(s.equals(strFilePath)){
                return true;
            }
            else if(strFilePath.contains(s)||s.contains(strFilePath)){
                return true;
            }
        }
        return false;
    }
    /**
     * 在云端建立目录
     * @param strFilePath
     */
    public static void createDirectory(String strFilePath) {
        if(!SecondActivity.isFileExistOnCloud(strFilePath)){
            SecondActivity.createDirToCloud(strFilePath);
        }
    }
    /**
     * 将需要备份的文件以键值对形式存入SharedPrerence
     * @param context
     * @param strFilePath
     */
    public static void storeToPrefsAndUpload(Context context, String strFilePath,String zipFilePath){
        String cloudPremix = "/"+SecondActivity.processName+"/version_"+SecondActivity.version;
        String strMd5 = common.getMd5ByFile(strFilePath);
//        SharedPreferences sharedPrefsBackupMd5 = context.getSharedPreferences("backupMd5_"+SecondActivity.processName, Context.MODE_PRIVATE);
//        SharedPreferences sharedPrefsChange = context.getSharedPreferences("changeMd5_"+SecondActivity.processName,Context.MODE_PRIVATE);
//        SharedPreferences.Editor editorBackup = sharedPrefsBackupMd5.edit();
//        SharedPreferences.Editor editorChange = sharedPrefsChange.edit();
        SharedPreferences sharedPrefsBackupMd5 = context.getSharedPreferences(SecondActivity.processName + "_version_0",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editorBackup = sharedPrefsBackupMd5.edit();
        //editorBackup.commit();
        String strTempMd5= sharedPrefsBackupMd5.getString(strFilePath, "null");//null代表返回的缺省值
        //首先合并此前状态的所有change_version的文件
        if(strTempMd5.equals("null")||!strTempMd5.equals(strMd5)) {
            File srcFile = new File(strFilePath);//判断源文件的大小
            if(srcFile.length()<FileSM.MIN_FILE_SIZE) {
                String strCloudFilePath = cloudPremix+zipFilePath;
                File cloudFile = new File(strCloudFilePath);
                if (SecondActivity.isFileExistOnCloud(strCloudFilePath)) {
                    SecondActivity.uploadFile("update", zipFilePath,
                            cloudFile.getName(), cloudFile.getParent());
                } else {
                    SecondActivity.uploadFile("upload", zipFilePath,
                            cloudFile.getName(), cloudFile.getParent());
                }
            }
            else{
                List<String>subFileList = FileSM.split(zipFilePath);
                for(String s:subFileList){
                    File cloudFile = new File(cloudPremix+s);
                    if (SecondActivity.isFileExistOnCloud(cloudFile.getAbsolutePath())) {
                        SecondActivity.uploadFile("update", s,
                                cloudFile.getName(), cloudFile.getParent());
                    } else {
                        SecondActivity.uploadFile("upload", s,
                                cloudFile.getName(), cloudFile.getParent());
                    }
                }
                FileRooter.deleteZipsOfFiles(subFileList);
            }
//            strMd5 = common.getFileMD5(strFilePath);
//            editorBackup.putString(strFilePath, strMd5);
//            editorBackup.commit();
//            if (strTempMd5.equals("null")) {
//                if(SecondActivity.FLAG_BACKUP!=1){
//                    editorChange.putString(strFilePath, strMd5);//将更改的文件列表存入changeMd5,,该文件用作以后同步的文件清单用
//                    editorChange.commit();
//                }
//            } else if (!strTempMd5.equals(strMd5)) {
//                editorChange.putString(strFilePath, strMd5);//将更改的文件列表存入changeMd5,,该文件用作以后同步的文件清单用
//                editorChange.commit();
//            }
            if(SecondActivity.version!=0) {
                SharedPreferences sharedPrefsChange = context.getSharedPreferences(SecondActivity.processName + "_version_" + SecondActivity.version,
                        Context.MODE_PRIVATE);
                SharedPreferences.Editor editorChange = sharedPrefsChange.edit();
                editorChange.commit();
                editorChange.putString(strFilePath,strMd5);
                editorChange.commit();
            }
            else{
                editorBackup.putString(strFilePath,strMd5);
                editorBackup.commit();
            }
        }
    }
}