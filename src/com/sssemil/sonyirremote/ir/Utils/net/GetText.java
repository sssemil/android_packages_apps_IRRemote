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

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class GetText extends AsyncTask<String, Integer, ArrayList<String>> {

    private static final String TAG = "GetText";
    private DefaultHttpClient httpclient = new DefaultHttpClient();
    private ArrayList<String> ar = new ArrayList<String>();

    protected ArrayList<String> doInBackground(String... sUrl) {
        try {
            HttpGet httppost = new HttpGet(sUrl[0]);
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity ht = response.getEntity();

            BufferedHttpEntity buf = new BufferedHttpEntity(ht);

            InputStream is = buf.getContent();

            BufferedReader r = new BufferedReader(new InputStreamReader(is));

            String line;
            ar.clear();
            while ((line = r.readLine()) != null) {
                ar.add(line);
            }
            Log.i("line", String.valueOf(ar.size()));

            return ar;
        } catch (IOException e) {
            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
            return null;
        }
    }
}