package com.example.wop;

import android.view.MotionEvent;
import android.view.View;

import java.util.concurrent.Delayed;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class OnDoubleClickListener implements View.OnTouchListener{

    private int count = 0;//点击次数
    private long firstClick = 0;//第一次点击时间
    private long secondClick = 0;//第二次点击时间
    private final int totalTime = 250;//两次点击时间间隔，单位毫秒
    private DoubleClickCallback mCallback;//自定义回调接口

    public interface DoubleClickCallback {
        void onDoubleClick(int style,float x,float y);
    }
    public OnDoubleClickListener(DoubleClickCallback callback) {
        super();
        this.mCallback = callback;
    }

    /**
     * 触摸事件处理
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (MotionEvent.ACTION_DOWN == event.getAction()) {//按下
            count++;
            if (1 == count) {
                firstClick = System.currentTimeMillis();//记录第一次点击时间
                mCallback.onDoubleClick(1,0,0);
            } else if (2 == count) {
                secondClick = System.currentTimeMillis();//记录第二次点击时间
                if (secondClick - firstClick < totalTime) {//判断二次点击时间间隔是否在设定的间隔时间之内
                    if (mCallback != null) {
                        mCallback.onDoubleClick(2,event.getX(),event.getY());
                    }
                    count = 0;
                    firstClick = 0;
                } else {
                    firstClick = secondClick;
                    count = 1;
                    mCallback.onDoubleClick(1,0,0);
                }
                secondClick = 0;
            }
        }
        return true;
    }
}
