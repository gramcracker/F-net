package io.underdark.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Set;

import io.underdark.app.model.Channel;
import io.underdark.app.model.Node;
import io.underdark.app.model.Transmission;


public class Messenger extends AppCompatActivity {

    //todo oncreate and on resume should bring back messages


    private TextView conversationTextView;
    private EditText message;
    android.support.v7.widget.Toolbar toolbar;
    public static boolean active = false;
    private ArrayList <String> messageHist;
    private Channel currentChannel;
    EventBus eventBus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);


        setContentView(R.layout.activity_messenger);
        Intent intent = getIntent();
        toolbar = findViewById(R.id.messageViewToolbar);
        if (intent.hasExtra("currentChannel")) {
            currentChannel =(Channel) intent.getSerializableExtra("currentChannel");
            toolbar.setTitle(currentChannel.title);

        }
        toolbar.setSubtitle("listening: "+0);

        conversationTextView = findViewById(R.id.conversation_text_view);
        message = findViewById(R.id.message);

        //todo: init message hist with diskcache message hist instead
        conversationTextView.setText("");
        for(Transmission t: currentChannel.recentMessages){
            conversationTextView.append(formatMessage(t.message,t.originName,t.time));
        }
    }


    @Subscribe
    public void onMessageEvent(Transmission t){
        if(t.channelTo.equals(currentChannel)){
            conversationTextView.append(formatMessage(t.message,t.originName,t.time));
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        this.active = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.active = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void send(View view) throws UnsupportedEncodingException {


        String m = message.getText().toString();
        message.getText().clear();
        if(m.contentEquals(""))return;

        conversationTextView.append(formatMessage(m,Node.username,Node.getTime()));

        try {
            Node.sendChannelMessage(currentChannel, m);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    public static void refreshFrames() {


        }


    public String formatMessage(String message, String user, String time){

        //todo: change to get date from transmission object
        String m = time+" "
                +user+": "
                +message+"\n";
        return m;
    }

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }



}


