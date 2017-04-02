package com.example.oo_raiser.rfidreaderapp;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class QueryActivity extends AppCompatActivity {

    //region ------ 物件宣告 ------
    private String TAG = "QueryActivity";

    //UI
    private TextView textTitle ;
    private TextView textView_count;
    private ListView listView_queryData;
    private Button button_refresh;

    private int timeCount = 0;

    private Handler hander;
    final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //do something in background
            try{

                if(timeCount<10)
                {
                    textView_count.setText(Integer.toString(timeCount)+"秒前更新");
                    timeCount++;

                }else{
                    //refresh listView

                    //update timeCount
                    textView_count.setText(Integer.toString(timeCount)+"秒前更新");
                    timeCount = 0;
                }


                hander.postDelayed(runnable,1000); //delay 1 sec
            }catch (Exception e){
                Log.e(TAG, e.getMessage());
            }
        }
    };

    //endregion


    //region ------ override function ------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);

        //initialize the UI
        initView();

        //set the button listener
        listener();

        //initialize handler
        hander = new Handler();
        hander.post(runnable);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(hander!=null)
        {
            hander.removeCallbacks(runnable);
        }
    }

    //endregion


    //region ------ functions ------

    //initialize the UI
    private void initView()
    {
        textTitle = (TextView)findViewById(R.id.textView_title);
        textTitle.setText("查看資料");

        textView_count = (TextView)findViewById(R.id.textView_count);
        listView_queryData = (ListView)findViewById(R.id.listView_queryData);
        button_refresh = (Button)findViewById(R.id.button_refresh);
    }

    //set the button listener
    private void listener()
    {
        //help
        button_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
               //do something
                Toast.makeText(QueryActivity.this,"刷新資料",Toast.LENGTH_SHORT).show();
                timeCount = 0;

                //get data from server and refresh listView

            }
        });
    }

    //get data from server


    //refresh listView



    //endregion
}
