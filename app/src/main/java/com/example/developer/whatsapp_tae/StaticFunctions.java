package com.example.developer.whatsapp_tae;

import android.content.Context;

import java.io.PrintWriter;
import java.io.StringWriter;


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
}