package io.underdark.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import io.underdark.app.model.Node;

import static io.underdark.app.MainActivity.node;

public class Messenger extends AppCompatActivity {


    private TextView peersTextView;
    private static TextView conversationTextView;
    private EditText message;
    public static boolean active = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);

        peersTextView =  findViewById(R.id.peersTextView);
        conversationTextView =  findViewById(R.id.tv_conversation_view);
        message = findViewById(R.id.message);



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
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void send(View view) throws UnsupportedEncodingException {


        String m = node.formatMessage(message.getText().toString(), node.username);
        conversationTextView.append(m);
        node.sendChannelMessage("main", m);


    }

    public void refreshPeers()
    {
        peersTextView.setText(node.getLinks().size() + " connected");
    }

    public static void refreshFrames()
    {
        conversationTextView.setText(node.getMessage());
    }
}
