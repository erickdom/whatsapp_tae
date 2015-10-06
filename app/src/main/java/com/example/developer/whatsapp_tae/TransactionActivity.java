package com.example.developer.whatsapp_tae;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class TransactionActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_layout);

        Intent intent = getIntent();
        String message = intent.getStringExtra("Folio");
        String detail = intent.getStringExtra("Detalle");
        String fecha = intent.getStringExtra("Fecha");
        String number = intent.getStringExtra("Numero");

        TextView folio = (TextView) findViewById(R.id.text_folio);
        TextView date = (TextView) findViewById(R.id.text_date);
        TextView phone = (TextView) findViewById(R.id.phone_title);
        TextView detalle = (TextView) findViewById(R.id.response_details);

        folio.setText(message);
        date.setText(fecha);
        phone.setText(number);
        detalle.setText(detail);
    }
}
