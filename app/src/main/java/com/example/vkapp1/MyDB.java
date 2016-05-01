package com.example.vkapp1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MyDB {

    private final Context mCtx;

    public DBHelper mDBHelper;
    public SQLiteDatabase mDB;


    public boolean isTableExists(String tableName, Context ctx, boolean openDb) {
        Log.i("log", "============================== mydb istableexists "+tableName);
        DBHelper dbHelper;
        dbHelper = new DBHelper(ctx);
        if(openDb) {
            if(mDB == null || !mDB.isOpen()) {
                mDB = dbHelper.getReadableDatabase();
            }

            if(!mDB.isReadOnly()) {
                mDB.close();
                mDB = dbHelper.getReadableDatabase();
            }
        }

        Cursor cursor = mDB.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
        if(cursor!=null) {
            if(cursor.getCount()>0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    void createTable(String tb) {
        Log.i("log", "============================== mydb createtable " + tb);
        mDB.execSQL("create table " + tb + " ("
                + "_id integer primary key autoincrement,"
                + "artist text,"
                + "title text,"
                + "url text,"
                + "status int,"
                + "filepath text"
                + ");");
    }

    public MyDB(Context ctx) {
        Log.i("log", "============================== mydb constructor ");
        mCtx = ctx;/*
        DBHelper dbHelper;
        dbHelper = new DBHelper(mCtx);
        Log.i("log", "============================== mydb constructor dbhelper created");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Log.i("log", "============================== mydb constructor db opened");
        if(!isTableExists("vkActual", mCtx, false)) {
            this.createTable("vkActual");
        }
        if(!isTableExists("vkLoaded", mCtx, false)) {
            this.createTable("vkLoaded");
        }
        Log.i("log", "============================== mydb tables exist ");*/
    }

    // открыть подключение
    public void open() {
        mDBHelper = new DBHelper(mCtx);
        mDB = mDBHelper.getWritableDatabase();
    }

    // закрыть подключение
    public void close() {
        if (mDBHelper!=null) mDBHelper.close();
    }

    // получить все данные из таблицы DB_TABLE
    public Cursor getAllData(String table) {
        return mDB.query(table, null, null, null, null, null, null);
    }

    public Cursor getByID(String table, long id) {
        return mDB.query(table, null, "_id = "+Long.toString(id), null, null, null, null);
    }

    // добавить запись в DB_TABLE
    public void addRec(String table, String artist,String title,String url) {//,String status,String filepath) {
        ContentValues cv = new ContentValues();
        cv.put("artist",artist);
        cv.put("title",title);
        cv.put("url",url);
        cv.put("status",0);
        cv.put("filepath","");
        mDB.insert(table, null, cv);
    }

    // удалить запись из DB_TABLE
    public void delRec(String table, long id) {
        mDB.delete(table, "_id = " + id, null);
    }

    // класс по созданию и управлению БД
    private class DBHelper extends SQLiteOpenHelper {

        public String tbName1 = "vkLoaded", tbName2 = "vkActual";

        public DBHelper(Context context) {
            super(context, "myDB", null, 2);
        }

        // создаем и заполняем БД
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table " + tbName1 + " ("
                    + "_id integer primary key autoincrement,"
                    + "artist text,"
                    + "title text,"
                    + "url text,"
                    + "status integer,"
                    + "filepath text"
                    + ");");
            db.execSQL("create table " + tbName2 + " ("
                    + "_id integer primary key autoincrement,"
                    + "artist text,"
                    + "title text,"
                    + "url text,"
                    + "status integer,"
                    + "filepath text"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
