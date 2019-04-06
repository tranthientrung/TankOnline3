package com.tranthientrung.tankonline;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends Activity implements View.OnTouchListener, View.OnClickListener {

    private ImageView imgTank, imgPoint, imgTopBorder, imgBotBorder, imgLeftBorder, imgRightBorder;
    private Button btnUp, btnDown, btnRight, btnLeft, btnShoot;
    private Tank tank;
    private Map map;
    private RelativeLayout layoutMiniMap, layoutMap, layoutFloor;
    private Screen screen;
    private Item item;
    private ArrayList<Player> arrPlayer;
    private ArrayList<Point> arrPoint;
    private int numTank;
    private ArrayList<Item> arrItem;
    private TextView txtBullet, txtHeart, txtCurrPlayer;
    private ArrayList<Item> arrBullet;
    private int roomID, create;
    private MySocket mySocket;
    private Player me;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);



        mapping();
        onClick();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        float px=Screen.pxFromDp(MainActivity.this,100);
        map = new Map(MainActivity.this, size.x, size.y,px,px,10);
        map.initBorder(imgBotBorder,imgTopBorder,imgLeftBorder,imgRightBorder);


        arrBullet = new ArrayList<>();



        arrItem=initArrItem(numTank);

        screen = new Screen(MainActivity.this,layoutMap, tank, map, imgPoint,arrItem, arrBullet, mySocket.socket, me, roomID);

        mySocket.socket.on("serverSendTurn",updateTurn);
        mySocket.socket.on("serverSendLose",updateLose);
    }

    private Emitter.Listener updateLose = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final int position = (int) args[0];
            final int trend = (int)args[1];
            if(position != me.getPosition()){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tank.lose(roomID);
                        if(trend == Tank.UP){
                            arrItem.get(position-1).imgItem.setImageResource(R.drawable.tankuplose);
                        }else if(trend == Tank.RIGHT){
                            arrItem.get(position-1).imgItem.setImageResource(R.drawable.tankrightlose);
                        }else if(trend == Tank.DOWN){
                            arrItem.get(position-1).imgItem.setImageResource(R.drawable.tankdownlose);
                        }else if(trend == Tank.LEFT){
                            arrItem.get(position-1).imgItem.setImageResource(R.drawable.tankleftlose);
                        }
                    }
                });
            }
        }
    };

    private Emitter.Listener updateTurn = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            final int position = (int) args[0];
            final int trend = (int)args[1];
            if(position != me.getPosition()){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(trend == Tank.UP){
                            arrItem.get(position-1).imgItem.setImageResource(R.drawable.rivaltankup);
                        }else if(trend == Tank.RIGHT){
                            arrItem.get(position-1).imgItem.setImageResource(R.drawable.rivaltankright);
                        }else if(trend == Tank.DOWN){
                            arrItem.get(position-1).imgItem.setImageResource(R.drawable.rivaltankdown);
                        }else if(trend == Tank.LEFT){
                            arrItem.get(position-1).imgItem.setImageResource(R.drawable.rivaltankleft);
                        }
                    }
                });

            }

        }
    };



    private ArrayList<Item> initArrItem(int numTank){

        ArrayList<Item> arr = new ArrayList<>();
        Item temp = null;
        int num;
        int point=0;
        RelativeLayout.LayoutParams layoutParams;
        for(int i=0; i<numTank; i++){



            if(me.getPosition() == arrPlayer.get(i).getPosition()){
                tank = new Tank(MainActivity.this, imgTank,btnShoot ,map,Tank.LEFT,arrPoint.get(point).x,arrPoint.get(point).y,1,txtBullet,txtHeart, txtCurrPlayer, arrBullet, numTank, mySocket.socket);
                temp = new Item(true);
            }else{
                ImageView imgItem = new ImageView(MainActivity.this);
                imgItem.setImageResource(R.drawable.rivaltankleft);
                layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                layoutMap.addView(imgItem,layoutParams);
                imgItem.getLayoutParams().height = (int) map.unitWidth;
                imgItem.getLayoutParams().width = (int) map.unitWidth;

                imgItem.setVisibility(View.INVISIBLE);

                //Add Item in to arr
                temp = new Item(arrPlayer.get(i).getPlayerName(),0,arrPoint.get(point),imgItem);

            }
            point++;
            arr.add(temp);


        }

        for(int i=0; i<numTank; i++){
            ImageView img = new ImageView(MainActivity.this);
            img.setImageResource(R.drawable.oval);
            layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutMap.addView(img,layoutParams);
            img.getLayoutParams().width= (int) (tank.width / 3);
            img.getLayoutParams().height= (int) (tank.width/3);
            img.setX((tank.x - map.showFrom.x)*map.unitWidth - img.getLayoutParams().width/2);
            img.setY((tank.y - map.showFrom.y)*map.unitHeight - img.getLayoutParams().height/2);
            img.setVisibility(View.INVISIBLE);
            Item bullet = new Item("flyBullet",Tank.LEFT,new Point(-100,-100),img);
            arrBullet.add(bullet);
        }

        map.show(tank);

        //Create grass
        num = Item.NUM_GRASS;
        for(int i=0 ; i<num; i++){
            //Create ImageView for Item
            ImageView imgItem = new ImageView(MainActivity.this);
            imgItem.setBackgroundColor(Color.GREEN);
            layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutMap.addView(imgItem,layoutParams);
            imgItem.getLayoutParams().height = (int) map.unitWidth * 2;
            imgItem.getLayoutParams().width = (int) map.unitWidth * 3;

            imgItem.setVisibility(View.INVISIBLE);

            //Add Item in to arr
            temp = new Item("GRASS",0,arrPoint.get(point),imgItem);
            arr.add(temp);
            point++;
        }

        //Create bullet
        num =Item.NUM_BULLET_FOR_A_PLAYER * numTank;
        for(int i=0 ; i<num; i++){

            //Create ImageView for Item
            ImageView imgItem = new ImageView(MainActivity.this);
            imgItem.setImageResource(R.drawable.bullet32);
            layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutMap.addView(imgItem,layoutParams);
            imgItem.getLayoutParams().height = (int) map.unitWidth;
            imgItem.getLayoutParams().width = (int) map.unitWidth;

            imgItem.setVisibility(View.INVISIBLE);

            //Add Item in to arr
            temp = new Item("BULLET",Item.BULLET,arrPoint.get(point),imgItem);

            arr.add(temp);
            point++;
        }
        //Create heart
        num =Item.NUM_HEART_FOR_A_PLAYER * numTank;
        for(int i = 0; i < num; i++){


            //Create ImageView for Item
            ImageView imgItem = new ImageView(MainActivity.this);
            imgItem.setImageResource(R.drawable.heart32);
            layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutMap.addView(imgItem,layoutParams);
            imgItem.getLayoutParams().height = (int) tank.height;
            imgItem.getLayoutParams().width = (int) tank.width;
            imgItem.setVisibility(View.INVISIBLE);

            //Add Item in to arr
            temp = new Item("HEART",Item.HEART,arrPoint.get(point), imgItem);

            arr.add(temp);
            point++;
        }

        return arr;
    }

    private void onClick() {
        btnRight.setOnTouchListener(MainActivity.this);
        btnLeft.setOnTouchListener(MainActivity.this);
        btnUp.setOnTouchListener(MainActivity.this);
        btnDown.setOnTouchListener(MainActivity.this);
        btnShoot.setOnClickListener(this);
    }

    private ArrayList<Player> getArrPlayer(ArrayList<Player> arr){
        ArrayList<Player> arrTemp = new ArrayList<>();
        for(int i=0; i<arr.size(); i++){
            if(!arr.get(i).getID().equals("0")){
                arrTemp.add(arr.get(i));
            }
        }
        return arrTemp;
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        mSocket.emit("clientSendDestroy",roomID);
//    }

    private void mapping() {

        Intent lobby = getIntent();
        mySocket = (MySocket) lobby.getSerializableExtra("socket");
        me = LobbyActivity.me;
        roomID = lobby.getIntExtra("roomID",0);
        create = lobby.getIntExtra("create",0);
        arrPlayer = getArrPlayer(LobbyActivity.arrPlayer);
        arrPoint = new ArrayList<>();
        arrPoint = LobbyActivity.arrPoint;
        numTank = arrPlayer.size();

        layoutMap = (RelativeLayout)findViewById(R.id.layoutMap);
        layoutFloor = (RelativeLayout)findViewById(R.id.layoutFloor);

        imgTank = (ImageView)findViewById(R.id.imgTank);
        imgPoint = (ImageView)findViewById(R.id.imgPoint);
        imgTopBorder = (ImageView)findViewById(R.id.imgTopBorder);
        imgBotBorder = (ImageView)findViewById(R.id.imgBotBorder);
        imgLeftBorder = (ImageView)findViewById(R.id.imgLeftBorder);
        imgRightBorder = (ImageView)findViewById(R.id.imgRightBorder);

        btnUp = (Button)findViewById(R.id.btnUp);
        btnDown = (Button)findViewById(R.id.btnDown);
        btnLeft = (Button)findViewById(R.id.btnLeft);
        btnRight = (Button)findViewById(R.id.btnRight);
        btnShoot = (Button)findViewById(R.id.btnShoot);

        txtBullet = (TextView)findViewById(R.id.txtBullet);
        txtHeart = (TextView)findViewById(R.id.txtHeart);
        txtCurrPlayer = (TextView)findViewById(R.id.txtCurrPlayer);


    }



    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = MotionEventCompat.getActionMasked(motionEvent);
        int id = view.getId();
        switch(id) {
            case R.id.btnDown:
                    mySocket.socket.emit("clientSendTurn",roomID, me.getPosition(),Tank.DOWN);
                    tank.trend = Tank.DOWN;
                    imgTank.setImageResource(R.drawable.tankdown);

                    break;
                case R.id.btnUp:
                    mySocket.socket.emit("clientSendTurn", roomID, me.getPosition(),Tank.UP);
                    tank.trend = Tank.UP;
                    imgTank.setImageResource(R.drawable.tankup);

                    break;
                case R.id.btnLeft:
                    mySocket.socket.emit("clientSendTurn",roomID, me.getPosition(),Tank.LEFT);
                    tank.trend = Tank.LEFT;
                    imgTank.setImageResource(R.drawable.tankleft);

                    break;
                case R.id.btnRight:
                    mySocket.socket.emit("clientSendTurn",roomID, me.getPosition(),Tank.RIGHT);
                    tank.trend = Tank.RIGHT;
                    imgTank.setImageResource(R.drawable.tankright);

                    break;
            }
        switch(action) {
            case (MotionEvent.ACTION_DOWN) :
                tank.move =true;
                return true;
            case (MotionEvent.ACTION_UP) :
                tank.move=false;
                return true;
        }
        return false;
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.btnShoot:
                if(tank.bullet <= 0){
                    return;
                }
                tank.shoot(roomID, me.getPosition(), tank.trend, tank.x, tank.y);
                break;
        }
    }
}
