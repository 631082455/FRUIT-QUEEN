package com.example.secret.fruit2.UI;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.secret.fruit2.Adapter.VegetableAdapter;
import com.example.secret.fruit2.R;
import com.example.secret.fruit2.RecycleViewDivider;
import com.example.secret.fruit2.RecyclerItemClickListener;
import com.example.secret.fruit2.bean.Fruit;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class VegetableActivity extends AppCompatActivity {

    private RecyclerView rv;

    private VegetableAdapter adapter;

    //获取果蔬列表
    private List<Fruit> fruitList = new ArrayList<>();

    //顶部标题栏
    private TextView tvTitle;

    //后退按钮
    private ImageView btnBack;

    //搜索框
//    private SearchView searchView;


    private EditText etSearch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vegetable);

        initView();
        initData();
        initEvent();
    }

    private void initView(){

        rv = findViewById(R.id.rv);

        tvTitle = findViewById(R.id.tvTitle);

        btnBack = findViewById(R.id.btnBack);

//        searchView = findViewById(R.id.search);




        etSearch = findViewById(R.id.etSearch);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!TextUtils.isEmpty(etSearch.getText().toString())){
                    final List<Fruit> filteredDataList;
                    filteredDataList=new ArrayList<>();
                    BmobQuery<Fruit> query = new BmobQuery<>();
                    query.findObjects(new FindListener<Fruit>() {
                        @Override
                        public void done(List<Fruit> list, BmobException e) {
                            if (e==null){
                                for (Fruit fruit :list){

                                    String name = fruit.getName();
                                    if (name.toLowerCase().contains(etSearch.getText().toString().toLowerCase())){
                                        filteredDataList.add(fruit);
                                    }


                                }
                                System.out.println("Count:"+filteredDataList.size());
                                adapter.setFilter(filteredDataList);
                            }
                        }
                    });

                }else {
                    adapter.setFilter(fruitList);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
        etSearch.addTextChangedListener(watcher);

    }

    private void initData(){
        BmobQuery<Fruit> query = new BmobQuery<>();
        query.findObjects(new FindListener<Fruit>() {
            @Override
            public void done(List<Fruit> list, BmobException e) {
                if (e==null){
                    //更新数据
                    adapter.bindData(list);
                    adapter.notifyDataSetChanged();
                    fruitList.clear();
                    fruitList.addAll(list);
                }
            }
        });
    }

    private void initEvent(){

        tvTitle.setText("蔬果列表");

        //recyclerview相关事件
        adapter = new VegetableAdapter(this);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager.HORIZONTAL));
        rv.addOnItemTouchListener(new RecyclerItemClickListener(this, rv, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Fruit fruit = fruitList.get(position);
                Intent intent = new Intent(VegetableActivity.this,FruitDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("fruit",fruit);
                intent.putExtras(bundle);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        }));

        //后退按钮点击事件
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //搜索框事件
//        if(searchView == null) { return;}
//
//        try {        //--拿到字节码
//            Class<?> argClass = searchView.getClass();
//            //--指定某个私有属性,mSearchPlate是搜索框父布局的名字
//            Field ownField = argClass.getDeclaredField("mSearchPlate");
//            //--暴力反射,只有暴力反射才能拿到私有属性
//            ownField.setAccessible(true);
//            View mView = (View) ownField.get(searchView);
//            //--设置背景
//            mView.setBackgroundResource(R.drawable.btn_search);
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//        int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
//        TextView textView = searchView.findViewById(id);
//        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);//该表搜索框字体大小
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(final String s) {
//
//
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(final String s) {
//                if (!TextUtils.isEmpty(s)){
//                    final List<Fruit> filteredDataList;
//                    filteredDataList=new ArrayList<>();
//                    BmobQuery<Fruit> query = new BmobQuery<>();
//                    query.findObjects(new FindListener<Fruit>() {
//                        @Override
//                        public void done(List<Fruit> list, BmobException e) {
//                            if (e==null){
//                                for (Fruit fruit :list){
//
//                                    String name = fruit.getName();
//                                    if (name.toLowerCase().contains(s.toLowerCase())){
//                                        filteredDataList.add(fruit);
//                                    }
//
//
//                                }
//                                System.out.println("Count:"+filteredDataList.size());
//                                adapter.setFilter(filteredDataList);
//                            }
//                        }
//                    });
//
//                }else {
//                    adapter.setFilter(fruitList);
//                }
//                return true;
//            }
//        });


    }
}
