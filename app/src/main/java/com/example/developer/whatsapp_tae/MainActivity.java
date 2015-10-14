package com.example.developer.whatsapp_tae;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.sufficientlysecure.rootcommands.RootCommands;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final String TAG = "WHATSAPP_DEMO";
    WContacts wContacts = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RootCommands.DEBUG = true;

        //Show today transactions
        DBHelper mydb = DBHelper.getInstance(getApplicationContext());
        ArrayList<Transaction> arrayOfTransactions = mydb.fetchTransactions(getDate());
        String totales = mydb.fetchCountAllTransactions();


        TextView transToday = (TextView)findViewById(R.id.totalday);
        TextView transAll = (TextView)findViewById(R.id.totalall);
        if(arrayOfTransactions!=null){
            transToday.setText("T. Totales hoy: "+String.valueOf(arrayOfTransactions.size()));
            transAll.setText("T. Totales: "+totales);
        }else{
            transToday.setText("T. Totales hoy: "+String.valueOf(0));
            transAll.setText("T. Totales: "+totales);
        }


        UsersAdapter adapter = new UsersAdapter(this, arrayOfTransactions);
        RecyclerView listView = (RecyclerView) findViewById(R.id.list);
        listView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
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
        }else if( id == R.id.sync) {
            wContacts.Sync();
        }

        return super.onOptionsItemSelected(item);
    }
    public class BroadcastReceiverListen extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }

    public static class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
        Context context;
        ArrayList<Transaction> transactions;
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            View view = inflater.inflate(R.layout.item_layout, null);

            return new ViewHolder(view, new ViewHolder.IMyViewHolderClicks(){

                @Override
                public void onFolio(View caller, ViewHolder holder) {
                    Log.d(TAG,">>>><<<<" + (holder.folio));
                    Intent detalleIntent = new Intent(context,TransactionActivity.class);
                    detalleIntent.putExtra("Folio",holder.folio);
                    detalleIntent.putExtra("Numero",holder.number_text);
                    detalleIntent.putExtra("Detalle",holder.detalle_text);
                    detalleIntent.putExtra("Fecha",holder.date_text);
                    context.startActivity(detalleIntent);
                }
            });
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            Transaction transaction = this.transactions.get(position);
            viewHolder.date.setText(String.valueOf(transaction.getFecha()));
            viewHolder.detalle.setText(String.valueOf(transaction.getDetalle()));
            viewHolder.number.setText(String.valueOf(transaction.getNumero()));

            viewHolder.folio = Settings.APP_ID(context) + String.format("%07d",Long.parseLong(transaction.getFolio()));
            viewHolder.date_text = transaction.getFecha();
            viewHolder.detalle_text = transaction.getDetalle();
            viewHolder.number_text = transaction.getNumero();

        }

        @Override
        public int getItemCount() {
            if(this.transactions != null)
            {
                return this.transactions.size();
            }else{
                return 0;
            }
        }

        static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView number;
            TextView detalle;
            TextView date;
            String folio, detalle_text, date_text, number_text;
            public IMyViewHolderClicks mListener;
            public ViewHolder(View view, IMyViewHolderClicks iMyViewHolderClicks) {
                super(view);
                mListener = iMyViewHolderClicks;
                this.number = (TextView) view.findViewById(R.id.number);
                this.detalle = (TextView) view.findViewById(R.id.detalle);
                this.date = (TextView) view.findViewById(R.id.date);
                view.setOnClickListener(this);

            }

            @Override
            public void onClick(View v) {
                mListener.onFolio(v,this);
            }
            public interface IMyViewHolderClicks {
                void onFolio(View caller, ViewHolder holder);
            }
        }


        public UsersAdapter(Context context, ArrayList<Transaction> transactions) {
            this.context = context;
            this.transactions = transactions;
        }

    }
    class NotificationReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Receive");
        }
    }

}
