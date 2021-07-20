package com.example.wop.adapter;

import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.wop.MainActivity;
import com.example.wop.MainFragment;
import com.example.wop.Myapplication;
import com.example.wop.R;
import com.example.wop.model.Message;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private List<Message> data;
    private IOnItemClickListener mItemClickListener;

    public void setData(List<Message> messageList){
        data = messageList;
        notifyDataSetChanged();
    }


    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root =LayoutInflater.from(parent.getContext()).inflate(R.layout.radio_item,parent,false);
        return new VideoViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        holder.bind(data.get(position));
        holder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemCLick(position, data.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return data==null?0:data.size();
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder{
        private ImageView imgVideo;
        private TextView tvName;
        private View contentView;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            contentView = itemView;
            imgVideo = itemView.findViewById(R.id.img_video);
            tvName = itemView.findViewById(R.id.tv_name);
        }

        public void bind(Message message){
            Glide.with(Myapplication.getContext()).load(message.getImageUrl()).into(imgVideo);
//            imgVideo.setImageURI(Uri.parse(message.getImageUrl()));
            tvName.setText(message.getusername());
        }

        public void setOnClickListener(View.OnClickListener listener) {
            if (listener != null) {
                contentView.setOnClickListener(listener);
            }
        }
    }

    public interface IOnItemClickListener {
        void onItemCLick(int position, Message data);
    }

    public void setOnItemClickListener(IOnItemClickListener listener) {
        mItemClickListener = listener;
    }
}
