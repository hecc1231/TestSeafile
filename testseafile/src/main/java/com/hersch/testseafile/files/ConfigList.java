package com.hersch.testseafile.files;

import com.hersch.testseafile.ui.SecondActivity;

import java.io.File;
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
    public static List<String> getAppList(){
        List<String>s = new ArrayList<>();
        s.add("com.tencent.mm");
        s.add("com.tencent.mobileqq");
        return s;
    }
    /**
     * 获取需要在云端(cloud)建立的初始目录或者是本地(local)需要提前chmod的目录(一定从小到大因为目录是递归建立的)
     * @param packageName
     * @return
     */
    public static List<String> getInitDirList(String kind,String packageName){
        List<String> s = new ArrayList<>();
        switch (packageName) {
            case "system":
                s.add("/data");
                s.add("/data/data");
                s.add("/data/data/"+SecondActivity.processName);
                //s.add("/data/data/com.android.settings");
                //s.add("/data/data/com.android.settings/app_webview");
                //s.add("/data/data/com.tencent.mobileqq/files");
                //s.add("/data/data/com.tencent.mobileqq/files/nearby_gray_tips_configs");
                s.add("/data/media");
                s.add("/data/media/0");
                s.add("/data/system");
                s.add("/data/app");
                s.add("/data/backup");
                s.add("/data/dalvik-cache");
                break;
            case "com.tencent.mm":
                s.add("/data");
                s.add("/data/data");
                s.add("/data/data/com.tencent.mm");
                s.add("/data/data/com.tencent.mm/MicroMsg");
                s.add("/data/data/com.tencent.mm/shared_prefs");
                s.add("/data/data/com.tencent.mm/files");
                s.add("/data/data/com.tencent.mm/tinker");
                s.add("/data/data/com.tencent.mm/app_tbs");
                s.add("/data/media");
                s.add("/data/media/0");
                s.add("/data/media/0/tencent");
                s.add("/data/media/0/tencent/MicroMsg");
                s.add("/data/media/0/tencent/vusericon");
                s.add("/data/media/0/tencent/CDNTemp");
                break;
            case "com.tencent.mobileqq":
                s.add("/data");
                s.add("/data/data");
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
                s.add("/data/media/0/tencent/MobileQQ/data");
                s.add("/data/media/0/tencent/MobileQQ/shortvideo");
                break;
        }
        List<String>cloudList = new ArrayList<>();
        cloudList.add("/"+packageName);
        for(String strFilePath:s){
            cloudList.add("/"+packageName+strFilePath);
        }
        if(kind.equals("local")){
            return s;
        }
        return cloudList;
    }
    public static List<String> getList(final String packageName){
        List<String> s = new ArrayList<>();
        switch (packageName) {
            case "com.tencent.mm":
                s.add("/data/data/com.tencent.mm/MicroMsg");
                s.add("/data/data/com.tencent.mm/shared_prefs");
                s.add("/data/data/com.tencent.mm/tinker");
                s.add("/data/data/com.tencent.mm/app_tbs");
                //s.add("/data/data/com.tencent.mm/files");
                //s.add("/data/media/0/tencent/MicroMsg");
                s.add("/data/media/0/tencent/vusericon");
                s.add("/data/media/0/tencent/CDNTemp");
                break;
            case "com.tencent.mobileqq":
                s.add("/data/data/com.tencent.mobileqq/databases");
                s.add("/data/data/com.tencent.mobileqq/shared_prefs");
                s.add("/data/data/com.tencent.mobileqq/files/gm_history");
                s.add("/data/data/com.tencent.mobileqq/files/ConfigStore2.dat");
                //s.add("/data/media/0/tencent/MobileQQ/diskcache");
                //s.add("/data/media/0/tencent/MobileQQ/shortvideo");
                //s.add("/data/media/0/tencent/MobileQQ/data");
                //s = addUserDir(s,"/data/media/0/tecent/MobileQQ");
                break;
            case "system":
                s.add("/data/data");
                s.add("/data/system");
                s.add("/data/app");
                s.add("/data/dalvik-cache");
                s.add("/data/backup");
                //s.add("/data/data/com.android.settings/app_webview");
                //s.add("/data/data/com.tencent.mobileqq/files/nearby_gray_tips_configs");
                //s.add("/data/media/"+SecondActivity.processName);
                break;
        }
        return s;
    }

    /**
     * 添加/data/data/com.tencent.mobileqq/用户文件夹(61747311)
     */
    static List<String> addUserDir(List<String>list,String preName){
        List<String>preDirList = new ArrayList<>();
        preDirList.add("/data");
        preDirList.add("/data/media");
        preDirList.add("/data/media/0");
        preDirList.add("/data/media/0/tencent");
        preDirList.add("/data/media/0/tencent/MobileQQ");
        FileRooter.chmodPreDirPath(preDirList);//chmod 当前路径保证能够访问
        File file = new File("/data/media/0/tencent/MobileQQ");
        File[] files = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                Pattern pattern = Pattern.compile("[0-9]+");
                Matcher matcher = pattern.matcher(filename);
                if(matcher.find()){
                    return true;
                }
                return false;
            }
        });
        if(files.length!=0){
            for(File f:files){
                list.add(f.getAbsolutePath());
                FileBackup.createDirectory(f.getAbsolutePath());
            }
        }
        FileRooter.rollBackChmodFiles(SecondActivity.chmodIntList,SecondActivity.chmodFileList);
        return list;
    }
    public static List<String>getEscapeString(){
        List<String>list = new ArrayList<>();
        list.add("$");
        list.add(" ");
        return list;
    }
    /**
     * 添加/data/data/com.tencent.mobileqq/[0-9]+
     */
    static List<String> addStreamFile(List<String>list,String name){
        File file = new File("/data/data/com.tencent.mobileqq/files");
        File[] files = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if(filename.contains("commonusedSystemEmojiInfoFile_")){
                    return true;
                }
                return false;
            }
        });
        if(files.length!=0){
            for(File f:files){
                list.add(f.getAbsolutePath());
            }
        }
        return list;
    }
}
