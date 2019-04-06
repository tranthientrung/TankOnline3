package com.tranthientrung.tankonline;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class Tank {
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int UP = 3;
    public static final int DOWN = 4;
    public static final int MAX_BULLET=50;
    public static final int MAX_HEART=100;
    public int trend;
    public int x,y; //toa do tank
    public int v; //van toc tank
    public boolean move, shoot; //xe co di chuyen khong
    public float width, height;
    private ArrayList<Item> arrBullet;
    private Socket mSocket;
    public boolean load=true;
    private Map map;
    private ImageView imgTank;
    public int bullet, heart;
    private Button btnShoot;
    private TextView txtBullet, txtHeart, txtCurrPlayer;
    private int currPlayer, maxPlayer;
    private Context context;
    public Tank(Context context, ImageView imgTank,Button btnShoot, Map map, int trend, int x, int y, int v, TextView txtBullet, TextView txtHeart, TextView txtCurrPlayer, ArrayList<Item> arrBullet,int maxPlayer, Socket mSocket) {
        this.context = context;
        this.trend = trend;
        this.btnShoot = btnShoot;
        this.x = x;
        this.y = y;
        this.v = v;
        this.arrBullet = arrBullet;

        move =false;
        shoot = true;

        this.imgTank = imgTank;
        this.map = map;
        width = map.unitWidth;
        height = width;
        imgTank.getLayoutParams().height= (int) height;
        imgTank.getLayoutParams().width= (int) width;
        imgTank.requestLayout();

        bullet = 30;
        heart = 100;
        this.currPlayer = maxPlayer;
        this.maxPlayer = maxPlayer;
        this.txtBullet = txtBullet;
        this.txtHeart = txtHeart;
        this.txtCurrPlayer = txtCurrPlayer;

        this.txtCurrPlayer.setText(this.currPlayer+"/"+this.maxPlayer);
        this.txtBullet.setText(bullet+"");
        this.txtHeart.setText(heart+"");
        this.mSocket = mSocket;

        this.mSocket.on("serverSendShoot",updateArrBullet);


    }


    private Emitter.Listener updateArrBullet = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            int position = (int) args[0];
            int x = (int)args[1];
            int y = (int)args[2];
            int trend = (int)args[3];
            arrBullet.get(position-1).function  = trend;
            arrBullet.get(position-1).position.x = x;
            arrBullet.get(position-1).position.y = y;
        }
    };

    public void shoot(final int roomID, final int position, final int trend, int x, int y){
        mSocket.emit("clientSendShoot", roomID, position,x,y,trend);
//        arrBullet.get(position-1).function  = trend;
//        arrBullet.get(position-1).position.x = x;
//        arrBullet.get(position-1).position.y = y;
        int flyTime;
        if(trend == UP || trend == DOWN){
            flyTime = 501;
        }else{
            flyTime = 1001;
        }
        bullet--;
        txtBullet.setText(bullet+"");
        btnShoot.setEnabled(false);
        final CountDownTimer timer = new CountDownTimer(flyTime,flyTime) {
            @Override
            public void onTick(long l) {

            }
            @Override
            public void onFinish() {
                mSocket.emit("clientSendShoot",roomID,position,-100,-100,trend);
                btnShoot.setEnabled(true);
                arrBullet.get(position-1).position.x = -100;
                arrBullet.get(position-1).position.x = -100;

            }
        };
        timer.start();
    }
    public void pickBullet(){
        bullet+=Item.BULLET;
        if(bullet > MAX_BULLET)
            bullet = MAX_BULLET;
        txtBullet.setText(bullet+"");
    }
    public void pickHeart(){
        heart+=Item.HEART;
        if (heart > MAX_HEART)
            heart = MAX_HEART;
        txtHeart.setText(heart+"");
    }
    public void hitBullet(int roomID, int bulletPosition,int tankPosition){

        heart-=10;
        txtHeart.setText(heart+"");
        mSocket.emit("clientSendShoot",roomID,bulletPosition,-100,-100,this.trend);
        if(heart <= 0){
            mSocket.emit("clientSendLose",roomID,tankPosition,this.trend);
            txtHeart.setText("0");
            if(this.trend == UP){
                imgTank.setImageResource(R.drawable.tankuplose);
            }else if(this.trend == RIGHT){
                imgTank.setImageResource(R.drawable.tankrightlose);
            }else if(this.trend == DOWN){
                imgTank.setImageResource(R.drawable.tankdownlose);
            }else if(this.trend == LEFT){
                imgTank.setImageResource(R.drawable.tankleftlose);
            }
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle("You losed!!!");
            dialog.setMessage("RANK: "+currPlayer+" / "+maxPlayer);
            dialog.setCancelable(false);
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent roomList = new Intent(((MainActivity)context),RoomListActivity.class);
                    ((Activity)context).startActivity(roomList);
                    ((Activity)context).finish();
                }
            });
            dialog.show();
        }

    }

    public void lose(final int roomID){
        currPlayer--;
        txtCurrPlayer.setText(currPlayer+"/"+maxPlayer);
        if(currPlayer == 1){
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle("You win!!!");
            dialog.setMessage("RANK: "+currPlayer+" / "+maxPlayer);
            dialog.setCancelable(false);
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent roomList = new Intent(((MainActivity)context),RoomListActivity.class);
                    ((Activity)context).startActivity(roomList);
                    ((Activity)context).finish();
                    mSocket.emit("clientSendWin",roomID);
                }
            });
            dialog.show();
        }
    }

}
