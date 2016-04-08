package com.example.vkapp1;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiAudio;
import com.vk.sdk.api.model.VKList;

//import android.support.v7.app.AppCompatActivity;

public class MainActivity extends Activity {

    //private String[] scope = new String[] {VKScope.AUDIO};
    //final String LOG_TAG = "myLogs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //VKSdk.initialize(this);
        //String[] fingerprints = VKUtil.getCertificateFingerprint(this, this.getPackageName());
        setContentView(R.layout.activity_main);
        if (!VKSdk.isLoggedIn()){
            VKSdk.login(this,VKScope.AUDIO);
        }
        DBHelper dbHelper;
        dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
/*
        VKParameters params = new VKParameters();
        params.put(VKApiConst.COUNT, 6000);
        VKRequest requestAudio = VKApi.audio().get(params);
        final VKList<VKApiAudio> vkList = new VKList();
        requestAudio.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);

                for(int i = 0;i<((VKList<VKApiAudio>)response.parsedModel).size();i++){
                    VKApiAudio vkApiAudio = ((VKList<VKApiAudio>)response.parsedModel).get(i);
                    vkList.add(vkApiAudio);
                }
            }

            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                super.attemptFailed(request, attemptNumber, totalAttempts);
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
            }

            @Override
            public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
                super.onProgress(progressType, bytesLoaded, bytesTotal);
            }
        });
*/
        //System.out.println(Arrays.asList(fingerprints));
        dbHelper.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final Intent intent = new Intent(this, AuthErrActivity.class);
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                // Пользователь успешно авторизовался
            }
            @Override
            public void onError(VKError error) {
                // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
                startActivity(intent);
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
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
        ContentValues contentValues= new ContentValues();
        VKParameters params = new VKParameters();
        params.put(VKApiConst.COUNT, 6000);
        VKRequest requestAudio = VKApi.audio().get(params);
        final VKList<VKApiAudio> vkList = new VKList();
        requestAudio.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);

                for(int i = 0;i<((VKList<VKApiAudio>)response.parsedModel).size();i++){
                    VKApiAudio vkApiAudio = ((VKList<VKApiAudio>)response.parsedModel).get(i);
                    vkList.add(vkApiAudio);
                }
            }

            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                super.attemptFailed(request, attemptNumber, totalAttempts);
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
            }

            @Override
            public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
                super.onProgress(progressType, bytesLoaded, bytesTotal);
            }
        });

        DBHelper dbHelper;
        dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
/*
        Cursor cVkLoaded;
        cVkLoaded = db.query("vkLoaded", null, null, null, null, null, null);

        Cursor cVkActual;
        cVkActual = db.query("vkActual", null, null, null, null, null, null);

        for(int i = 0; !cVkActual.isAfterLast(); i ++) {

        }*/

        db.delete("vkActual",null,null);

        for(int i = 0;i < vkList.size(); i ++) {
            contentValues.put("artist",vkList.get(i).artist);
            contentValues.put("title",vkList.get(i).title);
            contentValues.put("url",vkList.get(i).url);
            contentValues.put("status",0);
            contentValues.put("filepath","");
            db.insert("vkActual",null,contentValues);
        }

        dbHelper.close();
    }

    public void onClickDelete(View view) {
    }

    class DBHelper extends SQLiteOpenHelper {

        public String tbName1 = "vkLoaded", tbName2 = "vkActual";

        public DBHelper(Context context) {
            // конструктор суперкласса
            super(context, "myDB", null, 1);
        }

        public DBHelper(Context context, String s) {
            // конструктор суперкласса
            super(context, s, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            //Log.d(LOG_TAG, "--- onCreate database ---");
            // создаем таблицу с полями
            db.execSQL("create table " + tbName1 + " ("
                    + "id integer primary key autoincrement,"
                    + "artist text,"
                    + "title text"
                    + "url text"
                    + "status integer"
                    + "filepath text"
                    + ");");
            db.execSQL("create table " + tbName2 + " ("
                    + "id integer primary key autoincrement,"
                    + "artist text,"
                    + "title text"
                    + "url text"
                    + "status integer"
                    + "filepath text"
                    + ");");
        }
/*
        public void onCreate(SQLiteDatabase db, String s) {
            Log.d(LOG_TAG, "--- onCreate database ---");
            // создаем таблицу с полями
            db.execSQL("create table " + s + " ("
                    + "id integer primary key autoincrement,"
                    + "name text,"
                    + "email text" + ");");
        }
*/
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

}
