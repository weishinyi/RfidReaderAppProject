package com.example.oo_raiser.rfidreaderapp.command;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * 為ListView提供內容的自定義Adapter
 */

public class QueryAdapter extends BaseAdapter {

    private Context context;

    //Constructor
    public QueryAdapter(Context c, List<BluetoothDevice> l)
    {

    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
