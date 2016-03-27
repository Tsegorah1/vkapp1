package com.example.vkapp1;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class PlaceActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);
    }

    public void onClickAccept(View view) {
        finish();
    }
}
