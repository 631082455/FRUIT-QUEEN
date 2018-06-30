package com.example.secret.fruit2.UI;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.example.secret.fruit2.R;
import com.example.secret.fruit2.view.CustomVideoView;

import java.util.Timer;
import java.util.TimerTask;

import cn.bmob.v3.Bmob;

public class MainActivity extends AppCompatActivity {
    //创建播放视频的控件对象
    private CustomVideoView videoview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Window window=getWindow();
        //隐藏状态栏
        int flag= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //设置当前窗体为全屏显示
        window.setFlags(flag,flag);

        ActionBar actionBar=getSupportActionBar();  //把ActionBar去掉
        if (actionBar!=null){
            actionBar.hide();
        }
        Bmob.initialize(this,"d6f6f80e5aed95694944dc606ebaad74");
        //设置定时器，播放完动态视频欢迎界面后，跳转到Ar界面。
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Intent intent=new Intent(MainActivity.this,ArActivity.class);  //跳转到登录的活动
                startActivity(intent);
                finish();  //销毁活动
            }
        },4500);

        initView();


    }
    private void initView() {
        //加载视频资源控件
        videoview = (CustomVideoView) findViewById(R.id.videoview);
        //设置播放加载路径
        videoview.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video));
        //播放
        videoview.start();
        //循环播放
        videoview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                videoview.start();
            }
        });
    }

    //返回重启加载
    @Override
    protected void onRestart() {
        initView();
        super.onRestart();
    }

    //防止锁屏或者切出的时候，视频还在播放
    @Override
    protected void onStop() {
        videoview.stopPlayback();
        super.onStop();
    }





}
