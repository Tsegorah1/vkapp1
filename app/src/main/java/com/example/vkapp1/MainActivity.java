package com.example.vkapp1;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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
import java.util.ArrayList;

//import android.support.v7.app.AppCompatActivity;

public class MainActivity extends Activity implements android.app.LoaderManager.LoaderCallbacks<Cursor>{

    //private String[] scope = new String[] {VKScope.AUDIO};
    //final String LOG_TAG = "myLogs";
    private boolean loading;
    private String filePath;
    private DBHelper dbHelper;

    private int totalDone;
    private int totalRequired;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    int currentId = 1;
    SQLiteDatabase mdb;

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
        Log.e("log", "======================== read premission " + permission);
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
            VKSdk.login(this, VKScope.AUDIO);
        }
        dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Log.i("log", "========================== current DB version is " + db.getVersion());
        if(!isTableExists("vkActual", db, false)) {
            Log.i("log", "========================== vkActual not exist");
            createTable("vkActual", db);
        }
        if(!isTableExists("vkLoaded", db, false)) {
            Log.i("log", "========================== vkLoaded not exist");
            createTable("vkLoaded", db);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // закрываем подключение при выходе
        dbHelper.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 111) {
            if (resultCode == RESULT_OK) {
                File sdPath = Environment.getExternalStorageDirectory();
                // добавляем свой каталог к пути
                sdPath = new File(sdPath.getAbsolutePath() + "/" + "MyFiles/");
                // создаем каталог
                sdPath.mkdirs();
                ArrayList<File> files = ListFilesWithSubFolders(sdPath);
                Log.e("log", "======================== total files " + files.size());
                for (File file : sdPath.listFiles()) {
                    file.delete();
                    Log.e("log", "======================== deleted file: " + file.getName());
                }
            }
            else
                if (resultCode == RESULT_CANCELED) {
                    Log.e("log", "======================== file deletion cancelled");
                }
                else {
                    Log.e("log", "======================== error in confirm activity");
                }
        }
        else {
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
        //DBHelper dbHelper;
        //dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        mdb = db;
        Cursor cursor = db.query("vkActual", null, null, null, null, null, null);
        totalDone = 0;
        totalRequired = cursor.getColumnCount();
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
            String fileName = Integer.toString(cursor.getInt(c_id))+"_"+
                    cursor.getString(c_artist)+"_"+
                    cursor.getString(c_title)+".mp3";
            filePath = Environment.DIRECTORY_MUSIC;
            File sdPath = Environment.getExternalStorageDirectory();
            // добавляем свой каталог к пути
            sdPath = new File(sdPath.getAbsolutePath() + "/" + "MyFiles/");
            // создаем каталог
            sdPath.mkdirs();
            filePath = sdPath.getAbsolutePath();
            downloadFileAsyncLoader(cursor.getString(c_url), filePath, fileName, 256);
            ContentValues cv = new ContentValues();
            cv.put("status", 2);
            cv.put("filepath", filePath);
            cv.put("filename", fileName);
            String[] args = {cursor.getString(c_id)};
            db.update("vkActual",cv,"_id = ?", args);
        }
    }

   /* public void downloadFile(String strURL, String strPath, String strName, int buffSize) {
        DownloadTask downloadTask = new DownloadTask(strURL, strPath, strName, buffSize);
        downloadTask.execute();
    }*/

    public void downloadFileAsyncLoader(String strURL, String strPath, String strName, int buffSize) {
        Loader<Cursor> loader;
        Bundle bundle = new Bundle();
        bundle.putString("url", strURL);
        bundle.putString("path", strPath);
        bundle.putString("name", strName);
        bundle.putInt("buff", buffSize);
        loader = getLoaderManager().getLoader(0);
        loader = getLoaderManager().restartLoader(0, bundle, this);
        loader.forceLoad();
    }

    public void onClickPlace(View view) {
        PlaceActivity fileDialog = new PlaceActivity(this);
        fileDialog.show();
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
                    contentValues.put("filename", "");
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
                Intent intent = new Intent(context, RefreshedActivity.class);
                startActivity(intent);
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

    private ArrayList<File> ListFilesWithSubFolders(File sdPath) {
        ArrayList<File> files = new ArrayList<File>();
        for (File file : sdPath.listFiles()) {
            if (file.isDirectory())
                files.addAll(ListFilesWithSubFolders(file));
            else
                files.add(file);
        }
        return files;
    }

    public void onClickDelete(View view) {
        Intent intent = new Intent(this, ConfirmActivity.class);
        startActivityForResult(intent, 111);
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
            if (cursor.getCount() > 0) {
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
                + "filepath text,"
                + "filename text"
                + ");");
        Log.i("log", "============================== createdTable "+tb);
    }

    public void onClickMenu(View view) {
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new DownloadTaskLoader(this, args);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.w("logg", "================== onLoadFinished");
        ContentValues cv = new ContentValues();
        cv.put("status", 2);
        String[] args = {Integer.toString(currentId)};
        mdb.update("vkActual", cv, "_id = ?", args);
        totalDone++;
        //if(totalDone == 10) {//totalRequired) {
            Intent intent = new Intent(this, LoadedActivity.class);
            startActivity(intent);
        //}
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.w("logg", "================== onLoadFinished");
        ContentValues cv = new ContentValues();
        cv.put("status", 2);
        String[] args = {Integer.toString(currentId)};
        mdb.update("vkActual", cv, "_id = ?", args);
    }


    class DBHelper extends SQLiteOpenHelper {

        public String tbName1 = "vkLoaded", tbName2 = "vkActual";

        public DBHelper(Context context) {
            // конструктор суперкласса
            super(context, "myDB", null, 2);

        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            //
            // создаем таблицу с полями
            //try {
            createTable(tbName1, db);
            createTable(tbName2, db);
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
            Log.i("log", "========================== onUpgrade old="+oldVersion+", new="+newVersion);
            db.execSQL("ALTER TABLE vkLoaded ADD COLUMN filename text DEFAULT null");
            db.execSQL("ALTER TABLE vkActual ADD COLUMN filename text DEFAULT null");
        }

    }
}
