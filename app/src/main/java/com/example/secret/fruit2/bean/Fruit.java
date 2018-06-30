package com.example.secret.fruit2.bean;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

/**
 * 存储蔬果信息
 */

public class Fruit extends BmobObject {

    //水果名
    private String name;
    //水果介绍
    private String intro;
    //水果营养价值
    private String nutrition;
    //药用价值
    private String medicine;
    //相克
    private String contrary;
    //适合人群
    private String fit;
    //不适合人群
    private String unfit;
    //水果图片
    private BmobFile pic;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getNutrition() {
        return nutrition;
    }

    public void setNutrition(String nutrition) {
        this.nutrition = nutrition;
    }

    public String getMedicine() {
        return medicine;
    }

    public void setMedicine(String medicine) {
        this.medicine = medicine;
    }

    public String getContrary() {
        return contrary;
    }

    public void setContrary(String contrary) {
        this.contrary = contrary;
    }

    public String getFit() {
        return fit;
    }

    public void setFit(String fit) {
        this.fit = fit;
    }

    public String getUnfit() {
        return unfit;
    }

    public void setUnfit(String unfit) {
        this.unfit = unfit;
    }

    public BmobFile getPic() {
        return pic;
    }

    public void setPic(BmobFile pic) {
        this.pic = pic;
    }
}
