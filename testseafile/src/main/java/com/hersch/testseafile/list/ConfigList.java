package com.hersch.testseafile.list;

import com.hersch.testseafile.files.FileBackup;
import com.hersch.testseafile.files.FileRooter;
import com.hersch.testseafile.ui.SecondActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Hersch
 * 列表类
 */
public class ConfigList {
    public static String[] getAppList(){
        String[] apps = new String[3];
        apps[0] = "com.tencent.mm";
        apps[1] = "com.tencent.mobileqq";
        apps[2] ="org.mozilla.firefox";
        return apps;
    }
    /**
     * 获取需要在云端(cloud)建立的初始目录或者是本地(local)需要提前chmod的目录(一定从小到大因为目录是递归建立的)
     * @param packageName
     * @return
     */
    public static List<String> getInitDirList(String kind,String packageName){
        List<String> s = new ArrayList<>();
        switch (packageName) {
            case"org.mozilla.firefox":
                s.add("/data");
                s.add("/data/data");
                s.add("/data/data/org.mozilla.firefox");
                s.add("/data/data/org.mozilla.firefox/files");
                s.add("/data/data/org.mozilla.firefox/shared_prefs");
                s.add("/data/data/org.mozilla.firefox/files/mozilla");
                break;
            case "com.tencent.mm":
                s.add("/data");
                s.add("/data/data");
                s.add("/data/system");
                s.add("/data/system/sync");
                s.add("/data/system/users");
                s.add("/data/system/users/0");
                s.add("/data/data/com.tencent.mm");
                s.add("/data/data/com.tencent.mm/MicroMsg");
                s.add("/data/data/com.tencent.mm/shared_prefs");
                s.add("/data/data/com.tencent.mm/files");
                break;
            case "com.tencent.mobileqq":
                s.add("/data");
                s.add("/data/data");
                s.add("/data/system");
                s.add("/data/data/com.tencent.mobileqq");
                s.add("/data/data/com.tencent.mobileqq/databases");
                s.add("/data/data/com.tencent.mobileqq/shared_prefs");
                s.add("/data/data/com.tencent.mobileqq/files");
                s.add("/data/data/com.tencent.mobileqq/files/gm_history");
                s.add("/data/data/com.tencent.mobileqq/files/ConfigStore2.dat");
                s.add("/data/media");
                s.add("/data/media/0");
                s.add("/data/media/0/tencent");
                s.add("/data/media/0/tencent/MobileQQ");
                s.add("/data/media/0/tencent/MobileQQ/diskcache");
                s.add("/data/media/0/tencent/MobileQQ/shortvideo");
                break;
        }
        List<String>cloudList = new ArrayList<>();
        cloudList.add("/"+packageName);
        cloudList.add("/"+packageName+"/version_"+SecondActivity.version);//文件格式version_0
        for(String strFilePath:s){
            cloudList.add("/"+packageName+"/version_"+SecondActivity.version+strFilePath);
        }
        if(kind.equals("local")){
            return s;
        }
        return cloudList;
    }
    public static List<String> getList(final String packageName){
        List<String> s = new ArrayList<>();
        switch (packageName) {
            case "org.mozilla.firefox":
                s.add("/data/data/org.mozilla.firefox/shared_prefs");//用户配置信息
                s = addSubFileofFilesInFox(s, "/data/data/org.mozilla.firefox/files/mozilla");//添加mozilla下的用户数据文件
                break;
            case "com.tencent.mm":
                s.add("/data/data/com.tencent.mm/MicroMsg");
                s.add("/data/data/com.tencent.mm/shared_prefs");
                s.add("/data/data/com.tencent.mm/files");
                s.add("/data/system/sync");
                s.add("/data/system/users/0");
                break;
            case "com.tencent.mobileqq":
                s.add("/data/data/com.tencent.mobileqq/databases");
                s.add("/data/data/com.tencent.mobileqq/shared_prefs");
                s.add("/data/data/com.tencent.mobileqq/files/gm_history");
                s.add("/data/data/com.tencent.mobileqq/files/ConfigStore2.dat");
                s.add("/data/media/0/tencent/MobileQQ/diskcache");
                s.add("/data/media/0/tencent/MobileQQ/shortvideo");
                s = addUserDir(s,"/data/media/0/tencent/MobileQQ");
                break;
        }
        return s;
    }

    /**
     * 添加/data/media/0/tencent/MobileQQ/61747311(用户账号)
     * @param list
     * @param filePath
     * @return
     */
    static List<String>addUserDir(List<String>list,String filePath){
        SecondActivity.chmodFileList.clear();
        SecondActivity.chmodIntList.clear();
        List<String>prePath = new ArrayList<>();
        prePath.add("/data");
        prePath.add("/data/media");
        prePath.add("/data/media/0");
        prePath.add("/data/media/0/tencent");
        prePath.add("/data/media/0/tencent/MobileQQ");
        FileRooter.getAccessFromFiles(prePath);
        File file = new File(filePath);
        File[] files = file.listFiles();
        String rexString = "[0-9]+";
        if(file.length()!=0) {
            for (File f : files) {
                if (f.getName().matches(rexString)) {
                    list.add(f.getAbsolutePath());
                    if(f.isDirectory()){
                        //云端创建该文件夹
                        FileBackup.createDirectory("/"+SecondActivity.processName+"/version_"+
                                SecondActivity.version+f.getAbsolutePath());
                    }
                }
            }
        }
        FileRooter.rollBackChmodFiles(SecondActivity.chmodIntList,SecondActivity.chmodFileList);
        SecondActivity.chmodFileList.clear();
        SecondActivity.chmodIntList.clear();
        return list;
    }
    /**
     * 添加firefox中的.default用户文件
     * @param list
     * @param filePath
     * @return
     */
    static List<String>addSubFileofFilesInFox(List<String>list,String filePath){
        SecondActivity.chmodFileList.clear();
        SecondActivity.chmodIntList.clear();
        List<String>prePath = new ArrayList<>();
        prePath.add("/data");
        prePath.add("/data/data");
        prePath.add("/data/data/org.mozilla.firefox");
        prePath.add("/data/data/org.mozilla.firefox/files");
        prePath.add("/data/data/org.mozilla.firefox/files/mozilla");
        FileRooter.getAccessFromFiles(prePath);
        File file = new File(filePath);
        File[] files = file.listFiles();
        String rexString = ".*\\.default";
        if(file.length()!=0) {
            for (File f : files) {
                if (f.getName().matches(rexString) || f.getName().equals("profiles.ini")) {
                    list.add(f.getAbsolutePath());
                }
                if(f.isDirectory()){
                    //云端创建该文件夹
                    FileBackup.createDirectory("/"+SecondActivity.processName+"/version_"+
                            SecondActivity.version+f.getAbsolutePath());
                }
            }
        }
        FileRooter.rollBackChmodFiles(SecondActivity.chmodIntList,SecondActivity.chmodFileList);
        SecondActivity.chmodFileList.clear();
        SecondActivity.chmodIntList.clear();
        return list;
    }
}
