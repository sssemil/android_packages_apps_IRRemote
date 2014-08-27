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

package com.sssemil.ir;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class IRCommon {

    private static final String TAG = "IRCommon";

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

    public static String getPowerNode(Resources res) {
        String[] array = res.getStringArray(R.array.powerNodePathVariants);
        File path_file;
        for (String anArray : array) {
            path_file = new File(anArray);
            if (path_file.exists()) {
                return path_file.getPath();
            }
        }
        return "/";
    }

    static {
        System.loadLibrary("jni_sonyopenir");
    }

    public static String getPrefsName(Context context) {
        return context.getPackageName() + "_preferences";
    }

    private native static int startIR(String powernode);

    private native static int stopIR(String powernode);

    private native static int learnKey(String filename);

    private native static int sendKey(String filename);

    private native static int sendRawKey(String key, int length);

    public static int start(Resources res) {
        return startIR(getPowerNode(res));
    }

    public static int stop(Resources res) {
        return stopIR(getPowerNode(res));
    }

    public static int send(String filename) {
        return sendKey(filename);
    }

    public static int sendRaw(String key, int length) {
        return sendRawKey(key, length);
    }

    public static int learn(String filename) {
        return learnKey(filename);
    }

    public static void restart(Resources res) {
        stopIR(getPowerNode(res));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        startIR(getPowerNode(res));
    }

    public static int getCurrentThemeId(Context context, int default_resid) {
        SharedPreferences settings = context.getSharedPreferences(getPrefsName(context), 0);
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

    public static String getID(){
        return "UA-XXXXXXXX-X";
    }
}

