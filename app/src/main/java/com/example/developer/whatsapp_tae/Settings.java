package com.example.developer.whatsapp_tae;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Random;
import java.util.prefs.Preferences;

public  class Settings {

    public static final String URL = "http://10.10.10.11:9110/service.asmx?WSDL";
    public static final String NAMESPACE = "http://www.ventamovil.com.mx/ws/";
    public static final String XML_VERSSION = "<?xml version=\"1.0\" encoding= \"UTF-8\" ?>";

    static public String APP_ID(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("APPID","801");
    }
}
