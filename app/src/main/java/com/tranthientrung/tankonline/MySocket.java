package com.tranthientrung.tankonline;

import com.github.nkzawa.socketio.client.Socket;

import java.io.Serializable;

public class MySocket implements Serializable {
    public static Socket socket;
    public static String un;
    public static String id;

    public MySocket(Socket socket){
        this.socket = socket;
    }

}
