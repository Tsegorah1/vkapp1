package com.example.vkapp1;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

public class MySimpleCursorAdapter extends SimpleCursorAdapter {

    MyDB db;

    public MySimpleCursorAdapter(Context context, int layout, Cursor c, String[] from,int[] to, int flags, MyDB mdb) {
        super(context, layout, c, from, to, flags);
        db = mdb;
        MyViewBinder mvb = new MyViewBinder();
        this.setViewBinder(mvb);
    }

    @Override
    public void setViewImage(ImageView v, String value) {
        String uri;
        Log.e("log", "====================== setViewImage");/*
        switch (this.getCursor().getInt(4)) {
            case 0:
                Log.e("log", "======================            0");
                uri = "@drawable/neload_icon.png";
                break;
            case 2:
                Log.e("log", "======================            2");
                uri = "@drawable/delete_icon.png";
                break;
            default:
                Log.e("log", "======================            default");
                uri = "@drawable/reload_icon.png";
        }*/
        super.setViewImage(v, value);//uri);
        switch (this.getCursor().getInt(4)) {
            case 0:
                Log.e("log", "======================            0");
                v.setImageResource(R.drawable.neload_icon);
                break;
            case 2:
                Log.e("log", "======================            2");
                v.setImageResource(R.drawable.delete_icon);
                break;
            default:
                Log.e("log", "======================            default");
                v.setImageResource(R.drawable.reload_icon_anim);
        }
        //Cursor c = db.getByID();
        //final ImageView w = v;
/*
        w.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                w.getDrawable();

            }
        });*/
    }

    class MyViewBinder implements SimpleCursorAdapter.ViewBinder {
        @Override
        public boolean setViewValue(View v, Cursor cursor, int columnIndex) {
            Log.e("log", "====================== setViewValue started");

            if (v.getId() == R.id.imageView && columnIndex == 4) {//v instanceof ImageView) {
                Log.e("log", "====================== instance of imageView");
                switch (cursor.getInt(4)) {
                    case 0:
                        Log.e("log", "======================            0");
                        ((ImageView) v).setImageResource(R.drawable.neload_icon);
                        break;
                    case 2:
                        Log.e("log", "======================            2");
                        ((ImageView) v).setImageResource(R.drawable.delete_icon);
                        break;
                    default:
                        Log.e("log", "======================            default");
                        ((ImageView) v).setImageResource(R.drawable.reload_icon_anim);
                }
                return true;
            }
            else {
                Log.e("log", "====================== other instance");
                return false;
            }
        }
    }
}
