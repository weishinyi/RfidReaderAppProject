package com.example.oo_raiser.rfidreaderapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.oo_raiser.rfidreaderapp.bluetooth.ConnectedThread;
import com.example.oo_raiser.rfidreaderapp.command.CommandThread;
import com.example.oo_raiser.rfidreaderapp.command.InventoryInfo;
import com.example.oo_raiser.rfidreaderapp.command.NewSendCommendManager;
import com.example.oo_raiser.rfidreaderapp.command.Tools;
import com.example.oo_raiser.rfidreaderapp.entity.EPC;
import com.example.oo_raiser.rfidreaderapp.util.Util;
import com.example.oo_raiser.rfidreaderapp.webApiHelper.ConnectHelper;
import com.example.oo_raiser.rfidreaderapp.webApiHelper.webApiUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class InventoryTagActivity extends AppCompatActivity {

    private String TAG = "InventoryTagActivity";

    //UI item
    private EditText editCountTag;
    private Button buttonClear;
    private Button buttonreadTag;
    private Button buttonSend;
    private RadioGroup radioGroup;
    private RadioButton rbSingle;
    private RadioButton rbLoop;
    private ListView listViewTag;
    private TextView textTitle;

    // 超高頻指令管理者
    private NewSendCommendManager manager;

    //bluetooth input/output stream
    private InputStream is;
    private OutputStream os;

    private boolean isSingleRead = false;

    private CommandThread commthread;
    public static final int READ_TAG = 2001;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Log.i(TAG, msg.getData().getString("str"));
            switch (msg.what)
            {
                case BluetoothActivity.CONNECT_INTERRUPT://連接中斷
                    BluetoothActivity.connFlag = false;
                    Toast.makeText(getApplicationContext(), "連接中斷", Toast.LENGTH_SHORT).show();
                    break;
                case InventoryTagActivity.READ_TAG:// 讀標籤數據
                    break;
                default:
                    break;
            }
        };
    };

    //onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_tag);

        textTitle = (TextView) findViewById(R.id.textView_title);
        textTitle.setText(R.string.inventory_tag);
        textTitle.append("--已連接");


        //set the Handler
        ConnectedThread.setHandler(mHandler);

        //initialize the UI
        this.initUI();

        //set the button & RadioButton listener
        this.listner();

        //get bluetooth input/output stream
        is = ConnectedThread.getSocketInoutStream();
        os = ConnectedThread.getSocketOutoutStream();

        //Send Commend Manager
        manager = new NewSendCommendManager(is, os);

        //start Threads
        new RecvThread().start();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new SendCmdThread().start();

        //initialize the SoundPool
        Util.initSoundPool(this);

    }

    //initialize the UI
    private void initUI() {
        editCountTag = (EditText) findViewById(R.id.editText_tag_count);
        buttonClear = (Button) findViewById(R.id.button_clear_data);
        buttonreadTag = (Button) findViewById(R.id.button_inventory);
        buttonSend = (Button) findViewById(R.id.button_sendData);
        radioGroup = (RadioGroup) findViewById(R.id.RgInventory);
        rbSingle = (RadioButton) findViewById(R.id.RbInventorySingle);
        rbLoop = (RadioButton) findViewById(R.id.RbInventoryLoop);
        listViewTag = (ListView) findViewById(R.id.listView_tag);
    }

    //onKeyDown(按下返回建)
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            isRuning = false;
            if (commthread != null) {
                commthread.interrupt();
            }
            isRecv = false ;
            isSend = false ;
            isRuning = false ;
            isRunning = false ;
        }
        return super.onKeyDown(keyCode, event);
    }

    //the button & RadioButton listener
    private void listner() {

        //read tag by once
        rbSingle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    isSingleRead = true;
                    isSend = false;
                    isRecv = false;
                    buttonreadTag.setText("讀標籤");
                    Log.i(TAG, "isSingle --- >" + isSingleRead);
                }
            }
        });

        //read tag by loop
        rbLoop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    isSingleRead = false;
                    Log.i(TAG, "isSingle --- >" + isSingleRead);
                }
            }
        });

        //read tag button
        buttonreadTag.setOnClickListener(new ButtonreadtagListner());

        //clear button
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                editCountTag.setText("");
                listEPC.removeAll(listEPC);
                listViewTag.setAdapter(null);
            }
        });

        //Send button
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{

                    if(listEPC.size()!=0)
                    {
                        //prepare the JSONObject data
                        JSONObject jsonObj = new JSONObject();
                        jsonObj.put("userId", "1");
                        jsonObj.put("locationId","1");

                        JSONArray jArray = new JSONArray();
                        for(EPC item : listEPC)
                        {
                            JSONObject j = new JSONObject();
                            j.put("Epc",item.getEpc());
                            j.put("count",item.getCount());

                            jArray.put(j);
                        }
                        jsonObj.put("epcList",jArray);


                        //new 一個 SendDataToServerRequestTask 來執行
                        new InventoryTagActivity.SendDataToServerRequestTask(InventoryTagActivity.this).execute(jsonObj);

                    }else{
                        Toast.makeText(InventoryTagActivity.this, "沒有資料需要傳送",Toast.LENGTH_SHORT).show();
                    }


                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });

    }//end listner

    List<InventoryInfo> listTag;//單次讀標籤返回的數據
    boolean isRuning = false;
    //ButtonreadtagListner class (讀標籤)
    class ButtonreadtagListner implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.i(TAG, "buttonreadTag click----");
            //單次
            if(isSingleRead){
                listTag = manager.inventoryRealTime();
                if (listTag != null && !listTag.isEmpty()) {
                    for(InventoryInfo epc : listTag){
                        addToList(listEPC, epc);
                    }
                }
            }else{
                //循環
                if(isSend){
                    isSend = false;
                    try {
                        Thread.sleep(500); //maybe是間隔時間
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    isRecv = false;
                    buttonreadTag.setText("讀標籤");
                }else{
                    isSend = true;
                    isRecv = true;
                    buttonreadTag.setText("停止");
                }
            }

        }

    }//end class

    boolean isRunning = true ;  //控制發送接收線程
    boolean isSend = false ;  //控制發送指令

    //Send Cmd Thread class(發送盤存指令執行緒class)
    private class SendCmdThread extends Thread{
        @Override
        public void run() {
            //盤存指令
            byte[] cmd = { (byte)0xAA, (byte) 0x00, (byte) 0x22, (byte) 0x00,
                    (byte) 0x00, (byte) 0x22, (byte)0x8E };
            while(isRunning){
                if(isSend){
                    try {
                        ConnectedThread.getSocketOutoutStream().write(cmd);
                    } catch (IOException e) {
                        isSend = false ;
                        isRunning = false ;
                        Log.e(TAG, "Socket 連接出錯" + e.toString());
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            super.run();
        }
    }

    boolean isRecv = false;
    //Recv Thread class (接收線程執行緒class)
    private class RecvThread extends Thread{
        @Override
        public void run() {
            InputStream is = ConnectedThread.getSocketInoutStream();
            int size = 0;
            byte[] buffer = new byte[256];
            byte[] temp = new byte[512];
            int index = 0;  //temp有效數據指向
            int count = 0;  //temp有效數據長度
            while(isRunning){
                if(isRecv){
                    try {
                        Thread.sleep(20);
                        size = is.read(buffer);
                        if(size > 0){
                            count += size ;
                            //超出temp長度清空
                            if(count > 512){
                                count = 0;
                                Arrays.fill(temp, (byte)0x00);
                            }
                            //先將接收到的數據拷到temp中
                            System.arraycopy(buffer, 0, temp, index, size);
                            index = index + size ;
                            if(count > 7){
								//Log.e(TAG, "temp: " + Tools.Bytes2HexString(temp, count));
                                //判斷AA022200
                                if((temp[0] == (byte)0xAA)&& (temp[1] == (byte)0x02) && (temp[2] == (byte)0x22) && (temp[3] == (byte)0x00)){
                                    //正確數據位長度等於RSSI（1個字節）+PC（2個字節）+EPC
                                    int len = temp[4]&0xff;
                                    if(count < len + 7){//數據區尚未接收完整
                                        continue;
                                    }
                                    if(temp[len + 6] != (byte)0x8E){//數據區尚未接收完整
                                        continue ;
                                    }
                                    //得到完整數據包
                                    byte[] packageBytes = new byte[len + 7];
                                    System.arraycopy(temp, 0, packageBytes, 0, len + 7);
									//Log.e(TAG, "packageBytes: " + Tools.Bytes2HexString(packageBytes, packageBytes.length));
                                    //校驗數據包
                                    byte crc = checkSum(packageBytes);
                                    InventoryInfo info = new InventoryInfo();
                                    if( crc == packageBytes[len + 5]){
                                        //RSSI
                                        info.setRssi(temp[5]);
                                        //PC
                                        info.setPc(new byte[]{temp[6],temp[7]});
                                        //EPC
                                        byte[] epcBytes = new byte[len - 5];
                                        System.arraycopy(packageBytes, 8, epcBytes, 0, len - 5);
                                        info.setEpc(epcBytes);
                                        Util.play(1, 0);//播放提示音
                                        addToList(listEPC, info);
                                    }
                                    count = 0;
                                    index = 0;
                                    Arrays.fill(temp, (byte)0x00);
                                }else{
                                    //包錯誤清空
                                    count = 0;
                                    index = 0;
                                    Arrays.fill(temp, (byte)0x00);
                                }
                            }

                        }
                    } catch (Exception e) {
                        isRunning = false ;
                        Log.e(TAG, "Socket 連接出錯" + e.toString());
                    }
                }
            }
            super.run();
        }
    }//end class

    //check sum
    public byte checkSum(byte[] data) {
        byte crc = 0x00;
        // 從指令類型累加到參數最後一位
        for (int i = 1; i < data.length - 2; i++) {
            crc += data[i];
        }
        return crc;
    }

    List<EPC> listEPC = new ArrayList<EPC>(); //EPC列表

    List<Map<String, Object>> listMap ;
    // 將讀取的EPC添加到LISTVIEW
    private void addToList(final List<EPC> list, final InventoryInfo info) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String epc = Tools.Byte2HexString(info.getEpc(), info.getEpc().length);
                String pc = Tools.Byte2HexString(info.getPc(), info.getPc().length);
                int rssi = info.getRssi();
                // 第一次讀入數據
                if (list.isEmpty()) {
                    EPC epcTag = new EPC();
                    epcTag.setEpc(epc);
                    epcTag.setCount(1);
                    epcTag.setPc(pc);
                    epcTag.setRssi(rssi);
                    list.add(epcTag);
                } else {
                    for (int i = 0; i < list.size(); i++) {
                        EPC mEPC = list.get(i);
                        //  list中有此EPC
                        if (epc.equals(mEPC.getEpc())) {
                            mEPC.setCount(mEPC.getCount() + 1);
                            mEPC.setRssi(rssi);
                            mEPC.setPc(pc);
                            list.set(i, mEPC);
                            break;
                        } else if (i == (list.size() - 1)) {
                            // list中沒有此epc
                            EPC newEPC = new EPC();
                            newEPC.setEpc(epc);
                            newEPC.setCount(1);
                            newEPC.setPc(pc);
                            newEPC.setRssi(rssi);
                            list.add(newEPC);
                        }
                    }
                }
                // 將數據添加到ListView
                listMap = new ArrayList<Map<String, Object>>();
                int idcount = 1;
                for (EPC epcdata : list) {
                    Map<String, Object> map = new HashMap<String, Object>();

                    map.put("EPC", epcdata.getEpc());
                    map.put("PC", epcdata.getPc() +"");
                    map.put("RSSI", epcdata.getRssi()+ "dBm");
                    map.put("COUNT", epcdata.getCount());
                    idcount++;
                    listMap.add(map);
                }
                editCountTag.setText("" + listEPC.size());
                listViewTag.setAdapter(new SimpleAdapter(InventoryTagActivity.this,
                        listMap, R.layout.list_epc_item, new String[] {
                        "EPC", "PC","RSSI","COUNT" }, new int[] {
                        R.id.textView_item_epc, R.id.textView_item_pc,
                        R.id.textView_item_rssi,R.id.textView_item_count }));
            }
        });
    }

    // class
    //AsyncTask<Params, Progress, Result>
    //  Params: 參數，你要餵什麼樣的參數給它。
    //  Progress: 進度條，進度條的資料型態要用哪種
    //  Result: 結果，你希望這個背景任務最後會有什麼樣的結果回傳給你。
    public class SendDataToServerRequestTask extends AsyncTask<JSONObject, Void, String>
    {
        private Context myContext;
        private ProgressDialog myDialog;

        ConnectivityManager connManager;
        NetworkInfo networkInfo;
        ConnectHelper service;

        //Constructor
        public SendDataToServerRequestTask(Context context)
        {
            this.myContext = context;
        }

        //onPreExecute: 執行前，一些基本設定可以在這邊做。
        @Override
        protected void onPreExecute() {
            //get item to connect to network
            connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            networkInfo = connManager.getActiveNetworkInfo();
            service = new ConnectHelper(connManager);

            //show the ProgressDialog
            myDialog = new ProgressDialog(myContext);
            myDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myDialog.setMessage(webApiUtil.DOWNLOADING);
            myDialog.setCancelable(false);
            myDialog.show();
        }


        //doInBackground: 執行中，在背景做任務。
        @Override
        protected String doInBackground(JSONObject... params) {
            String result = null;

            //判斷是否有開啟網路連線
            if(networkInfo!=null && networkInfo.isConnected())
            {
                String urlStr = webApiUtil.SendRecodeUrl;
                result = service.RequestWebApiPost(urlStr, params[0]);
            }

            return result;
        }


        //onPostExecute: 執行後，最後的結果會在這邊。
        @Override
        protected void onPostExecute(String resultStr) {
            //Respond to the user
            try{
                if(resultStr==null)
                {
                    Toast.makeText(InventoryTagActivity.this, "No Connection!!\n請開啟網路連線！", Toast.LENGTH_SHORT).show();
                }else if(resultStr.equals(webApiUtil.SERVER_CONNENTFAIL) || resultStr.equals(webApiUtil.SERVER_CONNENTFAIL) || resultStr.equals(webApiUtil.WIFI_CONNECTFAIL))
                {
                    Toast.makeText(InventoryTagActivity.this, resultStr, Toast.LENGTH_SHORT).show();
                }else{
                    //send data to server success!
                    Toast.makeText(InventoryTagActivity.this, resultStr, Toast.LENGTH_SHORT).show();
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            //close the dialog
            if (myDialog.isShowing())
            {
                myDialog.dismiss();
            }
        }
    }


}
