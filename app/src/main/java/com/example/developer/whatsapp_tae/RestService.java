package com.example.developer.whatsapp_tae;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by developer on 9/10/15.
 */
public class RestService extends AsyncTask<String,String,JSONObject> {
    private static final String TAG = "REST_SERVICE";
    private Context context;
    private JSONObject params;
    private String service;
    public enum SERVICE {
        TRANSACTION{
            public String toString(){
                return "transactions";
            }
        },
        LOG_ERRORS{
            public String toString(){
                return "log_errors";
            }
        }
    }
    public RestService(Context context, JSONObject params, String service) {
        this.context = context;
        this.params  = params;
        this.service = service;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        HttpURLConnection urlConnection = null;

        try {
            URL url = null;
            if(!this.params.has("response")){

                url = new URL(Settings.URL_REST(this.context) + this.service + "/");
            }else{
                String folio = "";
                try {
                    folio = this.params.getString("folio_android");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                url = new URL(Settings.URL_REST(this.context) + this.service + "/"+folio+"/");
            }
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Length", String.valueOf(this.params.toString().length()));
                urlConnection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                urlConnection.setRequestProperty("Accept", "application/json");

                OutputStream os = urlConnection.getOutputStream();
                os.write(this.params.toString().getBytes("UTF-8"));
                os.flush();


                int statusCode = urlConnection.getResponseCode();
                if (statusCode != HttpURLConnection.HTTP_CREATED && statusCode != HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "doInBackground(): connection failed: statusCode: " + statusCode);

                    InputStream content = urlConnection.getErrorStream();
                    BufferedReader in = new BufferedReader(new InputStreamReader(content));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        sb.append(line+"\n");
                    }

                    Log.d(TAG, "doInBackground(): connection failed: statusCode: " + sb.toString());

                    return null;
                }

                InputStream content = urlConnection.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(content));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    sb.append(line+"\n");
                }

                in.close();
                os.close();
                //System.out.println(""+sb.toString());
//                String responseText = in.toString();
                Log.d(TAG, "doInBackground() responseText: " + sb);
                return new JSONObject(sb.toString());


            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject s) {
        super.onPostExecute(s);
    }
}
