package com.hersch.testseafile.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Hersch on 2017/3/31.
 */
public class Network {
    public static boolean isNetWorkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // 去进行判断网络是否连接
        if (manager.getActiveNetworkInfo() != null) {
            return manager.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }
    public static final boolean ping() {
        String result = null;
        try {
            URL url = new URL("www.baidu.com");
            URLConnection urlConnection = url.openConnection();
            urlConnection.connect();
            return true;
        } catch (IOException e) {
            result = "IOException";
        } catch (RuntimeException e){
            result = "RuntimeException";
        }finally {
            Log.d("----result---", "result = " + result);
        }
        return false;
    }
}
