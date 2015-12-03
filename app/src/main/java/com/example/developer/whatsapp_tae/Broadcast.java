package com.example.developer.whatsapp_tae;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

/**
 * Created by erick on 11/10/15. Whatsapp-TAE
 */
public class Broadcast extends BroadcastReceiver {
    /**
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            /*Intent serviceIntent = new Intent(context, Receive.class);
            context.startService(serviceIntent);
            Intent serviceIntent2 = new Intent(context, Sender.class);
            context.startService(serviceIntent2);*/
            Intent intet = new Intent(context, Receive.class);
            Receive.openApp(context,"com.example.developer.whatsapp_tae");
            context.startService(intet);
        }
    }
}
