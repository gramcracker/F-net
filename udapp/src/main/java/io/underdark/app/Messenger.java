package io.underdark.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toolbar;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import io.underdark.app.model.Node;

import static io.underdark.app.MainActivity.node;

public class Messenger extends AppCompatActivity {

    //todo oncreate and on resume should bring back messages


    private static TextView conversationTextView;
    private EditText message;
    android.support.v7.widget.Toolbar toolbar;
    public static boolean active = false;
    private ArrayList <String> messageHist;
    private String currentChannel = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);
        Intent intent = getIntent();
        toolbar = findViewById(R.id.messageViewToolbar);
        if (intent.hasExtra("currentChannel")) {
            currentChannel = intent.getStringExtra("currentChannel");
            toolbar.setTitle(currentChannel);

        }
        toolbar.setSubtitle("people: "+0);

        conversationTextView = findViewById(R.id.tv_conversation_view);
        message = findViewById(R.id.message);

        //todo: init message hist with diskcache message hist instead
        messageHist = new ArrayList<>();
        /*
        for(m in messageHist){
           message view += m
        }
         */

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("mesh_irc_transmission"));
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            if (intent.hasExtra("message")) {
                if (intent.hasExtra("channelTo")) {
                    Bundle E = intent.getExtras();
                    if (E.getString("channelTo").contentEquals(currentChannel) &&
                            !E.getString("channelTo").contentEquals("")) {
                        String message = E.getString("message");
                        String user = E.getString("user");
                        String time = E.getString("time");
                        String formattedMessage = formatMessage(message, user, time);
                        //messageHist.add(formattedMessage);
                        conversationTextView.append(formattedMessage);

                        Log.e("receiver", "Got message: " + message);
                    }
                }
            }
        }
    };

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

        conversationTextView.append(formatMessage(m,node.username,node.getTime()));

        try {
            node.sendChannelMessage(currentChannel, m);
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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }


}


