package com.example.developer.whatsapp_tae;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONObject;

/**
 * Created by developer on 14/10/15.
 */
public class CheckServices extends AsyncTask<String, String, JSONObject> {

    Context context;

    public CheckServices(Context context) {


    }

    @Override
    protected JSONObject doInBackground(String... params) {
        return null;
    }
}
