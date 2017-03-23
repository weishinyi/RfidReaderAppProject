package com.example.oo_raiser.rfidreaderapp.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.example.oo_raiser.rfidreaderapp.BluetoothActivity;
import com.example.oo_raiser.rfidreaderapp.command.Tools;

/**
    已建立連接後啟動的線程，需要傳進來兩個參數
    socket用來獲取輸入流，讀取遠程藍牙發送過來的消息
    handler用來在收到數據時發送消息
*/

public class ConnectedThread extends Thread{

    private static final int RECEIVE_MSG = 7;
    private static final int SEND_MSG=8;
    private boolean isStop;
    private static BluetoothSocket socket;
    private static Handler handler;
    private static InputStream is;
    private static OutputStream os;

    private static String TAG = "ConnectedThread";

    //Constructor
    public ConnectedThread(BluetoothSocket s, Handler h)
    {
        socket = s;
        handler = h;
        isStop = false;
    }

    public static void setHandler(Handler h)
    {
        handler = h;
    }

    public void run()
    {
        Log.i(TAG,"[connectedThread]: run()");
        byte[] buf;

        buf = new byte[1024];
        try {
            try{
                is = socket.getInputStream();
                os = socket.getOutputStream();
                Log.i(TAG,"connect success");
            }catch (IOException e){
                e.printStackTrace();
            }

        }catch (Exception e){
            e.printStackTrace();
            Log.i(TAG,"disconnect");

            byte[] temp = "連接已斷開".getBytes();
            sendMsgToHandler(temp, BluetoothActivity.CONNECT_INTERRUPT);
            isStop = true;
        }

    }

    @Override
    public void interrupt()
    {
        Log.i(TAG,"[connectedThread]: interrupt()");
        isStop = true;

        //close the InputStream
        if(is!=null)
        {
            try{
                is.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        //close the OutputStream
        if(os!=null)
        {
            try{
                os.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        super.interrupt();
    }

    public static InputStream getSocketInoutStream(){
        return is;
    }

    public static OutputStream getSocketOutoutStream(){
        return os;
    }

    public static void write(byte[] buf)
    {
        try{
            os = socket.getOutputStream();
            os.write(buf);
        }catch (Exception e){
            e.printStackTrace();
        }

        Log.i(TAG,buf.length+"---");
        sendMsgToHandler(buf, SEND_MSG);
    }

    //send msg to handler
    private static void sendMsgToHandler(byte[] buf, int mode)
    {
        String  msgStr = Tools.Byte2HexString(buf, buf.length);
        Log.i(TAG, msgStr);

        Bundle bundle = new Bundle();
        bundle.putString("str", msgStr);

        Message msg = new Message();
        msg.setData(bundle);
        msg.what = mode;

        handler.sendMessage(msg);
    }
}
