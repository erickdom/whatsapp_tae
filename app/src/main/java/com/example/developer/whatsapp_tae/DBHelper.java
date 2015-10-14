package com.example.developer.whatsapp_tae;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "whatsapp_tae.db";
    public static final String LASTID_TABLE_NAME = "lastid";
    public Context context;
    private static DBHelper mInstance = null;
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 10);
        this.context = context;
    }
    public static synchronized DBHelper getInstance(Context ctx) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (mInstance == null) {
            mInstance = new DBHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table lastid " +
                        "(id integer primary key, storeid varchar(100))"
        );
        db.execSQL(
                "create table transactions " +
                        "(id integer primary key," +
                        "message text, " +
                        "send varchar(15), " +
                        "date_time datetime DEFAULT CURRENT_TIMESTAMP," +
                        "status varchar(5)," +
                        "response text," +
                        "toSend integer DEFAULT 0)"
        );
        db.execSQL(
                "create table log_errors" +
                        "(id integer primary key," +
                        "error text," +
                        "error_human text," +
                        "date_time datetime DEFAULT CURRENT_TIMESTAMP)"
        );
    }
    public long insertLog(String error, String error_human) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("error", error);
        contentValues.put("error_human", error_human);


        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("error",error);
            jsonObject.put("error_human", error_human);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RestService restService = new RestService(this.context, jsonObject, RestService.SERVICE.LOG_ERRORS.toString());
        restService.execute();


        return db.insert("log_errors", null, contentValues);
    }
    public void newTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(
                "drop table if exists log_errors"
        );
        db.execSQL(
                "create table log_errors" +
                        "(id integer primary key," +
                        "error text," +
                        "error_human text," +
                        "date_time datetime DEFAULT CURRENT_TIMESTAMP)"
        );
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
/*        db.execSQL(
                "create table transactions " +
                        "(id integer primary key," +
                        "message text, " +
                        "send varchar(15), " +
                        "date_time datetime DEFAULT CURRENT_TIMESTAMP," +
                        "status varchar(5)," +
                        "response text," +
                        "toSend integer DEFAULT 0)"
        );*/

    }
    public boolean cancelSend(String folio) {
        Log.d("DB",folio);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("toSend", 0);
        db.update("transactions", contentValues, "id = " + folio, null);
        return true;
    }
    public ArrayList<Transaction> fetchTransactionsToSend() {
        ArrayList<Transaction> ids = new ArrayList<>();
        try{
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor res =  db.rawQuery("select * from transactions WHERE toSend=1", null);
            if (res != null && !res.isClosed()) {
                if (res.moveToFirst()) {
                    do {

                        Transaction transaction = new Transaction(
                                res.getString(res.getColumnIndex("message")),
                                res.getString(res.getColumnIndex("status")),
                                res.getString(res.getColumnIndex("response")),
                                res.getString(res.getColumnIndex("send")),
                                res.getString(res.getColumnIndex("id")),
                                res.getString(res.getColumnIndex("date_time"))
                        );
                        ids.add(transaction);
                    } while (res.moveToNext());
                    res.close();
                    return ids;
                }
            }
            return ids;

        }catch (SQLiteCantOpenDatabaseException e){
            e.printStackTrace();
            return ids;
        }
    }
    public String fetchCountAllTransactions() {
        String totales;
        try{
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor res =  db.rawQuery("select COUNT(*) AS totales from transactions", null);
            if (res != null && !res.isClosed()) {
                if (res.moveToFirst()) {
                    do {

                        totales  = res.getString(res.getColumnIndex("totales"));
                    } while (res.moveToNext());
                    res.close();
                    return totales;
                }
            }
            return null;

        }catch (SQLiteCantOpenDatabaseException e){
            e.printStackTrace();
            return null;
        }
    }
    public ArrayList<Transaction> fetchTransactions(String date) {
        ArrayList<Transaction> ids = new ArrayList<>();
        try{
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor res =  db.rawQuery("select *,datetime(date_time,'localtime') AS date_time  from transactions WHERE date(date_time)= '"+date+"' ORDER BY id DESC", null);
            if (res != null && !res.isClosed()) {
                if (res.moveToFirst()) {
                    do {
                        Transaction transaction = new Transaction(
                                res.getString(res.getColumnIndex("message")),
                                res.getString(res.getColumnIndex("status")),
                                res.getString(res.getColumnIndex("response")),
                                res.getString(res.getColumnIndex("send")),
                                res.getString(res.getColumnIndex("id")),
                                res.getString(res.getColumnIndex("date_time"))
                                );
                        ids.add(transaction);
                    } while (res.moveToNext());
                    res.close();
                    return ids;
                }
            }
            return null;

        }catch (SQLiteCantOpenDatabaseException e){
            e.printStackTrace();
            return null;
        }
    }
    public long insertTransaction(String message, String send){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("message", message);
        contentValues.put("send", send);

        long folio =  db.insert("transactions", null, contentValues);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("message",message);
            jsonObject.put("send",send.substring(3, 13));
            jsonObject.put("folio_android",StaticFunctions.getFolio(this.context, folio));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RestService restService = new RestService(this.context, jsonObject, RestService.SERVICE.TRANSACTION.toString());
        restService.execute();
        return folio;
    }
    public String getDiffDateTransaction(String folio){
        String difference = null;
        try{
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor res =  db.rawQuery(
                    "select ((julianday(time('now'))-julianday(time(date_time)))*86400.0) AS difference from transactions WHERE id ="+folio, null);
            if (res != null && !res.isClosed()) {
                if (res.moveToFirst()) {
                    do {
                        difference = res.getString(res.getColumnIndex("difference"));
                    } while (res.moveToNext());
                    res.close();
                }
            }
            return difference;
        }catch (SQLiteCantOpenDatabaseException e){
            e.printStackTrace();
            return difference;
        }


    }
    public boolean updateTransaction(String id, String status, String response,String toSend){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("status", status);
        contentValues.put("response", response);
        contentValues.put("toSend", toSend);
        db.update("transactions", contentValues, "id = "+id, null);


        long folio =  Long.parseLong(id);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("status",status);
            jsonObject.put("response",response);
            jsonObject.put("folio_android",StaticFunctions.getFolio(this.context, folio));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RestService restService = new RestService(this.context, jsonObject, RestService.SERVICE.TRANSACTION.toString());
        restService.execute();


        return true;
    }
    public boolean insertLastID(String ID) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("storeid", ID);
        int rows = numberOfRows();
        if(rows == 0) {
            db.insert("lastid", null, contentValues);
        }else {
            updateID(ID);
        }
        return true;
    }
    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, LASTID_TABLE_NAME);
    }
    public ArrayList getData() throws SQLiteCantOpenDatabaseException{
        ArrayList<String> ids = new ArrayList<>();
        try{
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor res =  db.rawQuery("select * from lastid", null);
            if (res != null && !res.isClosed()) {
                if (res.moveToFirst()) {
                    do {
                        String id = res.getString(res.getColumnIndex("storeid"));
                        ids.add(id);
                    } while (res.moveToNext());
                    res.close();
                    return ids;
                }
            }
            ids.add("-1");
            return ids;
        }catch (SQLiteCantOpenDatabaseException e){
            e.printStackTrace();
            return ids;
        }

    }
    public boolean updateID(String ID) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("storeid", ID);
        db.update("lastid", contentValues, "id = 1", null);
        return true;
    }
}
