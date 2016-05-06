package com.example.vkapp1;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

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

/**
 * Created by Костя on 06.05.2016.
 */
public class DownloadTaskLoader extends AsyncTaskLoader<Cursor> {

    String strURL;
    String strPath;
    String strName;
    int buffSize;

    public DownloadTaskLoader(Context context, Bundle args) {
        super(context);
        if (args != null) {
            strURL = args.getString("url");
            strPath = args.getString("path");
            strName = args.getString("name");
            buffSize = args.getInt("buff");
        }
    }

    @Override
    public Cursor loadInBackground() {
        boolean b = false;
        //strPath = "/storage/emulated/";
        try {
            File file = new File(strPath,strName);
            Log.e("log", "======================== dir " + strPath);
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
                OutputStream writer = new FileOutputStream(file);
                    /*byte buffer[] = new byte[buffSize];
                    int c = 0;
                    while (c > 0) {
                        try {
                            c = in.read(buffer);
                        } catch (IOException i) {
                            Log.e("log", "======================== can_t read in cycle");
                        }
                        try {
                            writer.write(buffer, 0, c);
                        } catch (IOException i) {
                            Log.e("log", "======================== can_t write in cycle");
                        }
                    }*/
                int count;
                //long total = 0;
                byte data[] = new byte[buffSize];
                try {
                    while ((count = in.read(data)) != -1) {
                        // allow canceling with back button
                        if (this.isLoadInBackgroundCanceled()) {
                            in.close();
                            return null;
                        }
                        //total += count;
                        // publishing the progress....
                        //if (fileLength > 0) // only if total length is known
                        //    publishProgress((int) (total * 100 / fileLength));
                        writer.write((data), 0, count);
                    }
                } catch (IOException i) {
                    Log.e("log", "======================== can_t create file ==== " + i.getMessage());
                }
                try {
                    writer.flush();
                    writer.close();
                    in.close();
                } catch (IOException i) {
                    Log.e("log", "======================== can_t flush/close");
                }
                Log.e("log", "======================== file downloaded");
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
        return null;
    }
}
