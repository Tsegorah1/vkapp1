package com.example.vkapp1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDB {

    private final Context mCtx;

    private DBHelper mDBHelper;
    private SQLiteDatabase mDB;

    public MyDB(Context ctx) {
        mCtx = ctx;
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
            super(context, "myDB", null, 1);
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
