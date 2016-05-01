package com.example.vkapp1;

import android.content.Context;
import android.database.Cursor;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

public class MySimpleCursorAdapter extends SimpleCursorAdapter {

    MyDB db;

    public MySimpleCursorAdapter(Context context, int layout, Cursor c, String[] from,int[] to, int flags, MyDB mdb) {
        super(context, layout, c, from, to, flags);
        db = mdb;
    }

    @Override
    public void setViewImage(ImageView v, String value) {
        super.setViewImage(v, value);
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

}
