package com.hersch.testseafile.net;

/**
 * Created by Hersch on 2017/2/8.
 */

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

public class HttpRequest {
    public static String strIpAddress="192.168.137.1";
    public static byte[] m_binArray = null;

    /**
     *
     * @param url
     * @param param
     * @param strCookie(包含登录后的token和sessionid,每个包必须携带作为认证信息)
     * @return
     */
    public static String sendGet(String url, String param, String strCookie) {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url + "?" + param;
            urlNameString = checkParam(urlNameString);
            //urlNameString = URLEncoder.encode(urlNameString, "utf-8");
            //urlNameString = checkParam(urlNameString);
            URL realUrl = new URL(urlNameString);
            URLConnection connection = realUrl.openConnection();
            connection.setRequestProperty("accept", "application/json, text/javascript, */*; q=0.01");
            connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36");
            connection.setRequestProperty("Referer", "http://"+strIpAddress+":8000/");
            connection.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
            connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
            connection.setRequestProperty("Cookie", strCookie);
            connection.setRequestProperty("Host", strIpAddress + ":8000");
            connection.connect();
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        }catch (Exception e){
            System.out.println("不存在版本");
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    public static byte[] downloadFile(String url, String param, String strCookie) {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url + "?" + param;
            urlNameString = checkParam(urlNameString);
            //urlNameString = URLEncoder.encode(urlNameString,"utf-8");
            URL realUrl = new URL(urlNameString);
            URLConnection connection = realUrl.openConnection();
            connection.setRequestProperty("accept", "application/json, text/javascript, */*; q=0.01");
            connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36");
            connection.setRequestProperty("Referer", "http://"+strIpAddress+":8000/");
            connection.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
            connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
            connection.setRequestProperty("Cookie", strCookie);
            connection.setRequestProperty("Host", strIpAddress + ":8000");
            connection.connect();
            String length = connection.getHeaderField("Content-Length");
            System.out.println("Content-Length:" + length);
            int totalLen = Integer.valueOf(length);
            m_binArray = new byte[totalLen];
            BufferedInputStream input =new BufferedInputStream(connection.getInputStream());
            int readCount = 0;
            while(readCount<Integer.valueOf(length)) {
                readCount += input.read(m_binArray, readCount, totalLen - readCount);//read方法不一定读取指定的totalLen字节，因此需要多次
                // 记录已经读到的字节数readCount----->之前同步崩溃的原因,没有下载完整的文件
            }

            input.close();
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

        return m_binArray;
    }
//    public static String sendGetRoot(String url, String param, String strCookie) {
//        String result = "";
//        BufferedReader in = null;
//        String strParamValue = "";
//        param = checkParam(param);
//        url = checkParam(url);
//        try {
//            String urlNameString = url;
//            URL realUrl = new URL(urlNameString);
//            URLConnection connection = realUrl.openConnection();
//            connection.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
//            connection.setRequestProperty("Cache-Control", "max-age=0");
//            connection.setRequestProperty("connection", "Keep-Alive");
//            connection.setRequestProperty("user-agent",
//                    "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36");
//            connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
//            connection.setRequestProperty("Referer", "http://"+strIpAddress+":8000/accounts/login/");
//            connection.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
//            connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
//            connection.setRequestProperty("Cookie", strCookie);
//            connection.connect();
//            Map<String, List<String>> map = connection.getHeaderFields();
//            for (String key : map.keySet()) {
//                if(key!=null && !key.isEmpty() && key.equals(param))
//                {
//                    for(String value: map.get(key))
//                    {
//                        if(value.startsWith("sessionid"))
//                        {
//                            strParamValue += value.substring(0,value.indexOf(";")+1) + " ";
//                        }
//
//                        if(value.startsWith("csrftoken"))
//                        {
//                            strParamValue += value.substring(0,value.indexOf(";")+1);
//                        }
//                    }
//
//                }
//            }
//            in = new BufferedReader(new InputStreamReader(
//                    connection.getInputStream()));
//            String line;
//            while ((line = in.readLine()) != null) {
//                result += line;
//            }
//        } catch (Exception e) {
//            System.out.println("发送GET请求出现异常！" + e);
//            e.printStackTrace();
//        }
//        finally {
//            try {
//                if (in != null) {
//                    in.close();
//                }
//            } catch (Exception e2) {
//                e2.printStackTrace();
//            }
//        }
//        return result;
//    }
    public static String sendGetHeadItem(String url, String param) {
        String result = "";
        BufferedReader in = null;
        String strParamValue = "";
        param = checkParam(param);
        url = checkParam(url);
        try {
            String urlNameString = url;
            URL realUrl = new URL(urlNameString);
            URLConnection connection = realUrl.openConnection();
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36");
            connection.setRequestProperty("Host", strIpAddress+":8000");//8082
            connection.connect();
            Map<String, List<String>> map = connection.getHeaderFields();
            for (String key : map.keySet()) {
                if(key!=null && !key.isEmpty() && key.equals(param))
                {
                    for(String value: map.get(key))
                    {
                        if(value.startsWith("sessionid"))
                        {
                            strParamValue += value.substring(0,value.indexOf(";")+1) + " ";
                        }

                        if(value.startsWith("csrftoken"))
                        {
                            strParamValue += value.substring(0,value.indexOf(";")+1);
                        }
                    }

                }
            }
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return strParamValue;
    }
    public static String sendPost(String url, String param, String strCookie) {
        OutputStreamWriter out = null;
        BufferedReader in = null;
        String result = "";
        String strParamValue = "";
        param = checkParam(param);
        url = checkParam(url);
        try {
            URL realUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection)realUrl.openConnection();
            connection.setRequestProperty("Host", strIpAddress+":8000");
            connection.setRequestProperty("Content-Length",String.valueOf(param.length()) );
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Cookie", strCookie);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            System.out.println(strCookie);
            System.out.println(param);
            out = new OutputStreamWriter(connection.getOutputStream());
            out.write(param);
            out.flush();
            int status = connection.getResponseCode();
            boolean redirect = false;
            if (status != HttpURLConnection.HTTP_OK) {
                if (status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == HttpURLConnection.HTTP_SEE_OTHER)
                    redirect = true;
            }
            System.out.println("Response Code ... " + status);
            if (redirect) {
                String newUrl = connection.getHeaderField("Location");
                String cookies = connection.getHeaderField("Set-Cookie");
                System.out.println("Redirect to URL : " + newUrl);
            }
            String cookieval = connection.getHeaderField("set-cookie");
            strParamValue += cookieval.substring(0,cookieval.indexOf(";")+1);
            in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            System.out.println(result);
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
            e.printStackTrace();
        }
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return strParamValue;
    }
    public static String sendPost1(String url, String param, String strToken, String strCookie,String strContentType) {
        OutputStreamWriter out = null;
        BufferedReader in = null;
        String result = "";
        param = checkParam(param);
        url = checkParam(url);
        try {
            System.out.println("************** URL:" + url);
            URL realUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection)realUrl.openConnection();
            connection.setRequestProperty("Host", strIpAddress + ":8000");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            connection.setRequestProperty("Content-Length", String.valueOf(param.length()));
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("X-CSRFToken", strToken);
            connection.setRequestProperty("Content-Type", strContentType);
            connection.setRequestProperty("Cookie", strCookie);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            System.out.println(strCookie);
            System.out.println(param);
            out = new OutputStreamWriter(connection.getOutputStream());
            out.write(param);
            out.flush();
            in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            System.out.println(result);
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
            e.printStackTrace();
        }
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }

