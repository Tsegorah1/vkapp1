package com.example.vkapp1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.vk.sdk.util.VKUtil;

//import android.support.v7.app.AppCompatActivity;
import java.util.Arrays;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] fingerprints = VKUtil.getCertificateFingerprint(this, this.getPackageName());
        setContentView(R.layout.activity_main);
        System.out.println(Arrays.asList(fingerprints));
    }

    public void onClickMenu(View view) {
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }

    public void onClickAbout(View view) {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    public void onClickTolist(View view) {
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }

    public void onClickLogin(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void onClickLoad(View view) {
    }

    public void onClickPlace(View view) {
        Intent intent = new Intent(this, PlaceActivity.class);
        startActivity(intent);
    }

    public void onClickStyle(View view) {
        Intent intent = new Intent(this, ThemeActivity.class);
        startActivity(intent);
    }

    public void onClickRefresh(View view) {
    }

    public void onClickDelete(View view) {
    }
}
