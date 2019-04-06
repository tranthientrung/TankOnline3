package com.tranthientrung.tankonline;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.io.Serializable;
import java.net.URISyntaxException;

public class HomeActivity extends Activity implements View.OnClickListener, Serializable {


    private static Socket socket;
    {
        try {
            socket = IO.socket(Setting.urlServer);

        } catch (URISyntaxException e) {
            Toast.makeText(this, "No network connection!", Toast.LENGTH_SHORT).show();
        }
    }

    public static String SAVE_SOUND = "savesound";
    public static String OPEN_SOUND = "opensound";

    private Button btnPlay;
    private EditText edtPlayerName;
    private static MediaPlayer mPlayer;
    private ImageButton ibtnSound;
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
        setContentView(R.layout.activity_home);

        mySocket = new MySocket(socket);
        mySocket.socket.connect();

        mapping();

        mPlayer = MediaPlayer.create(HomeActivity.this,R.raw.loungegame);
        mPlayer.setLooping(true);
        sharedPreferences = this.getSharedPreferences(SAVE_SOUND,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        sound = sharedPreferences.getBoolean(OPEN_SOUND,false);
        if(sound){
            mPlayer.start();
            ibtnSound.setImageResource(R.drawable.sound);
        }
        btnPlay.setOnClickListener(this);
        ibtnSound.setOnClickListener(this);
    }



    private void mapping() {
        btnPlay = (Button)findViewById(R.id.btnPlay);
        edtPlayerName = (EditText)findViewById(R.id.edtPlayerName);
        ibtnSound = (ImageButton)findViewById(R.id.ibtnSound);
    }

    @Override
    public void onClick(View view) {


        int ID = view.getId();
        switch (ID){

            case R.id.btnPlay:
                mySocket.un = edtPlayerName.getText().toString();
                Intent roomList = new Intent(HomeActivity.this, RoomListActivity.class);
                roomList.putExtra("socket",mySocket);
                mPlayer.stop();
                startActivity(roomList);
                finish();
                break;
            case R.id.ibtnSound:
                if(mPlayer.isPlaying()) {
                    mPlayer.pause();
                    editor.putBoolean(OPEN_SOUND,false);
                    editor.commit();
                    ibtnSound.setImageResource(R.drawable.mute);
                    sound = false;
                }else{
                    mPlayer.start();
                    editor.putBoolean(OPEN_SOUND,true);
                    editor.commit();
                    ibtnSound.setImageResource(R.drawable.sound);
                    sound = true;
                }

                break;
        }
    }
}
