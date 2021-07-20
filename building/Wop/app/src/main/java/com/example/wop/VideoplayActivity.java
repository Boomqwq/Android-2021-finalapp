package com.example.wop;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class VideoplayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videoplay);

        String video_id=getIntent().getStringExtra("id");
        String video_url=getIntent().getStringExtra("videourl");

    }

    private void doback(){
        finish();
    }
}
