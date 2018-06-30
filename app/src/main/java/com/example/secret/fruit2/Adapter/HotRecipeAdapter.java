package com.example.secret.fruit2.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.secret.fruit2.R;
import com.example.secret.fruit2.UI.LearnRecipeActivity;
import com.example.secret.fruit2.bean.Collection;
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

/**
 * Created by secret on 18-6-16.
 */

public class HotRecipeAdapter extends RecyclerView.Adapter {

    private List<Recipe> list = new ArrayList<>();
    private Context context;


    //收藏
    public List<Collection> collections = new ArrayList<>();
    private Collection collection;


    public HotRecipeAdapter(Context context) {
        this.context = context;
    }

    //在activity中传入数据
    public void bindData(List<Recipe> list) {
        this.list.clear();
        //判断数据是否为空
        if (!list.isEmpty()) {
            this.list.addAll(list);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new HotRecipeHolder(LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final HotRecipeHolder recipeHolder = (HotRecipeHolder) holder;
        //当前位置的食谱
        final Recipe recipe = list.get(position);
        recipeHolder.tvName.setText(recipe.getName());
        recipeHolder.tvAuthor.setText("作者：" + recipe.getAuthor());
        recipeHolder.tvScore.setText(recipe.getScore() + "分");
        recipeHolder.tvDid.setText(recipe.getNum() + "做过");

        final BmobUser currentUser = BmobUser.getCurrentUser();
        //查看是否已经收藏了
        for (Collection c : collections) {
            if (c.getRecipe().getObjectId().trim().equals(recipe.getObjectId().trim())) {
                //如果能在列表中找到，说明该食谱被收藏了

                collection = c;
                recipeHolder.btnSave.setText("已收藏");
                break;
            }
        }


        //使用glide框架加载图片
        Glide.with(context).load(recipe.getPic().getUrl()).into(recipeHolder.ivPic);

        //“学习”按钮点击事件
        recipeHolder.btnLearn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //过滤掉上一层的点击事件
                recipeHolder.btnLearn.getParent().requestDisallowInterceptTouchEvent(true);
                //跳转到学习界面
                Intent intent = new Intent(context, LearnRecipeActivity.class);
                //使用bundle把菜谱数据传递过去
                Bundle bundle = new Bundle();
                bundle.putSerializable("recipe", recipe);
                intent.putExtras(bundle);
                context.startActivity(intent);
                return false;
            }
        });

        //收藏按钮的点击事件
        recipeHolder.btnSave.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //过滤掉上一层的点击事件
                recipeHolder.btnLearn.getParent().requestDisallowInterceptTouchEvent(true);

                if (currentUser == null) {
                    //如果当前用户为空，即未登录状态，则不能进行该项功能
                    toast("请先登录后再进行收藏");

                } else {
                    BmobQuery<Collection> query1 = new BmobQuery<>();
                    query1.addWhereEqualTo("user", new BmobPointer(currentUser));

                    BmobQuery<Collection> query2 = new BmobQuery<>();
                    query2.addWhereEqualTo("recipe", new BmobPointer(recipe));

                    List<BmobQuery<Collection>> queries = new ArrayList<>();
                    queries.add(query1);
                    queries.add(query2);

                    BmobQuery<Collection> andQuery = new BmobQuery<>();
                    andQuery.and(queries);
                    andQuery.findObjects(new FindListener<Collection>() {
                        @Override
                        public void done(List<Collection> list, BmobException e) {
                            if (e == null) {

                                System.out.println("===list:" + list.size());
                                if (list.size() == 0) {
                                    //当前用户没有收藏这个菜谱
                                    //创建新数据
                                    Collection collection = new Collection();
                                    collection.setUser(currentUser);
                                    collection.setRecipe(recipe);
                                    collection.save(new SaveListener<String>() {
                                        @Override
                                        public void done(String s, BmobException e) {
                                            if (e == null) {
                                                toast("收藏成功");

                                                recipeHolder.btnSave.setText("已收藏");

                                            } else {
                                                toast("收藏失败," + e.getMessage());
                                            }
                                        }
                                    });
                                } else {
                                    //用户收藏了菜谱了
                                    //删除收藏数据库的内容
                                    collection = list.get(0);
                                    collection.delete(new UpdateListener() {
                                        @Override
                                        public void done(BmobException e) {
                                            if (e == null) {
                                                toast("取消收藏");
                                                //控件的更新
                                                recipeHolder.btnSave.setText("收藏");


                                            } else {
                                                toast("取消收藏失败," + e.getMessage());
                                            }
                                        }
                                    });

                                }

                            }
                        }
                    });


                }


                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class HotRecipeHolder extends RecyclerView.ViewHolder {

        //食谱图片
        private ImageView ivPic;
        //食谱信息
        private TextView tvName, tvAuthor, tvScore, tvDid;
        //按钮
        private TextView btnLearn, btnSave;


        public HotRecipeHolder(View itemView) {
            super(itemView);

            ivPic = itemView.findViewById(R.id.ivPic);
            tvName = itemView.findViewById(R.id.tvName);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvScore = itemView.findViewById(R.id.tvScore);
            tvDid = itemView.findViewById(R.id.tvDid);

            btnLearn = itemView.findViewById(R.id.btnLearn);
            btnSave = itemView.findViewById(R.id.btnSave);
        }
    }

    private void toast(String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }
}
