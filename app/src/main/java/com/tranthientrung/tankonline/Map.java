package com.tranthientrung.tankonline;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

public class Map {
    public float unitWidth, unitHeight, unitMiniWidth, unitMiniHeight;
    float screenWidth, screenHeight, screenMiniWidth, screenMiniHeight;
    private float mapWidth, mapHeight;
    public int numGrass = 10;
    public Point showFrom, showTo;
    ImageView imgLeftBorder, imgRightBorder, imgBotBorder, imgTopBorder;
    private int widen;
    private Context context;

    public Map(Context context, float screenWidth, float screenHeight, float screenMiniWidth, float screenMiniHeight, int widen){
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.screenMiniWidth = screenMiniWidth;
        this.screenMiniHeight = screenMiniHeight;
        this.widen = widen;
        this.context = context;


        unitWidth = this.screenWidth / (widen*2);
        unitHeight = this.screenHeight / widen;

        unitMiniWidth = this.screenMiniWidth / 100;
        unitMiniHeight = this.screenMiniHeight / 100;

        this.mapHeight = unitHeight * 100;
        this.mapWidth = unitWidth * 100;
        showFrom = new Point();
        showTo = new Point();


    }



    public void initBorder(ImageView imgBotBorder, ImageView imgTopBorder, ImageView imgLeftBorder, ImageView imgRightBorder){
        this.imgTopBorder = imgTopBorder;
        this.imgBotBorder = imgBotBorder;
        this.imgRightBorder = imgRightBorder;
        this.imgLeftBorder = imgLeftBorder;

        imgBotBorder.setBackgroundColor(Color.BLUE);
        imgTopBorder.setBackgroundColor(Color.BLUE);
        imgLeftBorder.setBackgroundColor(Color.BLUE);
        imgRightBorder.setBackgroundColor(Color.BLUE);

    }
    public void show(Tank tank){
        showFrom.x = tank.x - widen;
        showFrom.y = tank.y - widen/2;
        showTo.x = tank.x +widen;
        showTo.y = tank.y + widen/2;


    }

}
