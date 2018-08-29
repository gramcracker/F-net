package io.underdark.app;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import static io.underdark.app.MainActivity.node;

public class NewChannelDialog extends Dialog implements View.OnClickListener {

    private static NewChannelDialog dialog;
    String newChannel = "";
    EditText newChannelEditText;
    Button broadcastButton;

    public NewChannelDialog(@NonNull Context context) {
        super(context);
    }

    public static NewChannelDialog getInstance(AppCompatActivity activity){

        return dialog = (dialog == null) ? new NewChannelDialog(activity) : dialog;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_channel);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().getAttributes().width = WindowManager.LayoutParams.MATCH_PARENT;
        setCanceledOnTouchOutside(true);
        newChannelEditText = findViewById(R.id.channelNameEditText);
        broadcastButton = findViewById(R.id.broadcastButton);

        broadcastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "broadcasting", Toast.LENGTH_SHORT).show();
                createChannel();

            }
        });
    }

    @Override
    public void onClick(View view) {

    }

    public void createChannel(){
        String channel = newChannelEditText.getText().toString();
        try {
            node.newChannel(channel);
            this.dismiss();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
