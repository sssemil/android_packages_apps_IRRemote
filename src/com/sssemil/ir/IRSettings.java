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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.sssemil.ir.Utils.net.Download;
import com.sssemil.ir.Utils.net.GetText;
import com.sssemil.ir.Utils.zip.Compress;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class IRSettings extends PreferenceActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "IRSettings";
    private SharedPreferences settings;
    private String http_path_root2;
    private String http_path_last_download1;
    private String http_path_last_download2;
    private String last_ver = "zirt";
    private String cur_ver;
    private ProgressDialog mProgressDialog;
    private AlertDialog.Builder adb;
    private String lastWord;
    private boolean cont = false;
    private String item = "null";
    private Spinner spinner;
    private int saved_theme;
    private ArrayList<String> ar = new ArrayList<String>();

    public static String normalisedVersion(String version) {
        return normalisedVersion(version, ".", 4);
    }

    public static String normalisedVersion(String version, String sep, int maxWidth) {
        String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
        StringBuilder sb = new StringBuilder();
        for (String s : split) {
            sb.append(String.format("%" + maxWidth + 's', s));
        }
        return sb.toString();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(IRSettings.this,
                IRMain.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        if (getActionBar() != null) getActionBar().setDisplayHomeAsUpEnabled(true);
        http_path_root2 = getString(R.string.http_path_root2);
        http_path_last_download1 = getString(R.string.http_path_last_download1);
        http_path_last_download2 = getString(R.string.http_path_last_download2);
        settings = getSharedPreferences(IRCommon.getPrefsName(this), 0);
        adb = new AlertDialog.Builder(this);
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            cur_ver = pInfo.versionName;
            findPreference("buildPref").setSummary(cur_ver);
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
        }
        assert pInfo != null;
        cur_ver = pInfo.versionName;
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onApplyThemeResource(@NonNull Resources.Theme theme, int resid, boolean first) {
        saved_theme = IRCommon.getCurrentThemeId(this, resid);
        theme.applyStyle(saved_theme, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent intent = new Intent(IRSettings.this,
                        IRMain.class);
                intent.putExtra("restart", "1");
                startActivity(intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public String compare(String v1, String v2) {
        String s1 = normalisedVersion(v1);
        String s2 = normalisedVersion(v2);
        int cmp = s1.compareTo(s2);
        return cmp < 0 ? "<" : cmp > 0 ? ">" : "==";
    }

    public void doOnDown(final String content) {
        adb = new AlertDialog.Builder(this);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.checking));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.show();
            }
        });
        new Thread(new Runnable() {
            public void run() {
                cont = false;
                try {
                    lastWord = content.substring(content.lastIndexOf(" ") + 1);
                    cont = true;
                } catch (NullPointerException e) {
                    Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                    cont = false;
                    adb.setTitle(getString(R.string.error));
                    adb.setMessage(getString(R.string.you_need_to_select));
                    adb.setPositiveButton(getString(R.string.pos_ans), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog.cancel();
                            adb.show();
                        }
                    });
                }
                if (cont) {
                    mProgressDialog.setMessage(getString(R.string.downloading));
                    mProgressDialog.setIndeterminate(true);
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog.cancel();
                            mProgressDialog.show();
                        }
                    });
                    try {
                        File df = new File(IRCommon.getIrPath()
                                + lastWord.substring(lastWord.lastIndexOf("/") + 1)
                                .substring(0, lastWord.substring(
                                        lastWord.lastIndexOf("/") + 1).length() - 4));
                        IRCommon.delete(df);
                    } catch (IOException e) {
                        Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                    }
                    Download downloadZip1 = new Download(lastWord,
                            IRSettings.this,
                            "zip"
                    );

                    try {
                        Log.d(TAG, lastWord);
                        String list = downloadZip1.execute().get();
                        Log.d(TAG, list);
                        if (list.equals("ko")) {
                            mProgressDialog.cancel();
                            adb.setTitle(getString(R.string.download));
                            adb.setMessage(getString(R.string.ser3));
                            adb.setPositiveButton(getString(R.string.pos_ans), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    doOnDown(content);
                                }
                            });

                            adb.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //finish();
                                }
                            });
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressDialog.cancel();
                                    adb.show();
                                }
                            });
                        } else {
                            mProgressDialog.cancel();
                            LayoutInflater li = LayoutInflater.from(IRSettings.this);
                            final View promptsView = li.inflate(R.layout.done_menu, null);
                            TextView tw = (TextView) promptsView.findViewById(R.id.textView2);
                            tw.setText(list);
                            adb = new AlertDialog.Builder(IRSettings.this);
                            adb.setTitle(getString(R.string.downloadT));
                            adb
                                    .setCancelable(false)
                                    .setPositiveButton(getString(R.string.pos_ans),
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //onAddDeviceClick(promptsView);
                                                }
                                            }
                                    );
                            adb.setView(promptsView);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressDialog.cancel();
                                    adb.show();
                                }
                            });
                        }
                    } catch (InterruptedException e) {
                        Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                    } catch (ExecutionException e) {
                        Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                    }
                }
            }
        }).start();
    }


    public void onAddDeviceClick(View paramView) {
        AlertDialog.Builder adb;
        try {
            EditText itemN = (EditText) paramView
                    .findViewById(R.id.editText);
            EditText brandN = (EditText) paramView
                    .findViewById(R.id.editText2);
            if (itemN.getText() != null && brandN.getText() != null) {
                String all = brandN.getText().toString() + "-" + itemN.getText().toString();
                if (!all.equals("-")) {
                    File localFile2 = new File(IRCommon.getIrPath() + brandN.getText().toString() + "-" + itemN.getText().toString());
                    if (!localFile2.isDirectory()) {
                        localFile2.mkdirs();
                    }
                }
                adb = new AlertDialog.Builder(this);
                adb.setTitle(getString(R.string.done));
                adb.setMessage(getString(R.string.new_item) + " " + brandN.getText().toString() + "-" + itemN.getText().toString() + " " + getString(R.string.crt_slf));
                adb.setPositiveButton(getString(R.string.pos_ans), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                adb.show();
            } else {
                throw new NullPointerException();
            }
        } catch (NullPointerException e) {
            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
            adb = new AlertDialog.Builder(this);
            adb.setTitle(getString(R.string.error));
            adb.setMessage(getString(R.string.you_need_to_select));
            adb.setPositiveButton(getString(R.string.pos_ans), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            adb.show();
        }
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                         final Preference preference) {
        String key = preference.getKey();
        adb = new AlertDialog.Builder(this);
        if (key != null) {
            if (key.equals("aboutPref")) {
                Intent intent = new Intent(this,
                        IRAbout.class);
                startActivity(intent);
            } else if (key.equals("open_sourcePref")) {
                Intent intent = new Intent(this,
                        IRLicense.class);
                startActivity(intent);
            } else if (key.equals("addBtn")) {

                LayoutInflater li = LayoutInflater.from(IRSettings.this);
                final View promptsView = li.inflate(R.layout.add_device_menu, null);
                adb = new AlertDialog.Builder(IRSettings.this);
                adb.setTitle(getString(R.string.add_new_device));
                adb
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.pos_ans),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        onAddDeviceClick(promptsView);
                                    }
                                }
                        )
                        .setNegativeButton(getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                }
                        );
                adb.setView(promptsView);
                adb.show();
            } else if (key.equals("downBtn")) {
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage(getString(R.string.gtlst));
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.show();

                new Thread(new Runnable() {
                    public void run() {
                        final GetText getAwItems1 = new GetText();
                        try {
                            ar = getAwItems1.execute(http_path_root2 + "downloads").get();
                        } catch (InterruptedException e) {
                            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                        } catch (ExecutionException e) {
                            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                        }
                        adb.setTitle(getString(R.string.downloadT));
                        String[] types = new String[ar.size()];
                        types = ar.toArray(types);
                        adb.setItems(types, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                mProgressDialog.cancel();
                                Log.i("pr", ar.get(which));
                                doOnDown(ar.get(which));
                            }

                        });
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialog.cancel();
                                adb.show();
                            }
                        });
                    }
                }).start();
            } else if (key.equals("rmBtn")) {
                try {
                    adb.setTitle(getString(R.string.remove));
                    ar.clear();
                    for (File localFile1 : new File(IRCommon.getIrPath()).listFiles()) {
                        if (localFile1.isDirectory()) {
                            if (!ar.contains(localFile1.getName())) {
                                ar.add(localFile1.getName());
                            }
                        }
                    }
                    String[] types = new String[ar.size()];
                    types = ar.toArray(types);
                    adb.setItems(types, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final int selected = which;
                            dialog.dismiss();
                            adb = new AlertDialog.Builder(IRSettings.this);
                            adb.setTitle(getString(R.string.warning));
                            adb.setMessage(getString(R.string.are_u_s_del));
                            adb.setPositiveButton(getString(R.string.pos_ans), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    item = ar.get(selected);
                                    File dir = new File(IRCommon.getIrPath() + item);
                                    try {
                                        IRCommon.delete(dir);
                                    } catch (IOException e) {
                                        Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                                        adb.setTitle(getString(R.string.error));
                                        adb.setMessage(getString(R.string.failed_del_fl_io));
                                        adb.setPositiveButton(getString(R.string.pos_ans), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        });
                                        adb.show();
                                    }
                                    adb = new AlertDialog.Builder(IRSettings.this);
                                    adb.setTitle(getString(R.string.done));
                                    adb.setMessage(getString(R.string.done_removing) + " " + item + " " + getString(R.string.files));
                                    adb.setPositiveButton(getString(R.string.pos_ans), null);
                                    adb.show();
                                }
                            });

                            adb.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            adb.show();
                        }
                    });
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adb.show();
                        }
                    });
                } catch (NullPointerException e) {
                    Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                    adb.setTitle(getString(R.string.error));
                    adb.setMessage(getString(R.string.you_need_to_select));
                    adb.setPositiveButton(getString(R.string.pos_ans), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    adb.show();
                }
            } else if (key.equals("checkUpd")) {
                update();
            } else if (key.equals("sbmtBug")) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/sssemil/android_packages_apps_SonyIRRemote/issues"));
                startActivity(browserIntent);
            } else if (key.equals("sbmtDev")) {
                LayoutInflater li = LayoutInflater.from(IRSettings.this);
                final View promptsView = li.inflate(R.layout.sbmt_device_menu, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        IRSettings.this);
                alertDialogBuilder.setTitle(getString(R.string.select_dev));
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        try {
                                            spinner = (Spinner) promptsView
                                                    .findViewById(R.id.spinner);
                                            item = spinner.getSelectedItem().toString();
                                            Compress c = new Compress(IRCommon.getIrPath() + item,
                                                    Environment.getExternalStorageDirectory()
                                                            + "/" + item + ".zip");
                                            c.zip();
                                            Intent emailIntent = new Intent(Intent.ACTION_SEND);
                                            emailIntent.setType("application/zip");
                                            emailIntent.putExtra(Intent.EXTRA_EMAIL,
                                                    new String[]{"suleymanovemil8@gmail.com"});
                                            emailIntent.putExtra(Intent.EXTRA_SUBJECT,
                                                    "New IR device");
                                            emailIntent.putExtra(Intent.EXTRA_TEXT, item);
                                            emailIntent.putExtra(Intent.EXTRA_STREAM,
                                                    Uri.parse("file:///"
                                                            + Environment
                                                            .getExternalStorageDirectory() + "/"
                                                            + item + ".zip"));
                                            startActivity(Intent.createChooser(emailIntent,
                                                    "Send by mail..."));
                                        } catch (NullPointerException e) {
                                            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                                            adb = new AlertDialog.Builder(IRSettings.this);
                                            adb.setTitle(getString(R.string.error));
                                            adb.setMessage(getString(R.string.you_need_to_select));
                                            adb.setPositiveButton(getString(R.string.pos_ans),
                                                    new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,
                                                                    int which) {

                                                }
                                            });
                                            adb.show();
                                        }

                                    }
                                }
                        )
                        .setNegativeButton(getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                }
                        );
                alertDialogBuilder.setView(promptsView);
                alertDialogBuilder.show();

                spinner = ((Spinner) promptsView.findViewById(R.id.spinner));
                ArrayList<String> localArrayList1 = new ArrayList<String>();
                boolean edited = false;

                for (File localFile1 : new File(IRCommon.getIrPath()).listFiles()) {
                    if (localFile1.isDirectory()) {
                        if (!localArrayList1.contains(localFile1.getName())) {
                            localArrayList1.add(localFile1.getName());
                            edited = true;
                        }
                    }

                    if (edited) {
                        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                                android.R.layout.simple_spinner_item, localArrayList1);
                        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(dataAdapter);
                    }
                }
            } else if (key.equals(("theme"))) {
                SharedPreferences settings = getSharedPreferences(IRCommon.getPrefsName(this), 0);
                if (settings.contains("theme")) {
                    if (settings.getString("theme", null).equals("1")) {
                        super.setTheme(R.style.Holo);
                    } else if (settings.getString("theme", null).equals("2")) {
                        super.setTheme(R.style.Holo_Light_DarkActionBar);
                    } else if (settings.getString("theme", null).equals("3")) {
                        super.setTheme(R.style.Theme_Holo_Light);
                    }
                }
            }
        }
        return true;
    }

    public void update() {
        final GetText getLastVer1 = new GetText();
        final AlertDialog.Builder adb = new AlertDialog.Builder(this);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.checking));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.show();
            }
        });
        new Thread(new Runnable() {
            public void run() {
                try {
                    last_ver=getLastVer1.execute(http_path_root2
                            + "last.php").get().get(0);
                    Log.i("Update", "last_ver : " + last_ver + " cur_ver : " + cur_ver);
                } catch (InterruptedException e) {
                    Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                } catch (ExecutionException e) {
                    Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                }
                if (last_ver.equals("zirt")) {
                    adb.setTitle(getString(R.string.update));
                    adb.setMessage(getString(R.string.ser3));
                    adb.setPositiveButton(getString(R.string.pos_ans), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            update();
                        }
                    });

                    adb.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //finish();
                        }
                    });
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog.cancel();
                            adb.show();
                        }
                    });
                } else {
                    String result = compare(cur_ver, last_ver);
                    boolean doUpdate = false;
                    if (result.equals(">")) {
                        doUpdate = false;
                    } else if (result.equals("<")) {
                        doUpdate = true;
                    } else if (result.equals("==")) {
                        doUpdate = false;
                    }


                    if (doUpdate) {
                        adb.setTitle(getString(R.string.update));
                        adb.setMessage(getString(R.string.new_version_available));
                        adb.setPositiveButton(getString(R.string.pos_ans), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mProgressDialog = new ProgressDialog(IRSettings.this);
                                new Thread(new Runnable() {
                                    public void run() {
                                        mProgressDialog.setMessage(
                                                getString(R.string.downloading_new));
                                        mProgressDialog.setIndeterminate(true);
                                        mProgressDialog.setProgressStyle(
                                                ProgressDialog.STYLE_SPINNER);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mProgressDialog.show();
                                            }
                                        });

                                        final Download downloadApp1 = new Download(
                                                http_path_last_download1
                                                + last_ver + http_path_last_download2,
                                                IRSettings.this, "apk"
                                        );
                                        try {
                                            downloadApp1.execute(http_path_last_download1
                                                    + last_ver + http_path_last_download2).get();
                                        } catch (InterruptedException e) {
                                            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                                        } catch (ExecutionException e) {
                                            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                                        }
                                        mProgressDialog.cancel();
                                    }
                                }).start();
                            }
                        });

                        adb.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialog.cancel();
                                adb.show();
                            }
                        });
                    } else {
                        adb.setTitle(getString(R.string.update));
                        adb.setMessage(getString(R.string.already_new));
                        adb.setPositiveButton(getString(R.string.pos_ans), null);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialog.cancel();
                                adb.show();
                            }
                        });
                    }
                }
            }
        }).start();
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStart(this);
        settings.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker easyTracker = EasyTracker.getInstance(this);
        easyTracker.set(Fields.TRACKING_ID, IRCommon.getID());
        easyTracker.activityStart(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        settings.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        settings.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        settings = getSharedPreferences(IRCommon.getPrefsName(this), 0);
        if (settings.contains("theme")) {
            if (saved_theme != IRCommon.getCurrentThemeId(this, saved_theme)) {
                saved_theme = IRCommon.getCurrentThemeId(this, saved_theme);
                Intent i = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(getBaseContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            }
        }
    }
}
