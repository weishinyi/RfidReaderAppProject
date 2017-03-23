package com.example.oo_raiser.rfidreaderapp.command;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.oo_raiser.rfidreaderapp.R;

import java.util.List;

/**
 * 為ListView提供內容的自定義Adapter
 * 賣萌專用..其實用ArrayAdapter就能完成功能了
 */

public class MyAdapter extends BaseAdapter{

    private Context context;
    private List<BluetoothDevice> list;

    //Constructor
    private MyAdapter(Context c, List<BluetoothDevice> l)
    {
        context = c;
        list = l;
    }

    @Override
    public int getCount() {
        return list.size();
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

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.list_item, null);

        TextView name = (TextView)layout.findViewById(R.id.name);
        TextView address = (TextView)layout.findViewById(R.id.address);
        name.setTextSize(20f);
        name.setText(list.get(position).getName());
        address.setText(list.get(position).getAddress());

        return layout;
    }
}
