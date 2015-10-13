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
/*        try {
            cleanOldContacts();
        } catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
        }*/

        RequestWebService requestWebService = new RequestWebService(this.context);
        requestWebService.execute("whatsapp_device", null, "[{\"deviceID\":\"10\",\"shareKey\":\"evovcte$2015$\"}]");

    }

    public void insertContacts(JSONObject contacts) throws JSONException, RemoteException, OperationApplicationException {
        JSONArray contactsArray = contacts.getJSONArray("whatsapp_device");
        insertAsynContacts syncContacts = new insertAsynContacts(contactsArray);
        syncContacts.execute();
    }
    public void cleanOldContacts() throws RemoteException, OperationApplicationException {
        Thread a = new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                while (cursor.moveToNext() ) {
                        try{
                            String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                            Log.d("borro",uri.toString());
                            contentResolver.delete(uri, null, null);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
//10-08 03:26:30.060  20614-20614/com.example.developer.whatsapp_tae D/borro﹕ content://com.android.contacts/contacts/lookup/0r255348-3551292F293F514731313F395B292B314F37453F3551394347314B
//10-08 03:27:47.999  20614-20614/com.example.developer.whatsapp_tae D/borro﹕ content://com.android.contacts/contacts/lookup/0r255348-3551292F293F514731313F395B292B314F37453F3551394347314B

                }
            }
        });
        a.run();

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
