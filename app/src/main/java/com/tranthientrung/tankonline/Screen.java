package com.tranthientrung.tankonline;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;

import android.graphics.Point;
import android.os.CountDownTimer;
import android.os.Handler;


import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;


import java.util.ArrayList;

public class Screen extends Activity {

    Tank tank;
    Map map;
    Context context;
    Paint paint;
    RelativeLayout layoutMap;
    ImageView imgTank, imgPoint;
    ArrayList<Item> arrItem;
    ArrayList<Item> arrBullet;
    Button btnUp, btnRight, btnLeft, btnDown, btnShoot;
    Socket mSocket;
    private Player me;
    private int roomID;


    public static float dpFromPx(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }
    public Screen(Context context, RelativeLayout layoutMap, Tank tank, Map map, ImageView imgPoint, ArrayList<Item> arrItem, ArrayList<Item> arrBullet, com.github.nkzawa.socketio.client.Socket mSocket, Player me, int roomID) {
        this.me = me;
        this.roomID = roomID;
        this.mSocket = mSocket;
        this.layoutMap = layoutMap;
        this.tank = tank;
        this.map = map;
        this.context = context;
        this.imgPoint = imgPoint;
        this.arrItem = arrItem;
        this.arrBullet = arrBullet;

        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);

        imgTank = null;

