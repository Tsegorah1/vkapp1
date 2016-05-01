package com.example.vkapp1;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;

public class ListActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CM_DELETE_ID = 1;
    ListView lvData;
    MyDB db;
    MySimpleCursorAdapter scAdapter;
    String db_table = "vkActual";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        db = new MyDB(this);
        db.open();

        String[] from = new String[] {"artist","title"};
        int[] to = new int[] { R.id.textView20, R.id.textView21 };

        scAdapter = new MySimpleCursorAdapter(this, R.layout.list_item, null, from, to, 0, db);
        lvData = (ListView) findViewById(R.id.listView);
        lvData.setAdapter(scAdapter);

        lvData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Log.d("log", "=======================itemClick: position = " + position + ", id = "
                        + id);
                Cursor c = db.getByID("vkActual", id);
                Log.d("log", "=======================rows = " + c.getCount());
                c.moveToFirst();
                Log.d("log", "=======================cursor id = " + c.getLong(0));
                Log.d("log", "=======================cursor status = " + c.getLong(4));
                if (c.getInt(4) == 0) {
                    File sdPath = Environment.getExternalStorageDirectory();
                    // добавляем свой каталог к пути
                    sdPath = new File(sdPath.getAbsolutePath() + "/" + "MyFiles/");
                    // создаем каталог
                    sdPath.mkdirs();
                    String filePath = sdPath.getAbsolutePath();
                    String fileName = Integer.toString(c.getInt(0))+"_"+
                            c.getString(1)+"_"+
                            c.getString(2)+".mp3";
                    Log.d("log", "======================= download file task");
                    downloadFile(c.getString(3), filePath, fileName, 256);
                    ContentValues cv = new ContentValues();
                    cv.put("status", 2);
                    cv.put("filepath", filePath);
                    cv.put("filename", fileName);
                    String[] args = {c.getString(0)};
                    db.mDB.update("vkActual", cv, "_id = ?", args);
                }
                if (c.getInt(4) == 2) {
                    Log.d("log", "======================= delete file task");
                    File f = new File(c.getString(5), c.getString(6));
                    String[] s=f.getParentFile().list();
                    Log.d("log", "======================= list of files in d:");
                    for(String i:s) {
                        Log.d("log", "=======================       filename == "+i);
                    }
                    Log.d("log", "======================= name of current file is: "+c.getString(6));
                    Log.d("log", "======================= path of current file is: "+c.getString(5));
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
                    String[] args = {c.getString(0)};
                    db.mDB.update("vkActual", cv, "_id = ?", args);
                }
                if (c.getInt(4) == 1) {

                }
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

        getSupportLoaderManager().initLoader(0, null, this);

        Cursor cursor=db.getAllData(db_table);
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
        DownloadTask downloadTask = new DownloadTask(strURL, strPath, strName, buffSize);
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
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bndl) {
        return new MyCursorLoader(this, db, db_table);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        scAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

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
