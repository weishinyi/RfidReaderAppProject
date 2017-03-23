package com.example.oo_raiser.rfidreaderapp.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.util.UUID;

/**
    已建立連接後啟動的線程，需要傳進來兩個參數
    socket用來獲取輸入流，讀取遠程藍牙發送過來的消息
    handler用來在收到數據時發送消息
*/

public class ConnectThread extends Thread{
    private static String TAG = "ConnectThread";

    private static final UUID UUID_CHAT = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final UUID UUID_COM = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";   //SPP服務UUID號

    private static final int CONNECT_FAIL = 4;
    private static final int CONNECT_SUCCEED_P = 5;

    private BluetoothDevice device;
    private Handler handler;
    private BluetoothSocket socket;

    private boolean isChat;

    //Constructor
    public ConnectThread(BluetoothDevice d, Handler h, boolean i)
    {
        device = d;
        handler = h;
        isChat = i;
    }

    public void run()
    {
        try{
            socket = device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
            socket.connect();
        }catch (Exception e){
            e.printStackTrace();
            handler.sendEmptyMessage(CONNECT_FAIL);
            socket = null;
        }

        if(socket != null){
            Message msg = new Message();
            Bundle bundle = new Bundle();
            bundle.putString("name", device.getName());
            msg.setData(bundle);
            msg.what = CONNECT_SUCCEED_P;
            handler.sendMessage(msg);
        }
    }

    public BluetoothSocket getSocket(){
        return socket;
    }


}
