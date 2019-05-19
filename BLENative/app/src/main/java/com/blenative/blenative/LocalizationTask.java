package com.blenative.blenative;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class LocalizationTask extends AsyncTask<String, Void, String> {

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
