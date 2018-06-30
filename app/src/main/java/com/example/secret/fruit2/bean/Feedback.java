package com.example.secret.fruit2.bean;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobUser;

/**
 * Created by Secret on 2018/6/17.
 */

public class Feedback extends BmobObject{

    //记录用户
    private BmobUser user;
    //记录反馈内容
    private String content;

    public BmobUser getUser() {
        return user;
    }

    public void setUser(BmobUser user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
