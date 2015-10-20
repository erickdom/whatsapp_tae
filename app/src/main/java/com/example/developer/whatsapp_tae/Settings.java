package com.example.developer.whatsapp_tae;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public  class Settings {

    public static final String NAMESPACE = "http://www.ventamovil.com.mx/ws/";
    public static final String XML_VERSSION = "<?xml version=\"1.0\" encoding= \"UTF-8\" ?>";
    static public String URL_REST(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("URL_REST","http://localhost/transactions/");
    }
    static public String APP_ID(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("APPID","801");
    }
    static public String URL(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return  preferences.getString("URLWS","http://ventamovil.com.mx:9110");
    }
    static public boolean ACTIVATE_REST(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return  preferences.getBoolean("ACTIVATE_REST", false);
    }
    static public boolean ACTIVATE_SYNC(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return  preferences.getBoolean("ACTIVATE_SYNC",false);
    }
}
