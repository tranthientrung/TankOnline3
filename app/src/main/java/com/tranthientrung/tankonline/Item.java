package com.tranthientrung.tankonline;

import android.content.Context;
import android.graphics.Point;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class Item {
    public static final int BULLET = 5;
    public static final int HEART = 20;
    public static final int NUM_BULLET_FOR_A_PLAYER = 3;
    public static final int NUM_HEART_FOR_A_PLAYER = 3;
    public static final int NUM_GRASS= 10;
    public static final int TOTAL_ITEMS_FOR_A_PAYER = NUM_BULLET_FOR_A_PLAYER + NUM_BULLET_FOR_A_PLAYER + NUM_GRASS;
    public String name;
    public int function;
    public Point position;
    public boolean show;
    public ImageView imgItem;
    public Boolean me;

    public Item(String name, int function, Point position, ImageView imgItem){
        this.function = function;
        this.name = name;
        this.position = position;
        this.imgItem = imgItem;
        show = false;
        this.me = false;

    }
    public Item(Boolean me){
        this.me = me;
    }
}
