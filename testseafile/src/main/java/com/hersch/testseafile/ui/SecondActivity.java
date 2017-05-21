package com.hersch.testseafile.ui;

import android.content.Context;
import android.content.DialogInterface;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hersch.testseafile.process.CustomProcess;
import com.hersch.testseafile.R;
import com.hersch.testseafile.files.ConfigList;
import com.hersch.testseafile.files.FileSM;
import com.hersch.testseafile.net.HttpRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.hersch.testseafile.files.FileRooter;
import com.hersch.testseafile.files.FileBackup;
import com.hersch.testseafile.files.common;

public class SecondActivity extends AppCompatActivity {
    static String strToken = MainActivity.strToken;
    static String strCookie = MainActivity.strCookie;
    public final static int MSG_COMPLETE_BACKUP = 0;
    public static int version = 0;//表明当前备份的版本
    public static int FLAG_BACKUP = 0;//记录是否初次备份(与同步清单有关)
    public final static int MSG_COMPLETE_SYNC = 2;
    public final static int MSG_BACKUP_FILE_INFO = 3;
    public final static int MSG_VERSION_NUM = 4;
    public final static int MSG_NOT_SYNC = 5;
    public final static int SNAP_VERSION_NUM=5;//历史快照最多保存五份
    public static List<Integer>chmodIntList = new ArrayList<>();
    public static List<String>chmodFileList = new ArrayList<>();
    public static List<String>deleteZipList = new ArrayList<>();
    public static String processName = null;
    static String strIpAddress = HttpRequest.strIpAddress;//
    static String strFirstFile = "------WebKitFormBoundaryWwA1f0fjjPetVzQa\r\nContent-Disposition: form-data; name=\"parent_dir\"\r\n\r\n";
    static String strTargetFile = "\r\n------WebKitFormBoundaryWwA1f0fjjPetVzQa\r\nContent-Disposition: form-data; name=\"target_file\"\r\n\r\n";
    static String strDirFile = "\r\n------WebKitFormBoundaryWwA1f0fjjPetVzQa\r\nContent-Disposition: form-data; name=\"file\"; filename=\"";
    static String strEndFile = "\r\n------WebKitFormBoundaryWwA1f0fjjPetVzQa--\r\n";
    static String strMiddleFile = "\"\r\nContent-Type: application/octet-stream\r\n\r\n";
    public static String strRootId = MainActivity.strRootId;
    public static byte[] m_binArray = null;
    public static String strCurrentPath = "/data/data/com.hersch.testseafile";
    static int testDirNum = 0;
    static int testFileNum = 0;
    static int stateNum = 0;//快照状态记录
    Button btnRecovery;
    Button btnBackup;
    Button btnTest;
    Button btnSelect;
    TextView tvFileScanner;
    static Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        context = getApplicationContext();
        findView();
    }
    void findView() {
        //initSpinner();
        //tvFileScanner = (TextView) findViewById(R.id.tvFileScanner);
        btnSelect = (Button)findViewById(R.id.btnSelectApp);
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                builder.setIcon(R.drawable.icon);
                builder.setTitle("选择一个App");
                //    指定下拉列表的显示数据
                final String[] apps = ConfigList.getAppList();
                //    设置一个下拉的列表选择项
                builder.setItems(apps, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        processName = apps[which];
                        Toast.makeText(SecondActivity.this, "选择的应用为：" + apps[which], Toast.LENGTH_SHORT).show();
                        btnSelect.setText(processName);
                    }
                });
                builder.show();
            }
        });
        btnBackup = (Button) findViewById(R.id.btnBackup);
        btnBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (processName == null) {
                    Toast.makeText(SecondActivity.this, "请选择应用", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (CustomProcess.isProcessRunning(context, processName) || CustomProcess.isServiceRunning(context, processName)) {
                    //当前微信正在运行
                    createBackUpDialg();//弹出确认框
                } else {
                    backupToSeafile();
                }
            }
        });
        btnRecovery = (Button) findViewById(R.id.btnRecovery);
        btnRecovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (processName == null) {
                    Toast.makeText(SecondActivity.this, "请选择应用", Toast.LENGTH_SHORT).show();
                    return;
                }
                System.out.println(processName);
                Context context = getApplicationContext();
                if (CustomProcess.isProcessRunning(context, processName) || CustomProcess.isServiceRunning(context, processName)) {
                    //当前微信正在运行
                    createRecoveryDialg();//弹出确认框
                } else {
                    getVersionFromCloud();
                }
            }
        });
        btnTest = (Button)findViewById(R.id.btnTest);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SharedPreferences sharedPrefsFirst = getSharedPreferences("state1",Context.MODE_PRIVATE);
                        SharedPreferences.Editor editorFirst = sharedPrefsFirst.edit();
                        for(int i=2;i<=4;i++){
                            System.out.println("state:"+i);
                            SharedPreferences sharedPreferences = getSharedPreferences("state" + i, Context.MODE_PRIVATE);
                            Map<String,?>map = sharedPreferences.getAll();
                            for(String s:map.keySet()){
                                SharedPreferences sharedPrefBackup = getSharedPreferences("state"+i+"backup",Context.MODE_PRIVATE);
                                SharedPreferences.Editor editorBackup = sharedPrefBackup.edit();
                                String tempStr = sharedPrefsFirst.getString(s,null);
                                if(tempStr==null){
                                    System.out.println(s);
                                    editorBackup.putString(s,"");
                                    editorBackup.commit();
                                }
                            }
                        }
                        System.out.println("End");
