package com.example.developer.whatsapp_tae;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class WContacts {


    public ContentResolver contentResolver;
    public Context context;
    public static final String TAG = "CONTACTS";
    public WContacts(Context context){
        this.context = context;
        this.contentResolver = context.getContentResolver();
    }

    public void Sync(){

        RequestWebService requestWebService = new RequestWebService(this.context);
        requestWebService.execute("Sales_point_whatsapp_synchronize", null, "[{\"deviceID\":\"10\",\"shareKey\":\"evovcte$2015$\"}]");
    }
    public void Download() {

        RequestWebService requestWebService = new RequestWebService(this.context);
        requestWebService.execute("whatsapp_device", null, "[{\"deviceID\":\"10\",\"shareKey\":\"evovcte$2015$\"}]");

    }

    public void insertContacts(JSONArray contacts) throws JSONException, RemoteException, OperationApplicationException {
        insertAsynContacts syncContacts = new insertAsynContacts(contacts);
        syncContacts.execute();
    }



    public class insertAsynContacts extends AsyncTask<String,String,String>{

        private JSONArray __contacts;

        public insertAsynContacts(JSONArray contacts){
            this.__contacts = contacts;
        }
        @Override
        protected String doInBackground(String... params) {
                try {
                    for(int i = 0; i < __contacts.length(); i++) {
                        String name;
                        JSONObject contact = this.__contacts.getJSONObject(i);
                        if(contact.has("salesPoint") && contact.has("status")) {
                            if (contact.getString("status").compareTo("1")  == 0) {
                                name = contact.getString("salesPoint").replaceAll("\\s+", "");
                                String numero = contact.getString("salesPoint").replaceAll("\\s+", "");
                                addContact(numero, name);
                            }
                        }else {

                            name = contact.getString("nombre").replaceAll("\\s+", "");
                            String numero = contact.getString("numero").replaceAll("\\s+", "");
                            addContact(numero, name);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            Log.d("BACKGROUND-ASYNC","SE COMPLETO LA SINCRONIZACION");
            return "Exito";

        }
        private void addContact(String numero, String name) {
            ArrayList<ContentProviderOperation> operationList = new ArrayList<>();
            //Create contact
            operationList.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());
            //q
            operationList.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                    .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(StructuredName.GIVEN_NAME, "")
                    .withValue(StructuredName.FAMILY_NAME, name)
                    .build());

            operationList.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, numero)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, Phone.TYPE_HOME)
                    .build());

            try{
                contentResolver.applyBatch(ContactsContract.AUTHORITY, operationList);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
