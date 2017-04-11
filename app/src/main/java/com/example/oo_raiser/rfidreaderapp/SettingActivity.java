package com.example.oo_raiser.rfidreaderapp;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.oo_raiser.rfidreaderapp.bluetooth.ConnectedThread;
import com.example.oo_raiser.rfidreaderapp.command.NewSendCommendManager;
import com.example.oo_raiser.rfidreaderapp.entity.GlobalVariable;

import java.util.ArrayList;
import java.util.List;

import static com.example.oo_raiser.rfidreaderapp.util.Util.context;

public class SettingActivity extends AppCompatActivity {
    private String TAG = "SettingActivity";

    //宣告view元件
    private Spinner outputSpinner;
    private Spinner workareaSpinner;
    private Button buttonSetOutput;
    private Button buttonSetWork;
    private TextView textTitle;

    private NewSendCommendManager cmdManager;

    private String[] outputStrings = {"35dBm","34dBm","33dBm","32dBm","31dBm","30dBm","29dBm","28dBm","27dBm", "26dBm", "25dBm", "24dBm", "23dBm",
            "22dBm", "21dBm", "20dBm", "19dBm", "18dBm", "17dBm", "16dBm", "15dBm", "14dBm" , "13dBm"};
    private String[] workAreaStrings;

    private List<String> listOutput = new ArrayList<String>();
    private List<String> listWorkarea = new ArrayList<String>();

    private int outputValue = 2700;
    private int area = 0;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what)
            {
                case BluetoothActivity.CONNECT_INTERRUPT:
                    BluetoothActivity.connFlag = false;
                    Toast.makeText(getApplicationContext(), "連接中斷", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        textTitle = (TextView) findViewById(R.id.textView_title);
        textTitle.setText("模組參數設置");
        cmdManager = new NewSendCommendManager(ConnectedThread.getSocketInoutStream(), ConnectedThread.getSocketOutoutStream());

        //initialize the UI
        initView();

        //set the Listener
        listener();

    }

    //初始化UI
    private void initView() {
        //get UI item
        outputSpinner = (Spinner) findViewById(R.id.spinner_setting_output);
        workareaSpinner = (Spinner) findViewById(R.id.spinner_set_work_area);
        buttonSetOutput = (Button) findViewById(R.id.button_set_output);
        buttonSetWork = (Button) findViewById(R.id.button_set_work_area);

        //get workArea list
        workAreaStrings = getResources().getStringArray(R.array.work_area);
        for (String area : workAreaStrings) {
            listWorkarea.add(area);
        }

        //get Output list
        for (String output : outputStrings) {
            listOutput.add(output);
        }

        //Spinner(下拉選單) set adapter
        outputSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, listOutput));
        workareaSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, listWorkarea));
    }

    //set the button & spinner listener
    private void listener()
    {
        //註冊buttonSetOutpu監聽器(設置輸出功率button)
        buttonSetOutput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(cmdManager.setOutputPower(outputValue)){
                    Toast.makeText(getApplicationContext(), "設置成功", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "設置失敗", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //註冊buttonSetWork監聽器(設置工作區域button)
        buttonSetWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(cmdManager.setWorkArea(area) == 0){
                    //set globalVariable
                    GlobalVariable globalVariable = ((GlobalVariable)getApplicationContext());
                    globalVariable.setLocId(area);

                    Toast.makeText(getApplicationContext(), "設置成功", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "設置失敗", Toast.LENGTH_SHORT).show();
                }
            }
        });


        //註冊outputSpinner選項選擇監聽器
        outputSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View view, int position, long id)
            {
                outputValue = 2700 - (position*100);
                Log.e(TAG, outputValue + "");
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            { }
        });

        //註冊workareaSpinner選項選擇監聽器
        workareaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View view,
                                       int position, long id) {
                String mArea = listWorkarea.get(position);
                if("中國".equals(mArea)){
                    area = 1;
                }else if("中山大學".equals(mArea)){
                    area = 2;
                }else if("陽明大學".equals(mArea)){
                    area = 3;
                }else if("台灣大學".equals(mArea)){
                    area = 4;
                }else if("淹水的桃園機場".equals(mArea)){
                    area = 5;
                }
                Log.e(TAG, area + "");
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            { }
        });
    }


}
