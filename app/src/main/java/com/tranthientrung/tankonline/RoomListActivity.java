package com.tranthientrung.tankonline;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RoomListActivity extends Activity implements View.OnClickListener {

    public static final int CREATE = 19;
    public static final int JOIN = 20;
    public static final int MAX_PLAYER = 6;

    private EditText edtRoomID;
    private ListView lstRoom;
    private ImageButton ibtnSearchRoom, ibtnSound;
    private Button btnCancel, btnCreateRoom;
    private ArrayList<Room> arrRoom;
    private ListRoomAdapter adapter;
    private ArrayList<Room> arrSearch;
    private MediaPlayer mPlayer;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Boolean sound;
    private MySocket mySocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_room_list);

        mapping();

        mPlayer = MediaPlayer.create(RoomListActivity.this,R.raw.loungegame);
        mPlayer.setLooping(true);
        sharedPreferences = this.getSharedPreferences(HomeActivity.SAVE_SOUND,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        sound = sharedPreferences.getBoolean(HomeActivity.OPEN_SOUND,false);
        if(sound){
            mPlayer.start();
            ibtnSound.setImageResource(R.drawable.sound);
        }

        mySocket.socket.emit("clientSendPlayerName", mySocket.un);
        mySocket.socket.emit("clientSendRequestRoomList");
        mySocket.socket.on("serverSendRoomList", updateArrRoom);

        btnCancel.setOnClickListener(this);
        btnCreateRoom.setOnClickListener(this);
        ibtnSearchRoom.setOnClickListener(this);
        ibtnSound.setOnClickListener(this);

        lstRoom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if(!arrRoom.get((int)l-1).start && (arrRoom.get((int)l-1).currPlayer < arrRoom.get((int)l -1).maxPlayer)){
               mySocket.socket.emit("clientSendJoinRoom",(int)l);
               Intent joinRoom = new Intent(RoomListActivity.this, LobbyActivity.class);
               joinRoom.putExtra("roomID",(int) l);
               joinRoom.putExtra("createRoom", JOIN);
               joinRoom.putExtra("socket",mySocket);
               startActivity(joinRoom);
            }
            }
        });


    }

    private Emitter.Listener updateArrRoom = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Room room;
                        int ID, maxPlayer, currPlayer;
                        String keyID, keyName;
                        Boolean start;
                        JSONArray array = new JSONArray(args[0].toString());
                        arrRoom.clear();
                        if(array.length() == 0){
                            adapter = new ListRoomAdapter(arrRoom,RoomListActivity.this);
                            adapter.notifyDataSetChanged();
                            lstRoom.setAdapter(adapter);
                            return;
                        }
                        for(int i = 0; i < array.length(); i++){
                            JSONObject object = array.getJSONObject(i);
                            ID = object.getInt("ID");
                            maxPlayer = object.getInt("maxPlayer");
                            currPlayer = object.getInt("currPlayer");
                            keyID = object.getString("keyID");
                            keyName = object.getString("keyName");
                            start = object.getBoolean("start");
                            room = new Room(ID,keyID, keyName, currPlayer, maxPlayer, start);
                            arrRoom.add(room);
                        }
                        adapter = new ListRoomAdapter(arrRoom,RoomListActivity.this);
                        adapter.notifyDataSetChanged();
                        lstRoom.setAdapter(adapter);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private void mapping() {
        Intent home = getIntent();
        mySocket = (MySocket) home.getSerializableExtra("socket");

        edtRoomID = (EditText)findViewById(R.id.edtRoomID);
        lstRoom = (ListView)findViewById(R.id.lstRoom);
        ibtnSearchRoom = (ImageButton)findViewById(R.id.ibtnSearchRoom);
        ibtnSound = (ImageButton)findViewById(R.id.ibtnSound);
        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnCreateRoom = (Button)findViewById(R.id.btnCreateRoom);

        arrRoom = new ArrayList<>();

    }

    private int searchRoomID(){
        int ID=1;
        for(int i=0; i<arrRoom.size(); i++){
            if(ID == arrRoom.get(i).ID){
                ID++;
            }else{
                return ID;
            }
        }
        return ID;
    };
    @Override
    public void onClick(View view) {
        int ID = view.getId();
        switch (ID){
            case R.id.btnCancel:
                Intent home = new Intent(RoomListActivity.this, HomeActivity.class);
                startActivity(home);
                mPlayer.stop();
                finish();
                break;
            case R.id.btnCreateRoom:
                int roomID = searchRoomID();
                mySocket.socket.emit("clientSendCreateRoom",roomID,MAX_PLAYER);
                Intent boobly = new Intent(RoomListActivity.this, LobbyActivity.class);
                boobly.putExtra("createRoom",CREATE);
                boobly.putExtra("roomID",roomID);
                boobly.putExtra("socket",mySocket);
                startActivity(boobly);
                mPlayer.stop();
                finish();
                break;
            case R.id.ibtnSearchRoom:
                if(edtRoomID.getText().toString().trim().equals("")){
                    adapter = new ListRoomAdapter(arrRoom, RoomListActivity.this);
                    adapter.notifyDataSetChanged();
                    lstRoom.setAdapter(adapter);
                    return;
                }
                int searchID =Integer.parseInt(edtRoomID.getText().toString());
                arrSearch = new ArrayList<>();
                for(int i = 0; i<arrRoom.size(); i++){
                    if(arrRoom.get(i).ID == searchID){
                        arrSearch.add(arrRoom.get(i));
                        break;
                    }
                }
                adapter = new ListRoomAdapter(arrSearch,RoomListActivity.this);
                adapter.notifyDataSetChanged();
                lstRoom.setAdapter(adapter);
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
}
