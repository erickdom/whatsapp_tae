package com.example.developer.whatsapp_tae;


import android.util.Log;

public class MilitoTime {
    long minute;
    long second;
    long hour;

    public MilitoTime(long miliseconds) {
        long totalSeconds=miliseconds/1000;
        this.second=(int)(totalSeconds%60);
        long totalMinutes=totalSeconds/60;
        this.minute=(int)(totalMinutes%60);
        long totalHours=totalMinutes/60;
        this.hour=(int)(totalHours%24);
    }
    public static long differenceMinutes(long milisecondsWhatsapp){
        long milinow = System.currentTimeMillis();
        long difference = milinow-milisecondsWhatsapp;
        MilitoTime trans = new MilitoTime(difference);
        Log.d("MILITOTIME", String.valueOf(difference));
        Log.d("MILITOTIME",String.valueOf(trans.getMinute()));
        return trans.getMinute();

    }
    public long getHour() {
        return hour;
    }

    public long getMinute() {
        return minute;
    }

    public long getSecond() {
        return second;
    }
}
