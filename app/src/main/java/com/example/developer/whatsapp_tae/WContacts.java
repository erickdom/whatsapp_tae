package com.example.developer.whatsapp_tae;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
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
        try {
            cleanOldContacts();
        } catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
        }

        RequestWebService requestWebService = new RequestWebService(this.context);
        requestWebService.execute("whatsapp_device", null, "[{\"deviceID\":\"10\",\"shareKey\":\"evovcte$2015$\"}]");

    }

    public void insertContacts(JSONObject contacts) throws JSONException, RemoteException, OperationApplicationException {
        JSONArray contactsArray = contacts.getJSONArray("whatsapp_device");
        insertAsynContacts syncContacts = new insertAsynContacts(contactsArray);
        syncContacts.execute();
        Log.d(TAG, "ya termine");
    }
    private void cleanOldContacts() throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> ops =
                new ArrayList<>();
        for(int i = 0; i<200; i++){
            ops.add(ContentProviderOperation.newDelete(Data.CONTENT_URI)
                    .withSelection(Data._ID + "=?", new String[]{String.valueOf(i)})
                    .build());
        }
        contentResolver.applyBatch(ContactsContract.AUTHORITY, ops);

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
                        name = contact.getString("nombre").replaceAll("\\s+", "");
                        String numero = contact.getString("numero").replaceAll("\\s+", "");
                        addContact(numero, name);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            Log.d("BACKGROUND-ASYNC","SE COMPLETO LA SINCRONISACION");
            return "Exito";

        }
    }
}
