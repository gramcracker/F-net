package io.underdark.app.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import io.underdark.app.R;
import io.underdark.app.model.Node;

public class UsernameDialog extends Dialog {

    private EditText etUsername;

    private static UsernameDialog dialog;

    public static UsernameDialog getInstance(Context context){
        if(dialog == null){
            dialog = new UsernameDialog(context);
            return dialog;
        }else return dialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.username);
        etUsername = findViewById(R.id.username);
        Button okButton = findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String u = etUsername.getText().toString();
                if (u.equals("")) return;
                Node.username = u;
                dialog.dismiss();

            }
        });
    }


    private UsernameDialog(@NonNull Context context) {
        super(context);
    }

}
