package com.example.vkapp1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class SearchActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
    }

    public void onClickSearch(View view) {
        Intent intent = new Intent(this, ListActivity.class);
        finish();//startActivity(intent);
    }

    public void onClickReset(View view) {
        Intent intent = new Intent(this, ListActivity.class);
        finish();//startActivity(intent);
    }
}