//                        processName = "org.mozilla.firefox";
//                        stateNum++;
//                        chmodFileList.clear();
//                        chmodIntList.clear();
//                        SharedPreferences sharedPreferences = getSharedPreferences("state" + stateNum, Context.MODE_PRIVATE);
//                        SharedPreferences.Editor editor = sharedPreferences.edit();
//                        editor.commit();
//                        List<String> preList = ConfigList.getInitDirList("local", "system");
//                        FileRooter.chmodPreDirPath(preList);
//                        List<String> backupList = ConfigList.getList("system");
//                        testFileNum = 0;
//                        for (String s : backupList) {
//                            traverseFile(s, stateNum);
//                        }
//                        FileRooter.rollBackChmodFiles(chmodIntList, chmodFileList);
//                        System.out.println("测试完成");
//                        System.out.println("Directorys: " + testDirNum);
//                        System.out.println("Files: " + testFileNum);
                    }
                }).start();
            }
        });
    }
    void traverseFile(String strFilePath,int stateNum){
        //创建目录
        List<String> listFile = new ArrayList<>();
        List<String> listDir = new ArrayList<>();
        File file = new File(strFilePath);
        File[] files = file.listFiles();
        for (int j = 0; j < files.length; j++) {
            if (files[j].isDirectory()) {
                if(strFilePath.equals("/data/data")||strFilePath.equals("/data/user/0")){
                    if(isSystemApp(files[j].getName())||files[j].getName().equals(processName)){
                        listDir.add(files[j].getAbsolutePath());
                    }
                }
                else {
                    listDir.add(files[j].getAbsolutePath());
                }
            } else {
                listFile.add(files[j].getAbsolutePath());
            }
        }
        if (listDir.size() > 0) {
            FileRooter.getAccessFromFiles(listDir);//获取当前目录下文件夹权限并将文件夹chmod为777
            for (int j = 0; j < listDir.size(); j++) {
                File cFile = new File(listDir.get(j));
                if(cFile.canRead()) {
                    if (listDir.get(j).length() > 0) {//文件夹不空才记录
                        System.out.println(listDir.get(j));
                        testDirNum++;
                        traverseFile(listDir.get(j), stateNum);
                    }
                }
            }
        }
        if (listFile.size() > 0) {
            FileRooter.cmdChmod(listFile);
            for (int j = 0; j < listFile.size(); j++) {
                File cFile = new File(listFile.get(j));
                if(cFile.canRead()) {
                    testFileNum++;
                    addSharedPrefs(listFile.get(j), stateNum);
                    System.out.println(listFile.get(j));
                }
            }
        }
    }
    boolean isSystemApp(String pkgName){
        PackageManager packageManager = this.getPackageManager();
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(0);
        for (int i = 0; i < packageInfoList.size(); i++) {
            PackageInfo pak = (PackageInfo)packageInfoList.get(i);
            //判断是否为系统预装的应用
            if(pak.packageName.equals(pkgName)) {
                if ((pak.applicationInfo.flags & pak.applicationInfo.FLAG_SYSTEM) <= 0) {
                    return false;
                } else {
                    return true;//系统应用
                }
            }
        }
        return false;
    }
    void addSharedPrefs(String fileName,int stateNum){
        SharedPreferences currentSharedPrefs = getSharedPreferences("state" + stateNum, Context.MODE_PRIVATE);
        SharedPreferences.Editor currentEditor = currentSharedPrefs.edit();
        if(stateNum==1) {
            String strCurrentMd5 = common.getFileMD5(fileName);
            currentEditor.putString(fileName,strCurrentMd5);
            currentEditor.commit();
            return;
        }
        SharedPreferences initSharedPrefs = getSharedPreferences("state1", Context.MODE_PRIVATE);
        SharedPreferences.Editor initEditor = initSharedPrefs.edit();
        String strExistMd5 = initSharedPrefs.getString(fileName, "null");
        String strCurrentMd5 = common.getFileMD5(fileName);
        if(strExistMd5.equals("null")||!strExistMd5.equals(strCurrentMd5)){
            currentEditor.putString(fileName, strCurrentMd5);
            initEditor.putString(fileName,strCurrentMd5);
            initEditor.commit();
            currentEditor.commit();
        }
    }
    //退出微信提示框
    void createBackUpDialg(){
        AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
        builder.setMessage("检测到应用正在运行,确认退出应用开始备份数据吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                CustomProcess.kill(processName);
                backupToSeafile();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * 弹出同步版本选择窗口
     */
    void createRecoverVersionDialg(){
        AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
        builder.setIcon(R.drawable.icon);
        builder.setTitle("选择一个版本同步");
        //    指定下拉列表的显示数据
        final String[] versions = new String[version+1];
        for(int i=0;i<=version;i++){
            versions[i] = "version_"+i;
        }
        //    设置一个下拉的列表选择项
        builder.setItems(versions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strVersion = versions[which];
                int index = strVersion.lastIndexOf("_");
                version = Integer.parseInt(strVersion.substring(index + 1));
                Toast.makeText(SecondActivity.this, "选择的版本为：" + version, Toast.LENGTH_SHORT).show();
                recoveryToApp();
            }
        });
        builder.show();
    }
    /**
     * 弹出确认关闭应用的窗口
     */
    void createRecoveryDialg() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
        builder.setMessage("检测到应用正在运行,确认退出应用开始同步数据吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                CustomProcess.kill(processName);
                //recoveryToApp();
                getVersionFromCloud();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    void initPreDirOnCloud(){
        List<String>list = ConfigList.getInitDirList("cloud",processName);
        for(String s:list){
            FileBackup.createDirectory(s);
        }
    }
    void initList(){
        chmodFileList.clear();
        chmodIntList.clear();
    }
    void createSharedPrefs(){
        //创建当前备份版本的备份清单
        SharedPreferences sharedPrefsBackupMd5 = context.getSharedPreferences(SecondActivity.processName + "_version_0",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editorBackup = sharedPrefsBackupMd5.edit();
        editorBackup.commit();
        if(version!=0){
            SharedPreferences sharedPrefsChangeMd5 = context.getSharedPreferences(SecondActivity.processName + "_version_"+version,
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editorChangeMd5 = sharedPrefsChangeMd5.edit();
            editorChangeMd5.commit();
        }
    }
    void backupToSeafile(){
        btnBackup.setText("数据备份中.....");
        btnBackup.setEnabled(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                initList();
                int versionNo = FileRooter.getVersionFromCloud("/"+processName);//判断本次备份时第几版本
                if(versionNo>5){
                    System.out.println("备份达到上限,请清除备份");
                }
                version = versionNo+1;//当前版本
                System.out.println("当前版本"+version);
                createSharedPrefs();
                initPreDirOnCloud();//在云端创建父级目录如/data/data/com.tencent.mm之前的/data文件和/data/data
                List<String>listTraverseFile = ConfigList.getList(processName);//获取需要遍历的路径
                List<String>initDirList = ConfigList.getInitDirList("local",processName);
                FileRooter.chmodPreDirPath(initDirList);//对父级目录进行chmod以便可以正常访问子文件
                for (String s : listTraverseFile) {
                    FileBackup.traverseFileCy(SecondActivity.this, s, myHandler);
                }
                System.out.println("----->还原文件权限中");
                FileRooter.rollBackChmodFiles(chmodIntList, chmodFileList);
                System.out.println("---->备份xml到云端....");
                syncSharedPrefsToCloud(SecondActivity.processName + "_version_" + version+".xml");
                System.out.println("----->备份xml完成");
                sendMsg(MSG_COMPLETE_BACKUP);
                }
        }).start();
    }

    /**
     * 从云端获取版本数
     */
    void getVersionFromCloud(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                version = FileRooter.getVersionFromCloud("/"+processName);
                sendMsg(MSG_VERSION_NUM);
            }
        }).start();
    }
    void recoveryToApp(){
        btnRecovery.setText("同步数据中....");
        btnRecovery.setEnabled(false);
        final Context context = getApplicationContext();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String fileNamePreMix = processName+"_version_";
                File changeMd5File = new File(strCurrentPath+"/shared_prefs/"+fileNamePreMix+version+".xml");
                File backupMd5File = new File(strCurrentPath+"/shared_prefs/"+fileNamePreMix+0+".xml");
                if(isFileExistOnCloud("/"+processName)) {
                    String cloudBackupPreMix = "/"+processName+"/version_0";//"/processname/version_0";
                    String cloudChangePreMix = "/"+processName+"/version_"+version;
                    //在云端进行过备份
                    if (!changeMd5File.exists()||!backupMd5File.exists()) {
                        //本地不存在changeMd5说明未进行过备份
//                        backupMd5Editor.commit();
//                        changeMd5Editor.commit();
                        //本地不存在说明还未进行备份,从服务端下载上次备份到云端的文件覆盖本地
                        downloadFile(changeMd5File.getAbsolutePath(), cloudChangePreMix+"/"+processName+"_version_"+version+".xml");
                        downloadFile(backupMd5File.getAbsolutePath(), cloudBackupPreMix+"/"+processName+"_version_"+0+".xml");
                    }
                    SharedPreferences backupMd5Prefs = context.getSharedPreferences(fileNamePreMix+"0",Context.MODE_PRIVATE);
                    recoveryFromList(backupMd5Prefs.getAll(),cloudBackupPreMix);
                    if(version!=0) {
                        SharedPreferences changeMd5Prefs = context.getSharedPreferences(fileNamePreMix+version,Context.MODE_PRIVATE);
                        recoveryFromList(changeMd5Prefs.getAll(), cloudChangePreMix);
                    }
                    sendMsg(MSG_COMPLETE_SYNC);
                }
                else{
                    //服务器上未进行过备份
                    sendMsg(MSG_NOT_SYNC);
                }
            }
        }).start();
    }
    void recoveryFromList(Map<String,?>map,String strCloudPreMix){
        List<String> srcZipFilePath = new ArrayList<String>();
        List<String> desZipFilePath = new ArrayList<String>();
        for (String key : map.keySet()) {
            String zipName = key + ".gz";
            String strCloudPath = strCloudPreMix+key+".gz";
            String unZipName = key;
            File parentFile = new File(strCurrentPath + unZipName).getParentFile();
            parentFile.mkdirs();
            if (isFileExistOnCloud(strCloudPath)) {
                downloadFile(strCurrentPath + zipName, strCloudPath);//将云端的文件存入当前app中
                srcZipFilePath.add(strCurrentPath + zipName);//记录暂时的压缩包路径和在微信目录中的路径
                desZipFilePath.add(unZipName);
                System.out.println(zipName + " is downloaded");
            }
            else {
                List<String> splitList = FileSM.getSplitListFromCloud(strCloudPath);//列表存放在云端上的路径
                if(splitList.size()==0){
                    //说明云端不存在该文件且不是大文件，文件可能丢失
                    continue;
                }
                if (FileSM.isCompleteSplitNum(splitList)) {//判断分割后的文件是否完整
                    for (int i = 0; i < splitList.size(); i++) {
                        String splitZipName = splitList.get(i);//云端路径/com.tencent.mm
                        String localPath = splitZipName.substring(strCloudPreMix.length());//获取云端路径中的本地路径
                        downloadFile(strCurrentPath + localPath, splitZipName);
                        System.out.println(splitZipName + " is downloaded");
                    }
                    FileSM.merge(strCurrentPath + zipName, splitList);
                } else {
                    Toast.makeText(SecondActivity.this, "合并时文件序号丢失", Toast.LENGTH_SHORT).show();
                }
            }
        }
        System.out.println("----解压到用户应用目录----");
        FileRooter.cmdUnzips(srcZipFilePath, desZipFilePath);
    }
    /**
     * 每次备份文件后将期间以及以前发生变化的文件都存入sharedPrefs并将xml上传至云平台
     */
    void syncSharedPrefsToCloud(String fileName){
        //将期间发生变化的ChangeMd5文件传到云平台供以后的同步所用,在云端根目录下
        String cloudPremix = "/"+SecondActivity.processName+"/version_"+SecondActivity.version;
        String strSharedPrefsPath = strCurrentPath + "/shared_prefs/"+fileName;//在本地的目录
        String strCloudPath = cloudPremix+"/"+fileName;
        if(isFileExistOnCloud(strCloudPath)){
            uploadFile("update", strSharedPrefsPath, fileName, cloudPremix+"/");
        }
        else{
            uploadFile("upload", strSharedPrefsPath, fileName, cloudPremix+"/");
        }
    }
    /**
     * 子线程更新UI消息
     * @param msgType
     */
    private void sendMsg(int msgType){
        Message msg = Message.obtain();
        msg.what = msgType;
        myHandler.sendMessage(msg);
    }
    /**
     * 上传文件(需要保证是文件而不是文件夹,否则出错)
     * @param strFileDir(本地绝对路径)
     * @param strFileName
     * @param uploadPath(云平台父亲目录路径)
     */
    public static void uploadFile(String cmd,String strFileDir, String strFileName,
                                  String uploadPath) {
        try {
            String strFile = HttpRequest.sendGet("http://" + strIpAddress
                            + ":8000/ajax/repo/" + strRootId + "/file_op_url/",
                    "op_type=" + cmd + "&path=" + uploadPath + "&_=14815507370953",
                    strCookie);
            System.out.println("****" + strFile);
            String strFilePath = strFile.substring(9, strFile.length() - 2);
            System.out.println(strFilePath);
            String strUpload="";
            if(cmd.equals("update")){
                strUpload = HttpRequest.uploadFile(strFilePath,
                        mergeBodyUpdate(strFileDir, strFileName, uploadPath),
                        strCookie);
            }
            else{
                strUpload = HttpRequest.uploadFile(strFilePath,
                        mergeBodyUpload(strFileDir, strFileName,uploadPath),
                        strCookie);
            }
            System.out.println(strUpload);
        } catch (Exception e) {
            System.out.println("======== uploadFile Error ======= "
                    + strFileDir);
            e.printStackTrace();
        }
    }
    /**
     * 更新文件(比上传文件多了一个参数:在云平台上的目标更新文件Target)
     * @param strFileDir
     * @param strFileName
     * @param strRelativeDirpath
     * @return
     */
    public static byte[] mergeBodyUpdate(String strFileDir,String strFileName,String strRelativeDirpath){
        byte[] temp1 = common.readFile(strFileDir);
        strFileDir = strRelativeDirpath + "/" + strFileName;//strFileDir是在手机上的路径/data/data/packgname/sharedPrefs
        //云平台没有sharedPrefs的文件夹
        m_binArray = new byte[strFirstFile.length()
                + strRelativeDirpath.length()+ strTargetFile.length()
                + strFileDir.length() + strDirFile.length()
                + strFileName.length() + strMiddleFile.length()
                + strEndFile.length() + temp1.length];
        System.arraycopy(strFirstFile.getBytes(), 0, m_binArray, 0,
                strFirstFile.length());
        System.arraycopy(strRelativeDirpath.getBytes(), 0, m_binArray,
                strFirstFile.length(), strRelativeDirpath.length());
        System.arraycopy(strTargetFile.getBytes(), 0, m_binArray,
                strFirstFile.length() + strRelativeDirpath.length(),
                strTargetFile.length());
        System.arraycopy(strFileDir.getBytes(), 0,m_binArray,
                strFirstFile.length() + strRelativeDirpath.length() + strTargetFile.length()
                ,strFileDir.length());
        System.arraycopy(strDirFile.getBytes(), 0, m_binArray,
                strFirstFile.length() + strRelativeDirpath.length() + strTargetFile.length()
                        + strFileDir.length(),
                strDirFile.length());
        System.arraycopy(strFileName.getBytes(), 0, m_binArray,
                strFirstFile.length() + strRelativeDirpath.length() + strTargetFile.length()
                        + strFileDir.length() + strDirFile.length(),
                strFileName.length());
        System.arraycopy(strMiddleFile.getBytes(), 0, m_binArray,
                strFirstFile.length() + strRelativeDirpath.length() + strTargetFile.length()
                        + strFileDir.length() + strDirFile.length() + strFileName.length(),
                strMiddleFile.length());
        System.arraycopy(temp1, 0, m_binArray,
                strFirstFile.length() + strRelativeDirpath.length() + strTargetFile.length()
                        + strFileDir.length() + strDirFile.length() + strFileName.length()
                        + strMiddleFile.length(),
                temp1.length);
        System.arraycopy(strEndFile.getBytes(), 0, m_binArray,
                strFirstFile.length() + strRelativeDirpath.length() + strTargetFile.length()
                        + strFileDir.length() + strDirFile.length() + strFileName.length()
                        + strMiddleFile.length() + temp1.length,
                strEndFile.length());
        return m_binArray;
    }
    /**
     * 上传文件
     * @param strFileDir
     * @param strFileName
     * @param strRelativeDirpath
     * @return
     */
    public static byte[] mergeBodyUpload(String strFileDir, String strFileName,
                                         String strRelativeDirpath) {
        byte[] temp1 = common.readFile(strFileDir);
        m_binArray = new byte[strFirstFile.length()
                + strRelativeDirpath.length() + strDirFile.length()
                + strFileName.length() + strMiddleFile.length()
                + strEndFile.length() + temp1.length];
        System.arraycopy(strFirstFile.getBytes(), 0, m_binArray, 0,
                strFirstFile.length());
        System.arraycopy(strRelativeDirpath.getBytes(), 0, m_binArray,
                strFirstFile.length(), strRelativeDirpath.length());
        System.arraycopy(strDirFile.getBytes(), 0, m_binArray,
                strFirstFile.length() + strRelativeDirpath.length(),
                strDirFile.length());
        System.arraycopy(strFileName.getBytes(), 0, m_binArray,
                strFirstFile.length() + strRelativeDirpath.length()
                        + strDirFile.length(), strFileName.length());
        System.arraycopy(strMiddleFile.getBytes(), 0, m_binArray,
                strFirstFile.length() + strRelativeDirpath.length()
                        + strDirFile.length() + strFileName.length(),
                strMiddleFile.length());
        System.arraycopy(temp1, 0, m_binArray, strFirstFile.length()
                + strRelativeDirpath.length() + strDirFile.length()
                + strFileName.length() + strMiddleFile.length(), temp1.length);
        System.arraycopy(strEndFile.getBytes(), 0, m_binArray,
                strFirstFile.length() + strRelativeDirpath.length()
                        + strDirFile.length() + strFileName.length()
                        + strMiddleFile.length() + temp1.length,
                strEndFile.length());
        return m_binArray;
    }
    /**
     * 下载文件存放在源文件路径下命名为.backup文件
     * @param strFileDir
     * @param strFileCloudPath
     */
    public static void downloadFile(String strFileDir, String strFileCloudPath) {
        byte[] fileArray = HttpRequest.downloadFile("http://" + strIpAddress
                        + ":8000/lib/" + strRootId + "/file" + strFileCloudPath, "dl=1",
                strCookie);///file后面应该跟云平台对应的路径名,当前所下载的文件是在根目录下
        common.writeFile(strFileDir, fileArray);
    }
    /**
     * 创建文件的post包格式必须包含XCRSToken！！
     * %2F后跟文件夹名字代表在该文件夹下/dir
     * 创建文件夹
     */
    public static void createDirToCloud(final String strFilePath){
        int i1 = strFilePath.lastIndexOf("/");
        String strParentPath = strFilePath.substring(0,i1+1);
        String strFileName = strFilePath.substring(i1 + 1);
        String strContent = HttpRequest.sendPost1("http://" + strIpAddress + ":8000/ajax/repo/" + strRootId +
                        "/dir/new/?parent_dir=" + strParentPath, "dirent_name=" + strFileName, strToken, strCookie,
                "application/x-www-form-urlencoded; charset=UTF-8");
        System.out.println(strContent);
    }
    /**
     * 判断当前云端是否存在该文件(为upload和update服务)
     * @param strFilePath(在云端的绝对路径)
     * @return
     */
    public static boolean isFileExistOnCloud(String strFilePath){
        int i1 = strFilePath.lastIndexOf("/");
        String strParentPath = strFilePath.substring(0,i1+1);//上层目录
        String fileName = strFilePath.substring(i1+1);
        String strFile = HttpRequest.sendGet("http://" + strIpAddress
                        + ":8000/ajax/lib/" + strRootId + "/dir/",
                "p=" + strParentPath + "&thumbnail_size=48&&_=14815507370953",
                strCookie);
        if(strFile.contains("\""+fileName+"\"")){
            //存在一个问题就是前缀的问题 csd先创建,到后来cs就默认为存在了
            return true;
        }
        return false;
    }
    public Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_COMPLETE_BACKUP:
                    btnBackup.setEnabled(true);
                    btnBackup.setText("备份");
                    Toast.makeText(SecondActivity.this,"数据成功备份到云端",Toast.LENGTH_SHORT).show();
                    break;
                case MSG_COMPLETE_SYNC:
                    btnRecovery.setEnabled(true);
                    btnRecovery.setText("同步");
                    Toast.makeText(SecondActivity.this,"应用数据同步完成",Toast.LENGTH_SHORT).show();
                    break;
                case MSG_BACKUP_FILE_INFO:
                    tvFileScanner.setText(msg.getData().getString("filePath"));
                    break;
                case MSG_NOT_SYNC:
                    Toast.makeText(SecondActivity.this,"云端不存在备份文件,请先备份",Toast.LENGTH_SHORT).show();
                    btnRecovery.setEnabled(true);
                    btnRecovery.setText("同步");
                    break;
                case MSG_VERSION_NUM:
                    if(version>=0){
                        System.out.println("云端最近版本:" + version);
                        createRecoverVersionDialg();
                    }
                    else{
                        Toast.makeText(SecondActivity.this,"云端不存在备份版本，请先备份",Toast.LENGTH_SHORT).show();
                    }
            }
        }
    };
    boolean clickFlag = false;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK&&event.getAction()==KeyEvent.ACTION_DOWN) {
            if (clickFlag == false) {
                clickFlag = true;//第一次点击
                Toast.makeText(SecondActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                final Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        //计时器两秒后自动把clickFlag改为false
                        clickFlag = false;
                    }
                }, 2000);
                return true;
            } else {
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
