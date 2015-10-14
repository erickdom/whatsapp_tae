package com.example.developer.whatsapp_tae;

import android.content.Context;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class StaticFunctions {

    static String throwToString(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }
    public static String getFolio(Context context, long folio) {
        return Settings.APP_ID(context) + String.format("%07d", folio);
    }
    public static String getDate() {

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
    public static String timeElapsed(String message, String site) {
        return String.format("WHERE-->> %s | MENSAJE-->>  %s | TIME ELAPSED -->> %s", site, message, getDate());
    }
}