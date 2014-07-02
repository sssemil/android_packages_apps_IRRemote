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

package com.sssemil.sonyirremote.ir;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class IRCommon {

    private static final String TAG = "IRCommon";
    private static IRCommon instance = null;

    protected IRCommon() {
    }

    public static IRCommon getInstance() {
        if (instance == null) {
            instance = new IRCommon();
        }
        return instance;
    }

    public static void delete(File file)
            throws IOException {

        if (file.isDirectory()) {

            //directory is empty, then delete it
            if (file.list().length == 0) {

                file.delete();
                Log.d(TAG, "Directory is deleted : "
                        + file.getAbsolutePath());

            } else {

                //list all the directory contents
                String files[] = file.list();

                for (String temp : files) {
                    //construct the file structure
                    File fileDelete = new File(file, temp);

                    //recursive delete
                    delete(fileDelete);
                }

                //check the directory again, if empty then delete it
                if (file.list().length == 0) {
                    file.delete();
                    Log.d(TAG, "Directory is deleted : "
                            + file.getAbsolutePath());
                }
            }

        } else {
            //if file, then delete it
            file.delete();
            Log.d(TAG, "File is deleted : " + file.getAbsolutePath());
        }
    }

    public static String getIrPath() {
        return Environment.getExternalStorageDirectory() + "/irremote_keys/";
    }

    public String getPowernode(Resources res) {
        String[] array = res.getStringArray(R.array.powerNodePathVariants);
        File path_file;
        for (int i = 0; i <= array.length; i++) {
            path_file = new File(array[i]);
            if (path_file.exists()) {
                return path_file.getPath();
            }
        }
        return null;
    }

    static {
        System.loadLibrary("jni_sonyopenir");
    }

    private native int startIR(String powernode);

    private native int stopIR(String powernode);

    private native int learnKey(String filename);

    private native int sendKey(String filename);

    private native int sendRawKey(String key, int length);

    public int start(Resources res) {
        return startIR(getPowernode(res));
    }

    public int stop(Resources res) {
        return stopIR(getPowernode(res));
    }

    public int send(String filename) {
        return sendKey(filename);
    }

    public int sendRaw(String key, int length) {
        return sendRawKey(key, length);
    }

    public int learn(String filename) {
        return learnKey(filename);
    }

    public void restart(Resources res) {
        stopIR(getPowernode(res));
        startIR(getPowernode(res));
    }

    public String PREFS_NAME(Context context) {
        return context.getPackageName() + "_preferences";
    }

    public int getCurrentThemeId(Context context, int default_resid) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME(context), 0);
        if (settings.contains("theme")) {
            String saved_theme = settings.getString("theme", null);
            if (saved_theme.equals("1")) {
                return R.style.Holo;
            } else if (saved_theme.equals("2")) {
                return R.style.Holo_Light_DarkActionBar;
            } else if (saved_theme.equals("3")) {
                return R.style.Theme_Holo_Light;
            }
        }
        return default_resid;
    }
}

