package com.example.secret.fruit2.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.secret.fruit2.R;
import com.example.secret.fruit2.bean.Fruit;

import java.util.ArrayList;
import java.util.List;

public class VegetableAdapter extends RecyclerView.Adapter {

    //蔬果列表
    private List<Fruit> list = new ArrayList<>();

    private Context context;

    public VegetableAdapter(Context context) {
        this.context = context;
    }

    public void bindData(List<Fruit> list) {
        this.list.clear();
        if (!list.isEmpty()) {
            this.list.addAll(list);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new VegetableHolder(LayoutInflater.from(context).inflate(R.layout.item_vegetable, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

        VegetableHolder vegetableHolder = (VegetableHolder) viewHolder;
        //获取蔬果
        Fruit fruit = list.get(i);
        //显示蔬果信息
        vegetableHolder.tvName.setText(fruit.getName());
        vegetableHolder.tvIntro.setText(fruit.getIntro());
        Glide.with(context).load(fruit.getPic().getUrl()).into(((VegetableHolder) viewHolder).ivPic);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    //搜索时过滤掉不相关的
    public void setFilter(List<Fruit> FilteredDataList) {
        bindData(FilteredDataList);
        notifyDataSetChanged();
    }

    class VegetableHolder extends RecyclerView.ViewHolder {

        private ImageView ivPic;
        private TextView tvName;
        private TextView tvIntro;

        public VegetableHolder(@NonNull View itemView) {
            super(itemView);

            ivPic = itemView.findViewById(R.id.ivPic);
            tvIntro = itemView.findViewById(R.id.tvIntro);
            tvName = itemView.findViewById(R.id.tvName);

        }
    }
}
