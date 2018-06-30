package com.example.secret.fruit2.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.secret.fruit2.R;
import com.example.secret.fruit2.UI.LearnRecipeActivity;
import com.example.secret.fruit2.bean.IsSelect;
import com.example.secret.fruit2.bean.Recipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by secret on 18-6-17.
 */

public class MyRecipeAdapter extends RecyclerView.Adapter {

    //收藏列表数据
    private List<IsSelect<Recipe>> list = new ArrayList<>();

    //是否已经选中的收藏列表
    public List<IsSelect<Recipe>> selectedItems = new ArrayList<>();

    private Context context;

    //是否出现checkbox
    public boolean isShow = false;

    //是否已经点击了checkbox
    public boolean isCheckBoxPress = false;

    //监听checkbox是否被选中
    private CollectAdapter.CheckBoxChangedListener listener;

    public MyRecipeAdapter(Context context) {
        this.context = context;
    }

    public void bindData(List<IsSelect<Recipe>> list) {
        this.list.clear();
        if (!list.isEmpty()) {

            this.list.addAll(list);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyRecipeHolder(LayoutInflater.from(context).inflate(R.layout.item_collect, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final MyRecipeHolder recipeHolder = (MyRecipeHolder) holder;

        final Recipe recipe = list.get(position).getType();
        //显示菜谱信息
        recipeHolder.tvName.setText(recipe.getName());
        recipeHolder.tvAuthor.setText(recipe.getAuthor());
        recipeHolder.tvScore.setText(recipe.getScore() + "分");
        recipeHolder.tvDid.setText(recipe.getNum() + "人做过");
        //加载图片
        Glide.with(context).load(recipe.getPic().getUrl()).into(recipeHolder.ivPic);
        //判断是否同一天，如果是同一天就不用显示日期
        if (position==0){
            //如果是第一条数据的话就直接显示
            recipeHolder.tvTime.setText(format(list.get(position).getType().getCreatedAt()));
            recipeHolder.layout_time.setVisibility(View.VISIBLE);
        }else {
            if (isSameDay(position)){
                recipeHolder.layout_time.setVisibility(View.GONE);
            }else {
                recipeHolder.tvTime.setText(format(list.get(position).getType().getCreatedAt()));
                recipeHolder.layout_time.setVisibility(View.VISIBLE);
            }
        }


        //学习按钮点击事件
        recipeHolder.btnLearn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //过滤掉上一层的点击事件
                recipeHolder.btnLearn.getParent().requestDisallowInterceptTouchEvent(true);
                //跳转到学习界面
                Intent intent = new Intent(context, LearnRecipeActivity.class);
                //使用bundle把菜谱数据传递过去
                Bundle bundle = new Bundle();
                bundle.putSerializable("recipe",recipe);
                intent.putExtras(bundle);
                context.startActivity(intent);
                return false;
            }
        });

        //长按后的事件
        //判断该条目有没有被选中
        if (list.get(position).isSelected()){
            recipeHolder.checkBox.setChecked(true);
            selectedItems.add(list.get(position));
            recipeHolder.checkBox.setBackground(context.getResources().getDrawable(R.mipmap.select));
        }
        else {
            recipeHolder.checkBox.setChecked(false);
            recipeHolder.checkBox.setBackground(context.getResources().getDrawable(R.mipmap.not_select));
            selectedItems.remove(list.get(position));
        }
        listener.setCheckChanged();

        //判断是否要出现checkbox
        if (isShow){
            recipeHolder.checkBox.setVisibility(View.VISIBLE);
            recipeHolder.btnLearn.setVisibility(View.GONE);

            recipeHolder.checkBox.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    //过滤上一层的点击事件
                    recipeHolder.checkBox.getParent().requestDisallowInterceptTouchEvent(true);
                    if (recipeHolder.checkBox.isChecked()){
                        if (event.getAction() == MotionEvent.ACTION_UP ){
                            list.get(position).setSelected(false);
                            isCheckBoxPress = true;
                        }
                    }
                    else {
                        if (event.getAction() == MotionEvent.ACTION_UP ){
                            list.get(position).setSelected(true);
                            isCheckBoxPress = true;
                        }
                    }

                    return false;
                }
            });
        }
        else {
            recipeHolder.checkBox.setVisibility(View.GONE);
            recipeHolder.btnLearn.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setListener(CollectAdapter.CheckBoxChangedListener listener) {
        this.listener = listener;
    }

    public interface CheckBoxChangedListener {
        void setCheckChanged();
    }

    //判断是否同一天
    private boolean isSameDay(int position) {

        String t1 = list.get(position).getType().getCreatedAt();
        String t2 = list.get(position - 1).getType().getCreatedAt();
        return format(t1).equals(format(t2));


    }

    //只判断年月日
    private String format(String time) {
        String y = time.substring(0, 4);
        String m = time.substring(5, 7);
        String d = time.substring(8, 10);
        String day = y + "-" + m + "-" + d;
        return day;
    }

    class MyRecipeHolder extends RecyclerView.ViewHolder {
        //菜谱信息
        private TextView tvName, tvAuthor, tvScore, tvDid, tvTime;
        private ImageView ivPic;
        //学习按钮
        private TextView btnLearn;
        //时间布局
        private LinearLayout layout_time;
        //是否选中
        private CheckBox checkBox;

        public MyRecipeHolder(View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvScore = itemView.findViewById(R.id.tvScore);
            tvDid = itemView.findViewById(R.id.tvDid);
            ivPic = itemView.findViewById(R.id.ivPic);
            btnLearn = itemView.findViewById(R.id.btnLearn);
            layout_time = itemView.findViewById(R.id.layout_time);
            tvTime = itemView.findViewById(R.id.tvTime);

            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }
}
