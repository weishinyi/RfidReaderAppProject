package com.example.oo_raiser.rfidreaderapp.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import java.util.UUID;

import android.os.Message;
import android.util.Log;

/**
 * 等待遠端藍芽連接的Thread
 */

public class AcceptThread extends Thread{
    private static String TAG = "AcceptThread";

    private static final int CONNECT_SUCCEED_N = 6;

    private Handler handler;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothServerSocket serverSocket;
    private BluetoothSocket socket;

    //Constructor
    public AcceptThread(Handler h, BluetoothAdapter adapter)
    {
        handler = h;
        bluetoothAdapter = adapter;
    }

    public BluetoothSocket getSocket()
    {
        return socket;
    }

    public void run()
    {
        //get socket
        try{
            serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("BluetoothChatSecure", UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66"));
            Log.i(TAG,"開始等待遠端藍芽連接");
            socket = serverSocket.accept();
            Log.i(TAG,"接收遠端藍芽連接成功");

        }catch (Exception e){
            e.printStackTrace();
            Log.i(TAG,"接收遠端藍芽連接失敗");
            socket = null;
        }

        if(socket!=null)
        {
            Message msg = new Message();
            Bundle bundle = new Bundle();
            bundle.putString("name",socket.getRemoteDevice().getName());
            msg.setData(bundle);
            msg.what = CONNECT_SUCCEED_N;
            handler.sendMessage(msg);
        }
    }

}
