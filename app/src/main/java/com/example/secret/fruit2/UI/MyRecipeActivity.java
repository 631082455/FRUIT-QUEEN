package com.example.secret.fruit2.UI;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.secret.fruit2.Adapter.MyRecipeAdapter;
import com.example.secret.fruit2.R;
import com.example.secret.fruit2.RecycleViewDivider;
import com.example.secret.fruit2.RecyclerItemClickListener;
import com.example.secret.fruit2.bean.IsSelect;
import com.example.secret.fruit2.bean.Recipe;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobBatch;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BatchResult;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListListener;

public class MyRecipeActivity extends CollectActivity {
    private RecyclerView rv;

    private MyRecipeAdapter adapter;

    private ImageView btnBack;

    //顶部布局
    private RelativeLayout layout_top;

    //选择删除的布局
    private LinearLayout bottom_delete;
    private RelativeLayout top_select;
    //删除的相关控件
    private TextView tvCancel, tvSelectAll, tvCancelAll, tvCount;
    private LinearLayout btnDelete;

    //存储数据
    private List<IsSelect<Recipe>> collections = new ArrayList<>();

    //判断是否长按
    private boolean isLongClick = false;

    //顶部标题栏
    private TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect);

        initView();
        initData();
        initEvent();
    }

    private void initView() {

        layout_top = findViewById(R.id.top);

        rv = findViewById(R.id.rv);

        btnBack = findViewById(R.id.btnBack);

        bottom_delete = findViewById(R.id.bottom_delete);
        top_select = findViewById(R.id.top_select);

        tvCancel = findViewById(R.id.tvCancel);
        tvCancelAll = findViewById(R.id.tvCancelAll);
        tvCount = findViewById(R.id.tvCount);
        tvSelectAll = findViewById(R.id.tvSelectAll);
        btnDelete = findViewById(R.id.btnDelete);

        tvTitle = findViewById(R.id.tvTitle);
    }

    /**
     *
     */
    private void initData() {

        BmobUser currentUser = BmobUser.getCurrentUser();
        BmobQuery<Recipe> query = new BmobQuery<>();
        //找寻菜谱数据库中，当前用户所上传的食谱
        query.addWhereEqualTo("user", new BmobPointer(currentUser));
        //使用include来获得对象的具体数据
       // query.include("recipe");
        //按时间降序
        query.order("-createdAt");
        query.findObjects(new FindListener<Recipe>() {
            @Override
            public void done(List<Recipe> list, BmobException e) {
                if (e == null) {
                    System.out.println("====size:" + list.size());
                    for (Recipe recipe : list) {
                        IsSelect<Recipe> isSelect = new IsSelect<>();
                        isSelect.setType(recipe);
                        collections.add(isSelect);
                        System.out.println("===type:" + recipe.getName());
                    }
                    adapter.bindData(collections);
                    adapter.notifyDataSetChanged();

                }
            }
        });
    }

    private void initEvent() {

        tvTitle.setText("我的菜谱");

        adapter = new MyRecipeAdapter(this);
        adapter.setListener(this);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager.HORIZONTAL));
        rv.addOnItemTouchListener(new RecyclerItemClickListener(this, rv, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //判断是否长按
                if (isLongClick) {
                    if (collections.get(position).isSelected()) {
                        //判断是否选择了
                        collections.get(position).setSelected(false);
                    } else {
                        collections.get(position).setSelected(true);
                    }

                    adapter.notifyItemChanged(position);

                }

            }

            @Override
            public void onItemLongClick(View view, int position) {
                //实现长按多选
                isLongClick = true;
                adapter.isShow = true;
                adapter.notifyDataSetChanged();

                layout_top.setVisibility(View.GONE);
                top_select.setVisibility(View.VISIBLE);
                bottom_delete.setVisibility(View.VISIBLE);

                if (!adapter.isCheckBoxPress) {

                    collections.get(position).setSelected(true);
                    adapter.notifyItemChanged(position);
                }
            }
        }));

        //全选
        tvSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.selectedItems.clear();
                for (IsSelect<Recipe> isSelect : collections) {
                    isSelect.setSelected(true);
                }
                adapter.notifyDataSetChanged();
                tvCancelAll.setVisibility(View.VISIBLE);
                tvSelectAll.setVisibility(View.GONE);
            }
        });
        //全不选
        tvCancelAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (IsSelect<Recipe> isSelect : adapter.selectedItems) {
                    isSelect.setSelected(false);
                }
                adapter.notifyDataSetChanged();
                tvCancelAll.setVisibility(View.GONE);
                tvSelectAll.setVisibility(View.VISIBLE);
            }
        });
        //取消选择
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (IsSelect<Recipe> isSelect : adapter.selectedItems) {
                    isSelect.setSelected(false);
                }
                adapter.isShow = false;
                top_select.setVisibility(View.GONE);
                bottom_delete.setVisibility(View.GONE);
                layout_top.setVisibility(View.VISIBLE);
                adapter.selectedItems.clear();
                adapter.notifyDataSetChanged();
            }
        });

        //删除的点击事件
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<BmobObject> deleteCollections = new ArrayList<>();
                for (IsSelect<Recipe> isSelect : adapter.selectedItems) {
                    deleteCollections.add(isSelect.getType());
                }
                //批量删除
                new BmobBatch().deleteBatch(deleteCollections).doBatch(new QueryListListener<BatchResult>() {
                    @Override
                    public void done(List<BatchResult> list, BmobException e) {
                        if (e == null) {
                            Toast.makeText(MyRecipeActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MyRecipeActivity.this, "删除失败," + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        collections.removeAll(adapter.selectedItems);
                        adapter.bindData(collections);
                        adapter.selectedItems.clear();
                        adapter.notifyDataSetChanged();
                        bottom_delete.setVisibility(View.GONE);
                        top_select.setVisibility(View.GONE);
                        layout_top.setVisibility(View.VISIBLE);
                        adapter.isShow = false;
                    }
                });
            }
        });


        //后退按钮点击事件
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //显示已选择几项
    @Override
    public void setCheckChanged() {
        tvCount.setText("已选择" + String.valueOf(adapter.selectedItems.size()) + "个");
    }

    //按手机的后退键，取消选择
    @Override
    public void onBackPressed() {


        if (adapter.isShow) {
            for (IsSelect<Recipe> isSelect : adapter.selectedItems) {
                isSelect.setSelected(false);
            }
            adapter.isShow = false;
            top_select.setVisibility(View.GONE);
            bottom_delete.setVisibility(View.GONE);
            layout_top.setVisibility(View.VISIBLE);
            adapter.selectedItems.clear();
            adapter.notifyDataSetChanged();
        }

        super.onBackPressed();
    }
}