        return result;
    }
    public static String uploadFile(String url, byte[] param, String strCookie) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            //url = URLEncoder.encode(url,"utf-8");
            url = checkParam(url);
            System.out.println("************** URL:" +url);
            URL realUrl = new URL(url);
            URLConnection connection = realUrl.openConnection();
            connection.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Length", String.valueOf(param.length));
           //connection.setRequestProperty("Cookie", "sessionid=ys8oujbkzs1t0ax3on31eiffj1p3128t; csrftoken=W7kbDde9L0o5IlCOTUSrMZh5oBQLSHte");
            connection.setRequestProperty("Cookie", strCookie);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundaryWwA1f0fjjPetVzQa");
            connection.setRequestProperty("Referer", "http://"+strIpAddress+":8000/");
            connection.setRequestProperty("Origin", "http://"+strIpAddress+":8000");
            connection.setRequestProperty("Host", strIpAddress + ":8082");
            connection.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
            connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            OutputStream outStream = connection.getOutputStream();
            outStream.write(param);
            outStream.flush();
            outStream.close();
            in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
            e.printStackTrace();
        }
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 对于文件中含有转义字符需要进行转换
     * @param param
     * @return
     */
    static String checkParam(String param){
        param = param.replace("%","%25");
        param = param.replace(" ","%20");
        return param;
    }
}

