package com.hersch.testseafile;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
public class MainActivity extends AppCompatActivity {
    static final int FILE_UPDATE_MSG = 1;
    static final int BUTTON_UPDATE_END = 2;
    static final int BACKUP_CMD = 1;
    static final int COMPARE_CMD = 2;
    static String strToken = "";
    static String strCookie = "";
    static String processName = "com.tencent.mm";
    static String strIpAddress = "192.168.1.22";//"10.108.20.142";//
    static String strFirstFile = "------WebKitFormBoundaryWwA1f0fjjPetVzQa\r\nContent-Disposition: form-data; name=\"parent_dir\"\r\n\r\n";
    static String strDirFile = "\r\n------WebKitFormBoundaryWwA1f0fjjPetVzQa\r\nContent-Disposition: form-data; name=\"file\"; filename=\"";
    static String strEndFile = "\r\n------WebKitFormBoundaryWwA1f0fjjPetVzQa--\r\n";
    static String strMiddleFile = "\"\r\nContent-Type: application/octet-stream\r\n\r\n";
    static String strRootId = "";
    public static byte[] m_binArray = null;
    static String strFileDir = "/data/data/com.test.testseafile2/EnMicroMsg.db.sm";
    static String strUserName = "hcc_public@126.com";
    static String strPassword = "beijing520";
    EditText editTextIpAddress;
    EditText editTextFileDir;
    EditText editTextUserName;
    EditText editTextPassword;
    Button btnLogin;
    Button btnSnapshot;
    TextView tvFileScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
    }
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case FILE_UPDATE_MSG:
                    Bundle bundle = msg.getData();
                    tvFileScanner.setText(bundle.getString("filename"));
                    break;
                case BUTTON_UPDATE_END:
                    btnSnapshot.setText("完成文件快照");
                    btnSnapshot.setEnabled(true);
                    break;
            }
            super.handleMessage(msg);
        }
    };
    void findView() {
        tvFileScanner = (TextView)findViewById(R.id.tvFileScanner);
        editTextIpAddress = (EditText) findViewById(R.id.editTextIpAdress);
        editTextFileDir = (EditText) findViewById(R.id.editTextFileDir);
        editTextUserName = (EditText) findViewById(R.id.editTextUserName);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test();
                Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
            }
        });
        btnSnapshot = (Button) findViewById(R.id.btnSnapshot);
        btnSnapshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSnapshot.setText("文件正在生成快照");
                btnSnapshot.setEnabled(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                            //File fileRead = new File("/storage/emulated/0/tencent/MicroMsg/");//手机上的内置SD卡目录
                            File file = new File("/data/data/com.tencent.mm");
                            ArrayList<File> arrayListFile = new ArrayList<File>();
                            FileSnapshot.getFileList(BACKUP_CMD,MainActivity.this,arrayListFile,file,mHandler);
                            Message message = mHandler.obtainMessage();
                            message.what = BUTTON_UPDATE_END;
                            mHandler.sendMessage(message);
                        }
                        else{
                            System.out.println("No SD card");
                        }
                    }
                }).start();
            }
        });

    }

    /**
     * 测试上传同步文件
     */
    void test() {
        strIpAddress = editTextIpAddress.getText().toString();
        strFileDir = editTextFileDir.getText().toString();
        strUserName = editTextUserName.getText().toString();
        strPassword = editTextPassword.getText().toString();
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
                strCookie = strSession + " csrftoken=" + strToken;
                System.out.println();

                String strFirstPage = HttpRequest.sendGetRoot("http://"
                        + strIpAddress + ":8000/", "Set-Cookie", strCookie);
                System.out.println();

                String strRoot = HttpRequest.sendGet("http://" + strIpAddress
                                + ":8000/api2/repos/", "type=mine&_=1481540118100",
                        strCookie);
                System.out.println("****" + strRoot);
                System.out.println();

                int i1 = strRoot.indexOf("id") + 6;
                int i2 = strRoot.indexOf(",", i1);
                strRootId = strRoot.substring(i1, i2 - 1);
                System.out.println(strRootId);

                String strDir = HttpRequest.sendGet("http://" + strIpAddress
                                + ":8000/ajax/lib/" + strRootId + "/dir/",
                        "p=%2F&thumbnail_size=48&_=14815507370932", strCookie);
                System.out.println("****" + strDir);
                System.out.println();
            }
        }).start();
    }
}
