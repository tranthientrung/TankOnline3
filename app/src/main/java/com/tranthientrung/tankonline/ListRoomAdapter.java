package com.tranthientrung.tankonline;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ListRoomAdapter extends BaseAdapter {
    ArrayList<Room> arrRoom;
    Context context;

    public ListRoomAdapter(ArrayList<Room> arrRoom, Context context) {
        this.arrRoom = arrRoom;
        this.context = context;
    }

    @Override
    public int getCount() {
        return arrRoom.size();
    }

    @Override
    public Object getItem(int i) {
        return arrRoom.get(i);
    }

    @Override
    public long getItemId(int i) {
        return arrRoom.get(i).ID;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Item item;
        if(view == null){
            item = new Item();
            view = ((Activity)context).getLayoutInflater().inflate(R.layout.list_room_item,viewGroup,false);
            item.txtCurrPlayer = (TextView)view.findViewById(R.id.txtCurrPlayer);
            item.txtKeyName = (TextView)view.findViewById(R.id.txtKeyName);
            item.txtID = (TextView)view.findViewById(R.id.txtID);
            item.layout = (LinearLayout)view.findViewById(R.id.linearLayout);
            view.setTag(item);
        }else{
            item=(Item)view.getTag();
        }
        item.txtKeyName.setText(arrRoom.get(i).keyName);
        item.txtCurrPlayer.setText(arrRoom.get(i).currPlayer+"/"+arrRoom.get(i).maxPlayer);
        item.txtID.setText(arrRoom.get(i).ID+"");
        if(arrRoom.get(i).start || (arrRoom.get(i).currPlayer >= arrRoom.get(i).maxPlayer)){
            item.layout.setBackgroundColor(Color.parseColor("#ff6565"));
            item.txtKeyName.setEnabled(false);
            item.txtCurrPlayer.setEnabled(false);
            item.txtID.setEnabled(false);

        }else{
            item.layout.setBackgroundColor(Color.WHITE);
            item.txtKeyName.setEnabled(true);
            item.txtCurrPlayer.setEnabled(true);
            item.txtID.setEnabled(true);

        }
        return view;
    }

    class Item{
        TextView txtID, txtKeyName, txtCurrPlayer;
        LinearLayout layout;
    }
}
