package io.underdark.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
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

    private TextView conversationTextView;
    private EditText message;
    private ScrollView scrollView;
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
        toolbar.setSubtitle("listening: "+ currentChannel.usersListening.size());

        conversationTextView = findViewById(R.id.conversation_text_view);
        message = findViewById(R.id.message);
        scrollView = findViewById(R.id.messageScrollView);
        conversationTextView.setText("");
        for(Transmission t: currentChannel.recentMessages){
            appendMessage(t);
        }
    }


    @Subscribe
    public void onMessageEvent(Transmission t){
        if(t.channelTo.equals(currentChannel)){
            appendMessage(t);

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

        //conversationTextView.append(formatMessage(m,Node.username,Node.getTime()));
        appendMessage(m);

        try {
            Node.sendChannelMessage(currentChannel, m);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    public static void refreshFrames() {


        }


    public String formatMessage(String message, String user, String time){

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

    public static void appendColoredText(TextView tv, String text, int color) {
        int start = tv.getText().length();
        tv.append(text);
        int end = tv.getText().length();

        Spannable spannableText = (Spannable) tv.getText();
        spannableText.setSpan(new ForegroundColorSpan(color), start, end, 0);
    }

    public void appendMessage( String message){
        appendColoredText(conversationTextView,formatMessage(message,Node.username,Node.getTime()), Node.textColor);
        scrollView.fullScroll(scrollView.FOCUS_DOWN);
    }
    public void appendMessage(Transmission t){
        appendColoredText(conversationTextView, formatMessage(t.message,t.originName,t.time), t.color);
        scrollView.fullScroll(scrollView.FOCUS_DOWN);

    }

}