        mSocket.on("serverSendTankPosition",updateTankPosition);
        mSocket.on("serverSendPickItem",pickItem);
        showDialog();
        update();
    }

    private void showDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        builder.setTitle("How to win");
        builder.setMessage("Become final survivor\nOr\nCollect 5 diamonds");
        builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();

    }
    private Emitter.Listener pickItem = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            final int position = (int)args[0];
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    arrItem.get(position).imgItem.setVisibility(View.GONE);
                    arrItem.remove(position);
                }
            });

        }
    };

    private Emitter.Listener updateTankPosition = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            int position, x, y, trend;

            x = (int)args[0];
            y = (int)args[1];
            position = (int)args[2];
            if(position != me.getPosition()){
                arrItem.get(position-1).position=new Point(x,y);
            }

        }
    };



    private void draw(){


        imgPoint.setX(tank.x * map.unitMiniWidth);
        imgPoint.setY(tank.y * map.unitMiniHeight);
        imgPoint.requestLayout();

        map.imgLeftBorder.getLayoutParams().width = (int)((map.unitWidth * (-1)*map.showFrom.x) - (tank.width/2));
        map.imgLeftBorder.requestLayout();
        map.imgRightBorder.getLayoutParams().width = (int)((map.unitWidth * (map.showTo.x-90)) - (tank.width/2));
        map.imgRightBorder.requestLayout();
        map.imgTopBorder.getLayoutParams().height = (int)((map.unitHeight * (-1)*map.showFrom.y) - (tank.height/2));
        map.imgTopBorder.requestLayout();
        map.imgBotBorder.getLayoutParams().height = (int)((map.unitHeight * (map.showTo.y-90)) - (tank.height/2));
        map.imgBotBorder.requestLayout();

        for(int i=0; i<arrItem.size(); i++){
            if(!arrItem.get(i).me){
                int x=arrItem.get(i).position.x;
                int y=arrItem.get(i).position.y;
                if(x>=map.showFrom.x && x<=map.showTo.x && y>=map.showFrom.y && y<=map.showTo.y){
                    arrItem.get(i).imgItem.setX((x-map.showFrom.x)*map.unitWidth - arrItem.get(i).imgItem.getWidth()/2 );
                    arrItem.get(i).imgItem.setY((y-map.showFrom.y)*map.unitHeight - arrItem.get(i).imgItem.getHeight()/2);
                    arrItem.get(i).imgItem.setVisibility(View.VISIBLE);
                    arrItem.get(i).imgItem.requestLayout();
                }else{
                    arrItem.get(i).imgItem.setVisibility(View.INVISIBLE);
                    arrItem.get(i).imgItem.requestLayout();
                }

                if(x == tank.x && y == tank.y){

                    if(arrItem.get(i).name == "BULLET" && tank.bullet < tank.MAX_BULLET){
                        arrItem.get(i).imgItem.setVisibility(View.GONE);
                        tank.pickBullet();
                        mSocket.emit("clientSendPickItem", roomID, i);
                    }else if(arrItem.get(i).name == "HEART" && tank.heart < tank.MAX_HEART){
                        arrItem.get(i).imgItem.setVisibility(View.GONE);
                        tank.pickHeart();
                        mSocket.emit("clientSendPickItem", roomID, i);
                    }
                }
            }

        }

        for(int i=0; i<arrBullet.size(); i++){
            int x = arrBullet.get(i).position.x;
            int y = arrBullet.get(i).position.y;
            if(x>=map.showFrom.x && x<=map.showTo.x && y>=map.showFrom.y && y<=map.showTo.y){
                arrBullet.get(i).imgItem.setX((arrBullet.get(i).position.x - map.showFrom.x)*map.unitWidth - arrBullet.get(i).imgItem.getLayoutParams().width/2);
                arrBullet.get(i).imgItem.setY((arrBullet.get(i).position.y - map.showFrom.y)*map.unitHeight - arrBullet.get(i).imgItem.getLayoutParams().height/2);
                arrBullet.get(i).imgItem.setVisibility(View.VISIBLE);
            }else{
                arrBullet.get(i).imgItem.setVisibility(View.INVISIBLE);
            }
            if(tank.x == x && tank.y == y ){
                tank.hitBullet(roomID,i+1,me.getPosition());
            }

        }


    }
    private void update() {
        final Handler update = new Handler();
        update.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (tank.move) {
                    int id = tank.trend;
                    switch (id) {
                        case Tank.DOWN:
                            if (tank.y + tank.v > 90) {
                                tank.y = 90;

                            } else {
                                tank.y += tank.v;
                            }
                            break;
                        case Tank.UP:

                            if (tank.y - tank.v < 0) {
                                tank.y = 0;

                            } else {
                                tank.y -= tank.v;
                            }
                            break;
                        case Tank.LEFT:

                            if (tank.x - tank.v < 0) {
                                tank.x = 0;

                            } else {
                                tank.x -= tank.v;
                            }
                            break;
                        case Tank.RIGHT:

                            if (tank.x + tank.v > 90) {
                                tank.x = 90;

                            } else {
                                tank.x += tank.v;
                            }

                            break;
                    }
                    mSocket.emit("clientSendTankPosition",roomID, tank.x, tank.y, me.getPosition());
                }

                map.show(tank);
                for(int i=0; i < arrBullet.size(); i++){
                        switch (arrBullet.get(i).function){
                            case Tank.DOWN:
                                arrBullet.get(i).position.y+=2;
                                if(arrBullet.get(i).position.y-1 == tank.y && arrBullet.get(i).position.x == tank.x){
                                    arrBullet.get(i).position.y-=1;
                                }
                                break;
                            case Tank.UP:
                                arrBullet.get(i).position.y-=2;
                                if(arrBullet.get(i).position.y+1 == tank.y && arrBullet.get(i).position.x == tank.x){
                                    arrBullet.get(i).position.y+=1;
                                }
                                break;
                            case Tank.LEFT:
                                arrBullet.get(i).position.x-=2;
                                if(arrBullet.get(i).position.y == tank.y && arrBullet.get(i).position.x+1 == tank.x){
                                    arrBullet.get(i).position.x+=1;
                                }
                                break;
                            case Tank.RIGHT:
                                arrBullet.get(i).position.x+=2;
                                if(arrBullet.get(i).position.y == tank.y && arrBullet.get(i).position.x-1 == tank.x){
                                    arrBullet.get(i).position.x-=1;
                                }
                                break;
                        }

                }

                draw();
                update.postDelayed(this, 200);
            }
        }, 200);
    }

}

