package com.blenative.blenative;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothScanner;
    private Handler mHandler;
    private ArrayAdapter arrayAdapter;
    private ListView listView;
    private ListView wifiListView;
    private String[] whitelist = {"D9:9B:DE:C0:3E:8F","CC:EA:DB:79:AD:73","FD:89:D3:B9:BC:9A",
                                  "FE:89:59:3D:83:F8","DE:C2:26:2F:B6:BF","C1:19:93:E0:C9:2F",
                                  "F3:3B:79:CC:CD:2E","CF:33:7F:9E:A4:3B","F4:C9:8D:E0:78:62",
                                  "D2:F6:90:C9:64:64","CA:1F:86:10:E7:CD","C9:47:66:A7:69:AD",
                                  "EE:9A:45:AC:25:C5"};
    private ArrayList<String> macAddresses = new ArrayList<>();
    private ArrayList<Integer> rssis = new ArrayList();
    private ArrayList<ArrayList<Integer>> rssiAvgs = new ArrayList();

    private BroadcastReceiver receiver;

    private WifiManager wifiManager;
    private volatile boolean wifiScanDone = true;

    private ArrayList<String> wifiMacAddresses = new ArrayList<>();
    private ArrayList<Integer> wifiRssis = new ArrayList();
    private ArrayList<ArrayList<Integer>> wifiRssiAvgs = new ArrayList();
    private JSONObject payload;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent serviceIntent = new Intent(this, BLEService.class);
        startService(serviceIntent);

        //request permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.CHANGE_WIFI_STATE,
                        Manifest.permission.ACCESS_WIFI_STATE
                    },
                    0);
        }

        listView = (ListView) findViewById(R.id.list);
        wifiListView = (ListView) findViewById(R.id.wifiList);
        editText = (EditText) findViewById(R.id.editText);

        mHandler = new Handler();
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
        }
        bluetoothScanner = bluetoothAdapter.getBluetoothLeScanner();

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null && !wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        Toast.makeText(getApplicationContext(), "Started!", Toast.LENGTH_SHORT).show();

        receiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(wifiScanDone){
                    return;
                }
                Log.i("wifi broadcast", "received!");
                parseWifi();
                wifiScanDone = true;
            }
        };

        IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(receiver, filter);


        final ToggleButton b = (ToggleButton) findViewById(R.id.myButton);
        b.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v){
                boolean on = ((ToggleButton) v).isChecked();
                if (on) {
                    Toast.makeText(getApplicationContext(), "pressed!", Toast.LENGTH_SHORT).show();
                    //do something when toggle is on
                    macAddresses.clear();
                    rssis.clear();
                    rssiAvgs.clear();

                    wifiMacAddresses.clear();
                    wifiRssis.clear();
                    wifiRssiAvgs.clear();
//                    bluetoothScanner.startScan(mScanCallback);
                    Toast.makeText(getApplicationContext(), "Start scan!", Toast.LENGTH_SHORT).show();
                    payload = new JSONObject();
                    wifiScanDone = false;
                    wifiManager.startScan();
                    bluetoothScanner.startScan(mScanAvgCallback);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            bluetoothScanner.stopScan(mScanAvgCallback);
                            b.toggle();
                            parseWifi();
                            while(!wifiScanDone){
                                //do nothing
                            }
                            parseBLE();
//                              generatePayload();
                            String[] xy = editText.getText().toString().split(" ");
                            try {
                                payload.put("x", xy[0]);
                                payload.put("y", xy[1]);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            Log.i("Response", payload.toString());
                            new LocalizationTask().execute(payload.toString());
                        }
                    }, 15000);

                } else {
                    //do something when toggle is off
                    bluetoothScanner.stopScan(mScanCallback);
                    Toast.makeText(getApplicationContext(), "Stop scan!", Toast.LENGTH_SHORT).show();
                    Log.i("scan", "Stopped scan");
                }
            }
        });

        Button mapButton = (Button) findViewById(R.id.toMap);

        mapButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v){
                Intent myIntent = new Intent(getApplicationContext(), MapActivity.class);
                startActivity(myIntent);
            }
        });
    }

    public void parseWifi(){
        List<android.net.wifi.ScanResult> wifiScans = wifiManager.getScanResults();
        for(android.net.wifi.ScanResult s : wifiScans){
            wifiMacAddresses.add(s.BSSID.substring(0, 5) + " " + s.SSID);
            wifiRssis.add(s.level);
            try {
                payload.put(s.BSSID + " " + s.SSID, s.level);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        WifiListAdapter listAdapter = new WifiListAdapter(getApplicationContext(), wifiMacAddresses, wifiRssis);
        wifiListView.setAdapter(listAdapter);
    }

    public void parseBLE(){
        Log.i("Response ble", "" + rssiAvgs.size());
        for(int i = 0; i < rssiAvgs.size(); i++){
            try {
                payload.put(macAddresses.get(i), average(rssiAvgs.get(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private int average(ArrayList<Integer> list){
        int sum = 0;
        for(Integer i : list){
            sum += i;
        }
        return sum / list.size();
    }

    private void generatePayload(){
        for(int i = 0; i < rssiAvgs.size(); i++){
            try {
                payload.put(macAddresses.get(i), average(rssiAvgs.get(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.i("Response",macAddresses.get(0));
        for(int i = 0; i < wifiRssiAvgs.size(); i++){
            try {
                payload.put(wifiMacAddresses.get(i), average(wifiRssiAvgs.get(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.i("Response", payload.toString());
        new LocalizationTask().execute(payload.toString());


    }


    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            BluetoothDevice btDevice = result.getDevice();
            String address = btDevice.getAddress();
            boolean cont = false;
            for(String s : whitelist){
                if(s.equals(address)){
                    cont = true;
                }
            }
            if(!cont){
                return;
            }
            Log.i("callbackType", String.valueOf(callbackType));
            Log.i("result", result.toString());
            int loc = 0;
            boolean contains = false;
            for(int i = 0; i < macAddresses.size(); i++){
                if(macAddresses.get(i).equals(address)){
                    contains = true;
                    loc = i;
                }
            }
            if(!contains){
                macAddresses.add(address);
                rssis.add(result.getRssi());
            } else if(rssis.get(loc) != result.getRssi()){
                rssis.set(loc, result.getRssi());
            }
            ListAdapter listAdapter = new ListAdapter(getApplicationContext(), macAddresses, rssis, null);
            listView.setAdapter(listAdapter);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };

    private ScanCallback mScanAvgCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            BluetoothDevice btDevice = result.getDevice();
            String address = btDevice.getAddress();
            boolean cont = false;
            for(String s : whitelist){
                if(s.equals(address)){
                    cont = true;
                }
            }
            if(!cont){
                return;
            }
            Log.i("callbackType", String.valueOf(callbackType));
            Log.i("result", result.toString());
            int loc = 0;
            boolean contains = false;
            for(int i = 0; i < macAddresses.size(); i++){
                if(macAddresses.get(i).equals(address)){
                    contains = true;
                    loc = i;
                }
            }
            if(!contains){
                macAddresses.add(address);
                ArrayList<Integer> newList = new ArrayList<>();
                rssis.add(result.getRssi());
                newList.add(result.getRssi());
                rssiAvgs.add(newList);
            } else if(rssis.get(loc) != result.getRssi()){
                rssis.set(loc, result.getRssi());
            }
            rssiAvgs.get(loc).add(result.getRssi());
            ListAdapter listAdapter = new ListAdapter(getApplicationContext(), macAddresses, rssis, rssiAvgs);
            listView.setAdapter(listAdapter);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };

    class WifiBroadcastReceiver extends BroadcastReceiver {
        private static final String TAG = "MyBroadcastReceiver";
        @Override
        public void onReceive(Context context, Intent intent) {
            StringBuilder sb = new StringBuilder();
            sb.append("Action: " + intent.getAction() + "\n");
            sb.append("URI: " + intent.toUri(Intent.URI_INTENT_SCHEME).toString() + "\n");
            String log = sb.toString();
            Log.d(TAG, log);
            Toast.makeText(context, log, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            bluetoothScanner.stopScan(mScanCallback);
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            bluetoothScanner.stopScan(mScanCallback);
        }
        if(receiver!=null){
            unregisterReceiver(receiver);
        }
    }
}