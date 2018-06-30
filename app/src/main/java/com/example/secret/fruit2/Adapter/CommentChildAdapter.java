package com.example.secret.fruit2.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.secret.fruit2.R;
import com.example.secret.fruit2.bean.All_Comment;

import java.util.ArrayList;
import java.util.List;

/**
 * 评论的评论的适配器
 */

public class CommentChildAdapter extends RecyclerView.Adapter {
    List<All_Comment> list = new ArrayList<>();
    Context context;

    public CommentChildAdapter(Context context) {
        this.context = context;
    }

    public void bindData(List<All_Comment> list) {
        this.list.clear();
        if (list != null) {
            this.list.addAll(list);
        }
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CommentChildHolder holder = new CommentChildHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment_child, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        CommentChildHolder commentChildHolder = (CommentChildHolder) holder;
        commentChildHolder.tvReview.setText("@" + list.get(position).getUser1().getUsername());
        commentChildHolder.tvAuthor.setText("@" + list.get(position).getUser2().getUsername() + ":");
        commentChildHolder.tvContent.setText(list.get(position).getContent1());
        System.out.println("pinlun:" + list.get(position).getContent2());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public All_Comment getItem(int position) {
        return list.size() != 0 ? list.get(position) : null;
    }

    class CommentChildHolder extends RecyclerView.ViewHolder {
        TextView tvReview, tvReply, tvAuthor, tvContent;

        public CommentChildHolder(View itemView) {
            super(itemView);
            tvReview = (TextView) itemView.findViewById(R.id.tv_reviewName);
            tvReply = (TextView) itemView.findViewById(R.id.tv_reply);
            tvAuthor = (TextView) itemView.findViewById(R.id.tv_author);
            tvContent = (TextView) itemView.findViewById(R.id.tv_Content);
        }
    }
}