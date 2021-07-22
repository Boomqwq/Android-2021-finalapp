package com.example.wop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.IOException;

public class VideoplayActivity extends AppCompatActivity {

    private SurfaceView videoView;
    private SeekBar seekBar;
    private TextView tvTime;
    private ImageView imgPause;
    private ImageView imgHeart;
    private Button btnHeart;
    private String video_id;
    private String video_url;
    private MediaPlayer player;
    private SurfaceHolder holder;

    private int total_time = 0;
    private int now_time = 0;

    private final String SP_HEART = "sp_heart";
    private AnimatorSet animatorSet;

    private boolean FLAG = false;
    private boolean PAUSE = false;
    private boolean DOUBLECLICK = false;

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    mHandler.sendMessageDelayed(obtainMessage(1),100);
                    uploadtime();
                    break;
                case 2:
                    if (!DOUBLECLICK) changepause();
                    break;
                case 3:
                    animatorSet.end();
                    imgHeart.setVisibility(View.GONE);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videoplay);

        video_id=getIntent().getStringExtra("id");
        video_url=getIntent().getStringExtra("videourl");

        tvTime = findViewById(R.id.tv_time);
        imgPause = findViewById(R.id.img_pause);
        btnHeart = findViewById(R.id.btn_heart);
        imgHeart = findViewById(R.id.img_heart);

        btnHeart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeheart(0,0,0);
            }
        });

        videoView = findViewById(R.id.view_video);
        player = new MediaPlayer();
        try{
            player.setDataSource(this,Uri.parse(video_url));
            holder = videoView.getHolder();
            holder.addCallback(new PlayerCallBack());
            player.prepare();
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    total_time = player.getDuration();
                    uploadtime();
                    player.start();
                    player.setLooping(true);
                    mHandler.sendEmptyMessage(1);
                    checkheart();
                }

            });
        }catch (IOException e){
            e.printStackTrace();
        }

        videoView.setOnTouchListener(new OnDoubleClickListener(new OnDoubleClickListener.DoubleClickCallback() {
            @Override
            public void onDoubleClick(int style,float x,float y) {
                if (style == 1){
                    DOUBLECLICK = false;
                    mHandler.sendEmptyMessageDelayed(2,300);
                }
                else{
                    changeheart(1,x,y);
                    DOUBLECLICK = true;
                }
            }
        }));


        seekBar = findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (FLAG){
                    now_time = i * total_time / 100;
                    player.seekTo(now_time,MediaPlayer.SEEK_CLOSEST);
                    uploadtime();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                FLAG=true;
                player.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                FLAG=false;
                player.start();
            }
        });
    }

    private void changeheart(int style,float x,float y){
        SharedPreferences sp = this.getSharedPreferences(SP_HEART, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        int value = sp.getInt(video_id,0);
        if (value!=1){
            editor.putInt(video_id,1);
            editor.apply();
            if (style==1) setheartamination(x,y);
            btnHeart.setBackground(getResources().getDrawable(R.drawable.red_heart));
        }
        else{
            editor.putInt(video_id,0);
            editor.apply();
            btnHeart.setBackground(getResources().getDrawable(R.drawable.white_heart));
        }
    }

    private void setheartamination(float x,float y){
        imgHeart.setX(x-150);
        imgHeart.setY(y-150);
        imgHeart.setVisibility(View.VISIBLE);

        if (animatorSet != null) {
            animatorSet.cancel();
        }

        ObjectAnimator animator = ObjectAnimator.ofFloat(imgHeart,
                "scaleX",0.5f,1.2f);
        animator.setRepeatCount(1);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setDuration(250);
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(imgHeart,
                "scaleY",0.5f,1.2f);
        animator1.setRepeatCount(1);
        animator1.setRepeatMode(ValueAnimator.REVERSE);
        animator1.setDuration(250);

        mHandler.sendEmptyMessageDelayed(3,500);
        animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator,animator1);
        animatorSet.start();

    }

    private void checkheart(){
        SharedPreferences sp = this.getSharedPreferences(SP_HEART, MODE_PRIVATE);
        int value = sp.getInt(video_id,0);
        if (value==1){
            btnHeart.setBackground(getResources().getDrawable(R.drawable.red_heart));
        }
    }

    private void changepause(){
        if (PAUSE){
            player.start();
            PAUSE=false;
            imgPause.setVisibility(View.INVISIBLE);
        }
        else{
            player.pause();
            PAUSE=true;
            imgPause.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeCallbacksAndMessages(null);
        mHandler=null;
        player.release();
    }

    private void uploadtime(){
        int t1,t2,t3,t4;
        float time,time1;
        time = total_time;
        time = now_time/time*100;
        now_time = player.getCurrentPosition();
        t1=(now_time/1000) / 60;
        t2=(now_time/1000) % 60;
        t3=(total_time/1000) / 60;
        t4=(total_time/1000) % 60;
        seekBar.setProgress(Math.round(time));
        tvTime.setText(String.format("%02d:%02d/%02d:%02d",t1,t2,t3,t4));
    }

    private class PlayerCallBack implements SurfaceHolder.Callback{

        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            player.setDisplay(surfaceHolder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

        }
    }

    private void doback(){
        finish();
    }
}
