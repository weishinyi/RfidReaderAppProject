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
import com.example.oo_raiser.rfidreaderapp.entity.Barcode;

import java.util.List;

/**
 * 為ListView提供內容的自定義Adapter
 */

public class QueryAdapter extends BaseAdapter {

    private Context context;
    private List<Barcode>list;

    //Constructor
    public QueryAdapter(Context c, List<Barcode> l)
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
        LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.list_barcode_item,null);

        TextView textView_dateTime = (TextView)layout.findViewById(R.id.DateTime);
        TextView textView_EmpName = (TextView)layout.findViewById(R.id.EmpName);
        TextView textView_LocName = (TextView)layout.findViewById(R.id.LocName);
        TextView textView_Count = (TextView)layout.findViewById(R.id.Count);
        TextView textView_Barcode = (TextView)layout.findViewById(R.id.Barcode);

        textView_dateTime.setText(list.get(position).getBarcodeCreateTime());
        textView_EmpName.setText(list.get(position).getEmp_Name());
        textView_LocName.setText(list.get(position).getLoc_Name());
        textView_Count.setText(Integer.toString(list.get(position).getCount()));
        textView_Barcode.setText(list.get(position).getBarcode());

        return layout;
    }
}
