package com.example.developer.whatsapp_tae;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

public class Sender extends IntentService {

    private static String TAG = "SenderSERVICE";
    private static String CONCAT = "521%s@s.whatsapp.net";

    public Sender() {
        super("SenderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Loop
        //noinspection StatementWithEmptyBody
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true){
                        try{
                            ListenSenders();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
    }

    private void ListenSenders() throws InterruptedException {
        Thread.sleep(4500);
        DBHelper mydb = DBHelper.getInstance(getApplicationContext());
        ArrayList<Transaction> arrayOfTransactions = mydb.fetchTransactionsToSend();
        Log.d(TAG, String.valueOf(arrayOfTransactions.size()));
//        mydb.close();

        if(arrayOfTransactions.size() > 0){
            Log.d(TAG, arrayOfTransactions.get(0).getFolio());
            if(arrayOfTransactions.size() > 1) {
                Log.d(TAG, arrayOfTransactions.get(1).getFolio());

            }
            if(arrayOfTransactions.size() > 2) {
                Log.d(TAG, arrayOfTransactions.get(2).getFolio());

            }
            try {
                Messages messages = new Messages(arrayOfTransactions,getApplicationContext());
                messages.sendMessages();
                Log.d("FOLIO", "");

                messages.close();
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
                DBHelper dbHelper = DBHelper.getInstance(getApplicationContext());
                dbHelper.insertLog(StaticFunctions.throwToString(e), "Problema al ejecutar comando ROOT al enviar mensaje <<" + TAG + ">>");
                
            }
        }



    }

}
