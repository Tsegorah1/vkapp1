package com.example.vkapp1;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class ListActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CM_DELETE_ID = 1;
    ListView lvData;
    MyDB db;
    SimpleCursorAdapter scAdapter;
    String db_table = "vkActual";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        db = new MyDB(this);
        db.open();

        String[] from = new String[] {"artist","title"};
        int[] to = new int[] { R.id.textView20, R.id.textView21 };

        scAdapter = new SimpleCursorAdapter(this, R.layout.list_item, null, from, to, 0);
        lvData = (ListView) findViewById(R.id.listView);
        lvData.setAdapter(scAdapter);

        registerForContextMenu(lvData);

        getSupportLoaderManager().initLoader(0, null, this);

        Cursor cursor=db.getAllData(db_table);
        String [] s=cursor.getColumnNames();
        Log.e("logg", "================== columns: " + s[0]+s[1]+s[2]);

        db.addRec(db_table, "art", "tit", "");

        cursor=db.getAllData(db_table);
        int i=cursor.getCount();

        Log.e("logg", "================== rows:    " + i);
        Log.e("logg", "================== xz:      " + cursor.getColumnName(0));
    }

    protected void onDestroy() {
        super.onDestroy();
        // закрываем подключение при выходе
        db.close();
    }

    public void onClickMenu(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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
