package com.example.secret.fruit2.Adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.secret.fruit2.R;
import com.example.secret.fruit2.UI.CommentActivity;
import com.example.secret.fruit2.bean.All_Comment;
import com.example.secret.fruit2.bean.Comment;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;


/**
 * 评论界面的adapter
 */

public class CommentAdapter extends RecyclerView.Adapter {
    private Context context;
    private List<Comment> comments = new ArrayList<>();
    CommentChildAdapter childAdapter; //= new CommentChildAdapter(context);
    public List<CommentChildAdapter> adapterList = new ArrayList<>();
    public int childPosition;

    public CommentAdapter(Context context) {
        this.context = context;
    }

    public void bindData(List<Comment> list) {
        this.comments.clear();
        if (list != null) {
            this.comments.addAll(list);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CommentHolder holder = new CommentHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final CommentHolder commentHolder = (CommentHolder) holder;

        commentHolder.bindData(comments.get(position));

        childAdapter = new CommentChildAdapter(context);
        adapterList.add(childAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        //通过绑定父评论来查找子评论
        query(position);
        //子评论设置适配器
        commentHolder.ChildRecycler.setAdapter(childAdapter);
        commentHolder.ChildRecycler.setLayoutManager(linearLayoutManager);



        //点击回复他人评论
        final CommentActivity activity = (CommentActivity) context;
        commentHolder.ChildRecycler.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                commentHolder.ChildRecycler.getParent().requestDisallowInterceptTouchEvent(true);

                RecyclerView rv = (RecyclerView) view;
                View childView = rv.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
                //获取子评论的位置
                childPosition = rv.getChildPosition(childView);

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    activity.who = 3;
                    activity.itemPosition = position;
                    activity.etComment.requestFocus();
                    activity.etComment.setFocusable(true);
                    InputMethodManager imm = (InputMethodManager) activity.etComment.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                    activity.etComment.setHint("回复" + adapterList.get(position).getItem(childPosition).getUser1().getUsername());
                }

                return false;
            }
        });


    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public Comment getItem(int position) {
        return comments.get(position);
    }

    int count;

    public void query(final int position) {
        final ProgressDialog progressDialog = ProgressDialog.show(context, null, "正在加载....");
        Comment comment = comments.get(position);
        BmobQuery<All_Comment> query = new BmobQuery<>();
        query.addWhereEqualTo("comment", new BmobPointer(comment));
        query.include("User1,User2,comment");
        query.order("createdAt");
        query.findObjects(new FindListener<All_Comment>() {
            @Override
            public void done(List<All_Comment> list, BmobException e) {
                if (e == null) {
                    if (list != null) {
                        adapterList.get(position).bindData(list);
                        adapterList.get(position).notifyDataSetChanged();


                    }
                    progressDialog.dismiss();
                } else {
                    // Log.d("testss","data=onerror"+s);
                    progressDialog.dismiss();
                    Toast.makeText(context, "获取评论失败，" + e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    public class CommentHolder extends RecyclerView.ViewHolder {
        protected TextView tvUsername, tvComment, tvTime;
        public RecyclerView ChildRecycler;



        public CommentHolder(View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvName);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvTime = itemView.findViewById(R.id.tvTime);
            ChildRecycler = itemView.findViewById(R.id.rv_child);


        }

        public void bindData(Comment comment) {
            tvUsername.setText(comment.getUser().getUsername());
            tvComment.setText(comment.getCommentcontent());
            tvTime.setText(comment.getCreatedAt());
        }
    }
}
