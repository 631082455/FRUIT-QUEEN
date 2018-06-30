package com.example.secret.fruit2.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import java.util.Timer;
import java.util.TimerTask;

/**
 *
 */

public class CountDownTimerButton extends Button implements View.OnClickListener {
    private Context mContext;
    private OnClickListener mOnClickListener;
    private Timer mTimer;//调度器
    private TimerTask mTask;
    private long duration = 30000;//倒计时时长 设置默认30秒
    private long temp_duration;
    private String clickBefor = "重新获取";//点击前
    private String clickAfter = "秒后重获";//点击后

    public CountDownTimerButton(Context context) {
        super(context);
        mContext = context;
        setOnClickListener(this);
    }

    public CountDownTimerButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setOnClickListener(this);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            CountDownTimerButton.this.setText(temp_duration / 1000 + clickAfter);
            temp_duration -= 1000;
            if (temp_duration < 0) {
                //倒计时结束
                CountDownTimerButton.this.setEnabled(true);
                CountDownTimerButton.this.setText(clickBefor);
                stopTimer();
            }
        }
    };

    @Override
    public void setOnClickListener(OnClickListener onClickListener) {
        if (onClickListener instanceof CountDownTimerButton) {
            super.setOnClickListener(onClickListener);
        } else {
            this.mOnClickListener = onClickListener;
        }
    }

    @Override
    public void onClick(View view) {
        if (mOnClickListener != null) {
            mOnClickListener.onClick(view);
        }
        startTimer();
    }

    //计时开始
    private void startTimer() {
        temp_duration = duration;
        CountDownTimerButton.this.setEnabled(false);
        mTimer = new Timer();
        mTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(0x01);
            }
        };
        //延迟0秒  时间间隔为1秒
        mTimer.schedule(mTask, 0, 1000);
    }

    //计时结束
    private void stopTimer() {
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }
}
