package com.hersch.testseafile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SecondActivity extends AppCompatActivity {
    public static String strToken = MainActivity.strToken;
    public static String strCookie = MainActivity.strCookie;
    final static int MSG_COMPLETE_BACKUP = 1;
    final static int MSG_COMPLETE_SYNC = 2;
    final static int MSG_BACKUP_FILE_INFO = 3;
    static String processName = "com.tencent.mm";
    static List<String> listTraverseFile;
    static String strIpAddress = HttpRequest.strIpAddress;//"10.108.20.142";//
    static String strFirstFile = "------WebKitFormBoundaryWwA1f0fjjPetVzQa\r\nContent-Disposition: form-data; name=\"parent_dir\"\r\n\r\n";
    static String strTargetFile = "\r\n------WebKitFormBoundaryWwA1f0fjjPetVzQa\r\nContent-Disposition: form-data; name=\"target_file\"\r\n\r\n";
    static String strDirFile = "\r\n------WebKitFormBoundaryWwA1f0fjjPetVzQa\r\nContent-Disposition: form-data; name=\"file\"; filename=\"";
    static String strEndFile = "\r\n------WebKitFormBoundaryWwA1f0fjjPetVzQa--\r\n";
    static String strMiddleFile = "\"\r\nContent-Type: application/octet-stream\r\n\r\n";
    public static String strRootId = MainActivity.strRootId;
    public static byte[] m_binArray = null;
    static String strCurrentPath = "/data/data/com.hersch.testseafile/";
    static String strFileDir = "/data/data/com.hersch.testseafile/data/data/com.tencent.mm/";
    Button btnSync;
    Button btnSnapshot;
    TextView tvFileScanner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        findView();
        if(!CustomProcess.isAppExist(getApplicationContext(),processName)){
            //需要从云平台同步到本地

        }
        else{
            //不做操作
        }
    }
    void initPreDirectoryOnCloud(){
        Context context = SecondActivity.this;
        FileSnapshot.createDirectory("/data");
        FileSnapshot.createDirectory("/data/data");
        FileSnapshot.createDirectory("/data/data/com.tencent.mm");
        FileSnapshot.createDirectory("/storage");
        FileSnapshot.createDirectory("/storage/emulated");
        FileSnapshot.createDirectory("/storage/emulated/0");
        FileSnapshot.createDirectory("/storage/emulated/0/Tencent");
    }

    /**
     * APP初始化时同步prefs到云端
     * @param fileName(包含后缀)
     */
    void createPrefsToCloud(String fileName){
        SharedPreferences sharedPreferences = getSharedPreferences(fileName,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.commit();
        String strPrefsPath = strCurrentPath+"shared_prefs/"+fileName+".xml";
        sharedPreferences = getSharedPreferences("recordList",Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.commit();
        String isExistFlag = sharedPreferences.getString(fileName,"null");
        if(isExistFlag.equals("null")) {
            uploadFile("upload", strPrefsPath, fileName+".xml", "/");
            editor.putString(fileName, fileName);
            editor.commit();
        }
    }
    void findView() {
        listTraverseFile = new ArrayList<String>();
        listTraverseFile.add("/data/data/com.tencent.mm/shared_prefs");
        listTraverseFile.add("/data/data/com.tencent.mm/MicroMsg");
        //listTraverseFile.add("/storage/emulated/0/Tencent/MicroMsg");
        tvFileScanner=(TextView)findViewById(R.id.tvFileScanner);
        btnSnapshot = (Button) findViewById(R.id.btnSnapshot);
        btnSnapshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileSnapshot.differSize = FileSnapshot.fileSize = 0;
                btnSnapshot.setText("数据备份中.....");
                btnSnapshot.setEnabled(false);
                if (CustomProcess.isAppExist(getApplicationContext(), processName)) {
                    //手机上存在微信应用才进行备份
                    if (listTraverseFile.size() > 0) {
                        //选择备份的文件夹
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                initPreDirectoryOnCloud();
                                for (String s : listTraverseFile) {
                                    File file = new File(s);
                                    FileSnapshot.getFileList(SecondActivity.this, file,myHandler);
                                }
                                syncSharedPrefsToCloud("backupMd5.xml");//将备份后的md文件备份到云端
                                syncSharedPrefsToCloud("changeMd5.xml");
                                sendMsg(MSG_COMPLETE_BACKUP);
                            }
                        }).start();
                    } else {
                        Toast.makeText(SecondActivity.this, "请选择备份文件夹", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        btnSync = (Button) findViewById(R.id.btnSync);
        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getApplicationContext();
                if (!CustomProcess.isProcessRunning(context) || !CustomProcess.isServiceRunning(context)) {
                    createDialog();//弹出确认框
                } else {
                    //syncFileToLocal();//同步到本地文件夹
                    syncFileToMsg();
                }
            }
        });
    }
    /**
     * 弹出确认关闭微信的窗口
     */
    void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
        builder.setMessage("检测到微信正在运行,确认退出微信开始同步数据吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                CustomProcess.kill(processName);
                btnSync.setText("同步数据中....");
                btnSync.setEnabled(false);
                syncFileToMsg();
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
     * 同步到微信
     */
    void syncFileToMsg(){
        final Context context = getApplicationContext();
        new Thread(new Runnable() {
            @Override
            public void run() {
                File changeMd5File = new File(strCurrentPath +"shared_prefs/+changeMd5.xml");
                File backupMd5File = new File(strCurrentPath+"shared_prefs/backupMd5.xml");
                downloadFile(changeMd5File.getAbsolutePath(), "/changeMd5.xml");//覆盖本地,因为可能存在本地为空
                SharedPreferences backupMd5Prefs = context.getSharedPreferences("backupMd5", Context.MODE_PRIVATE);
                if(backupMd5Prefs.getAll().size()==0){
                    //说明本地是空手机,需要把云平台所有文件同步到本地
                    downloadFile(backupMd5File.getAbsolutePath(),"/backupMd5.xml");
                    Map<String,?>map = backupMd5Prefs.getAll();
                    for(String key:map.keySet()){
                        //FileRooter.chmod(key);
                        downloadFile(key, key);
                        System.out.println(key);
                    }
                }
                else {
                    SharedPreferences changeMd5Prefs = context.getSharedPreferences("changeMd5", Context.MODE_PRIVATE);
                    Map<String, ?> strFileMap = changeMd5Prefs.getAll();
                    for (String key : strFileMap.keySet()) {
                        //FileRooter.chmod(key);
                        downloadFile(key, key);
                        System.out.println(key);
                    }
                }
                sendMsg(MSG_COMPLETE_SYNC);
            }
        }).start();
    }
    /**
     * 同步到当前应用的文件夹下
     */
    void syncFileToLocal(){
        final Context context = getApplicationContext();
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File("/data/data/"+getPackageName()+"/shared_prefs/changeMd5.xml");
                if(!file.exists()) {
                    //说明本地未创建changeMd5，此时说明是从云端同步到本地数据,需要同步全部文件
                    System.out.println("changeMd5 is not exist");
                    String strfileDir = "/data/data/" + getPackageName() + "/shared_prefs/changeMd5.xml";
                    downloadFile(strfileDir, "/changeMd5.xml");
                }
                SharedPreferences sharedPrefs = context.getSharedPreferences("changeMd5", Context.MODE_PRIVATE);
                Map<String,?> strFileMap = sharedPrefs.getAll();
                for(String key:strFileMap.keySet()){
                    //创建父级目录以上的文件夹,因为在当前APP的目录下没有和微信对应的MicroMsg等目录
                    String strLocalPath = "/data/data/"+getPackageName()+key;
                    int i = strLocalPath.lastIndexOf("/");
                    String strParentPath = strLocalPath.substring(0,i);
                    new File(strParentPath).mkdirs();
                    downloadFile(strLocalPath,key);
                }
                sendMsg(MSG_COMPLETE_SYNC);
            }
        }).start();
    }
    void getFileDetailOnCloud(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String content = HttpRequest.sendGet("http://" + strIpAddress + ":8000/ajax/lib/" + strRootId + "/dir/?p=%2F&thumbnail_size=48&_=1490927383195", "", strCookie);
                System.out.println(content);
                if(content.contains("backupMd5")){

                }
            }
        }).start();
    }
    /**
     * 每次备份文件后将期间以及以前发生变化的文件都存入sharedPrefs并上传至云平台
     */
    void syncSharedPrefsToCloud(String fileName){
        //将期间发生变化的ChangeMd5文件传到云平台供以后的同步所用,在云端根目录下
        String strSharedPrefsPath = strCurrentPath + "shared_prefs/"+fileName;//在本地的目录
        if(isFileExistOnCloud("/"+fileName)){
            uploadFile("update", strSharedPrefsPath, fileName, "/");
        }
        else{
            uploadFile("upload", strSharedPrefsPath, fileName, "/");
        }
    }
    /**
     * 子线程更新UI消息
     * @param msgType
     */
    void sendMsg(int msgType){
        Message msg = Message.obtain();
        msg.what = msgType;
        myHandler.sendMessage(msg);
    }
    /**
     * 上传文件(保证是文件而不是文件夹,否则出错)
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
     * 更新文件(比上传文件多了一个参数:在云平台上的目标更新文件)
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
        String strFileName = strFilePath.substring(i1+1);
        String strContent = HttpRequest.sendPost1("http://" + strIpAddress + ":8000/ajax/repo/" + strRootId +
                        "/dir/new/?parent_dir=" + strParentPath, "dirent_name=" + strFileName, strToken, strCookie,
                "application/x-www-form-urlencoded; charset=UTF-8");
        System.out.println(strContent);
    }
    public static void deleteFileOnCloud(final String strFilePath){
        String strParam = "dirents_names=storage&dirents_names=data&dirents_names=dirList.xml&dirents_names=changeMd5.xml&dirents_names=backupMd5.xml";
        String strContent = HttpRequest.sendPost1("http://" + strIpAddress + ":8000/ajax/repo/" + strRootId +
                        "/dir/new/?parent_dir=" + strFilePath,strParam, strToken, strCookie,
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
                    btnSnapshot.setEnabled(true);
                    btnSnapshot.setText("备份");
                    Toast.makeText(SecondActivity.this,"数据成功备份到云端",Toast.LENGTH_SHORT).show();
                    break;
                case MSG_COMPLETE_SYNC:
                    btnSync.setEnabled(true);
                    btnSync.setText("同步");
                    Toast.makeText(SecondActivity.this,"微信数据同步完成",Toast.LENGTH_SHORT).show();
                    break;
                case MSG_BACKUP_FILE_INFO:
                    tvFileScanner.setText(msg.getData().getString("filePath"));
            }
        }
    };
}
