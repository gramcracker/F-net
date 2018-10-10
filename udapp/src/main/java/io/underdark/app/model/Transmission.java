package io.underdark.app.model;

import android.graphics.Color;

import java.io.Serializable;
import java.util.ArrayList;

public class Transmission implements Serializable {
    public int color = Color.WHITE;
    public String time = "";
    public Channel channelTo;
    public String message = "";
    public String originName  = "";
    public Channel broadcastingChannel;
    public long nodeFrom;
    public long nodeTo;

    public enum Type { none, messagesSync, channelsListRequest, channelList, messageList, channelMessage, newChannel, requestUnlock, acceptUnlock, privateMessage  }
    Type type = Type.none;

    ArrayList<String> listData;

    ArrayList <Channel> channelList;

    Transmission(Type _type){
        type = _type;
        listData = new ArrayList<>();
        channelList = new ArrayList<>();
    }
}
