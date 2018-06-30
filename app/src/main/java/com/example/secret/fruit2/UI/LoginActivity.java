package com.example.secret.fruit2.UI;

import android.app.ProgressDialog;
import android.content.Intent;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.secret.fruit2.R;
import com.example.secret.fruit2.view.CountDownTimerButton;
import com.mob.MobSDK;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class LoginActivity extends AppCompatActivity {
    public EventHandler handler;  //初始化事件接收器为全局变量
    private CountDownTimerButton timerBtn;
    private String phone;

    public void toast(String s) {
        Toast.makeText(LoginActivity.this, s, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActionBar actionBar = getSupportActionBar();  //把ActionBar去掉
        if (actionBar != null) {
            actionBar.hide();
        }
        //初始化APPkey  和  Appsecret
        MobSDK.init(this, "266d684068f90", "ca87929d8e3aef0665a5b00b7d051a5d");
        this.getWindow().setBackgroundDrawable(null);

        //初始化布局组件
        Button button_login = (Button) findViewById(R.id.login);   //校验短信验证码的button
        final EditText check_code = (EditText) findViewById(R.id.check_code);
        timerBtn = (CountDownTimerButton) findViewById(R.id.timeBtn);    //计时  以及获取短信验证码的button
        final EditText phone_number = (EditText) findViewById(R.id.Edt_phone);   //获取用户输入的手机号码

        timerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取验证码
                phone = phone_number.getText().toString();   //获取用户输入的手机号
                SMSSDK.getVerificationCode("86", phone);

            }
        });


        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //提交短信验证码到后台
                String checkcode = check_code.getText().toString();
                SMSSDK.submitVerificationCode("86", phone, checkcode);
            }
        });

        handler = new EventHandler() {
            @Override
            public void afterEvent(int event, int result, Object data) {
                if (result == SMSSDK.RESULT_COMPLETE) {
                    //回调完成
                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        //提交验证码成功
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);  //在登录过程中屏蔽用户与按键的交互
                                progressDialog.setTitle("Fruits Queen");
                                progressDialog.setMessage("用户正在登陆.");
                                progressDialog.setCancelable(false);
                                progressDialog.show();
//                                Toast.makeText(LoginActivity.this, "验证成功", Toast.LENGTH_SHORT).show();
                                //信息系统验证成功之后,把用户手机号码存入Bmob云,密码是默认的,之后再由用户自行修改
                                final String username = phone_number.getText().toString();  //用户输入手机号码

                                BmobQuery<BmobUser> query = new BmobQuery<BmobUser>();
                                query.addWhereEqualTo("username", username);
                                query.findObjects(new FindListener<BmobUser>() {
                                    @Override
                                    public void done(List<BmobUser> object, BmobException e) {
                                        if (object.size() == 1) {
                                            //数据库有这个用户就执行登录逻辑

                                            BmobUser bu1 = new BmobUser();
                                            bu1.setUsername(username);
                                            bu1.setPassword("123456");   //这个是默认密码
                                            bu1.login(new SaveListener<BmobUser>() {
                                                @Override
                                                public void done(BmobUser bmobUser, BmobException e) {
                                                    if (e == null) {
                                                        //登录成功
                                                        progressDialog.dismiss();

                                                        Toast.makeText(LoginActivity.this, "登陆成功", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(LoginActivity.this, ArActivity.class);
                                                        startActivity(intent);
                                                        finish();//销毁活动
                                                    } else {
                                                        //登录失败
                                                        Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                        } else {
                                            //数据库没这个用户就创建
                                            final BmobUser user = new BmobUser();  //创建用户

                                            user.setUsername(username);  //用手机号码做username
                                            user.setPassword("123456");  //用户初始默认密码为123456,即使让别人知道默认密码也没事,因为不认证用户,就一直都要用手机号码发验证码登录
                                            user.signUp(new SaveListener<BmobUser>() {
                                                @Override
                                                public void done(BmobUser myUser, BmobException e) {
                                                    if (e == null) {
                                                        user.login(new SaveListener<BmobUser>() {
                                                            @Override
                                                            public void done(BmobUser bmobUser, BmobException e) {
                                                                if (e == null) {
                                                                    //登录成功
                                                                    progressDialog.dismiss();

                                                                    Toast.makeText(LoginActivity.this, "登陆成功", Toast.LENGTH_SHORT).show();
                                                                    Intent intent = new Intent(LoginActivity.this, ArActivity.class);
                                                                    startActivity(intent);
                                                                    finish();//销毁活动
                                                                } else {
                                                                    //登录失败
                                                                    Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });

                                                    } else {
                                                        toast("注册失败，" + e.toString());
                                                    }
                                                }
                                            });

                                        }
                                    }
                                });
                            }
                        });
                    } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                        //获取验证码成功
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this, "验证码已发送", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {

                    }
                } else {
                    ((Throwable) data).printStackTrace();
                    Throwable throwable = (Throwable) data;
                    try {
                        JSONObject obj = new JSONObject(throwable.getMessage());
                        final String des = obj.optString("detail");
                        if (!TextUtils.isEmpty(des)) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LoginActivity.this, "提交错误信息", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        };

        SMSSDK.registerEventHandler(handler);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterEventHandler(handler);
    }


}
