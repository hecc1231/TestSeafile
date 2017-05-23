package com.hersch.testseafile.files;

import android.widget.Button;

import com.hersch.testseafile.net.HttpRequest;
import com.hersch.testseafile.ui.MainActivity;
import com.hersch.testseafile.ui.SecondActivity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Hersch on 2017/4/27.
 */
public class FileSM {
    public static final int MIN_FILE_SIZE = 1024*1024*5;//超过10M
    static Process process = null;
    static DataOutputStream dataOutputStream = null;
    /**
     * 初始化命令行进程
     */
    private static void initProcess() {
        if (process == null)
            try {
                process = Runtime.getRuntime().exec("su");
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
    public static List<String> split(String strFilePath){
        initProcess();
        List<String>subFileList=new ArrayList<>();
        try {
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            dataOutputStream.writeBytes("split -b 5m "+strFilePath+" -a 3 "+strFilePath+"-"+"\n");
            dataOutputStream.writeBytes("ls -l "+strFilePath+"-*"+"\n");
            dataOutputStream.writeBytes("ls "+strFilePath+"\n");//为了readLine()读完最后一行会阻塞
            String strInput = null;
            do{
                strInput = bufferedReader.readLine();
                if(strInput.charAt(10)==' '){
                    int i1 = strInput.lastIndexOf(" ");
                    int i2 = strFilePath.lastIndexOf("/");//得到父目录
                    String subFileName = strInput.substring(i1+1);
                    subFileList.add(strFilePath.substring(0,i2+1)+subFileName);
                }
                else{
                    break;
                }
            }while(strInput!=null);
            bufferedReader.close();
            for(int i=0;i<subFileList.size();i++){
                dataOutputStream.writeBytes("chmod 777 "+subFileList.get(i)+"\n");
            }
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if(dataOutputStream!=null){
                    dataOutputStream.close();
                }
                process.destroy();
                System.out.println("文件划分成功");
            }catch (IOException e){
                System.out.println("文件划分失败");
            }
        }
        return subFileList;
    }
    public static void merge(String targetFilePath,List<String> subFileList){
        String cmd = " ";//记住空格 cat file1 file2 子文件格式为filename.gz-a
        for(String s:subFileList){
            cmd+=" "+s;
        }
        initProcess();
        try {
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            dataOutputStream.writeBytes("cat" + cmd + ">" + targetFilePath + "\n");
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            try {
                if(dataOutputStream!=null){
                    dataOutputStream.close();
                }
                process.destroy();
                System.out.println("合并成功");
            }catch (IOException e){
                System.out.println("合并失败");
            }
        }
    }
    /**
     * 从云端获取切割文件的数目
     * @param strFilePath（源文件路径）
     * @return
     */
    public static List<String> getSplitListFromCloud(String strFilePath) {
        //获取云端分割文件的个数
        int max = -1;
        int i1 = strFilePath.lastIndexOf("/");
        List<String>splitList = new ArrayList<>();
        String strParentPath = strFilePath.substring(0, i1 + 1);//上层目录
        String fileName = strFilePath.substring(i1 + 1);
        String strFile = HttpRequest.sendGet("http://" + HttpRequest.strIpAddress
                        + ":8000/ajax/lib/" + MainActivity.strRootId + "/dir/",
                "p=" + strParentPath + "&thumbnail_size=48&&_=14815507370953",
                MainActivity.strCookie);
        String regEx = fileName + "-[a-z]+";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(strFile);
        while (matcher.find()) {
            String s = matcher.group();
            splitList.add(strParentPath+s);
        }
        Collections.sort(splitList);
        return splitList;
    }
    public static boolean isCompleteSplitNum(List<String>list){
        String s = list.get(list.size()-1);
        int i1 = s.lastIndexOf("-");
        String strNo = s.substring(i1+1);
        int num=0;
        for(int i=0;i<strNo.length();i++){
            num=26*num+(strNo.charAt(i)-'a');
        }
        if(num+1==list.size()){
            return true;
        }
        return false;
    }
}
