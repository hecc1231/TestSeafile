package com.hersch.testseafile.ui;

import android.content.Intent;
import android.os.*;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hersch.testseafile.R;
import com.hersch.testseafile.net.HttpRequest;
import com.hersch.testseafile.net.Network;

import com.hersch.testseafile.files.FileRooter;

public class MainActivity extends AppCompatActivity {
    public static String strToken = "";
    public static String strCookie = "";
    final static int MSG_UNSUCESS_NETWORK = 1;
    final static int MSG_UNSUCCESS_LOGIN = 2;
    final static int MSG_SUCCESS_LOGIN = 3;
    static String strIpAddress = HttpRequest.strIpAddress;//"10.108.20.142";//
    public static String strRootId = "";
    static String strCurrentPath = "/data/data/com.hersch.testseafile/";
    static String strUserName = "hcc_public@163.com";
    static String strPassword = "beijing520";
    EditText editTextIpAddress;
    EditText editTextUserName;
    EditText editTextPassword;
    Button btnLogin;
    TextView tvFileScanner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FileRooter.requestRoot();
        findView();
    }
    void findView() {
        tvFileScanner = (TextView)findViewById(R.id.tvFileScanner);
        editTextIpAddress = (EditText) findViewById(R.id.editTextIpAdress);
        editTextUserName = (EditText) findViewById(R.id.editTextUserName);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLoginInfo();
            }
        });
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
     * 通过用户名和密码登录并且获得cookie和rootId
     */
    void getLoginInfo(){
        strIpAddress = editTextIpAddress.getText().toString();
        strUserName = editTextUserName.getText().toString();
        strPassword = editTextPassword.getText().toString();
            new Thread(new Runnable() {
                @Override
                public void run() {
                        if (Network.isNetWorkAvailable(getApplicationContext())) {
                            try {
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
                                sendMsg(MSG_SUCCESS_LOGIN);
                            } catch (StringIndexOutOfBoundsException e) {
                                sendMsg(MSG_UNSUCCESS_LOGIN);
                                e.printStackTrace();
                            }
                        }
                    else {
                            sendMsg(MSG_UNSUCESS_NETWORK);
                        }
                }
            }).start();
    }
    private Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_UNSUCCESS_LOGIN:
                    Toast.makeText(MainActivity.this,"用户名或者密码错误",Toast.LENGTH_SHORT).show();
                    break;
                case MSG_SUCCESS_LOGIN:
                    Toast.makeText(MainActivity.this,"登录成功",Toast.LENGTH_SHORT).show();
                    btnLogin.setEnabled(false);
                    btnLogin.setText("已登录");
                    Intent intent = new Intent(MainActivity.this,SecondActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case MSG_UNSUCESS_NETWORK:
                    Toast.makeText(MainActivity.this,"无法连接网络，请检查联网",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}
