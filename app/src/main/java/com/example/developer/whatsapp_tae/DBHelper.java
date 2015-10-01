package com.example.developer.whatsapp_tae;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;


public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "whatsapp_tae.db";
    public static final String LASTID_TABLE_NAME = "lastid";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 7);
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
                        "response text)"
        );
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
/*        db.execSQL("\n" +
                "DROP TABLE IF EXISTS transactions");
                /*
        db.execSQL(
                "create table transactions " +
                        "(id integer primary key," +
                        "message text, " +
                        "send varchar(15), " +
                        "date_time datetime DEFAULT CURRENT_TIMESTAMP," +
                        "status varchar(5)," +
                        "response text)"
        );*/
//        db.execSQL("\n" +
//                "UPDATE SQLITE_SEQUENCE SET seq = 200 WHERE name = 'transactions'");
    }
    public ArrayList<Transaction> fetchTransactions(String date) {
        ArrayList ids = new ArrayList();
        try{
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor res =  db.rawQuery("select * from transactions WHERE date(date_time)= '"+date+"' ORDER BY id DESC", null);
            if (res != null) {
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
                    return ids;
                }
            }
            ids.add(new Transaction("","","","","", ""));
            return ids;

        }catch (SQLiteCantOpenDatabaseException e){
            e.printStackTrace();
            ids.add(new Transaction("","","","","", ""));
            return ids;
        }
    }
    public long insertTransaction(String message, String send){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("message", message);
        contentValues.put("send", send);
        return db.insert("transactions", null, contentValues);
    }
    public boolean updateTransaction(String id, String status, String response){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("status", status);
        contentValues.put("response", response);
        db.update("transactions", contentValues, "id = "+id, null);
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
//        Log.d("DBHELPER", "SE ARMO ->" + ID);
        return true;
    }
    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, LASTID_TABLE_NAME);
    }
    public List getData() throws SQLiteCantOpenDatabaseException{
        List ids = new ArrayList();
        try{
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor res =  db.rawQuery("select * from lastid", null);
            if (res != null) {
                // move res to first row
                if (res.moveToFirst()) {
                    do {
                        String id = res.getString(res.getColumnIndex("storeid"));
                        ids.add(id);
                    } while (res.moveToNext());
                }else{
                    ids.add("-1");
                }
            }else{
                ids.add("-1");
            }
            return ids;

        }catch (SQLiteCantOpenDatabaseException e){
            e.printStackTrace();
            ids.add("-1");
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
