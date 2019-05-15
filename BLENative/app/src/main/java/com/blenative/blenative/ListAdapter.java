package com.blenative.blenative;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapter extends BaseAdapter{

    Context context;
    ArrayList<String> list;
    ArrayList<Integer> rssis;
    LayoutInflater inflater;

    public ListAdapter(Context appContext, ArrayList<String> list, ArrayList<Integer> rssis){
        this.context = context;
        this.list = list;
        this.rssis = rssis;
        inflater = LayoutInflater.from(appContext);
    }

    public int getCount(){
        return list.size();
    }

    public Object getItem(int i){
        return null;
    }

    public long getItemId(int i){
        return 0;
    }

    public View getView(int i, View view, ViewGroup viewGroup){
        view = inflater.inflate(R.layout.ble_list, null);
        TextView macAddress = (TextView) view.findViewById(R.id.mac);
        TextView rssi = (TextView) view.findViewById(R.id.rssi);
        macAddress.setText(list.get(i));
        rssi.setText("rssi: " + rssis.get(i));
        return view;
    }

}
