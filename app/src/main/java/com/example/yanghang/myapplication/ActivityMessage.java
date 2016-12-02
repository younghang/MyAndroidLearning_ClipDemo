package com.example.yanghang.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class ActivityMessage extends AppCompatActivity {

    public static String DIALOG_MESSAGE = "dialog_message";
    TextView tvDialogMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_message);
        inital();
    }

    private void inital() {
        tvDialogMessage = (TextView) findViewById(R.id.dialogMessgaeShow);
        Intent intent = getIntent();
        String dialogmessage = intent.getStringExtra(DIALOG_MESSAGE);
        tvDialogMessage.setText(dialogmessage);


    }
}
