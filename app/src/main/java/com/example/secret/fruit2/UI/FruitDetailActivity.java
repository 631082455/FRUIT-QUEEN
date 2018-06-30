package com.example.secret.fruit2.UI;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.secret.fruit2.Adapter.HotRecipeAdapter;
import com.example.secret.fruit2.R;
import com.example.secret.fruit2.RecycleViewDivider;
import com.example.secret.fruit2.bean.Collection;
import com.example.secret.fruit2.bean.Fruit;
import com.example.secret.fruit2.bean.Recipe;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class FruitDetailActivity extends AppCompatActivity {

    //菜谱列表视图
    private RecyclerView rv;
    //水果信息
    private TextView tvName, tvIntro, tvNutrition, tvMedicine, tvContrary, tvFit, tvUnfit;
    //了解更多
    private TextView btnMore;
    //水果图片
    private ImageView ivPic;
    //按钮
    private ImageView btnUpload, btnBack;
    //显示更多的布局
    private RelativeLayout layout_more;
    //列表适配器
    private HotRecipeAdapter adapter;
    //食谱列表
    private List<Recipe> recipeList = new ArrayList<>();

    //顶部标题栏
    private TextView tvTitle;

    //水果
    private Fruit fruit;

    //标记是否已经展开更多
    private int flag = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fruit_detail);

        initView();
        initData();
        initEvent();
    }

    //初始化各种控件
    private void initView() {

        rv = findViewById(R.id.rv);

        tvName = findViewById(R.id.tvName);
        tvIntro = findViewById(R.id.tvIntro);
        tvNutrition = findViewById(R.id.tvNutrition);
        tvMedicine = findViewById(R.id.tvMedicine);
        tvContrary = findViewById(R.id.tvContrary);
        tvFit = findViewById(R.id.tvFit);
        tvUnfit = findViewById(R.id.tvUnfit);
        ivPic = findViewById(R.id.ivPic);

        btnMore = findViewById(R.id.btnMore);
        btnUpload = findViewById(R.id.btnUpload);
        btnBack = findViewById(R.id.btnBack);

        layout_more = findViewById(R.id.layout_more);

        adapter = new HotRecipeAdapter(this);

        tvTitle = findViewById(R.id.tvTitle);

    }

    //从数据库取出数据
    private void initData() {
        //取出相关食谱
        fruit = (Fruit) getIntent().getSerializableExtra("fruit");
        BmobQuery<Recipe> query = new BmobQuery<>();
        query.findObjects(new FindListener<Recipe>() {
            @Override
            public void done(List<Recipe> list, BmobException e) {
                if (e == null) {
                    recipeList.clear();
                    for (Recipe recipe : list) {
                        //遍历食谱，把含有该水果的食谱显示出来
                        if (recipe.getName().trim().contains(fruit.getName().trim())) {
                            recipeList.add(recipe);
                        }
                    }
                    adapter.bindData(recipeList);
                    adapter.notifyDataSetChanged();

                } else {
                    toast("食谱加载不出来啦，" + e.getMessage());
                }
            }
        });

        //取出收藏
        BmobUser currentUser = BmobUser.getCurrentUser();
        BmobQuery<Collection> query1 = new BmobQuery<>();
        //查出当前用户的收藏
        query1.addWhereEqualTo("user", new BmobPointer(currentUser));
        query1.findObjects(new FindListener<Collection>() {
            @Override
            public void done(List<Collection> list, BmobException e) {
                if (e == null) {
                    System.out.println("=====list:" + list.size());
                    adapter.collections.addAll(list);
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e("--collection-error", e.getMessage());
                }
            }
        });

    }

    private void initEvent() {

        //食谱列表相关事件
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(this));
        //添加分割线
        rv.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager.HORIZONTAL));

        //把蔬果的信息显示出来
        tvName.setText(fruit.getName());
        tvIntro.setText(fruit.getIntro());
        tvNutrition.setText(fruit.getNutrition());
        tvMedicine.setText(fruit.getMedicine());
        tvContrary.setText(fruit.getContrary());
        tvFit.setText(fruit.getFit());
        tvUnfit.setText(fruit.getUnfit());
        //把顶部标题设为水果的名称
        tvTitle.setText(fruit.getName());

        Glide.with(this).load(fruit.getPic().getUrl()).into(ivPic);



        //点击“了解更多”
        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag == 0) {//说明还没展开

                    //所以接下来是要展开
                    flag = 1;
                    layout_more.setVisibility(View.VISIBLE);
                    tvIntro.setMaxLines(Integer.MAX_VALUE);
                    tvIntro.setEllipsize(null);
                    tvNutrition.setMaxLines(Integer.MAX_VALUE);
                    tvNutrition.setEllipsize(null);
                    tvMedicine.setMaxLines(Integer.MAX_VALUE);
                    tvMedicine.setEllipsize(null);
                    btnMore.setText("收起");
                } else {
                    flag = 0;
                    tvIntro.setMaxLines(3);
                    tvIntro.setEllipsize(TextUtils.TruncateAt.END);
                    tvNutrition.setMaxLines(3);
                    tvIntro.setEllipsize(TextUtils.TruncateAt.END);
                    tvMedicine.setMaxLines(3);
                    tvIntro.setEllipsize(TextUtils.TruncateAt.END);
                    layout_more.setVisibility(View.GONE);
                    btnMore.setText("了解更多");
                }

            }
        });

        //返回按钮的点击事件
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //上传的点击事件
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BmobUser currentUser = BmobUser.getCurrentUser();
                if (currentUser == null) {
                    toast("请登录后再上传食谱吧");
                    return;
                }

                //跳转到上传的界面
                Intent intent = new Intent(FruitDetailActivity.this, UploadActivity.class);
                startActivity(intent);
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
