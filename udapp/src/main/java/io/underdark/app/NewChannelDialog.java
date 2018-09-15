package io.underdark.app;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import io.underdark.app.model.Channel;
import io.underdark.app.model.Node;

public class NewChannelDialog extends Dialog implements View.OnClickListener {

    private static NewChannelDialog dialog;
    String newChannel = "";
    EditText newChannelEditText;
    Button broadcastButton;
    TextView advancedOptionsToggle;
    View advancedOptionsView;
    TextInputLayout keyInputLayout;
    EditText keyEditText;
    Switch privateSwitch;
    boolean advancedOptionsVisible = false;
    boolean usesKey = false;



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
        advancedOptionsToggle = findViewById(R.id.advancedOptionsTextView);
        advancedOptionsView = findViewById(R.id.advancedOptionsLayout);
        privateSwitch = findViewById(R.id.privateChannelSwitch);
        keyInputLayout = findViewById(R.id.channelKeyInputLayout);
        keyEditText = findViewById(R.id.channelKeyEditText);


        broadcastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                createChannel();
                Toast.makeText(view.getContext(), "broadcasting", Toast.LENGTH_SHORT).show();

            }
        });

        privateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                usesKey = isChecked;
                if(isChecked){
                    keyInputLayout.setVisibility(View.VISIBLE);
                    privateSwitch.setTextColor(getContext().getResources().getColor(R.color.color_accent));
                }else {
                    keyInputLayout.setVisibility(View.GONE);
                    privateSwitch.setTextColor(getContext().getResources().getColor(R.color.greyed_out));


                }
            }
        });

        advancedOptionsToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(advancedOptionsVisible) {
                    advancedOptionsVisible = false;
                    advancedOptionsView.setVisibility(View.GONE);
                }
                else{
                    advancedOptionsVisible = true;
                    advancedOptionsView.setVisibility(View.VISIBLE);
                }


            }
        });


    }



    @Override
    public void onClick(View view) {

    }




    public void createChannel(){
        String channelTitle = newChannelEditText.getText().toString();
        try {
            Channel channel = new Channel(channelTitle, Node.username);
            if(usesKey)
            {
                channel.keyRequired = usesKey;
                channel.key = keyEditText.getText().toString();
            }
            Node.setNewChannel(channel);


        } catch (IOException e) {
            e.printStackTrace();
        }
        this.dismiss();
    }
}
