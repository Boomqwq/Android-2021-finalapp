package com.example.wop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MycameraActivity extends AppCompatActivity implements SurfaceHolder.Callback  {

    private SurfaceView surfaceView;
    private ImageView ivImg;
    private ImageView imgDot;
    private VideoView videoView;
    private Button btnPhoto;
    private Button btnVideo;
    private Button btnYes;
    private Button btnNo;
    private View mainview;

    private int request;
    private static final int REQUEST_CODE_COVER_TAKEIMAGE = 103;
    private static final int REQUEST_CODE_COVER_TAKEVIDEO = 104;
    private String filePath;

    private boolean isRecording = false;
    private String videoPath = "";
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private MediaRecorder mMediaRecorder;

    private int counter;
    private AnimatorSet animatorSet;

    private Handler mHandler= new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case 10:
                    setanimation();
                    counter=10;
                case 9: case 8: case 7: case 6: case 5: case 4: case 3: case 2: case 1:
                    counter--;
                    mHandler.sendEmptyMessageDelayed(counter,1000);
                    break;
                case 0:
                    record(mainview);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mycamera);

        mainview = this.getWindow().getDecorView();
        request = getIntent().getIntExtra("request",0);

        surfaceView = findViewById(R.id.surfaceview);
        ivImg = findViewById(R.id.iv_img);
        imgDot = findViewById(R.id.img_dot);
        videoView = findViewById(R.id.videoview);
        btnPhoto = findViewById(R.id.btn_photo);
        btnVideo = findViewById(R.id.btn_record);
        btnYes = findViewById(R.id.btn_yes);
        btnNo = findViewById(R.id.btn_no);

        if (request == REQUEST_CODE_COVER_TAKEIMAGE) btnPhoto.setVisibility(View.VISIBLE);
            else btnVideo.setVisibility(View.VISIBLE);

        mHolder = surfaceView.getHolder();
        initCamera();
        mHolder.addCallback(this);

        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCamera.takePicture(null, null, mPictureCallback);
            }
        });
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent data = new Intent();
                if (request == REQUEST_CODE_COVER_TAKEIMAGE) data.putExtra("File",filePath);
                    else data.putExtra("File",videoPath);
                setResult(RESULT_OK,data);
                finish();
            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ivImg.setVisibility(View.GONE);
                videoView.stopPlayback();
                videoView.setVisibility(View.GONE);
                btnYes.setVisibility(View.GONE);
                btnNo.setVisibility(View.GONE);
                if (request == REQUEST_CODE_COVER_TAKEIMAGE) btnPhoto.setVisibility(View.VISIBLE);
                else btnVideo.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initCamera() {
        mCamera = Camera.open();
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPictureFormat(ImageFormat.JPEG);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        parameters.set("orientation", "portrait");
        parameters.set("rotation", 90);
        mCamera.setParameters(parameters);
        mCamera.setDisplayOrientation(90);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (holder.getSurface() == null) {
            return;
        }
        //停止预览效果
        mCamera.stopPreview();
        //重新设置预览效果
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    public void takePhoto(View view) {
        mCamera.takePicture(null, null, mPictureCallback);
    }

    //获取照片中的接口回调
    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            FileOutputStream fos = null;
            filePath = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()+ File.separator+"1.jpg";
            File file = new File(filePath);
            try{
                fos = new FileOutputStream(file);
                fos.write(data);
                fos.flush();
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                Bitmap rotate = PathUtils.rotateImage(bitmap,filePath);
                btnPhoto.setVisibility(View.GONE);
                btnVideo.setVisibility(View.GONE);
                ivImg.setVisibility(View.VISIBLE);
                btnYes.setVisibility(View.VISIBLE);
                btnNo.setVisibility(View.VISIBLE);
                ivImg.setImageBitmap(rotate);
            }catch (IOException e){
                e.printStackTrace();
            }finally {
                mCamera.startPreview();
                if (fos != null){
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    public void record(View view) {
        if (!isRecording) {
            // todo 3.4 开始录制
            if (prepareVideoRecorder()){
                mMediaRecorder.start();
                mHandler.sendEmptyMessage(10);
            }
        } else {
            // 停止录制
            mMediaRecorder.setOnErrorListener(null);
            mMediaRecorder.setOnInfoListener(null);
            mMediaRecorder.setPreviewDisplay(null);
            try {
                mMediaRecorder.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
//            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mCamera.lock();
            // todo 3.5 播放录制的视频
            stopanimation();
            btnPhoto.setVisibility(View.GONE);
            btnVideo.setVisibility(View.GONE);
            videoView.setVisibility(View.VISIBLE);
            btnYes.setVisibility(View.VISIBLE);
            btnNo.setVisibility(View.VISIBLE);
            videoView.setVideoPath(videoPath);
            videoView.start();
        }
        isRecording = !isRecording;
    }

    private boolean prepareVideoRecorder() {
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        videoPath = getOutputMediaPath();
        mMediaRecorder.setOutputFile(videoPath);

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mHolder.getSurface());
        mMediaRecorder.setOrientationHint(90);

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException | IOException e) {
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder() {
    }

    private String getOutputMediaPath() {
        File mediaStorageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir, "IMG_" + timeStamp + ".mp4");
        if (!mediaFile.exists()) {
            mediaFile.getParentFile().mkdirs();
        }
        return mediaFile.getAbsolutePath();
    }

    private void setanimation(){
        imgDot.setVisibility(View.VISIBLE);
        btnVideo.setBackgroundResource(R.drawable.video_doing);

        if (animatorSet != null) {
            animatorSet.cancel();
        }

        ObjectAnimator animator1 = ObjectAnimator.ofFloat(imgDot,
                "rotation",-90,270);
        animator1.setRepeatCount(0);
        animator1.setDuration(10000);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(imgDot,
                "alpha",1,0.2f);
        animator2.setRepeatCount(0);
        animator2.setDuration(10000);

        animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator1,animator2);
        animatorSet.start();
    }
    private void stopanimation(){
        mHandler.removeCallbacksAndMessages(null);
        animatorSet.end();
        imgDot.setRotation(-90);
        imgDot.setAlpha(1);
        imgDot.setVisibility(View.GONE);
        btnVideo.setBackgroundResource(R.drawable.videobutton);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera == null) {
            initCamera();
        }
        mCamera.startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCamera.stopPreview();
    }

}
