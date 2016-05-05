package com.example.vkapp1;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;

public class ListActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    //private static final int CM_DELETE_ID = 1;
    ListView lvData;
    MyDB db;
    Cursor cursor;
    MySimpleCursorAdapter scAdapter;
    String db_table = "vkActual";
    int currentId = 0;
    DownloadTask downloadTask;
    android.app.LoaderManager lm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        //lm = this.getLoaderManager();
        //getLoaderManager().initLoader(0,null,this);
        db = new MyDB(this);
        db.open();
        cursor=db.getAllData(db_table);
        String[] from = new String[] {"artist","title","status"};
        int[] to = new int[] { R.id.textView20, R.id.textView21, R.id.imageView };

        scAdapter = new MySimpleCursorAdapter(this, R.layout.list_item, cursor, from, to, 0, db);
        lvData = (ListView) findViewById(R.id.listView);
        lvData.setAdapter(scAdapter);

        getLoaderManager().initLoader(0, null, this);
        //getLoaderManager().getLoader(0).forceLoad();

        lvData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Log.d("log", "=======================itemClick: position = " + position + ", id = "
                        + id);
                Cursor c = db.getByID("vkActual", id);
                //Cursor cursor =
                Log.d("log", "=======================rows = " + c.getCount());
                c.moveToFirst();
                Log.d("log", "=======================cursor id = " + c.getLong(0));
                Log.d("log", "=======================cursor status = " + c.getLong(4));
                cursor.moveToPosition(position);
                if (cursor.getInt(4) == 0) {//c.getInt(4) == 0) {
                    currentId = (int) ((long) id);
                    File sdPath = Environment.getExternalStorageDirectory();
                    // добавляем свой каталог к пути
                    sdPath = new File(sdPath.getAbsolutePath() + "/" + "MyFiles/");
                    // создаем каталог
                    sdPath.mkdirs();
                    String filePath = sdPath.getAbsolutePath();
                    String fileName = Integer.toString(cursor.getInt(0)) + "_" +
                            cursor.getString(1) + "_" +
                            cursor.getString(2) + ".mp3";
                    Log.d("log", "======================= download file task");
                    downloadFile(cursor.getString(3), filePath, fileName, 256);
                    ContentValues cv = new ContentValues();
                    cv.put("status", 2);
                    cv.put("filepath", filePath);
                    cv.put("filename", fileName);
                    String[] args = {cursor.getString(0)};
                    db.mDB.update("vkActual", cv, "_id = ?", args);
                }
                if (cursor.getInt(4) == 2) {
                    Log.d("log", "======================= delete file task");
                    File f = new File(cursor.getString(5), cursor.getString(6));
                    String[] s = f.getParentFile().list();
                    Log.d("log", "======================= list of files in d:");
                    for (String i : s) {
                        Log.d("log", "=======================       filename == " + i);
                    }
                    Log.d("log", "======================= name of current file is: " + cursor.getString(6));
                    Log.d("log", "======================= path of current file is: " + cursor.getString(5));
                    if (f.exists())
                        Log.d("log", "======================= file exists before deletion");
                    if (f.exists()) {
                        f.delete();
                        if (f.exists())
                            Log.d("log", "======================= file exists after deletion");
                    }
                    ContentValues cv = new ContentValues();
                    cv.put("status", 0);
                    cv.put("filepath", "");
                    cv.put("filename", "");
                    String[] args = {cursor.getString(0)};
                    db.mDB.update("vkActual", cv, "_id = ?", args);
                }
                if (cursor.getInt(4) == 1) {

                }
                cursor = db.getAllData(db_table);
                scAdapter.changeCursor(cursor);
                //scAdapter.notifyAll();
                //scAdapter.notifyDataSetChanged();
            }
        });

        lvData.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        registerForContextMenu(lvData);

        String [] s=cursor.getColumnNames();
        Log.e("logg", "================== columns: " + s[0]+s[1]+s[2]);
/*
        db.addRec(db_table, "art", "tit", "");

        cursor=db.getAllData(db_table);*/
        int i=cursor.getCount();

        Log.e("logg", "================== rows:    " + i);
        Log.e("logg", "================== xz:      " + cursor.getColumnName(0));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // закрываем подключение при выходе
        db.close();
    }

    public void downloadFile(String strURL, String strPath, String strName, int buffSize) {
        downloadTask = new DownloadTask(strURL, strPath, strName, buffSize);
        downloadTask.execute();
    }

    public void onClickMenu(View view) {
        //Intent intent = new Intent(this, MainActivity.class);
        //startActivity(intent);
        this.finish();
    }

    public void onClickSearch(View view) {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }

    public void onClickSort(View view) {
        Intent intent = new Intent(this, SortActivity.class);
        startActivityForResult(intent, 3);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 3) {
                //String s = data.get
                boolean bool = data.getBooleanExtra("checked", false);
                int pos = data.getIntExtra("pos", 0);
                String field;
                switch(pos) {
                    case 0: field = "artist"; break;
                    case 1: field = "title"; break;
                    default:field = "filename";
                }
                cursor = db.sortByField(field, bool);
                scAdapter.changeCursor(cursor);
            }
        }
    }

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        Log.w("logg", "================== onLoadFinished");
        ContentValues cv = new ContentValues();
        cv.put("status", 2);
        String[] args = {Integer.toString(currentId)};
        db.mDB.update("vkActual", cv, "_id = ?", args);
        cursor = db.getAllData(db_table);
        scAdapter.swapCursor(cursor);
        //scAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
        Log.w("logg", "================== onLoadFinished");
        ContentValues cv = new ContentValues();
        cv.put("status", 2);
        String[] args = {Integer.toString(currentId)};
        db.mDB.update("vkActual", cv, "_id = ?", args);
        cursor = db.getAllData(db_table);
        scAdapter.swapCursor(cursor);
        //scAdapter.swapCursor(cursor);
    }
/*
    @Override
    protected void onPostExecute(Cursor result) {
        Log.w("logg", "================== onLoadFinished");
        ContentValues cv = new ContentValues();
        cv.put("status", 2);
        String[] args = {Integer.toString(currentId)};
        db.mDB.update("vkActual", cv, "_id = ?", args);
        cursor = db.getAllData(db_table);
        scAdapter.swapCursor(cursor);
    }*/

    static class MyCursorLoader extends CursorLoader {

        MyDB db;
        String table;

        public MyCursorLoader(Context context, MyDB db, String table) {
            super(context);
            this.db = db;
            this.table=table;
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = db.getAllData(table);
            return cursor;
        }

    }

}
