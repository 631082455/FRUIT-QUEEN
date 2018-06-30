package com.example.secret.fruit2.UI;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.secret.fruit2.R;
import com.example.secret.fruit2.bean.Feedback;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class AboutUsActivity extends AppCompatActivity {

    //填写反馈的文字框
    private EditText etFeedback;

    //发送按钮
    private TextView btnSend;

    //顶部标题栏
    private TextView tvTitle;

    //后退按钮
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        initView();
        initEvent();

    }

    private void initView() {

        etFeedback = findViewById(R.id.etFeedback);

        btnSend = findViewById(R.id.btnSend);

        tvTitle = findViewById(R.id.tvTitle);


        btnBack = findViewById(R.id.btnBack);

    }

    private void initEvent() {

        tvTitle.setText("关于我们");

        //发送按钮的事件
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                BmobUser currentUser = BmobUser.getCurrentUser();
                if (currentUser == null) {
                    toast("请登录后再反馈吧");
                    return;
                }

                if (TextUtils.isEmpty(etFeedback.getText())) {
                    toast("请填写后再发送");
                    return;
                }

                Feedback feedback = new Feedback();
                feedback.setUser(currentUser);
                feedback.setContent(etFeedback.getText().toString());
                feedback.save(new SaveListener<String>() {
                    @Override
                    public void done(String s, BmobException e) {
                        if (e == null) {

                            toast("发送成功");
                            //清空反馈内容
                            etFeedback.setText("");
                        } else {
                            toast("反馈失败," + e.getMessage());
                        }
                    }
                });

            }
        });

        //后退按钮点击事件
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void toast(String s) {
        Toast.makeText(AboutUsActivity.this, s, Toast.LENGTH_SHORT).show();
    }
}
