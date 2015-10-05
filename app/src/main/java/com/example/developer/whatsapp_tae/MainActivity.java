package com.example.developer.whatsapp_tae;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.sufficientlysecure.rootcommands.RootCommands;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "WHATSAPP_DEMO";
    WContacts wContacts = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RootCommands.DEBUG = false;

        //Show today transactions
        DBHelper mydb = new DBHelper(getApplicationContext());
        ArrayList<Transaction> arrayOfTransactions = mydb.fetchTransactions(getDate());
        mydb.close();

        UsersAdapter adapter = new UsersAdapter(this, arrayOfTransactions);
        ListView listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);
        wContacts = new WContacts(getApplicationContext());

        //Servicio de recepcion
        Intent Listen = new Intent(MainActivity.this, Receive.class);
        Log.d(TAG, "Se inicio el monitor en segundo plano");
        startService(Listen);
        //Servicio de Envio
        Intent Sender = new Intent(MainActivity.this, Sender.class);
        Log.d(TAG, "Se inicio el monitor de envios en segundo plano");
        startService(Sender);


        final Button openWhatsApp = (Button)findViewById(R.id.send);
        openWhatsApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wContacts.Sync();

            }
        });
    }

    public String getDate(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return df.format(c.getTime());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this,
                    MySettings.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public class BroadcastReceiverListen extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }

    public static class UsersAdapter extends ArrayAdapter<Transaction> {

        static class ViewHolder {
            TextView number;
            TextView detalle;
            TextView date;
        }

        public UsersAdapter(Context context, ArrayList<Transaction> transactions) {
            super(context, 0, transactions);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            Transaction transaction = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view

            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_layout, parent, false);
                viewHolder.date = (TextView)convertView.findViewById(R.id.date);
                viewHolder.detalle = (TextView)convertView.findViewById(R.id.detalle);
                viewHolder.number = (TextView)convertView.findViewById(R.id.number);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }
            // Lookup view for data population
            // Populate the data into the template view using the data object
            viewHolder.number.setText(transaction.getNumero());
            viewHolder.detalle.setText(transaction.getDetalle());
            viewHolder.date.setText(transaction.getFecha());
            // Return the completed view to render on screen
            return convertView;
        }
    }
    class NotificationReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Receive");
        }
    }

}
