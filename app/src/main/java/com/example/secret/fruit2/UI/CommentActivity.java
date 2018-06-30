package com.example.secret.fruit2.UI;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.secret.fruit2.Adapter.CommentAdapter;
import com.example.secret.fruit2.R;
import com.example.secret.fruit2.RecycleViewDivider;
import com.example.secret.fruit2.RecyclerItemClickListener;
import com.example.secret.fruit2.bean.All_Comment;
import com.example.secret.fruit2.bean.Comment;
import com.example.secret.fruit2.bean.Recipe;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

public class CommentActivity extends AppCompatActivity {

    private RecyclerView rv;
    //发送评论和返回按钮
    private ImageButton btnSend;
    //后退键
    private ImageView btnBack;
    //写评论
    public EditText etComment;

    /*  who的值代表评论内容的不同
        who=1：只是纯粹的发表评论
        who=2:回复第一条评论
        who=3:回复他人的评论
    */
    public int who = 1;

    //评论的适配器
    private CommentAdapter adapter;
    //评论条目的位置
    public int itemPosition;

    //顶部标题栏
    private TextView tvTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        initView();

        initData();

        initEvent();
    }

    private void initView() {

        rv = findViewById(R.id.rv_Pinlun);
        //评论框
        etComment = findViewById(R.id.etComment);
        //设置最大高度
        etComment.setMaxHeight(400);
        //发送评论
        btnSend = findViewById(R.id.btnSendPinlun);
        //后退键
        btnBack = findViewById(R.id.btnBack);

        tvTitle = findViewById(R.id.tvTitle);
    }

    //获取评论内容
    public void initData() {

        Recipe recipe = (Recipe) getIntent().getSerializableExtra("recipe");
        BmobQuery<Comment> query = new BmobQuery<>();
        //获取当前食谱的评论
        query.addWhereEqualTo("recipe", new BmobPointer(recipe));
        query.include("recipe,user");
        query.order("-createdAt");
        query.findObjects(new FindListener<Comment>() {
            @Override
            public void done(List<Comment> list, BmobException e) {
                if (e == null) {
                    if (list != null) {
                        adapter.bindData(list);
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Log.e("==comment-error:", e.getMessage());
                }
            }


        });
    }

    private void initEvent() {

        tvTitle.setText("评论");

        //对评论设置适配器
        adapter = new CommentAdapter(this);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(this));
        //设置分割线
        rv.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager.HORIZONTAL));
        //点击该评论，表明要回复该评论
        rv.addOnItemTouchListener(new RecyclerItemClickListener(this, rv, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                itemPosition = position;
                who = 2;
                etComment.requestFocus();
                etComment.setFocusable(true);
                //弹出输入框
                InputMethodManager imm = (InputMethodManager) etComment.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                etComment.setHint("回复" + adapter.getItem(position).getUser().getUsername());

            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        }));

        //发送评论的点击事件
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                BmobUser currentUser = BmobUser.getCurrentUser();
                if (currentUser == null) {
                    toast("请先登录后再进行评论");
                    return;
                }

                if (who == 1) {//只是纯粹的发表评论
                    BmobUser user = BmobUser.getCurrentUser();
                    Recipe recipe = (Recipe) getIntent().getSerializableExtra("recipe");
                    String commentContent = etComment.getText().toString();
                    if (commentContent.trim().equals("")) {
                        return;
                    }
                    Comment comment = new Comment();
                    comment.setCommentcontent(commentContent);
                    comment.setRecipe(recipe);
                    comment.setUser(user);
                    comment.save(new SaveListener<String>() {
                        @Override
                        public void done(String s, BmobException e) {
                            if (e == null) {

                                toast("评论发表成功");
                                initData();
                                etComment.setText("");


                            } else {

                                Log.e("==comment-error", "失败：" + s);

                            }
                        }
                    });
                } else if (who == 2) {//回复第一条评论
                    //review为当前用户
                    BmobUser review = BmobUser.getCurrentUser();
                    //第一条评论
                    final Comment commentContent = adapter.getItem(itemPosition);
                    //第一条评论的评论内容
                    String content2 = commentContent.getCommentcontent();
                    //评论框内容
                    String contentChild = etComment.getText().toString();

                    All_Comment all_comment = new All_Comment();
                    all_comment.setUser1(review);//当前用户
                    all_comment.setUser2(commentContent.getUser());//之前发评论的用户
                    all_comment.setContent1(contentChild);//评论框的内容
                    all_comment.setContent2(content2);//之前发的评论内容
                    all_comment.setComment(commentContent);//之前的那条评论
                    all_comment.setWho(2);
                    all_comment.save(new SaveListener<String>() {
                        @Override
                        public void done(String s, BmobException e) {
                            if (e == null) {
                                toast("评论发表成功");
                                adapter.query(itemPosition);
                                etComment.setText("");
                            } else {
                                Log.e("bmob", "失败：" + s);
                            }
                        }
                    });
                } else if (who == 3) {//回复别人的评论
                    BmobUser review = BmobUser.getCurrentUser();
                    //点击的子评论
                    Comment commentContent = adapter.getItem(itemPosition);
                    //评论框的内容
                    String contentChild = etComment.getText().toString();

                    All_Comment all_comment = new All_Comment();
                    all_comment.setUser1(review);//当前用户
                    all_comment.setContent1(contentChild);
                    BmobUser user2 = adapter.adapterList.get(itemPosition).getItem(adapter.childPosition).getUser1();
                    all_comment.setUser2(user2);//回复评论的评论作者
                    all_comment.setContent2(adapter.adapterList.get(itemPosition).getItem(adapter.childPosition).getContent1());
                    all_comment.setComment(commentContent);
                    all_comment.setWho(3);
                    all_comment.save(new SaveListener<String>() {
                        @Override
                        public void done(String s, BmobException e) {
                            if (e == null) {
                                toast("评论发表成功");
                                adapter.query(itemPosition);
                                etComment.setText("");

                            } else {
                                toast("评论发表失败," + s);
                            }
                        }
                    });

                }
            }
        });


        //对评论框的editText进行监听
        final TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!TextUtils.isEmpty(etComment.getText().toString())) {
                    //如果有填文字，则可以发送
                    btnSend.setVisibility(View.VISIBLE);

                } else {
                    btnSend.setVisibility(View.GONE);

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
        etComment.addTextChangedListener(textWatcher);


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    public void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
