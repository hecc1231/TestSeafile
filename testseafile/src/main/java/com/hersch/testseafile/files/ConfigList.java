package com.hersch.testseafile.files;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hersch on 2017/4/29.
 */
public class ConfigList {
    public static List<String> getList(String packageName){
        List<String> s = new ArrayList<>();
        switch (packageName) {
            case "com.tencent.mm":
                //s.add("/data/data/com.tencent.mm");
                //s.add("/storage/emulated/0/tencent/MicroMsg");
                s.add("/data/data/com.tencent.mm/MicroMsg");
                //s.add("/data/data/com.tencent.mm/shared_prefs");
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
