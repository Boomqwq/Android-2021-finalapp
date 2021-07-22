package com.example.wop;


import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.example.wop.model.UploadResponse;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class UploadFragment extends Fragment{

    private Button btnLocalimg;
    private Button btnGetimg;
    private Button btnLocalvideo;
    private Button btnGetvideo;
    private Button btnUpload;
    private ImageView imgUpload;
    private VideoView videoUpload;

    private final static int PERMISSION_REQUEST_CODE = 1001;

    private static final int REQUEST_CODE_COVER_IMAGE = 101;
    private static final int REQUEST_CODE_COVER_VIDEO = 102;
    private static final int REQUEST_CODE_COVER_TAKEIMAGE = 103;
    private static final int REQUEST_CODE_COVER_TAKEVIDEO = 104;
    private static final String COVER_IMAGE_TYPE = "image/*";
    private static final String COVER_VIDEO_TYPE = "video/*";

    private static final long MAX_FILE_SIZE = 30 * 1024 * 1024;

    private Uri ImageUri;
    private Uri VideoUri;

    private IApi api;

    private AnimatorSet animatorSetupload;

    private final OkHttpClient client = new OkHttpClient.Builder().
            connectTimeout(60, TimeUnit.SECONDS).
            readTimeout(60,TimeUnit.SECONDS).
            writeTimeout(60,TimeUnit.SECONDS).
            build();

    private final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        initNetwork();
        View view;
        view = inflater.inflate(R.layout.fragment_upload, container, false);

        btnLocalimg = view.findViewById(R.id.btn_localimg);
        btnGetimg = view.findViewById(R.id.btn_getimg);
        btnLocalvideo = view.findViewById(R.id.btn_localvideo);
        btnGetvideo = view.findViewById(R.id.btn_getvideo);
        btnUpload = view.findViewById(R.id.btn_upload);
        imgUpload = view.findViewById(R.id.img_upload);
        videoUpload = view.findViewById(R.id.sv_upload);

        init();

        return view;
    }

    private void init(){

        btnGetimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermission();
                takephoto(REQUEST_CODE_COVER_TAKEIMAGE);
            }
        });

        btnLocalimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFile(REQUEST_CODE_COVER_IMAGE, COVER_IMAGE_TYPE, "选择图片");
            }
        });

        btnGetvideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermission();
                takephoto(REQUEST_CODE_COVER_TAKEVIDEO);
            }
        });

        btnLocalvideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFile(REQUEST_CODE_COVER_VIDEO, COVER_VIDEO_TYPE, "选择视频");
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setuploadanimation();
                submit();
            }
        });

        videoUpload.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                videoUpload.start();
            }
        });
    }

    private void initNetwork() {api = retrofit.create(IApi.class);}

    private void getFile(int requestCode, String type, String title) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(type);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        intent.putExtra(Intent.EXTRA_TITLE, title);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, requestCode);
    }

    private void takephoto(int requestCode){
        Intent intent=new Intent(Myapplication.getContext(),MycameraActivity.class);
        intent.putExtra("request",requestCode);
        startActivityForResult(intent,requestCode);
    }

    private void requestPermission() {
        boolean hasCameraPermission = ContextCompat.checkSelfPermission(Myapplication.getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean hasAudioPermission = ContextCompat.checkSelfPermission(Myapplication.getContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        if (hasCameraPermission && hasAudioPermission) {
        } else {
            List<String> permission = new ArrayList<String>();
            if (!hasCameraPermission) {
                permission.add(Manifest.permission.CAMERA);
            }
            if (!hasAudioPermission) {
                permission.add(Manifest.permission.RECORD_AUDIO);
            }
            requestPermissions(permission.toArray(new String[permission.size()]), PERMISSION_REQUEST_CODE);
        }

    }


    private void submit(){
        byte[] ImageData = readDataFromUri(ImageUri);
        if (ImageData == null || ImageData.length == 0) {
            Toast.makeText(getActivity(), "未设置封面", Toast.LENGTH_SHORT).show();
            resetloadanimation(0);
            return;
        }
        byte[] VideoData = readDataFromUri(VideoUri);
        if (VideoData == null || VideoData.length == 0) {
            Toast.makeText(getActivity(), "未设置视频", Toast.LENGTH_SHORT).show();
            resetloadanimation(0);
            return;
        }
        if ( ImageData.length+VideoData.length >= MAX_FILE_SIZE) {
            Toast.makeText(getActivity(), "文件过大", Toast.LENGTH_SHORT).show();
            resetloadanimation(0);
            return;
        }
        Log.d("upload", "submit: 0    imageuri:  "+ImageUri+"  videouri:  "+VideoUri);
        Log.d("upload", "submit: 1    imagelegth:  "+ImageData.length+"   videolegth:   "+VideoData.length);

        MultipartBody.Part coverIma = MultipartBody.Part.createFormData("cover_image", "cover_image.png",
                RequestBody.create(MediaType.parse("multipart/form-data"), ImageData));
        MultipartBody.Part coverVid = MultipartBody.Part.createFormData("video", "video.mp4",
                RequestBody.create(MediaType.parse("multipart/form-data"), VideoData));

        Call<UploadResponse> call = api.submitMessage(
                Constants.STUDENT_ID, Constants.USER_NAME,"",coverIma,coverVid,Constants.token);

        call.enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(final Call<UploadResponse> call, final Response<UploadResponse> response) {
                Log.d("upload", "submit: 3");
                if (!response.isSuccessful()) {
                    Log.d("upload", "submit: 4");
                    Toast.makeText(getActivity(), "上传失败", Toast.LENGTH_SHORT).show();
                    resetloadanimation(0);
                    return;
                }
                else {
                    Log.d("upload", "submit: 5");
                    Toast.makeText(getActivity(), "上传成功", Toast.LENGTH_SHORT).show();
                    resetloadanimation(1);
                }
            }
            @Override
            public void onFailure(final Call<UploadResponse> call, final Throwable t) {
                t.printStackTrace();
                Log.d("upload", "submit: 6 "+t);
                resetloadanimation(0);
            }
        });
    }

    private byte[] readDataFromUri(Uri uri) {
        byte[] data = null;
        try {
            InputStream is = getActivity().getContentResolver().openInputStream(uri);
            data = Util.inputStream2bytes(is);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_COVER_IMAGE && resultCode == Activity.RESULT_OK) {
            ImageUri = data.getData();
            imgUpload.setImageURI(ImageUri);
        }
        else if (requestCode == REQUEST_CODE_COVER_VIDEO && resultCode == Activity.RESULT_OK) {
            VideoUri = data.getData();
            videoUpload.setVideoURI(VideoUri);
            videoUpload.start();
            videoUpload.setVisibility(View.VISIBLE);
        }
        else if (requestCode == REQUEST_CODE_COVER_TAKEIMAGE && resultCode == Activity.RESULT_OK) {
            ImageUri = Uri.fromFile(new File(data.getStringExtra("File")));
            imgUpload.setImageURI(ImageUri);
        }
        else if (requestCode == REQUEST_CODE_COVER_TAKEVIDEO && resultCode == Activity.RESULT_OK) {
            VideoUri = Uri.fromFile(new File(data.getStringExtra("File")));
            Log.d("upload", "VideoUri: "+VideoUri);
            videoUpload.setVideoURI(VideoUri);
            videoUpload.start();
            videoUpload.setVisibility(View.VISIBLE);
        }
    }

    private void setuploadanimation(){

        if (animatorSetupload != null) {
            animatorSetupload.cancel();
        }

        btnUpload.setText("上传中");
        ObjectAnimator animator = ObjectAnimator.ofFloat(btnUpload,
                "scaleX",1,1.2f);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setDuration(1000);
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(btnUpload,
                "scaleY",1,1.2f);
        animator1.setRepeatCount(ValueAnimator.INFINITE);
        animator1.setRepeatMode(ValueAnimator.REVERSE);
        animator1.setDuration(1000);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(btnUpload,
                "alpha",1,0.5f);
        animator2.setRepeatCount(ValueAnimator.INFINITE);
        animator2.setRepeatMode(ValueAnimator.REVERSE);
        animator2.setDuration(1000);

        animatorSetupload = new AnimatorSet();
        animatorSetupload.playTogether(animator,animator1,animator2);
        animatorSetupload.start();
    }

    private void resetloadanimation(int station){
        btnUpload.setText("上 传");
        animatorSetupload.end();
        btnUpload.setScaleX(1);
        btnUpload.setScaleY(1);
        btnUpload.setAlpha(1);

        if (station==1){
            ImageUri=null;
            VideoUri=null;
            imgUpload.setImageURI(ImageUri);
            videoUpload.setVideoURI(VideoUri);
            videoUpload.stopPlayback();
            videoUpload.setVisibility(View.INVISIBLE);
        }
    }
}
