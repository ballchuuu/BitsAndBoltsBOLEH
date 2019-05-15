package com.blenative.blenative;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;

import static android.support.v4.app.ActivityCompat.startActivityForResult;


public class BLEService extends IntentService {



    public BLEService() {
        super("BLEService");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {

    }
}
