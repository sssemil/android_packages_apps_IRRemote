package com.sssemil.sonyirremote.ir;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sssemil.sonyirremote.ir.Zip.Compress;
import com.sssemil.sonyirremote.ir.Zip.Decompress;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

/**
 * Copyright (c) 2014 Emil Suleymanov
 * Distributed under the GNU GPL v2. For full terms see the file LICENSE.
 */

public class IRSettings extends PreferenceActivity {

    public String http_path_root2;
    public String http_path_last_download1;
    public String http_path_last_download2;
    public ArrayList<String> ar = new ArrayList<String>();
    public String irpath = "/data/data/com.sssemil.sonyirremote.ir/ir/";//place to store commands
    public String last_ver = "zirt";
    public String cur_ver;
    ProgressDialog mProgressDialog;
    String resp = "ko";
    String lastWord;
    Context thisS = this;
    boolean cont = false;
    String item = "null";
    EditText brandN, itemN;
    Spinner spinner;
    boolean changed = false;
    String saved_theme, new_theme;
    long lastPress;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        http_path_root2 = getString(R.string.http_path_root2);
        http_path_last_download1 = getString(R.string.http_path_last_download1);
        http_path_last_download2 = getString(R.string.http_path_last_download2);
        thisS = this;
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            cur_ver = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        cur_ver = pInfo.versionName;
        SharedPreferences settings = getSharedPreferences("com.sssemil.sonyirremote.ir_preferences", 0);
        if (settings.contains("theme")) {
            saved_theme = settings.getString("theme", null);
            if (saved_theme.equals("1")) {
                super.setTheme(R.style.Holo);
            } else if (saved_theme.equals("2")) {
                super.setTheme(R.style.Holo_Light_DarkActionBar);
            } else if (saved_theme.equals("3")) {
                super.setTheme(R.style.Theme_Holo_Light);
            }
        }
        Thread thread = new Thread() {
            public void run() {
                while (true) {
                    try {
                        SharedPreferences settings = getSharedPreferences("com.sssemil.sonyirremote.ir_preferences", 0);
                        new_theme = settings.getString("theme", null);
                        if (!new_theme.equals(saved_theme)) {
                            try {
                                Intent i = getBaseContext().getPackageManager()
                                        .getLaunchIntentForPackage(getBaseContext().getPackageName());
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                                break;
                            } catch (NullPointerException ex) {
                                ex.printStackTrace();
                            }
                        }
                        Thread.sleep(500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public String compare(String v1, String v2) {
        String s1 = normalisedVersion(v1);
        String s2 = normalisedVersion(v2);
        int cmp = s1.compareTo(s2);
        String cmpStr = cmp < 0 ? "<" : cmp > 0 ? ">" : "==";
        return cmpStr;
    }

    public void doOnDown(final String content) {
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
        final Context context = this;
        new Thread(new Runnable() {
            public void run() {
                cont = false;
                try {
                    lastWord = content.substring(content.lastIndexOf(" ") + 1);
                    cont = true;
                } catch (NullPointerException ex) {
                    cont = false;
                    //AlertDialog.Builder adb = new AlertDialog.Builder(this);
                    adb.setTitle(getString(R.string.error));
                    adb.setMessage(getString(R.string.you_need_to_select));
                    adb.setIcon(android.R.drawable.ic_dialog_alert);
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
// execute this when the downloader must be fired
                    final DownloadTask downloadTask = new DownloadTask(context);
                    try {
                        downloadTask.execute(lastWord).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    mProgressDialog.cancel();

                    if (!resp.equals("ok")) {
                        adb.setTitle(getString(R.string.download));
                        adb.setMessage(getString(R.string.ser3));
                        adb.setIcon(android.R.drawable.ic_dialog_alert);
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
                        adb.setTitle(getString(R.string.downloadT));
                        adb.setMessage(getString(R.string.done));
                        adb.setIcon(android.R.drawable.ic_dialog_alert);
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
                    resp = "ko";
                }
            }
        }).start();
    }

    public void onAddDeviceClick(View paramView) {
        try {
            itemN = (EditText) paramView
                    .findViewById(R.id.editText);
            brandN = (EditText) paramView
                    .findViewById(R.id.editText2);
            if (itemN.getText() != null || brandN.getText() != null) {
                String all = brandN.getText().toString() + "-" + itemN.getText().toString();
                if (!all.equals("-")) {
                    File localFile2 = new File(irpath + brandN.getText().toString() + "-" + itemN.getText().toString());
                    if (!localFile2.isDirectory()) {
                        localFile2.mkdirs();
                    }
                }
                AlertDialog.Builder adb = new AlertDialog.Builder(this);
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
        } catch (NullPointerException ex) {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setTitle(getString(R.string.error));
            adb.setMessage(getString(R.string.you_need_to_select));
            adb.setIcon(android.R.drawable.ic_dialog_alert);
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
        final AlertDialog.Builder adb = new AlertDialog.Builder(this);
        final AlertDialog.Builder adb2 = new AlertDialog.Builder(this);
        if (key.equals("aboutPref")) {
            adb.setTitle(getString(R.string.about));
            PackageInfo pInfo = null;
            String version = "-.-.-";
            try {
                pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            version = pInfo.versionName;
            adb.setMessage(getResources().getString(R.string.license1) + " v" + version + "\n" + getResources().getString(R.string.license2) + "\n" + getResources().getString(R.string.license3) + "\n" + getResources().getString(R.string.license4));
            adb.setPositiveButton(getString(R.string.pos_ans), null);
            AlertDialog dialog = adb.show();

            TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
            messageView.setGravity(Gravity.CENTER);
        } else if (key.equals("addBtn")) {

            LayoutInflater li = LayoutInflater.from(thisS);
            final View promptsView = li.inflate(R.layout.add_device_menu, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    thisS);
            alertDialogBuilder.setTitle(getString(R.string.add_new_device));
            alertDialogBuilder
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
            alertDialogBuilder.setView(promptsView);
            alertDialogBuilder.show();
        } else if (key.equals("downBtn")) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.gtlst));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.show();

            new Thread(new Runnable() {
                public void run() {
                    final GetAwItems getAwItems1 = new GetAwItems(thisS);
                    try {
                        getAwItems1.execute().get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
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
                for (File localFile1 : new File(irpath).listFiles()) {
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
                        final AlertDialog.Builder adb = new AlertDialog.Builder(thisS);
                        adb.setTitle(getString(R.string.warning));
                        adb.setMessage(getString(R.string.are_u_s_del));
                        adb.setIcon(android.R.drawable.ic_dialog_alert);
                        adb.setPositiveButton(getString(R.string.pos_ans), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                item = ar.get(selected);
                                File dir = new File(irpath + item);
                                try {
                                    FileUtils.deleteDirectory(dir);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                    adb.setTitle(getString(R.string.error));
                                    adb.setMessage(getString(R.string.failed_del_fl_io));
                                    adb.setIcon(android.R.drawable.ic_dialog_alert);
                                    adb.setPositiveButton(getString(R.string.pos_ans), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    });
                                    adb.show();
                                }
                                adb2.setTitle(getString(R.string.done));
                                adb2.setMessage(getString(R.string.done_removing) + " " + item + " " + getString(R.string.files));
                                adb2.setPositiveButton(getString(R.string.pos_ans), null);
                                adb2.show();
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
            } catch (NullPointerException ex) {
                ex.printStackTrace();
                adb.setTitle(getString(R.string.error));
                adb.setMessage(getString(R.string.you_need_to_select));
                adb.setIcon(android.R.drawable.ic_dialog_alert);
                adb.setPositiveButton(getString(R.string.pos_ans), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                adb.show();
            }
        } else if (key.equals("checkUpd")) {
            update();
        } else if (key.equals("sbmtBug")) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/sssemil/android_packages_apps_SonyIRRemote/issues"));
            startActivity(browserIntent);
        } else if (key.equals("sbmtDev")) {
            LayoutInflater li = LayoutInflater.from(thisS);
            final View promptsView = li.inflate(R.layout.sbmt_device_menu, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    thisS);
            alertDialogBuilder.setTitle(getString(R.string.select_dev));
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    try {
                                        spinner = (Spinner) promptsView.findViewById(R.id.spinner);
                                        item = spinner.getSelectedItem().toString();
                                        Compress c = new Compress(irpath + item, "/sdcard/" + item + ".zip");
                                        c.zip();
                                        Intent emailIntent = new Intent(Intent.ACTION_SEND);
                                        emailIntent.setType("application/zip");
                                        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"suleymanovemil8@gmail.com"});
                                        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "New IR device");
                                        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, item);
                                        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/" + item + ".zip"));
                                        startActivity(Intent.createChooser(emailIntent, "Send by mail..."));
                                    } catch (NullPointerException ex) {
                                        AlertDialog.Builder adb = new AlertDialog.Builder(thisS);
                                        adb.setTitle(getString(R.string.error));
                                        adb.setMessage(getString(R.string.you_need_to_select));
                                        adb.setIcon(android.R.drawable.ic_dialog_alert);
                                        adb.setPositiveButton(getString(R.string.pos_ans), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {

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
            ArrayList localArrayList1 = new ArrayList();
            boolean edited = false;

            for (File localFile1 : new File(irpath).listFiles()) {
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
            SharedPreferences settings = getSharedPreferences("com.sssemil.sonyirremote.ir_preferences", 0);
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
        return true;
    }

    public void update() {
        final GetLastVer getLastVer1 = new GetLastVer(this);
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
                    Log.i("Update", "last_ver : " + getLastVer1.execute().get() + " cur_ver : " + cur_ver);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                if (last_ver == "zirt") {
                    adb.setTitle(getString(R.string.update));
                    adb.setMessage(getString(R.string.ser3));
                    adb.setIcon(android.R.drawable.ic_dialog_alert);
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
                    if (result == ">") {
                        doUpdate = false;
                    } else if (result == "<") {
                        doUpdate = true;
                    } else if (result == "==") {
                        doUpdate = false;
                    }


                    if (doUpdate == true) {
                        adb.setTitle(getString(R.string.update));
                        adb.setMessage(getString(R.string.new_version_available));
                        adb.setIcon(android.R.drawable.ic_dialog_alert);
                        adb.setPositiveButton(getString(R.string.pos_ans), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mProgressDialog = new ProgressDialog(thisS);
                                new Thread(new Runnable() {
                                    public void run() {
                                        mProgressDialog.setMessage(getString(R.string.downloading_new));
                                        mProgressDialog.setIndeterminate(true);
                                        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mProgressDialog.show();
                                            }
                                        });

                                        final DownloadApp downloadApp1 = new DownloadApp(thisS);
                                        try {
                                            downloadApp1.execute(http_path_last_download1 + last_ver + http_path_last_download2).get();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        } catch (ExecutionException e) {
                                            e.printStackTrace();
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
                    } else if (doUpdate == false) {
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

    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPress > 5000) {
            Toast.makeText(getBaseContext(), getString(R.string.pr_bck_ag), Toast.LENGTH_LONG).show();
            lastPress = currentTime;
        } else {
            IRCommon.getInstance().stop();
            super.onBackPressed();
        }
        finish();
    }

    class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTask(Context context) {
            this.context = context;
        }

        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                Log.v("DownloadTask", "Starting... ");
                URL url = new URL(sUrl[0]);
                String filePath = url.getFile();
                String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);

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
                output = new FileOutputStream("/sdcard/" + fileName);
                Log.v("DownloadTask", "output " + "/sdcard/" + fileName);

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
                Log.v("DownloadTask", "Done!");
                //---------Unzip--------
                String zipFile = "/sdcard/" + fileName;
                String unzipLocation = irpath;

                Decompress d = new Decompress(zipFile, unzipLocation);
                d.unzip();
                //----------------------
                resp = "ok";
                return "ok";
            } catch (Exception e) {
                Log.e("DownloadTask", e.getMessage());
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
        }
    }

    class GetAwItems extends AsyncTask<String, Integer, String> {

        DefaultHttpClient httpclient = new DefaultHttpClient();
        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public GetAwItems(Context context) {
            this.context = context;
        }

        protected String doInBackground(String... sUrl) {
            try {
                HttpGet httppost = new HttpGet(http_path_root2 + "downloads");
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity ht = response.getEntity();

                BufferedHttpEntity buf = new BufferedHttpEntity(ht);

                InputStream is = buf.getContent();

                BufferedReader r = new BufferedReader(new InputStreamReader(is));

                StringBuilder total = new StringBuilder();
                String line;
                ar.clear();
                while ((line = r.readLine()) != null) {
                    total.append(line + "\n");
                    ar.add(line);
                }
                Log.i("line", String.valueOf(ar.size()));

                return ar.get(0);
            } catch (IOException ex) {
                return null;
            }
        }
    }

    class DownloadApp extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadApp(Context context) {
            this.context = context;
        }

        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                Log.v("DownloadApp", "Starting... ");
                URL url = new URL(sUrl[0]);

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
                output = new FileOutputStream("/sdcard/upd.apk");
                Log.v("DownloadApp", "output " + "/sdcard/upd.apk");

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
                Log.v("DownloadApp", "Done!");

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File("/sdcard/upd.apk")), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception e) {
                Log.e("DownloadApp", e.getMessage());
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }
    }

    class GetLastVer extends AsyncTask<String, Integer, String> {

        DefaultHttpClient httpclient = new DefaultHttpClient();
        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public GetLastVer(Context context) {
            this.context = context;
        }

        protected String doInBackground(String... sUrl) {
            try {
                HttpGet httppost = new HttpGet(http_path_root2 + "last.php");
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity ht = response.getEntity();

                BufferedHttpEntity buf = new BufferedHttpEntity(ht);

                InputStream is = buf.getContent();

                BufferedReader r = new BufferedReader(new InputStreamReader(is));

                String line;
                last_ver = "";
                while ((line = r.readLine()) != null) {
                    last_ver += line;
                }
                Log.i("GetLastVer", last_ver);
                return last_ver;
            } catch (IOException ex) {
                Log.e("GetLastVer", ex.getMessage());
                return null;
            }
        }
    }

}