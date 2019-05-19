package com.blenative.blenative;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class UserLocationTask extends AsyncTask<String, Void, String> {

    protected String doInBackground(String... params) {
        OutputStreamWriter write = null;
        try {
            URL url = new URL(params[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            connection.setRequestProperty("Content-Type", "application/json");

            connection.setDoOutput(true);
            write = new OutputStreamWriter (connection.getOutputStream());

            write.write(params[1]);
            write.flush();
            Log.e("Response", connection.getResponseCode() + "");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            Log.i("Response reply", response.toString());
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.BLUE);

            JSONObject json = null;

            json = new JSONObject(response.toString());
            MapActivity.x = json.getInt("x");
            MapActivity.y = json.getInt("y");
            JSONObject finalJson = json;
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
                public void run(){
                    Log.i("Printing", "updating canvas");

//                        BitmapFactory.Options myOptions = new BitmapFactory.Options();
//                        myOptions.inDither = true;
//                        myOptions.inScaled = false;
//                        myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// important
//                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.floorplan, myOptions);
                    Paint black = new Paint();
                    black.setAntiAlias(true);
                    black.setColor(Color.BLACK);
                    black.setStyle(Paint.Style.FILL_AND_STROKE);
//
                    Bitmap workingBitmap = Bitmap.createBitmap(MapActivity.bitmap);
                    Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
//
                    Canvas canvas = new Canvas(mutableBitmap);
                    try {
                        canvas.drawCircle(finalJson.getInt("x") * 15, (40 - finalJson.getInt("y")) * 14, 10, paint);
                        JSONArray route = finalJson.getJSONArray("route");
                        int prevX = finalJson.getInt("x");
                        int prevY = finalJson.getInt("y");
                        for(int i = 0; i < route.length(); i++){
                            JSONArray coord = route.getJSONArray(i);
                            int nextX = Integer.parseInt(coord.getString(0));
                            int nextY = Integer.parseInt(coord.getString(1));
                            Log.i("route", nextX + " " + nextY);
                            canvas.drawLine(prevX * 15, (40 - prevY) * 14,
                                    coord.getInt(0) * 15, (40 - coord.getInt(1)) * 14, black);
                            prevX = nextX;
                            prevY = nextY;
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    MapActivity.imageView.setImageResource(R.drawable.floorplan);
                    MapActivity.imageView.setAdjustViewBounds(true);
                    MapActivity.imageView.setImageBitmap(mutableBitmap);
//                        MapActivity.canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
//                        try {
//                            MapActivity.canvas.drawCircle(finalJson.getInt("x") * 40, finalJson.getInt("y") * 40, 5, paint);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
                }
            }, 0);
//                MapActivity.updateImage(json.getInt("x"), json.getInt("y"));
//                MapActivity.canvas.drawCircle(json.getInt("x") * 40, json.getInt("y") * 40, 5, paint);


        } catch (Exception e) {
            String stack = "";
            for(StackTraceElement s : e.getStackTrace()){
                stack += s + "\n";
            }
            Log.e("Response error", "request failed " + stack);
        } finally{
            if (write != null) {
                try {
                    write.close();
                } catch (IOException ignored) {
                }
            }
        }

        return null;
    }

    protected void onProgressUpdate(Integer... progress) {
    }

    protected void onPostExecute(String result) {
        // this is executed on the main thread after the process is over
        // update your UI here
//        Toast.makeText(getApplicationContext(), "Start scan!", Toast.LENGTH_SHORT).show();

    }

}
