package com.example.oo_raiser.rfidreaderapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.oo_raiser.rfidreaderapp.command.QueryAdapter;
import com.example.oo_raiser.rfidreaderapp.entity.Barcode;
import com.example.oo_raiser.rfidreaderapp.webApiHelper.ConnectHelper;
import com.example.oo_raiser.rfidreaderapp.webApiHelper.webApiUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class QueryActivity extends AppCompatActivity {

    //region ------ 物件宣告 ------
    private String TAG = "QueryActivity";

    //UI
    private TextView textTitle ;
    private TextView textView_count;
    private ListView listView_queryData;
    private Button button_refresh;

    private int timeCount = 0;


    //test
    String userSeq ="1";
    private String urlstr = "http://10.10.0.125:39643/api/Dashboard/Index/0"; //連本機server

    //handler
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
                    new GetDataFromServerRequestTask(QueryActivity.this).execute(userSeq);

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

        //new 一個 GetDataFromServerRequestTask 來執行
        new GetDataFromServerRequestTask(QueryActivity.this).execute(userSeq);

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
                //Toast.makeText(QueryActivity.this,"刷新資料",Toast.LENGTH_SHORT).show();
                timeCount = 0;

                //get data from server and refresh listView
                new GetDataFromServerRequestTask(QueryActivity.this).execute(userSeq);

            }
        });
    }

    //endregion

    //region ------ class ------

    //非同步任務class
    //AsyncTask<Params, Progress, Result>
    //  Params: 參數，你要餵什麼樣的參數給它。
    //  Progress: 進度條，進度條的資料型態要用哪種
    //  Result: 結果，你希望這個背景任務最後會有什麼樣的結果回傳給你。
    public class GetDataFromServerRequestTask extends AsyncTask<String, Void, String>
    {

        private Context myContext;
        private ProgressDialog myDialog;

        ConnectivityManager connManager;
        NetworkInfo networkInfo;
        ConnectHelper service;

        //Constructor
        public GetDataFromServerRequestTask(Context context)
        {
            this.myContext = context;
        }

        //onPreExecute: 執行前，一些基本設定可以在這邊做。
        @Override
        protected void onPreExecute()
        {
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
        protected String doInBackground(String... params)
        {
            String result = null;

            //判斷是否有開啟網路連線
            if(networkInfo!=null && networkInfo.isConnected())
            {
                String urlStr = webApiUtil.QueryRecodeUrl + params[0];
                result = service.RequestWebApiGet(urlStr);
            }

            return result;
        }

        //onPostExecute: 執行後，最後的結果會在這邊。
        @Override
        protected void onPostExecute(String resultStr)
        {
            //Respond to the user
            try{
                if(resultStr==null)
                {
                    Toast.makeText(QueryActivity.this, "No Connection!!\n請開啟網路連線！", Toast.LENGTH_SHORT).show();
                }else if(resultStr.equals(webApiUtil.SERVER_CONNENTFAIL) || resultStr.equals(webApiUtil.SERVER_CONNENTFAIL) || resultStr.equals(webApiUtil.WIFI_CONNECTFAIL))
                {
                    Toast.makeText(QueryActivity.this, resultStr, Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(QueryActivity.this, "刷新資料!", Toast.LENGTH_SHORT).show();

                    String jstr = resultStr.substring(1,resultStr.length()-1).replace("\\","");
                    JSONArray jarr = new JSONObject(jstr).getJSONArray("BarcodeList");
                    int jarrlen = jarr.length();


                    List<Barcode> listBarcode = new ArrayList<Barcode>(); //Barcode列表
                    for(int i=0; i<jarrlen; i++)
                    {
                        JSONObject job = jarr.getJSONObject(i);
                        Barcode b = new Barcode();
                        b.setBarcodeSeq(Integer.parseInt(job.getString("BarcodeSeq")));
                        b.setBarcode(job.getString("Barcode"));
                        b.setBarcodeCreateTime(job.getString("BarcodeCreateTime").replace("T","  "));
                        b.setCar_ID(Integer.parseInt(job.getString("Car_ID")));
                        b.setCar_Number(job.getString("Car_Number"));
                        b.setEmp_ID(Integer.parseInt(job.getString("Emp_ID")));
                        b.setEmp_Name( job.getString("Emp_Name").toString().trim());
                        b.setLoc_ID(Integer.parseInt(job.getString("Loc_ID")));
                        b.setLoc_Name(job.getString("Loc_Name"));
                        b.setLoc_Address(job.getString("Loc_Address"));
                        b.setUpdateTime(job.getString("UpdateTime"));
                        b.setUpdateUserId(Integer.parseInt(job.getString("UpdateUser")));
                        b.setCount(Integer.parseInt( job.getString("Count")));

                        listBarcode.add(b);
                    }

                    //update listView
                    QueryAdapter adpQuery = new QueryAdapter(QueryActivity.this, listBarcode);
                    listView_queryData.setAdapter(adpQuery);

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

    //endregion
}
