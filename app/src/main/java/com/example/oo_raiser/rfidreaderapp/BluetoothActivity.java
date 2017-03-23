package com.example.oo_raiser.rfidreaderapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.oo_raiser.rfidreaderapp.bluetooth.*;

public class BluetoothActivity extends AppCompatActivity {

    //region ------ 物件宣告 ------

    //bluetooth Adapter
    private BluetoothAdapter btAdapter;

    // 消息處理器使用的代號
    private static final int FOUND_DEVICE = 1; //發現設備
    private static final int START_DISCOVERY = 2; // 開始查找設備
    private static final int FINISH_DISCOVERY = 3; // 結束查找設備
    private static final int CONNECT_FAIL = 4; // 連接失敗
    private static final int CONNECT_SUCCEED_P = 5; // 主動連接成功
    private static final int CONNECT_SUCCEED_N = 6; // 收到連接成功
    private static final int RECEIVE_MSG = 7; // 收到消息
    private static final int SEND_MSG = 8; // 發送消息
    public static final int CONNECT_INTERRUPT = 101; //連接中斷

    //執行緒
    ConnectedThread connectedThread; //與遠程藍牙連接成功時啟動
    ConnectThread connectThread; // 用戶點擊列表中某一項，要與遠程藍牙連接時啟動
    AcceptThread acceptThread;

    // 連接設備對話框相關控件
    private Dialog dialog;
    private ProgressBar discoveryPro;
    private ListView foundList;
    List<BluetoothDevice> foundDevices;

    //UI
    ListView LvMain;
    private ArrayList<HashMap<String, String>> arrayMenu;
    private static ArrayList<String> deviceList = new ArrayList<String>();
    private SimpleAdapter adapter;
    private TextView textTitle ;


    public static boolean connFlag = false ;
    BluetoothSocket socket;

    private final int REQUEST_OPEN_BT = 101;

    private String TAG = "BluetoothActivity "; //debug用

    private BroadcastReceiver mReceiver; //廣播接受者，監聽藍牙狀態信息

    //endregion

    // Handler 消息處理器..日理萬機...
    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case FOUND_DEVICE:
                    //foundList.setAdapter(new );
                    break;
                case START_DISCOVERY:
                    break;
                case FINISH_DISCOVERY:
                    break;
                case CONNECT_FAIL:
                    break;
                case CONNECT_SUCCEED_P:
                case CONNECT_SUCCEED_N:
                    break;
                case CONNECT_INTERRUPT:
                    Toast.makeText(getApplicationContext(), "連接已斷開,請重新連接", Toast.LENGTH_SHORT).show();
                    textTitle.setText("連接已斷開");
                    connFlag = false;
                    break;
            }//end switch
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        initView();


    }

    //initialize the UI
    private void initView()
    {
        textTitle = (TextView)findViewById(R.id.textView_title);
        textTitle.setText("未連接設備");

        //get ListView
        LvMain = (ListView)findViewById(R.id.mainLv);

        //initialize menu
        arrayMenu = new ArrayList<HashMap<String, String>>();
        String[] array = {"連接", "斷開", "識別標籤", "讀取數據", "寫入數據", "參數設置", "鎖定標籤", "銷毀標籤", "退出"};
        for(int i=0; i<array.length; i++) {
            HashMap<String, String> item = new HashMap<String, String>();
            item.put("menuItem", array[i]);
            arrayMenu.add(item);
        }
        adapter = new SimpleAdapter(this, arrayMenu, R.layout.mainlv_items, new String[]{"menuItem"}, new int[]{R.id.TvMenu});
        LvMain.setAdapter(adapter);
        //LvMain.setOnClickListener(new Lv);
    }

}
