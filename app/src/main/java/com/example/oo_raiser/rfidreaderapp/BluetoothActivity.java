package com.example.oo_raiser.rfidreaderapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.Set;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.oo_raiser.rfidreaderapp.bluetooth.*;
import com.example.oo_raiser.rfidreaderapp.command.MyAdapter;

public class BluetoothActivity extends AppCompatActivity {

    //region ------ 物件宣告 ------
    private String TAG = "BluetoothActivity";

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

    private BroadcastReceiver mReceiver; //廣播接受者，監聽藍牙狀態信息

    //endregion

    //region --- Handler 消息處理器..日理萬機... ---
    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case FOUND_DEVICE:
                    foundList.setAdapter(new MyAdapter(BluetoothActivity.this, foundDevices));
                    break;
                case START_DISCOVERY:
                    discoveryPro.setVisibility(View.VISIBLE);
                    break;
                case FINISH_DISCOVERY:
                    discoveryPro.setVisibility(View.GONE);
                    break;
                case CONNECT_FAIL:
                    connFlag = false;
                    Toast.makeText(BluetoothActivity.this,"連接失敗",Toast.LENGTH_SHORT).show();
                    break;
                case CONNECT_SUCCEED_P:
                case CONNECT_SUCCEED_N:
                    Log.i(TAG,"藍芽連接成功");
                    if(msg.what == CONNECT_SUCCEED_P)
                    {
                        //接受執行緒不為Null
                        if(acceptThread!=null) {
                            acceptThread.interrupt();
                        }
                        socket = connectThread.getSocket();
                        connectedThread = new ConnectedThread(socket, mHandler);
                        connectedThread.start();

                    }else {
                        if(connectThread!=null){
                            connectThread.interrupt();
                        }
                        socket = acceptThread.getSocket();
                        connectedThread = new ConnectedThread(socket,mHandler);
                        connectedThread.start();
                    }
                    String deviceName = msg.getData().getString("name");
                    textTitle.setText("已連接: "+deviceName);
                    connFlag = true;
                    break;
                case CONNECT_INTERRUPT:
                    Toast.makeText(getApplicationContext(), "連接已斷開,請重新連接", Toast.LENGTH_SHORT).show();
                    textTitle.setText("連接已斷開");
                    connFlag = false;
                    break;
            }//end switch
        }
    };
    //endregion

    //region --- Override android functions ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        //initialize the UI
        initView();

        //register the Broad Receiver
        registerBroadReceiver();
    }

    @Override
    protected void onResume() {
        initBluetooth(); //初始化藍牙
        ConnectedThread.setHandler(mHandler); //設定Handler

        super.onResume();
    }

    //退出程序時處理一下後事，取消註冊廣播接收器，中止線程，關閉socket
    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Unregister mReceiver
        if(mReceiver != null)
        {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }

        //interrupt the thread
        if(connectThread != null)
        {
            connectThread.interrupt();
        }
        if(connectedThread != null)
        {
            connectedThread.interrupt();
        }
        if(acceptThread != null)
        {
            acceptThread.interrupt();
        }

        //close the socket
        if(socket!=null)
        {
            try{
                socket.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    //按返回建
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            //Unregister mReceiver
            if(mReceiver != null)
            {
                unregisterReceiver(mReceiver);
                mReceiver = null;
            }

            //interrupt the thread
            if(connectThread != null)
            {
                connectThread.interrupt();
            }
            if(connectedThread != null)
            {
                connectedThread.interrupt();
            }
            if(acceptThread != null)
            {
                acceptThread.interrupt();
            }

            //close the socket
            if(socket!=null)
            {
                try{
                    socket.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            //Reminder to turn off Bluetooth
            if(btAdapter.isEnabled())
            {
                Toast.makeText(BluetoothActivity.this, "請手動關閉藍牙", Toast.LENGTH_SHORT).show();
            }

            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "requestCode: " +requestCode+", resultCode: "+resultCode);

        if(requestCode==REQUEST_OPEN_BT && resultCode!=0)
        {
            Toast.makeText(getApplicationContext(), "bluetooth open success!", Toast.LENGTH_SHORT).show();
            // 查詢所有配對的設備
            Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
            // 判斷需要的設備是否存在
            if(pairedDevices.size()>0)
            {
                for(BluetoothDevice d :pairedDevices)
                {
                    Log.i(TAG,"Name: "+d.getName()+"; Address: " + d.getAddress());
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //endregion

    //region --- functions ---

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
        LvMain.setOnItemClickListener(new LvMainItemClickListener());
    }

    //register the Broad Receiver
    private void registerBroadReceiver()
    {
        //new the mReceiver
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String actionStr = intent.getAction();
                Log.i(TAG,"actionStr: "+actionStr);
                if(actionStr.equals(BluetoothDevice.ACTION_FOUND))
                {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    //if find the different device then add to device list
                    if(!isFoundDevices(device))
                    {
                        foundDevices.add(device);
                    }
                    Toast.makeText(BluetoothActivity.this,"找到藍芽設備: "+device.getName(),Toast.LENGTH_SHORT).show();
                    mHandler.sendEmptyMessage(FOUND_DEVICE);

                }else if(actionStr.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)){
                    mHandler.sendEmptyMessage(START_DISCOVERY);
                }else if(actionStr.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
                    mHandler.sendEmptyMessage(FINISH_DISCOVERY);
                }
            }
        };

        //register Receiver
        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter filter2 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        IntentFilter filter3 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver ,filter1);
        registerReceiver(mReceiver ,filter2);
        registerReceiver(mReceiver ,filter3);
    }

    //Whether to find the same device (是否找到同一台設備)
    private boolean isFoundDevices(BluetoothDevice device)
    {
        boolean flag = false;
        if(foundDevices != null && !foundDevices.isEmpty())
        {
            for(BluetoothDevice d : foundDevices)
            {
                if(device.getAddress().equals(d.getAddress())){
                   flag = true;
                }
            }
        }
        return flag;
    }

    //initialize bluetooth
    private void initBluetooth()
    {
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        //Does this device support Bluetooth?
        if(btAdapter == null)
        {
            Toast.makeText(getApplicationContext(),"此設備不支持藍牙",Toast.LENGTH_SHORT).show();
        }

        //check that is the Bluetooth available
        if(!btAdapter.isEnabled())
        {
            Intent enableBt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBt, REQUEST_OPEN_BT);
        }
    }

    //connect the  Bluetooth device
    private void connectBluetooth()
    {
        btAdapter.cancelDiscovery();
        btAdapter.startDiscovery();


        //通過LayoutInflater得到對話框中的三個控件 第一個ListView為局部變量，因為它顯示的是已配對的藍牙設備，不需隨時改變
		//第二個ListView和ProgressBar為全局變量
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog, null);
        discoveryPro =(ProgressBar)view.findViewById(R.id.discoveryPro);
        ListView bondedList = (ListView)view.findViewById(R.id.bondedList);
        foundList = (ListView)view.findViewById(R.id.foundList);

        //將已配對的藍牙設備顯示到第一個ListView中
        Set<BluetoothDevice> deviceSet = btAdapter.getBondedDevices();
        final List<BluetoothDevice> bondedDevices = new ArrayList<BluetoothDevice>();
        if(deviceSet.size()>0)
        {
            for(Iterator<BluetoothDevice>it=deviceSet.iterator(); it.hasNext();)
            {
                BluetoothDevice device = (BluetoothDevice)it.next();
                bondedDevices.add(device);
            }
        }
        bondedList.setAdapter(new MyAdapter(BluetoothActivity.this, bondedDevices));

        //將找到的藍牙設備顯示到第二個ListView中
        foundDevices = new ArrayList<BluetoothDevice>();
        foundList.setAdapter(new MyAdapter(BluetoothActivity.this, foundDevices));

        //兩個ListView綁定監聽
        bondedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                BluetoothDevice device = bondedDevices.get(position);
                connect(device);
            }
        });
        foundList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                BluetoothDevice device = foundDevices.get(position);
                connect(device);
            }
        });

        //AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(BluetoothActivity.this);
        builder.setMessage("請選擇要連接的藍牙設備").setPositiveButton("取消",
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        btAdapter.cancelDiscovery();
                    }
                });
        builder.setView(view);
        builder.create();
        dialog = builder.show();

    }

    //connecting
    private void connect(BluetoothDevice device)
    {
        btAdapter.cancelDiscovery();
        dialog.dismiss();
        Toast.makeText(this,"正在連接"+device.getName(),Toast.LENGTH_SHORT).show();
        connectThread = new ConnectThread(device, mHandler, true);
        connectThread.start();
    }


    //endregion

    //region --- class ---
    //class LvMainItemClickListener
    class LvMainItemClickListener implements AdapterView.OnItemClickListener
    {
        //on item click
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            HashMap<String,String> item = (HashMap<String,String>) LvMain.getItemAtPosition(position);
            String itemStr = item.get("menuItem");

            switch (itemStr)
            {
                case "連接":
                    //Toast.makeText(BluetoothActivity.this,"連接",Toast.LENGTH_SHORT).show();
                    if(connFlag)
                    {
                      Toast.makeText(getApplicationContext(),"請先斷開連接，再連接",Toast.LENGTH_SHORT ).show();
                    }else{
                        connectBluetooth();
                    }
                    break;
                case "斷開":
                    Toast.makeText(BluetoothActivity.this,"斷開",Toast.LENGTH_SHORT).show();
                    break;
                case "識別標籤":
                    Toast.makeText(BluetoothActivity.this,"識別標籤",Toast.LENGTH_SHORT).show();
                    break;
                case "讀取數據":
                    Toast.makeText(BluetoothActivity.this,"讀取數據",Toast.LENGTH_SHORT).show();
                    break;
                case "寫入數據":
                    Toast.makeText(BluetoothActivity.this,"寫入數據",Toast.LENGTH_SHORT).show();
                    break;
                case "參數設置":
                    Toast.makeText(BluetoothActivity.this,"參數設置",Toast.LENGTH_SHORT).show();
                    break;
                case "鎖定標籤":
                    Toast.makeText(BluetoothActivity.this,"鎖定標籤",Toast.LENGTH_SHORT).show();
                    break;
                case "銷毀標籤":
                    Toast.makeText(BluetoothActivity.this,"銷毀標籤",Toast.LENGTH_SHORT).show();
                    break;
                case "退出":
                    finish();
                    break;
            }
        }
    }

    //endregion

}
