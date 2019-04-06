package com.tranthientrung.tankonline;

import java.util.ArrayList;

public class Room {
    public int ID;
    public String keyID, keyName;
    public int maxPlayer, currPlayer;
    public Boolean start;


    public Room(int ID, String keyID, String keyName, int currPlayer, int maxPlayer, Boolean start){
        this.ID = ID;
        this.keyID = keyID;
        this.keyName = keyName;
        this.maxPlayer = maxPlayer;
        this.currPlayer = currPlayer;
        this.start = start;

    }

}
