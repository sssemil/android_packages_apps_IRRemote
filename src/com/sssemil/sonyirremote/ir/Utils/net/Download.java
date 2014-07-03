/*
 * Copyright (c) 2014 Emil Suleymanov
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package com.sssemil.sonyirremote.ir.Utils.net;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.sssemil.sonyirremote.ir.IRCommon;
import com.sssemil.sonyirremote.ir.Utils.zip.Decompress;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Download extends AsyncTask<String, Integer, String> {

    private static final String TAG = "Download";
    private String mUrl;
    private String mOut_path;
    private String mApk_oder_zip;
    private Context mContext;

    public Download(String url, String out_path, Context context, String apk_oder_zip) {
        mUrl = url;
        mContext = context;
        mOut_path = out_path;
        mApk_oder_zip = apk_oder_zip;
    }

    protected String doInBackground(String... unused) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            Log.v(TAG, "Starting... ");
            URL url = new URL(mUrl);

            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();

            // download the file
            input = connection.getInputStream();
            output = new FileOutputStream(mOut_path);
            Log.v(TAG, "output " + mOut_path);

            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                if (isCancelled()) {
                    input.close();
                    return null;
                }
                total += count;
                // publishing the progress....
                if (fileLength > 0) // only if total length is known
                    publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);
            }

            if (mApk_oder_zip.equals("apk")) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File(Environment
                                .getExternalStorageDirectory() + "/upd.apk")),
                        "application/vnd.android.package-archive"
                );
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            } else if (mApk_oder_zip.equals("zip")) {
                String zipFile = mOut_path;
                String unzipLocation = IRCommon.getIrPath();

                Decompress d = new Decompress(zipFile, unzipLocation);
                String list = d.unzip();
                return list;
            }
            Log.v(TAG, "Done!");
        } catch (MalformedURLException e) {
            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
            return e.toString();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
            return e.toString();
        } catch (IOException e) {
            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
            return e.toString();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException e) {
                Log.d(TAG, "catch " + e.toString() + " hit in run", e);
            }

            if (connection != null)
                connection.disconnect();
        }
        return null;
    }
}