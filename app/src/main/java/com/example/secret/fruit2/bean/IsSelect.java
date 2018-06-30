package com.example.secret.fruit2.bean;

/**
 * 存储数据，判断该收藏是否被选中
 */

public class IsSelect<T> {

    private T type;

    private boolean isSelected;

    public T getType() {
        return type;
    }

    public void setType(T type) {
        this.type = type;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
