package com.hersch.testseafile.files;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
     * 获取需要在云端建立的初始目录(一定从小到大因为目录是递归建立的)
     * @param packageName
     * @return
     */
    public static List<String> getInitDirList(String packageName){
        List<String> s = new ArrayList<>();
        switch (packageName) {
            case "com.tencent.mm":
                s.add("/data");
                s.add("/data/data");
                s.add("/data/data/com.tencent.mm");
                s.add("/data/data/com.tencent.mm/MicroMsg");
                s.add("/data/data/com.tencent.mm/shared_prefs");
                s.add("/storage");
                s.add("/storage/emulated");
                s.add("/storage/emulated/0");
                s.add("/storage/emulated/0/tencent");
                s.add("/storage/emulated/0/tencent/MicroMsg");
                break;
            case "com.tencent.mobileqq":
                s.add("/data");
                s.add("/data/data");
                s.add("/data/data/com.tencent.mobileqq");
                s.add("/data/data/com.tencent.mobileqq/databases");
                s.add("/data/media");
                s.add("/data/media/0");
                s.add("/data/media/0/tencent");
                s.add("/data/media/0/tencent/MobileQQ");
                s.add("/data/media/0/tencent/MobileQQ/diskcache");
                s.add("/data/media/0/tencent/MobileQQ/shortvideo");
                break;
        }
        return s;
    }
    public static List<String> getList(String packageName){
        List<String> s = new ArrayList<>();
        switch (packageName) {
            case "com.tencent.mm":
                s.add("/data/data/com.tencent.mm/MicroMsg");
                s.add("/data/data/com.tencent.mm/shared_prefs");
                break;
            case "com.tencent.mobileqq":
                s.add("/data/data/com.tencent.mobileqq/databases");
                s.add("/data/media/0/tencent/MobileQQ/diskcache");
                s.add("/data/media/0/tencent/MobileQQ/shortvideo");
                break;
        }
        return s;
    }
}
