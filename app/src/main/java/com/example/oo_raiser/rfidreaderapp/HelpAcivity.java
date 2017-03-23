package com.example.oo_raiser.rfidreaderapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class HelpAcivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_acivity);

        TextView textTitle = (TextView) findViewById(R.id.textView_title);
        textTitle.setText("幫助");
    }
}
