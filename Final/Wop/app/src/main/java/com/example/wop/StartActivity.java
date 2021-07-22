package com.example.wop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    private int SKIP_TIME = 5;
    private Button btn_skip;

    //Handler收到消息之后调用函数来更新倒计时
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    timedecrease();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        //将状态栏隐藏
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        //按下跳过按钮时关闭开屏界面，清空Handler中的消息
        btn_skip = findViewById(R.id.btn_skip);
        btn_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.removeCallbacksAndMessages(null);
                skiptomain();
            }
        });

        //每秒向Handler中发送一条消息
        mHandler.sendMessageDelayed(Message.obtain(mHandler,1),1000);
        mHandler.sendMessageDelayed(Message.obtain(mHandler,1),2000);
        mHandler.sendMessageDelayed(Message.obtain(mHandler,1),3000);
        mHandler.sendMessageDelayed(Message.obtain(mHandler,1),4000);
        mHandler.sendMessageDelayed(Message.obtain(mHandler,1),5000);
    }


    //以淡出的方式关闭开屏页面，主界面显示出来，并让状态栏显示出来
    private void skiptomain(){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    //更新倒计时，如果五秒已到，则跳转到主界面
    private void timedecrease(){
        SKIP_TIME -= 1;
        if (SKIP_TIME>0){
            btn_skip.setText(String.format("跳过 | %d S",SKIP_TIME));
        }
        else {
            mHandler.removeCallbacksAndMessages(null);
            skiptomain();
        }
    }
}
