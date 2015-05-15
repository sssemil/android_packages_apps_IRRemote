/*
 * Copyright (c) 2014-2015 Emil Suleymanov <suleymanovemil8@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
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
package com.sssemil.ir.Utils.net;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.sssemil.ir.IRCommon;
import com.sssemil.ir.Utils.zip.Decompress;

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

    public Download(String url) {
        mUrl = url;
    }

    protected String doInBackground(String... unused) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;

        try {
            mOut_path = Environment.getExternalStorageDirectory() + "/"
                    + new URL(mUrl).getFile().substring(
                    new URL(mUrl).getFile().lastIndexOf("/") + 1);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            Log.v(TAG, "Starting... ");
            URL url = new URL(mUrl);
            Log.v(TAG, mUrl);

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


            String zipFile = mOut_path;
            String unzipLocation = IRCommon.getIrPath();

            Decompress d = new Decompress(zipFile, unzipLocation);
            Log.v(TAG, "Done!");
            return d.unzip();
        } catch (IOException e) {
            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
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
        return "ko";
    }
}