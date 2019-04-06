package com.tranthientrung.tankonline;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class LobbyActivity extends Activity implements View.OnClickListener {

    private TextView txtPlayerName1, txtPlayerName2, txtPlayerName3, txtPlayerName4, txtPlayerName5, txtPlayerName6,
            txtReady1,txtReady2,txtReady3,txtReady4,txtReady5,txtReady6,
            txtShowChat, txtRoomID;
    private Button btnSend, btnCancel, btnStart, btnReady;
    private EditText edtChat;
    private ImageButton ibtnSound;
    public static ArrayList<Player> arrPlayer;
    public static ArrayList<Point> arrPoint;
    private ArrayList<TextView> arrTxtPlayerName;
    private ArrayList<TextView> arrTxtReady;
    private int roomID, create;
    public static Player me;
    private MediaPlayer mPlayer;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Boolean sound;
    private MySocket mySocket;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_lobby);

        mapping();
        mPlayer = MediaPlayer.create(LobbyActivity.this,R.raw.loungegame);
        mPlayer.setLooping(true);
        sharedPreferences = this.getSharedPreferences(HomeActivity.SAVE_SOUND,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        sound = sharedPreferences.getBoolean(HomeActivity.OPEN_SOUND,false);
        if(sound){
            mPlayer.start();
            ibtnSound.setImageResource(R.drawable.sound);
        }
        init();
        click();
        mySocket.socket.on("serverSendArrPlayer",updateArrPlayer);
        mySocket.socket.on("serverSendPlayerID",idPlayer);
        mySocket.socket.on("serverSendReady", updateReady);
        mySocket.socket.on("serverSendRemovePlayer", removePlayer);
        mySocket.socket.on("serverSendRemoveRoom",removeRoom);
        mySocket.socket.on("serverSendChat", chat);
        mySocket.socket.on("serverSendStart",startGame);
    }

    private Emitter.Listener startGame = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            arrPlayer = new ArrayList<>();
            arrPoint = new ArrayList<>();
            JSONArray array = null;
            try {
                array = new JSONArray(args[0].toString());
                for(int i=0; i<array.length(); i++){
                    JSONObject object = array.getJSONObject(i);
                    Player player = new Player();
                    player.setPlayerName(object.getString("playerName"));
                    player.setReady(object.getBoolean("ready"));
                    player.setID(object.getString("ID"));
                    player.setPosition(object.getInt("position"));
                    arrPlayer.add(player);
                }
                array = new JSONArray(args[1].toString());
                for(int i=0; i<array.length(); i++){
                    JSONObject object = array.getJSONObject(i);
                    Point point = new Point();
                    point.x = object.getInt("x");
                    point.y = object.getInt("y");
                    arrPoint.add(point);
                }
                Intent start = new Intent(LobbyActivity.this, MainActivity.class);
                start.putExtra("create",create);
                start.putExtra("roomID",roomID);
                start.putExtra("socket",mySocket);
                mPlayer.stop();
                startActivity(start);
                finish();
            }catch (Exception e) {
                e.printStackTrace();
            }


        }
    };
    private Emitter.Listener chat = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String playerID = args[0].toString();
            String playerName = args[1].toString();
            String mess = args[2].toString();
            if(playerID.equals(me.getID())){
                mess="Me: "+mess;
            }else{
                mess = playerName+": "+mess;
            }
            append(txtShowChat,mess);
        }
    };
    private Emitter.Listener removeRoom = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            mySocket.socket.emit("clientSendLeaveRoom",roomID);
            Intent roomList = new Intent(LobbyActivity.this, RoomListActivity.class);
            roomList.putExtra("socket",mySocket);
            startActivity(roomList);
            finish();
        }
    };
    private Emitter.Listener removePlayer = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            int position = (int) args[0];
            setText(arrTxtPlayerName.get(position-1),"Open");
            setText(arrTxtReady.get(position-1),"");
        }
    };

    private Emitter.Listener updateReady = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            int position = (int) args[0];
            Boolean ready = (Boolean) args[1];
            String stt = "";
            if(ready){
                stt = "Ready";
            }else{
                stt = "Not Ready";
            }
            setText(arrTxtReady.get(position-1),stt);
        }
    };
    private  Emitter.Listener idPlayer = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            mySocket.id = args[0].toString();
        }
    };

    private void setText(final TextView txt, final String value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txt.setText(value);
            }
        });
    }
    private Emitter.Listener updateArrPlayer = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            try {
                arrPlayer = new ArrayList<>();
                JSONArray array = new JSONArray(args[0].toString());
                for(int i=0; i<array.length(); i++){
                    JSONObject object = array.getJSONObject(i);
                    Player player = new Player();
                    player.setPlayerName(object.getString("playerName"));
                    player.setReady(object.getBoolean("ready"));
                    player.setID(object.getString("ID"));
                    player.setPosition(object.getInt("position"));
                    setText(arrTxtPlayerName.get(i),player.getPlayerName());

                    if(!player.getID().equals("0")){
                        if(player.isReady()){
                            setText(arrTxtReady.get(i),"Ready");
                        }else{
                            setText(arrTxtReady.get(i),"Not Ready");
                        }
                        if(player.getID().equals(mySocket.id)){
                            setColor(arrTxtPlayerName.get(i),Color.RED);

                            me = player;
                        }
                    }else{
                        setText(arrTxtReady.get(i),"");
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    private void setColor(final TextView txt, final int color){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txt.setTextColor(color);
            }
        });

    }
    private void click() {

        btnSend.setOnClickListener(LobbyActivity.this);
        btnStart.setOnClickListener(LobbyActivity.this);
        btnReady.setOnClickListener(LobbyActivity.this);
        btnCancel.setOnClickListener(LobbyActivity.this);
        ibtnSound.setOnClickListener(LobbyActivity.this);

    }

    private void init() {
        Intent i = getIntent();
        roomID = i.getIntExtra("roomID",0);
        create = i.getIntExtra("createRoom",0);
        if(create == RoomListActivity.CREATE){
            mySocket.socket.emit("clientSendCreatedRoom",roomID);
            btnStart.setVisibility(View.VISIBLE);
            btnReady.setVisibility(View.GONE);
        }else{
            mySocket.socket.emit("clientSendJoinedRoom",roomID);
            btnReady.setVisibility(View.VISIBLE);
            btnStart.setVisibility(View.GONE);
        }

        txtRoomID.setText(roomID+"");
    }

    private void mapping() {

        Intent room =getIntent();
        mySocket = (MySocket) room.getSerializableExtra("socket");
        mySocket.socket.emit("clientSendGetPlayerID");

        txtPlayerName1 = (TextView)findViewById(R.id.txtPlayerName1);
        txtPlayerName2 = (TextView)findViewById(R.id.txtPlayerName2);
        txtPlayerName3 = (TextView)findViewById(R.id.txtPlayerName3);
        txtPlayerName4 = (TextView)findViewById(R.id.txtPlayerName4);
        txtPlayerName5 = (TextView)findViewById(R.id.txtPlayerName5);
        txtPlayerName6 = (TextView)findViewById(R.id.txtPlayerName6);
        txtReady1 = (TextView)findViewById(R.id.txtReady1);
        txtReady2 = (TextView)findViewById(R.id.txtReady2);
        txtReady3 = (TextView)findViewById(R.id.txtReady3);
        txtReady4 = (TextView)findViewById(R.id.txtReady4);
        txtReady5 = (TextView)findViewById(R.id.txtReady5);
        txtReady6 = (TextView)findViewById(R.id.txtReady6);
        txtShowChat = (TextView)findViewById(R.id.txtShowChat);
        txtRoomID = (TextView)findViewById(R.id.txtRoomID);

        btnReady = (Button)findViewById(R.id.btnReady);
        btnStart = (Button)findViewById(R.id.btnStart);
        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnSend = (Button)findViewById(R.id.btnSend);

        edtChat = (EditText)findViewById(R.id.edtChat);

        arrTxtPlayerName = new ArrayList<>();
        arrTxtReady = new ArrayList<>();

        arrTxtPlayerName.add(txtPlayerName1);
        arrTxtPlayerName.add(txtPlayerName2);
        arrTxtPlayerName.add(txtPlayerName3);
        arrTxtPlayerName.add(txtPlayerName4);
        arrTxtPlayerName.add(txtPlayerName5);
        arrTxtPlayerName.add(txtPlayerName6);

        arrTxtReady.add(txtReady1);
        arrTxtReady.add(txtReady2);
        arrTxtReady.add(txtReady3);
        arrTxtReady.add(txtReady4);
        arrTxtReady.add(txtReady5);
        arrTxtReady.add(txtReady6);

        ibtnSound = (ImageButton)findViewById(R.id.ibtnSound);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.btnCancel:
                mPlayer.stop();
                if(create == RoomListActivity.CREATE){
                    mySocket.socket.emit("clientSendRemoveRoom",roomID);

                }else{
                    mySocket.socket.emit("clientSendRemovePlayer",roomID, me.getPosition());
                    finish();
                }
                break;
            case R.id.btnStart:
                int numPlayer=0;
                for(int i =0; i<RoomListActivity.MAX_PLAYER; i++){
                    if(arrTxtReady.get(i).getText().toString().equals("Not Ready")){
                        Toast.makeText(this, arrTxtPlayerName.get(i).getText().toString()+ " Not Ready", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(arrTxtReady.get(i).getText().toString().equals("Ready")){
                        numPlayer++;
                    }
                }
                mySocket.socket.emit("clientSendStart",roomID, numPlayer*Item.TOTAL_ITEMS_FOR_A_PAYER + numPlayer);
                break;
            case R.id.btnReady:
                mySocket.socket.emit("clientSendReady",roomID, me.getPosition(), me.isReady());
                me.setReady(!me.isReady());
                break;
            case R.id.btnSend:
                String mess=edtChat.getText().toString();
                edtChat.setText("");
                if(!mess.trim().equals("")){
                    mySocket.socket.emit("clientSendChat",roomID, me.getID(), me.getPlayerName(), mess);
                }
                break;
            case R.id.ibtnSound:
                if(mPlayer.isPlaying()) {
                    mPlayer.pause();
                    editor.putBoolean(HomeActivity.OPEN_SOUND,false);
                    editor.commit();
                    ibtnSound.setImageResource(R.drawable.mute);
                    sound = false;
                }else{
                    mPlayer.start();
                    editor.putBoolean(HomeActivity.OPEN_SOUND,true);
                    editor.commit();
                    ibtnSound.setImageResource(R.drawable.sound);
                    sound = true;
                }
                break;
        }
    }

    private void append(final TextView txt, final String mess){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txt.append(mess+"\n");
            }
        });
    }
}