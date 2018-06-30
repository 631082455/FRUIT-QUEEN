package com.example.secret.fruit2.UI;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.secret.fruit2.R;
import com.example.secret.fruit2.bean.Like;
import com.example.secret.fruit2.bean.Recipe;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class LearnRecipeActivity extends AppCompatActivity {

    //食谱信息
    private TextView tvName, tvAuthor, tvLike, tvMateril, tvProcedure;
    private ImageView ivPic;
    //评论按钮
    private TextView btnComment;
    //点赞和后退按钮
    private ImageView btnLike, btnBack;

    //记录点赞的记录
    private Like like;
    //赞的个数
    private int num;

    //顶部标题栏
    private TextView tvTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_recipe);

        initView();
        loadLike();
        initEvent();

    }

    private void initView() {

        tvName = findViewById(R.id.tvName);
        tvAuthor = findViewById(R.id.tvAuthor);
        tvLike = findViewById(R.id.tvLike);
        tvMateril = findViewById(R.id.tvMaterial);
        tvProcedure = findViewById(R.id.tvProcedure);
        ivPic = findViewById(R.id.ivPic);

        btnComment = findViewById(R.id.btnComment);
        btnLike = findViewById(R.id.btnLike);
        btnBack = findViewById(R.id.btnBack);


        tvTitle = findViewById(R.id.tvTitle);
    }

    private void initEvent() {


        //获取食谱
        final Recipe recipe = (Recipe) getIntent().getSerializableExtra("recipe");
        tvName.setText(recipe.getName());
        tvAuthor.setText("作者:" + recipe.getAuthor());
        tvLike.setText(recipe.getLikeNum()+"");
        tvMateril.setText(recipe.getMaterial());
        tvProcedure.setText(recipe.getProcedure());
        Glide.with(this).load(recipe.getPic().getUrl()).into(ivPic);

        //把食谱名放在顶部标题栏
        tvTitle.setText(recipe.getName());

        //点赞的点击事件
        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取当前用户
                final BmobUser currentUser = BmobUser.getCurrentUser();
                if (currentUser == null) {
                    toast("请登录后再点赞吧");
                } else {

                    //获取当前点赞个数
                    num = recipe.getNum();

                    //查找有没有当前用户
                    BmobQuery<Like> query1 = new BmobQuery<>();
                    query1.addWhereEqualTo("user", new BmobPointer(currentUser));

                    //查找有没有当前食谱
                    BmobQuery<Like> query2 = new BmobQuery<>();
                    final Recipe recipe = (Recipe) getIntent().getSerializableExtra("recipe");
                    query2.addWhereEqualTo("recipe", new BmobPointer(recipe));

                    //合并查询
                    List<BmobQuery<Like>> list = new ArrayList<>();
                    list.add(query1);
                    list.add(query2);
                    BmobQuery<Like> andQuery = new BmobQuery<>();
                    andQuery.and(list);
                    andQuery.findObjects(new FindListener<Like>() {
                        @Override
                        public void done(List<Like> list, BmobException e) {
                            if (e == null) {
                                if (list.size() != 0) {//说明有点赞记录

                                    //获取第一条记录，即是当前的点赞记录
                                    like = list.get(0);
                                    like.delete(new UpdateListener() {
                                        @Override
                                        public void done(BmobException e) {
                                            if (e==null){
                                                toast("取消成功");
                                                num--;
                                                tvLike.setText(num+"");
                                                btnLike.setImageResource(R.mipmap.dislike);
                                            }else {
                                                toast("取消失败,"+e.getMessage());

                                            }
                                        }
                                    });
                                } else {
                                    //说明还没赞过
                                    //创建新数据
                                    Like like = new Like();
                                    like.setUser(currentUser);
                                    like.setRecipe(recipe);
                                    like.save(new SaveListener<String>() {
                                        @Override
                                        public void done(String s, BmobException e) {
                                            if (e == null) {
                                                toast("点赞成功");
                                                num++;
                                                tvLike.setText(num+"");
                                                btnLike.setImageResource(R.mipmap.like);

                                            } else {
                                                toast("点赞失败," + e.getMessage());
                                            }
                                        }
                                    });
                                }
                            } else {
                                System.out.println("====error:" + e.getMessage());
                            }
                        }
                    });


                }
            }
        });


        //发表评论的事件
        btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BmobUser currentUser = BmobUser.getCurrentUser();
                if (currentUser==null){
                    toast("请登录后再进行评论");
                    return;
                }

                Intent intent = new Intent(LearnRecipeActivity.this, CommentActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("recipe", recipe);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        //后退按钮事件
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    //获取点赞数据库，来判断是否已经点赞了
    private void loadLike() {
        //查找有没有当前用户
        BmobQuery<Like> query1 = new BmobQuery<>();
        BmobUser currentUser = BmobUser.getCurrentUser();
        query1.addWhereEqualTo("user", new BmobPointer(currentUser));

        //查找有没有当前食谱
        BmobQuery<Like> query2 = new BmobQuery<>();
        Recipe recipe = (Recipe) getIntent().getSerializableExtra("recipe");
        query2.addWhereEqualTo("recipe", new BmobPointer(recipe));

        //合并查询
        List<BmobQuery<Like>> list = new ArrayList<>();
        list.add(query1);
        list.add(query2);
        BmobQuery<Like> andQuery = new BmobQuery<>();
        andQuery.and(list);
        andQuery.findObjects(new FindListener<Like>() {
            @Override
            public void done(List<Like> list, BmobException e) {
                if (e == null) {
                    if (list.size() != 0) {
                        //说明有点赞记录

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnLike.setImageResource(R.mipmap.like);
                            }
                        });
                    }

                } else {
                    System.out.println("====error:" + e.getMessage());
                }
            }
        });
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
