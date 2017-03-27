package com.hersch.testseafile;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.*;
import android.os.Process;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    static String strToken = "";
    static String strCookie = "";
    final static int MSG_COMPLETE_BACKUP = 1;
    static String processName = "com.tencent.mm";
    static List<String>listTraverseFile;
    static String strIpAddress = "10.50.138.135";//"10.108.20.142";//
    static String strFirstFile = "------WebKitFormBoundaryWwA1f0fjjPetVzQa\r\nContent-Disposition: form-data; name=\"parent_dir\"\r\n\r\n";
    static String strTargetFile = "\r\n------WebKitFormBoundaryWwA1f0fjjPetVzQa\r\nContent-Disposition: form-data; name=\"target_file\"\r\n\r\n";
    static String strDirFile = "\r\n------WebKitFormBoundaryWwA1f0fjjPetVzQa\r\nContent-Disposition: form-data; name=\"file\"; filename=\"";
    static String strEndFile = "\r\n------WebKitFormBoundaryWwA1f0fjjPetVzQa--\r\n";
    static String strMiddleFile = "\"\r\nContent-Type: application/octet-stream\r\n\r\n";
    static String strRootId = "";
    public static byte[] m_binArray = null;
    static String strFileDir = "/data/data/com.hersch.testseafile/data/data/com.tencent.mm/";
    static String strUserName = "hcc1231@126.com";
    static String strPassword = "beijing520";
    EditText editTextIpAddress;
    EditText editTextUserName;
    EditText editTextPassword;
    Button btnSync;
    Button btnLogin;
    Button btnSnapshot;
    TextView tvFileScanner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
    }
    void initDataDirectory(){
        File dataFile = new File("/data");
        FileSnapshot.createDirToCloud(MainActivity.this, dataFile);
        File data2File = new File("/data/data");
        FileSnapshot.createDirToCloud(MainActivity.this, data2File);
        File data3File = new File("/data/data/com.tencent.mm");
        FileSnapshot.createDirToCloud(MainActivity.this, data3File);
    }

    /**
     * 弹出确认关闭微信的窗口
     */
    void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("检测到微信正在运行,确认退出微信吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ActivityManager mAm = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
                CustomProcess.kill(processName);
                Toast.makeText(MainActivity.this, "WeChat has been killed", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {@Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    void findView() {
        listTraverseFile = new ArrayList<String>();
        listTraverseFile.add("/data/data/com.tencent.mm/shared_prefs");
        listTraverseFile.add("/data/data/com.tencent.mm/MicroMsg");
        tvFileScanner = (TextView)findViewById(R.id.tvFileScanner);
        editTextIpAddress = (EditText) findViewById(R.id.editTextIpAdress);
        editTextUserName = (EditText) findViewById(R.id.editTextUserName);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                init();//获取框内信息
                getLoginInfo();
                Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
            }
        });
        btnSnapshot = (Button) findViewById(R.id.btnSnapshot);
        btnSnapshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileSnapshot.differSize = FileSnapshot.fileSize = 0;
                btnSnapshot.setText("back up.....");
                btnSnapshot.setEnabled(false);
                if (listTraverseFile.size() > 0) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            initDataDirectory();
                            for (String s : listTraverseFile) {
                                File file = new File(s);
                                FileSnapshot.getFileList(MainActivity.this, file);
                            }
                            syncSharedPrefsToCloud();
                            sendMsg(MSG_COMPLETE_BACKUP);
                        }
                    }).start();
                } else {
                    Toast.makeText(MainActivity.this, "请选择备份文件夹", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnSync = (Button) findViewById(R.id.btnSync);
        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getApplicationContext();
                if (!CustomProcess.isProcessRunning(context)||!CustomProcess.isServiceRunning(context)) {
                    createDialog();//弹出确认框
                }
                else{
                    System.out.println("ready to sync");
                    //同步
                }
            }
        });
    }
    /**
     * 每次备份文件后将期间以及以前发生变化的文件都存入sharedPrefs并上传至云平台
     */
    void syncSharedPrefsToCloud(){
        String strSharedPrefsPath = "/data/data/" + getApplicationContext().getPackageName() + "/shared_prefs";
        SharedPreferences sharedPreferences = getSharedPreferences("record",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String strBackupMd5 = sharedPreferences.getString("backupMd5", "null");
        String strDir = sharedPreferences.getString("dirList","null");
        String strChangeMd5 = sharedPreferences.getString("changeMd5","null");
        //将期间发生变化的ChangeMd5文件传到云平台供以后的同步所用
        if(strBackupMd5.equals("null")){
            editor.putString("backupMd5","backupMd5");
            editor.commit();
            uploadFile("upload",strSharedPrefsPath+"/backupMd5.xml","backupMd5.xml","/");
        }
        else{
            uploadFile("update",strSharedPrefsPath+"/backupMd5.xml","backupMd5.xml","/");
        }
        //将已创建的文件夹记录传到云平台以备不时之需
        if(strDir.equals("null")){
            editor.putString("dirList","dirList");
            editor.commit();
            uploadFile("upload",strSharedPrefsPath+"/dirList.xml","dirList.xml","/");
        }
        else{
            uploadFile("update",strSharedPrefsPath+"/dirList.xml","dirList.xml","/");
        }
        if(strChangeMd5.equals("null")){
            editor.putString("changeMd5", "changeMd5");
            editor.commit();
            uploadFile("upload",strSharedPrefsPath+"/changeMd5.xml","changeMd5.xml","/");
        }
        else{
            uploadFile("update",strSharedPrefsPath+"/changeMd5.xml","changeMd5.xml","/");
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
    void init(){
        strIpAddress = editTextIpAddress.getText().toString();
        strUserName = editTextUserName.getText().toString();
        strPassword = editTextPassword.getText().toString();
    }
    /**
     * 通过用户名和密码登录并且获得cookie和rootId
     */
    void getLoginInfo(){
         new Thread(new Runnable() {
             @Override
             public void run() {
                 strCookie = HttpRequest.sendGetHeadItem("http://" + strIpAddress
                         + ":8000/accounts/login/", "Set-Cookie");
                 int i3 = strCookie.indexOf("csrftoken") + 10;
                 int i4 = strCookie.indexOf(";", i3);
                 strToken = strCookie.substring(i3, i4);
                 System.out.println("****" + strToken);
                 System.out.println();
                 String strSession = HttpRequest.sendPost("http://" + strIpAddress
                                 + ":8000/accounts/login/?next=/", "csrfmiddlewaretoken="
                                 + strToken
                                 + "&login=" + strUserName + "&password=" + strPassword + "&next=/",
                         "django_language=en; " + strCookie);
                 System.out.println("****" + strSession);
                 strCookie = strSession + " csrftoken=" + strToken;//更新新的sessionid和token组合成认证Cookie
                 System.out.println("New Cookie....." + strCookie);
                 String strRoot = HttpRequest.sendGet("http://" + strIpAddress
                                 + ":8000/api2/repos/", "type=mine&_=1481540118100",
                         strCookie);
                 System.out.println("****" + strRoot);
                 int i1 = strRoot.indexOf("id") + 6;
                 int i2 = strRoot.indexOf(",", i1);
                 strRootId = strRoot.substring(i1, i2 - 1);//保存id
                 System.out.println(strRootId);
             }
         }).start();
    }
    /**
     * 上传文件(保证是文件而不是文件夹,否则出错)
     * @param strFileDir(绝对路径)
     * @param strFileName
     * @param uploadPath(父亲目录路径)
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
                        + strFileDir.length()+ strDirFile.length() + strFileName.length()
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
     * @param strFileName
     */
    public static void downloadFile(String strFileDir, String strFileName) {
        byte[] fileArray = HttpRequest.downloadFile("http://" + strIpAddress
                        + ":8000/lib/" + strRootId + "/file/" + strFileName, "dl=1",
                strCookie);///file后面应该跟云平台对应的路径名,当前所下载的文件是在根目录下
        common.writeFile(strFileDir + ".backup", fileArray);
    }
    /**
     * 创建文件的post包格式必须包含XCRSToken！！
     * %2F后跟文件夹名字代表在该文件夹下/dir
     */
    public static void createDiretory(final String strFilePath){
                int i1 = strFilePath.lastIndexOf("/");
                String strParentPath = strFilePath.substring(0,i1+1);
                String strFileName = strFilePath.substring(i1+1);
                String strContent = HttpRequest.sendPost1("http://" + strIpAddress + ":8000/ajax/repo/" + strRootId +
                                "/dir/new/?parent_dir=" + strParentPath, "dirent_name=" + strFileName, strToken, strCookie,
                        "application/x-www-form-urlencoded; charset=UTF-8");
                System.out.println(strContent);
    }
    Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_COMPLETE_BACKUP:
                    btnSnapshot.setEnabled(true);
                    btnSnapshot.setText("Back Up");
                    break;
            }
        }
    };
}
