package com.example.secret.fruit2.bean;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobUser;

/**
 * 存储收藏信息
 */

public class Collection extends BmobObject {

    //绑定用户
    private BmobUser user;
    //食谱
    private Recipe recipe;

    public BmobUser getUser() {
        return user;
    }

    public void setUser(BmobUser user) {
        this.user = user;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }
}
