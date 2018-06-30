package com.example.secret.fruit2.bean;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobUser;

/**
 * Created by Administrator on 2016/10/14.
 * 父评论，即不是回复的评论
 */
public class Comment extends BmobObject {
    private String Commentcontent;
    private BmobUser user;
    private Recipe recipe;
    private Integer count;


    public void setCommentcontent(String commentcontent){
        this.Commentcontent = commentcontent;
    }
    public String getCommentcontent(){
        return Commentcontent;
    }
    public void setUser(BmobUser user){
        this.user = user;
    }
    public BmobUser getUser(){
        return user;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }


    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}