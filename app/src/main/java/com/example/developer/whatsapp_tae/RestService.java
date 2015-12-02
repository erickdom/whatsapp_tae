package com.example.developer.whatsapp_tae;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
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

public class RestService extends AsyncTask<String,String,JSONArray> {
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
    protected JSONArray doInBackground(String... params) {
        HttpURLConnection urlConnection;
        if(!Settings.ACTIVATE_REST(this.context))
        {
            return null;
        }
        try {
            URL url;
            Log.d(TAG,this.params.toString());
            if(!this.params.has("response")){

                url = new URL(Settings.URL_REST(this.context) + this.service + "/");
                Log.d(TAG,Settings.URL_REST(this.context) + this.service + "/");
            }else{
                String folio = "";
                try {
                    folio = this.params.getString("folio_android");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                url = new URL(Settings.URL_REST(this.context) + this.service + "/"+folio+"/");
            }
            JSONObject newJsonObject = new JSONObject();
            try {
                newJsonObject.put("json",this.params.toString().replace(":\"[{",":[{").replace("}]\"}","}]}"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d(TAG,newJsonObject.toString());
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Length", String.valueOf(newJsonObject.toString().replace(":\"[{", ":[{").replace("}]\"}", "}]}").length()));
                urlConnection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                urlConnection.setRequestProperty("Accept", "application/json");

                OutputStream os = urlConnection.getOutputStream();
                os.write(newJsonObject.toString().replace(":\"[{", ":[{").replace("}]\"}", "}]}").getBytes("UTF-8"));
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
                    urlConnection.disconnect();
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
                urlConnection.disconnect();
                //System.out.println(""+sb.toString());
//                String responseText = in.toString();
                String output = sb.toString().substring(1, sb.length() - 1);
                Log.d(TAG, "doInBackground() responseText: " + output);
                return new JSONArray(output);


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
    protected void onPostExecute(JSONArray s) {
        DBHelper dbHelper = DBHelper.getInstance(this.context);
        if(s != null)
        for(int i = 0; i<s.length(); i++){
            try {
                JSONObject object = s.getJSONObject(i);
                String response = object.getString("response");
                String folio_android = object.getString("folio_android");
                String folio_casiLimpio = folio_android.substring(3);
                Log.d(TAG,folio_casiLimpio);
                String folioToUpdate = folio_casiLimpio.replaceFirst("^0+(?!$)", "");
                Log.d(TAG,folioToUpdate);

                dbHelper.updateTransaction(folioToUpdate, response, RandomMessages.getStringRandom("Status", response,folio_casiLimpio), "1");
            } catch (JSONException e) {
                e.printStackTrace();
            }



        }

        super.onPostExecute(s);
    }
}
//{'folio_android': u'8010000066', 'response': u'Inicial=30.48 Compra=0 Venta=0 Actual=30.48 '}
//[{'folio_android': '8010000068', 'response': 'AUTORIZADOR NO DISPONIBLE TELEFONO: 2222222222 PRODUCTO: MOVISTAR                       20.0000 FOLIO:  SALDO: 30.48'}]"
