package com.example.vkapp1;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class ConfirmActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.buttonOk)
            setResult(RESULT_OK);
        else
            setResult(RESULT_CANCELED);
        this.finish();
    }
}
