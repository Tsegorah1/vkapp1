package com.example.vkapp1;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
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

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        Log.e("log", "======================== write premission "+permission);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            Log.e("log", "======================== no write premission");
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
            permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            Log.e("log", "======================== write premission " + permission);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                Log.e("log", "======================== failed to get write premission");
            }
        }
        int permission1 = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        Log.e("log", "======================== read premission "+permission);
        if (permission1 != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            Log.e("log", "======================== no read premission");
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
            permission1 = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
            Log.e("log", "======================== read premission "+permission);
            if (permission1 != PackageManager.PERMISSION_GRANTED) {
                Log.e("log", "======================== failed to get read premission");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        verifyStoragePermissions(this);
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
            File sdPath = Environment.getExternalStorageDirectory();
            // добавляем свой каталог к пути
            sdPath = new File(sdPath.getAbsolutePath() + "/" + "MyFiles");
            // создаем каталог
            sdPath.mkdirs();
            filePath = sdPath.getAbsolutePath();
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
            boolean b = false;
            strPath = "/storage/emulated/";
            try {
                File file = new File(strPath, strName);
                Log.e("log", "======================== dir "+strPath);
                Log.e("log", "======================== name "+strName);
                if (file.getParentFile() == null) {
                    Log.e("log", "======================== no parent file");
                    file.getParentFile().mkdirs();
                }
                try {
                    if (!file.exists()) {
                        b = file.createNewFile();
                        if (b = true) Log.e("log", "======================== file created");
                        if (b = false) Log.e("log", "======================== file already exists");
                        Log.e("log", "======================== file not exist");
                    }
                } catch (IOException i) {
                    Log.e("log", "======================== can_t create file ==== " + i.getMessage());
                }
                if (b || file.isFile()) {
                    Log.e("log", "======================== writing in file");
                    URL connection = new URL(strURL);
                    HttpURLConnection urlConn = null;
                    try {
                        urlConn = (HttpURLConnection) connection.openConnection();
                    } catch (IOException i) {
                        Log.e("log", "======================== can_t open connection");
                    }
                    urlConn.setRequestMethod("GET");
                    try {
                        urlConn.connect();
                    } catch (IOException i) {
                        Log.e("log", "======================== can_t connect");
                    }
                    InputStream in = null;
                    try {
                        in = urlConn.getInputStream();
                    } catch (IOException i) {
                        Log.e("log", "======================== can_t get input stream");
                    }
                    OutputStream writer = new FileOutputStream(strPath + strName);
                    byte buffer[] = new byte[buffSize];
                    int c = 0;
                    try {
                        c = in.read(buffer);
                    } catch (IOException i) {
                        Log.e("log", "======================== can_t read");
                    }
                    while (c > 0) {
                        try {
                            writer.write(buffer, 0, c);
                        } catch (IOException i) {
                            Log.e("log", "======================== can_t write in cycle");
                        }
                        try {
                            c = in.read(buffer);
                        } catch (IOException i) {
                            Log.e("log", "======================== can_t read in cycle");
                        }
                    }
                    try {
                        writer.flush();
                        writer.close();
                        in.close();
                    } catch (IOException i) {
                        Log.e("log", "======================== can_t flush/close");
                    }
                }
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
            /*catch (IOException i) {
                Log.e("log", "======================== io exception");
            }*/
            return null;
        }
    }
}
