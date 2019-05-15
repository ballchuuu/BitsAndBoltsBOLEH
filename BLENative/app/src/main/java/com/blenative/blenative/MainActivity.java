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
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothScanner;
    private Handler mHandler;
    private ArrayAdapter arrayAdapter;
    private ListView listView;
    private ArrayList<String> macAddresses = new ArrayList<>();
    private ArrayList<Integer> rssis = new ArrayList();

    private final BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                int  rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                Toast.makeText(getApplicationContext(),"  RSSI: " + rssi + "dBm", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent serviceIntent = new Intent(this, BLEService.class);
        startService(serviceIntent);

        //request permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    0);
        }

        listView = (ListView) findViewById(R.id.list);

        mHandler = new Handler();
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
        }
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        bluetoothScanner = bluetoothAdapter.getBluetoothLeScanner();
        Toast.makeText(getApplicationContext(), "Started!", Toast.LENGTH_SHORT).show();

        ToggleButton b = (ToggleButton) findViewById(R.id.myButton);
        b.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v){
                boolean on = ((ToggleButton) v).isChecked();
                if (on) {
                    //do something when toggle is on
                    bluetoothScanner.startScan(mScanCallback);
                    Toast.makeText(getApplicationContext(), "Start scan!", Toast.LENGTH_SHORT).show();
                    Log.i("scan", "Started scan");
                } else {
                    //do something when toggle is off
                    bluetoothScanner.stopScan(mScanCallback);
                    Toast.makeText(getApplicationContext(), "Stop scan!", Toast.LENGTH_SHORT).show();
                    Log.i("scan", "Stopped scan");
                }
            }
        });

    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("callbackType", String.valueOf(callbackType));
            Log.i("result", result.toString());
            BluetoothDevice btDevice = result.getDevice();
            String address = btDevice.getAddress();
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
            ListAdapter listAdapter = new ListAdapter(getApplicationContext(), macAddresses, rssis);
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
        unregisterReceiver(receiver);
    }
}
