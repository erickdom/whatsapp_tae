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
        while(true) {
            try {
                ListenSenders();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void ListenSenders() throws InterruptedException {
        Thread.sleep(5000);

        DBHelper mydb = new DBHelper(getApplicationContext());
        ArrayList<Transaction> arrayOfTransactions = mydb.fetchTransactionsToSend();
        Log.d(TAG, String.valueOf(arrayOfTransactions.size()));

        if(arrayOfTransactions.size() > 0){
            Log.d(TAG, arrayOfTransactions.get(0).getNumero());
            try {
                Messages messages = new Messages(arrayOfTransactions,getApplicationContext());
                messages.sendMessages();
                messages.close();
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
        }
        mydb.close();



    }

}
