package com.example.wop;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.wop.adapter.VideoAdapter;
import com.example.wop.model.Message;
import com.example.wop.model.MessageListResponse;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static android.os.Looper.getMainLooper;


public class MineFragment extends Fragment implements VideoAdapter.IOnItemClickListener{

    private RecyclerView myrecyclerView;
    private VideoAdapter myAdapter;
    private StaggeredGridLayoutManager layoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view;
        view = inflater.inflate(R.layout.fragment_mine, container, false);

        myrecyclerView = view.findViewById(R.id.recycler_mine);
        init();

        return view;
    }

    private void init(){
        layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        myrecyclerView.setLayoutManager(layoutManager);
        myAdapter = new VideoAdapter();
        myAdapter.setOnItemClickListener(this);
        myrecyclerView.setAdapter(myAdapter);
        getData(Constants.STUDENT_ID);
    }


    private void showmessages(List<Message> messageList){
        Log.d("Mainfragment", "success");
        myAdapter.setData(messageList);
    }

    private void getData(final String studentId){

        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Message> messages;
                messages = baseGetMessageFromRemote(studentId,Constants.token);
                Log.d("Mainfragment", "messages: "+messages);
                if (messages != null && !messages.isEmpty()) {
                    new Handler(getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            showmessages(messages);
                        }
                    });
                }
            }
        }).start();
    }

    public List<Message> baseGetMessageFromRemote(String student_id, String token){
        String urlStr = Constants.BASE_URL+String.format("video?student_id=%s",student_id);
        MessageListResponse result = null;
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(6000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("token", token);

            if (conn.getResponseCode() == 200) {

                InputStream in = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                result = new Gson().fromJson(reader, MessageListResponse.class);
                Log.d("Mainfragment", "baseGetMessageFromRemote: "+result.feeds);
                reader.close();
                in.close();

            } else {
                // 错误处理
            }
            conn.disconnect();

        } catch (final Exception e) {
            e.printStackTrace();
            Log.d("Mainfragment", "Fail "+e);
        }

        return result.feeds;
    }

    @Override
    public void onItemCLick(int position, Message data) {
        Intent intent=new Intent(Myapplication.getContext(),VideoplayActivity.class);
        intent.putExtra("id",data.getId());
        intent.putExtra("videourl",data.getvideoUrl());
        startActivity(intent);
    }

}
