package com.example.vkapp1;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

//import android.support.v7.app.AppCompatActivity;

public class MainActivity extends Activity {

    //private String[] scope = new String[] {VKScope.AUDIO};
    //final String LOG_TAG = "myLogs";
    private boolean loading;
    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //VKSdk.initialize(this);
        //String[] fingerprints = VKUtil.getCertificateFingerprint(this, this.getPackageName());
        setContentView(R.layout.activity_main);
        loading = false;
        if (!VKSdk.isLoggedIn()){
            VKSdk.login(this,VKScope.AUDIO);
        }
        DBHelper dbHelper;
        dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if(!isTableExists("vkActual", db, false)) {
            createTable("vkActual", db);
        }
        if(!isTableExists("vkLoaded", db, false)) {
            createTable("vkLoaded", db);
        }
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

    public void onClickAbout(View view) {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    public void onClickTolist(View view) {
        if (!loading) {
            Intent intent = new Intent(this, ListActivity.class);
            startActivity(intent);
        }
        else {
            Intent intent = new Intent(this, NotLoadedActivity.class);
            startActivity(intent);
        }
    }

    public void onClickLogin(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void onClickLoad(View view) {
        DBHelper dbHelper;
        dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query("vkActual", null, null, null, null, null, null);
        String [] columnNames = cursor.getColumnNames();
        int columnCount = cursor.getColumnCount();
        int c_id=0, c_artist=0, c_title=0, c_url=0;
        for (int i=0; i<columnCount; i++) {
            switch (columnNames[i]) {
                case "_id"   : c_id     = i; break;
                case "artist": c_artist = i; break;
                case "title" : c_title  = i; break;
                case "url"   : c_url    = i; break;
                default:
            }
        }
        for(cursor.moveToFirst(); cursor.getPosition()<10; cursor.moveToNext()) {//isAfterLast(); cursor.moveToNext()) {
            String fileName = Integer.toString(cursor.getInt(c_id))+
                    cursor.getString(c_artist)+
                    cursor.getString(c_title);
            filePath = Environment.DIRECTORY_MUSIC;
            downloadFile(cursor.getString(c_url), filePath, fileName,8);
        }
    }

    public void downloadFile(String strURL, String strPath, String strName, int buffSize) {
        DownloadTask downloadTask = new DownloadTask(strURL, strPath, strName, buffSize);
        downloadTask.execute();
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
        loading = true;
        final ContentValues contentValues= new ContentValues();
        final Context context = this;
        VKParameters params = new VKParameters();
        params.put(VKApiConst.COUNT, 6000);
        VKRequest requestAudio = VKApi.audio().get(params);
        final VKList<VKApiAudio> vkList = new VKList();
        requestAudio.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);

                DBHelper dbHelper;
                dbHelper = new DBHelper(context);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.delete("vkActual", null, null);

                for (int i = 0; i < ((VKList<VKApiAudio>) response.parsedModel).size(); i++) {
                    VKApiAudio vkApiAudio = ((VKList<VKApiAudio>) response.parsedModel).get(i);
                    vkList.add(vkApiAudio);
                    Log.i("log", "========================== onComplete request");
                    contentValues.put("artist", vkList.get(i).artist);
                    contentValues.put("title", vkList.get(i).title);
                    contentValues.put("url", vkList.get(i).url);
                    contentValues.put("status", 0);
                    contentValues.put("filepath", "");
                    Log.i("log", "========================== onComplete r cv created(" + i + ")");
                    db.insert("vkActual", null, contentValues);
                    Log.i("log", "==========================" + vkList.get(i).title);
                }

                Log.i("log", "========================== onComplete");
                //db.delete("vkActual", null, null);
                //Log.i("log", "========================== tb deleted");
                for (int i = 0; i < vkList.size(); i++) {
                    contentValues.put("artist", vkList.get(i).artist);
                    contentValues.put("title", vkList.get(i).title);
                    contentValues.put("url", vkList.get(i).url);
                    contentValues.put("status", 0);
                    contentValues.put("filepath", "");
                    Log.i("log", "========================== onComplete cv created(" + i + ")");
                    //db.insert("vkActual", null, contentValues);
                    Log.i("log", "==========================" + vkList.get(i).title);
                }

                loading = false;
            }

            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                super.attemptFailed(request, attemptNumber, totalAttempts);
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                Log.i("log", "==========================" + "load error");
            }

            @Override
            public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
                super.onProgress(progressType, bytesLoaded, bytesTotal);
            }
        });

    }

    public void onClickDelete(View view) {
    }

    public boolean isTableExists(String tableName, SQLiteDatabase mDatabase, boolean openDb) {
        DBHelper dbHelper;
        dbHelper = new DBHelper(this);
        if(openDb) {
            if(mDatabase == null || !mDatabase.isOpen()) {
                mDatabase = dbHelper.getReadableDatabase();
            }

            if(!mDatabase.isReadOnly()) {
                mDatabase.close();
                mDatabase = dbHelper.getReadableDatabase();
            }
        }

        Cursor cursor = mDatabase.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
        if(cursor!=null) {
            if(cursor.getCount()>0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    void createTable(String tb, SQLiteDatabase db) {
        db.execSQL("create table " + tb + " ("
                + "_id integer primary key autoincrement,"
                + "artist text,"
                + "title text,"
                + "url text,"
                + "status int,"
                + "filepath text"
                + ");");
        Log.i("log", "============================== createdTable "+tb);
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
            //
            // создаем таблицу с полями
            //try {
                db.execSQL("create table " + tbName1 + " ("
                        + "_id integer primary key autoincrement,"
                        + "artist text,"
                        + "title text,"
                        + "url text,"
                        + "status integer,"
                        + "filepath text"
                        + ");");
                db.execSQL("create table " + tbName2 + " ("
                        + "_id int primary key autoincrement,"
                        + "artist text,"
                        + "title text,"
                        + "url text,"
                        + "status int,"
                        + "filepath text"
                        + ");");
                Log.w("log","============================== tables created");
            //}
            //catch(SQLException ex) {
            //    Log.d("log", "--- onCreate failed ---");
            //}
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

    class DownloadTask extends AsyncTask<Void, Void, Void> {

        String strURL;
        String strPath;
        String strName;
        int buffSize;

        public DownloadTask(String url, String path, String name, int buff) {
            super();
            strURL = url;
            strPath = path;
            strName = name;
            buffSize = buff;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                File file = new File(strPath, strName);
                if (!file.exists()) {
                    Log.e("log", "======================== file not exist");
                }
                URL connection = new URL(strURL);
                HttpURLConnection urlConn;
                urlConn = (HttpURLConnection) connection.openConnection();
                urlConn.setRequestMethod("GET");
                urlConn.connect();
                InputStream in;
                in = urlConn.getInputStream();
                OutputStream writer = new FileOutputStream(strPath+strName);
                byte buffer[] = new byte[buffSize];
                int c = in.read(buffer);
                while (c > 0) {
                    writer.write(buffer, 0, c);
                    c = in.read(buffer);
                }
                writer.flush();
                writer.close();
                in.close();
            }
            catch (MalformedURLException m) {
                Log.e("log", "======================== malformed url exception");
            }
            catch (ProtocolException m) {
                Log.e("log", "======================== protocol exception");
            }
            catch (FileNotFoundException m) {
                Log.e("log", "======================== file not found exception");
            }
            catch (IOException i) {
                Log.e("log", "======================== io exception");
            }
            return null;
        }
    }
}
