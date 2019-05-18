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
    ArrayList<ArrayList<Integer>> rssiAvgs = new ArrayList();
    LayoutInflater inflater;

    public ListAdapter(Context appContext, ArrayList<String> list, ArrayList<Integer> rssis, ArrayList<ArrayList<Integer>> rssiAvgs){
        this.context = context;
        this.list = list;
        this.rssis = rssis;
        this.rssiAvgs = rssiAvgs;
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
//        TextView rssi = (TextView) view.findViewById(R.id.rssi);
        TextView rssiAvg = (TextView) view.findViewById(R.id.rssiAvg);
        if(rssiAvgs != null){
            int sum = 0;
            for(int j = 0; j < rssiAvgs.get(i).size(); j++){
                sum += rssiAvgs.get(i).get(j);
            }
            rssiAvg.setText("avg: " + (sum / rssiAvgs.get(i).size()));
        }
        macAddress.setText(list.get(i));
//        rssi.setText("rssi: " + rssis.get(i));
        return view;
    }

}
