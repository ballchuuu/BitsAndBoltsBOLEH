package com.blenative.blenative;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MapActivity extends AppCompatActivity {

    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1.0f;
    public static ImageView imageView;
    int windowwidth; // Actually the width of the RelativeLayout.
    int windowheight; // Actually the height of the RelativeLayout.
    private ViewGroup mRrootLayout;
    private int _xDelta;
    private int _yDelta;
    private boolean isOutReported = false;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothScanner;

    private ArrayList<String> macAddresses = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> rssiAvgs = new ArrayList();

    private BroadcastReceiver receiver;

    private WifiManager wifiManager;
    private volatile boolean wifiScanDone = true;
    private Context context = this;

    private Handler handler;

    private JSONObject payload;

    public static int x = 0;
    public static int y = 0;

    public static Bitmap bitmap;

    private String[] whitelist = {"D9:9B:DE:C0:3E:8F","CC:EA:DB:79:AD:73","FD:89:D3:B9:BC:9A",
            "FE:89:59:3D:83:F8","DE:C2:26:2F:B6:BF","C1:19:93:E0:C9:2F",
            "F3:3B:79:CC:CD:2E","CF:33:7F:9E:A4:3B","F4:C9:8D:E0:78:62",
            "D2:F6:90:C9:64:64","CA:1F:86:10:E7:CD","C9:47:66:A7:69:AD",
            "EE:9A:45:AC:25:C5"};

    public static Canvas canvas;

    private static String uniqueID = null;
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";
    public synchronized static String id(Context context) {
        if (uniqueID == null) {
            SharedPreferences sharedPrefs = context.getSharedPreferences(
                    PREF_UNIQUE_ID, Context.MODE_PRIVATE);
            uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(PREF_UNIQUE_ID, uniqueID);
                editor.commit();
            }
        }
        return uniqueID;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_main);
//        Intent serviceIntent = new Intent(this, BLEService.class);
//        startService(serviceIntent);



        BitmapFactory.Options myOptions = new BitmapFactory.Options();
        myOptions.inDither = true;
        myOptions.inScaled = false;
        myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// important

        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.floorplan, myOptions);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLUE);

        Bitmap workingBitmap = Bitmap.createBitmap(bitmap);
        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);

        canvas = new Canvas(mutableBitmap);
//        canvas.drawCircle(60, 50, 25, paint);

        imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setAdjustViewBounds(true);
        imageView.setImageBitmap(mutableBitmap);

        imageView=(ImageView)findViewById(R.id.imageView);
        mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothScanner = bluetoothAdapter.getBluetoothLeScanner();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        receiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            if(wifiScanDone){
                return;
            }
            parseWifi();
            wifiScanDone = true;
            }
        };
        IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(receiver, filter);

        handler = new Handler();
        repeatingTask.run();
    }


    Runnable repeatingTask = new Runnable() {
        @Override
        public void run() {
            macAddresses.clear();
            rssiAvgs.clear();
            payload = new JSONObject();
            wifiScanDone = false;
            wifiManager.startScan();
            bluetoothScanner.startScan(mScanAvgCallback);
            Log.i("BLE", "starting BLE scan");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bluetoothScanner.stopScan(mScanAvgCallback);
                    Log.i("BLE", "stopping BLE scan");
                    parseBLE();
                    while(!wifiScanDone){
                        //do nothing
                    }
                    try {
                        String UUID = id(context);
//                        Log.i("UUID", UUID);
                        payload.put("UUID", UUID);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
//                    Log.i("Response", payload.toString());
                    new UserLocationTask().execute("http://13.250.107.52:8080/getloc", payload.toString());
//                    Paint paint = new Paint();
//                    paint.setAntiAlias(true);
//                    paint.setColor(Color.BLUE);
//                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
//                    canvas.drawCircle(x * 40, y * 40, 5, paint);
                    repeatingTask.run();
                }
            }, 8000);
        }
    };

    public void parseWifi(){
        Log.i("wifi", "parsing wifi");
        List<ScanResult> wifiScans = wifiManager.getScanResults();
        for(android.net.wifi.ScanResult s : wifiScans){
            try {
                payload.put(s.BSSID + " " + s.SSID, s.level);
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

    private ScanCallback mScanAvgCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, android.bluetooth.le.ScanResult result) {
            BluetoothDevice btDevice = result.getDevice();
            String address = btDevice.getAddress();
            boolean cont = false;
            for (String s : whitelist) {
                if (s.equals(address)) {
                    cont = true;
                }
            }
            if (!cont) {
                return;
            }
//            Log.i("callbackType", String.valueOf(callbackType));
//            Log.i("result", result.toString());
            int loc = 0;
            boolean contains = false;
            for (int i = 0; i < macAddresses.size(); i++) {
                if (macAddresses.get(i).equals(address)) {
                    contains = true;
                    loc = i;
                }
            }
            if (!contains) {
                macAddresses.add(address);
                ArrayList<Integer> newList = new ArrayList<>();
                newList.add(result.getRssi());
                rssiAvgs.add(newList);
            }
            rssiAvgs.get(loc).add(result.getRssi());
        }

        @Override
        public void onBatchScanResults(List<android.bluetooth.le.ScanResult> results) {
            for (android.bluetooth.le.ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

//
    public boolean onTouchEvent(MotionEvent motionEvent) {
        mScaleGestureDetector.onTouchEvent(motionEvent);
        return true;
    }

//
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector){
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(0.1f,
                    Math.min(mScaleFactor, 10.0f));
            imageView.setScaleX(mScaleFactor);
            imageView.setScaleY(mScaleFactor);
            return true;
        }
    }
}

