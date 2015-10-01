package com.example.developer.whatsapp_tae;

/**
 * Created by developer on 1/10/15.
 */
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
