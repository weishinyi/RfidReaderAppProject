package com.example.oo_raiser.rfidreaderapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.View.OnClickListener;

public class MainActivity extends AppCompatActivity {

    private Button buttonBluetooth ;
    private Button buttonQuery;
    private Button buttonHelp;
    private Button buttonExit ;
    private TextView textTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textTitle = (TextView) findViewById(R.id.textView_title);
        textTitle.setText("Rfid Reader");

        //初始UI
        this.initView();

        //監聽按鈕
        this.listener();
    }

    //initialize the UI
    private void initView()
    {
        buttonBluetooth = (Button) findViewById(R.id.buttonBluetoothOption);
        buttonQuery =  (Button) findViewById(R.id.buttonQuery);
        buttonExit = (Button) findViewById(R.id.button_exit);
        buttonHelp = (Button) findViewById(R.id.button_help);
    }

    //Button Listener
    private void listener()
    {
        //bluetooth
        buttonBluetooth.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
                startActivity(intent);
            }
        });

        //query
        buttonQuery.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, QueryActivity.class);
                startActivity(intent);
            }
        });

        //help
        buttonHelp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent helpIntent = new Intent(MainActivity.this, HelpAcivity.class);
                startActivity(helpIntent);
            }
        });

        //exit
        buttonExit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v)
            {
                MainActivity.this.finish();
            }
        });
    }


}
