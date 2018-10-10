package io.underdark.app.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import io.underdark.app.R;
import io.underdark.app.model.Channel;
import io.underdark.app.model.Node;

public class PasswordPrompt extends Dialog {
    private EditText password;
    public Channel channel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.username);
        password = findViewById(R.id.username);
        Button okButton = findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unlock(password.getText().toString());

            }
        });
    }

    private void unlock(String p){
        if (p.equals("")) return;
        channel.key = p;
        Node.unlock(channel);
        this.dismiss();
    }

    public PasswordPrompt(@NonNull Context context, Channel channel) {
        super(context);
        this.channel = channel;
    }

}
