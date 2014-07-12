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

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.sssemil.sonyirremote.ir.Utils.net.Download;
import com.sssemil.sonyirremote.ir.Utils.net.GetText;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class IRMain extends Activity implements ActionBar.TabListener, View.OnLongClickListener {

    private static final String TAG = "IRMain";
    public String http_path_root2;
    public String http_path_last_download1;
    public String http_path_last_download2;
    public int state = 0;
    public String brand;
    public String item = "Example-TV";
    public String current_mode = "send";
    public String last_ver = "zirt";
    public String cur_ver;
    public ArrayList<String> first = new ArrayList<String>();
    public ArrayList<String> total = new ArrayList<String>();
    public ArrayList<String> disable = new ArrayList<String>();
    boolean main = true;
    boolean result = false;
    boolean do_restart = false;
    private String last_mode;
    private ProgressDialog mProgressDialog;
    private SharedPreferences settings;
    private AlertDialog.Builder adb;
    private String volkey = "1";
    private TextView alert;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private boolean run_threads = true;
    private int item_position;
    private HandlerThread mCheckThread;
    private Handler mCheckHandler;
    private Resources res;

    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ArrayList<View> mView = new ArrayList<View>();
    private int mCurrent = 0;

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

    public void onClick(final View view) {
        Button btn = (Button) view;
        Log.i(TAG, (String) btn.getContentDescription());
        String usage = (String) btn.getContentDescription();
        if (prepBISpinner()) {
            result = false;
            if (current_mode.equals("send")) {
                sendKeyBool(IRCommon.getIrPath() + item + "/" + usage + ".bin");
            } else if (current_mode.equals("write")) {
                learnKeyBool(IRCommon.getIrPath() + item + "/" + usage + ".bin");
            } else if (current_mode.equals("rename")) {
                LayoutInflater li = LayoutInflater.from(this);
                final View promptsView = li.inflate(R.layout.rename_menu, null);
                Button button = (Button) mViewPager.findViewById(view.getId());
                assert promptsView != null;
                final EditText ed = (EditText) promptsView.findViewById(R.id.editText);
                ed.setHint(button.getText());
                adb = new AlertDialog.Builder(this);
                adb.setTitle(getString(R.string.add_new_device));
                adb
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.pos_ans),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        onRename(ed.getText().toString(),
                                                getResources().getResourceEntryName(view.getId()));
                                    }
                                }
                        )
                        .setNeutralButton(getString(R.string.reset),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        onReset(getResources().getResourceEntryName(view.getId()));
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
            } else if (current_mode.equals("endis")) {
                onEndis(getResources().getResourceEntryName(view.getId()));
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        new Thread(new Runnable() {
            public void run() {
                IRCommon.getInstance().stop(res);
            }
        }).start();
        EasyTracker.getInstance(this).activityStart(this);
        run_threads = false;
        //mHandler.removeCallbacksAndMessages(null);
        mCheckHandler.removeCallbacksAndMessages(null);
        if (mCheckThread.isAlive()) {
            mCheckThread.quit();
        }
        mCheckHandler = null;
        mCheckThread = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        new Thread(new Runnable() {
            public void run() {
                IRCommon.getInstance().start(res);
            }
        }).start();
        EasyTracker easyTracker = EasyTracker.getInstance(this);
        easyTracker.set(Fields.TRACKING_ID, "UA-52301928-1");
        easyTracker.activityStart(this);
        run_threads = true;
        if (mCheckThread == null || !mCheckThread.isAlive()) {
            mCheckThread = new HandlerThread("StateChecker");
            mCheckThread.start();
            mCheckHandler = new StateChecker(mCheckThread.getLooper());
            mCheckHandler.sendEmptyMessage(0);
        }
        prepItemBrandArray();
    }

    private void selectItem(int position, boolean long_click) {
        if (mDrawerList.getCount() - 1 == position) {
            if (!long_click) {
                item = mDrawerList.getItemAtPosition(0).toString();
                mDrawerList.setItemChecked(0, true);

                LayoutInflater li = LayoutInflater.from(this);
                final View promptsView = li.inflate(R.layout.add_device_menu, null);
                adb = new AlertDialog.Builder(this);
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
            }
        } else {
            item_position = position;
            try {
                item = mDrawerList.getItemAtPosition(position).toString();
            } catch (NullPointerException e) {
                item = "Example-TV";
            }

            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            if (!long_click) {
                mDrawerLayout.closeDrawer(mDrawerList);
                getActionBar().setTitle(getString(R.string.app_name) + " - " + item);
            } else {
                item_position = position;
                adb = new AlertDialog.Builder(IRMain.this);
                adb.setTitle(getString(R.string.warning));
                adb.setMessage(getString(R.string.are_u_s_del));
                adb.setIcon(android.R.drawable.ic_dialog_alert);
                adb.setPositiveButton(getString(R.string.pos_ans),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                item = mDrawerList.getItemAtPosition(item_position).toString();
                                File dir = new File(IRCommon.getIrPath() + item);
                                try {
                                    IRCommon.delete(dir);
                                } catch (IOException e) {
                                    Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                                    adb.setTitle(getString(R.string.error));
                                    adb.setMessage(getString(R.string.failed_del_fl_io));
                                    adb.setIcon(android.R.drawable.ic_dialog_alert);
                                    adb.setPositiveButton(getString(R.string.pos_ans),
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                }
                                            }
                                    );
                                    adb.show();
                                }
                                adb = new AlertDialog.Builder(IRMain.this);
                                adb.setTitle(getString(R.string.done));
                                adb.setMessage(getString(R.string.done_removing)
                                        + " " + item + " " + getString(R.string.files));
                                adb.setPositiveButton(getString(R.string.pos_ans), null);
                                adb.show();
                                prepItemBrandArray();
                            }
                        }
                );

                adb.setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }
                );
                adb.show();
            }
        }
    }

    public void onAddDeviceClick(View paramView) {
        AlertDialog.Builder adb;
        try {
            EditText itemN = (EditText) paramView
                    .findViewById(R.id.editText);
            EditText brandN = (EditText) paramView
                    .findViewById(R.id.editText2);
            if (itemN.getText() != null || brandN.getText() != null) {
                String all = brandN.getText().toString() + "-" + itemN.getText().toString();
                if (!all.equals("-")) {
                    File localFile2 = new File(IRCommon.getIrPath() + brandN.getText().toString()
                            + "-" + itemN.getText().toString());
                    if (!localFile2.isDirectory()) {
                        localFile2.mkdirs();
                    }
                }
                adb = new AlertDialog.Builder(this);
                adb.setTitle(getString(R.string.done));
                adb.setMessage(getString(R.string.new_item) + " "
                        + brandN.getText().toString() + "-" + itemN.getText().toString()
                        + " " + getString(R.string.crt_slf));
                adb.setPositiveButton(getString(R.string.pos_ans),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }
                );
                adb.show();
                prepItemBrandArray();
            } else {
                throw new NullPointerException();
            }
        } catch (NullPointerException e) {
            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
            adb = new AlertDialog.Builder(this);
            adb.setTitle(getString(R.string.error));
            adb.setMessage(getString(R.string.you_need_to_select));
            adb.setIcon(android.R.drawable.ic_dialog_alert);
            adb.setPositiveButton(getString(R.string.pos_ans),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }
            );
            adb.show();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!volkey.equals("1")) {
            String file1 = "/volPl.bin", file2 = "/volMn.bin";
            if (volkey.equals("3")) {
                file1 = "/chanelPl.bin";
                file2 = "/chanelMn.bin";
            }
            if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
                sendKeyBool(IRCommon.getIrPath() + item + file1);
            } else if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
                sendKeyBool(IRCommon.getIrPath() + item + file2);
            }
        }
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            finish();
        }
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return true;
    }

    @Override
    protected void onApplyThemeResource(Resources.Theme theme, int resid, boolean first) {
        theme.applyStyle(IRCommon.getInstance().getCurrentThemeId(this, resid), true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = getSharedPreferences(IRCommon.getInstance().PREFS_NAME(this), 0);

        if (settings.contains("volkey")) {
            volkey = settings.getString("volkey", null);
        }
        res = getResources();

        setContentView(R.layout.root);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
                mCurrent = position;
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this)
            );
        }

        Thread ft = new Thread() {
            public void run() {
                fixPermissionsForIr();
            }
        };
        ft.start();
        http_path_root2 = getString(R.string.http_path_root2);
        http_path_last_download1 = getString(R.string.http_path_last_download1);
        http_path_last_download2 = getString(R.string.http_path_last_download2);
        alert = (TextView) findViewById(R.id.alert);
        new Thread(new Runnable() {
            public void run() {
                IRCommon.getInstance().start(res);
            }
        }).start();
        firstRunChecker();
        prepIRKeys();
        prepItemBrandArray();
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            cur_ver = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
        } catch (NullPointerException e) {
            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
        }
        if (pInfo != null) {
            cur_ver = pInfo.versionName;
        }

        if (savedInstanceState == null) {
            selectItem(0, false);
        }

        mCheckThread = new HandlerThread("StateChecker");
        if (run_threads) {
            if (!mCheckThread.isAlive()) {
                mCheckThread.start();
                mCheckHandler = new StateChecker(mCheckThread.getLooper());
                mCheckHandler.sendEmptyMessage(0);
            }
        } else {
            if (mCheckThread.isAlive()) {
                mCheckThread.quit();
            }
        }

        findViewById(R.id.button2).setOnLongClickListener(this);
    }

    private void checkState() {
        if (main && mView.size() > 0 && mView.get(mCurrent) != null) {
            File f;
            if (!current_mode.equals("endis")) {
                Log.w(TAG, current_mode + " != endis");
                f = new File(IRCommon.getIrPath() + item + "/disable.ini");
                if (f.exists()) {
                    try {
                        String arr[] = mView.get(mCurrent).getContentDescription()
                                .toString().split("-", 2);
                        String firstWord = arr[0];
                        String theRest = arr[1];
                        for (int i = Integer.parseInt(firstWord); i <= Integer.parseInt(theRest); i++) {
                            final String btn = "button" + i;
                            if (!disable.contains(btn)) {
                                if (mView.get(mCurrent) != null) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            int id = mView.get(mCurrent).getResources().getIdentifier(btn,
                                                    "id", "com.sssemil.sonyirremote.ir");
                                            Button button = ((Button) mView.get(mCurrent).findViewById(id));
                                            button.setEnabled(true);
                                        }
                                    });
                                }
                            }
                        }
                        FileInputStream is = new FileInputStream(f);
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(is));
                        String line;
                        disable.clear();
                        while ((line = reader.readLine()) != null) {
                            disable.add(line);
                            if (mView.get(mCurrent) != null) {
                                final Button button = (Button) mView.get(mCurrent)
                                        .findViewById(mView.get(mCurrent).getResources()
                                                .getIdentifier(line, "id", getPackageName()));
                                if (button != null) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            button.setEnabled(false);//TODO fix
                                        }
                                    });
                                }
                            }
                        }
                        reader.close();
                        is.close();
                    } catch (FileNotFoundException e) {
                        Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                    } catch (IOException e) {
                        Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                    }
                } else if (!f.exists()) {
                    for (int i = 3; i <= 38; i++) {
                        final String btn = "button" + i;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mView.get(mCurrent) != null) {
                                    int id = mView.get(mCurrent).getResources().getIdentifier(btn,
                                            "id", "com.sssemil.sonyirremote.ir");
                                    Button button = (Button) mView.get(mCurrent).findViewById(id);
                                    if (button != null) button.setEnabled(true);
                                }
                            }
                        });
                    }
                }
            } else if (current_mode.equals("endis")) {
                f = new File(IRCommon.getIrPath() + item + "/disable.ini");
                if (f.exists()) {
                    try {
                        for (int i = 3; i <= 38; i++) {
                            final String btn = "button" + i;
                            if (!disable.contains(btn)) {
                                final Button button = (Button) mView.get(mCurrent)
                                        .findViewById(mView.get(mCurrent).getResources()
                                                .getIdentifier(btn, "id", getPackageName()));
                                if (mView.get(mCurrent) != null && button != null) {

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            button.setTextColor(Color.DKGRAY);

                                            if (settings.contains("theme")) {
                                                if (settings.getString("theme",
                                                        null).equals("1")) {
                                                    button.setTextColor(
                                                            Color.WHITE);
                                                } else {
                                                    button.setTextColor(
                                                            Color.BLACK);
                                                }
                                                button.setEnabled(true);
                                            }
                                        }
                                    });
                                }
                            }
                        }
                        FileInputStream is = new FileInputStream(f);
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(is));
                        String line;
                        disable.clear();
                        while ((line = reader.readLine()) != null) {
                            disable.add(line);
                            final Button button = (Button) mView.get(mCurrent)
                                    .findViewById(mView.get(mCurrent).getResources()
                                            .getIdentifier(line, "id", getPackageName()));
                            if (mView.get(mCurrent) != null && button!= null) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        button.setTextColor(Color.DKGRAY);
                                        button.setEnabled(true);
                                    }
                                });
                            }
                        }
                        reader.close();
                        is.close();
                    } catch (FileNotFoundException e) {
                        Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                    } catch (IOException e) {
                        Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                    }
                } else if (!f.exists()) {
                    for (int i = 3; i <= 38; i++) {
                        final String btn = "button" + i;
                        final Button button = (Button) mView.get(mCurrent).findViewById(mView.get(mCurrent).getResources()
                                .getIdentifier(btn, "id", getPackageName()));
                        if (button != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    button.setEnabled(true);
                                }
                            });
                        }
                    }
                }
            }
            f = new File(IRCommon.getIrPath() + item + "/text.ini");
            if (f.exists()) {
                try {
                    FileInputStream is = new FileInputStream(f);
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(is));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String arr[] = line.split(" ", 2);
                        final String firstWord = arr[0];
                        final String theRest = arr[1];
                        final Button button = (Button) mView.get(mCurrent)
                                .findViewById(mView.get(mCurrent).getResources()
                                        .getIdentifier(firstWord, "id", getPackageName()));
                        if (button != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    button.setText(theRest);
                                }
                            });
                        }
                    }
                    reader.close();
                    is.close();
                } catch (FileNotFoundException e) {
                    Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                } catch (IOException e) {
                    Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                }
            }
        }
        if (do_restart) {
            IRCommon.getInstance().restart(res);
            do_restart = false;
        }
    }

    public void fixPermissionsForIr() {
        File enable = new File(IRCommon.getInstance().getPowernode(res));
        File device = new File("/dev/ttyHSL2");
        final String[] enablePermissions = {"su", "-c", "chmod 222 ", enable.getPath()};
        final String[] devicePermissions = {"su", "-c", "chmod 666 ", device.getPath()};
        boolean do_fix = false;
        boolean found = false;

        if (!device.canRead() || !device.canWrite() || !enable.canWrite()) {
            do_fix = true;
            try {
                Runtime.getRuntime().exec("su");
            } catch (IOException e) {
                found = false;
            }

            if (!found) {
                adb = new AlertDialog.Builder(this);
                adb.setTitle(getString(R.string.warning));
                adb.setMessage(getString(R.string.no_root));
                adb.setIcon(android.R.drawable.ic_dialog_alert);
                adb.setPositiveButton(getString(R.string.pos_ans),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }
                );
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adb.show();
                    }
                });
            }
        }

        if (do_fix) {
            adb = new AlertDialog.Builder(this);
            adb.setTitle(getString(R.string.warning));
            adb.setMessage(getString(R.string.bad_perm));
            adb.setIcon(android.R.drawable.ic_dialog_alert);
            adb.setPositiveButton(getString(R.string.pos_ans),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                Runtime.getRuntime().exec(enablePermissions);
                                Runtime.getRuntime().exec(devicePermissions);
                                IRCommon.getInstance().start(res);
                            } catch (IOException e) {
                                Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                            }
                            IRCommon.getInstance().restart(res);
                        }
                    }
            );

            adb.setNegativeButton(getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }
            );
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adb.show();
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        new Thread(new Runnable() {
            public void run() {
                IRCommon.getInstance().stop(res);
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        new Thread(new Runnable() {
            public void run() {
                IRCommon.getInstance().start(res);
            }
        }).start();
    }

    public void firstRunChecker() {
        boolean isFirstRun;
        File f = new File(IRCommon.getIrPath());
        File f2 = new File(IRCommon.getIrPath() + "Example-TV");
        if (!f.exists() && !f2.exists()) {
            f.mkdir();
            f2.mkdir();
        } else if (f.exists() && f.listFiles().length == 0) {
            f2.mkdir();
        }
        SharedPreferences settings =
                getSharedPreferences(IRCommon.getInstance().PREFS_NAME(this), 0);
        SharedPreferences.Editor editor;

        if (!settings.contains("isFirstRun")) {
            isFirstRun = true;
            editor = settings.edit();
            editor.putString("theme", "1");
            editor.putBoolean("autoUpd", true);
            editor.commit();
        } else {
            isFirstRun = settings.getBoolean("isFirstRun", false);
        }

        if (isFirstRun) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.welcome));
            builder.setMessage(getString(R.string.fr));
            builder.setPositiveButton(getString(R.string.pos_ans), null);
            builder.show();
            editor = settings.edit();
            editor.putBoolean("isFirstRun", false);
            editor.commit();
        }

        boolean checkUpd;
        if (!settings.contains("autoUpd")) {
            checkUpd = false;
        } else {
            checkUpd = settings.getBoolean("autoUpd", true);
        }
        Log.i(TAG, "Update " + String.valueOf(checkUpd));
        if (checkUpd) {
            update(true);
        }
    }

    public void errorT(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void learnKeyBool(final String filename) {
        startLearning(filename);
    }

    public void startLearning(final String filename) {
        File to = new File(filename);
        if (to.exists()) {
            adb = new AlertDialog.Builder(this);
            adb.setTitle(getString(R.string.warning));
            adb.setMessage(getString(R.string.already_exists));
            adb.setIcon(android.R.drawable.ic_dialog_alert);
            adb.setPositiveButton(getString(R.string.pos_ans),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            learnAction(filename);
                        }
                    }
            );

            adb.setNegativeButton(getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }
            );
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adb.show();
                }
            });
        } else {
            learnAction(filename);
        }
    }

    public void learnAction(final String filename) {
        mProgressDialog = new ProgressDialog(IRMain.this);
        mProgressDialog.setMessage(getString(R.string.waiting_for_signal));
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
                mProgressDialog.show();
                IRCommon.getInstance().restart(res);
                state = IRCommon.getInstance().learn(filename);
                if (state < 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            errorT(getString(R.string.failed_lk) + filename);
                        }
                    });
                }
                mProgressDialog.cancel();
            }
        }).start();
    }

    private void sendKeyBool(final String filename) {
        File to = new File(filename);
        if (mDrawerList.getItemAtPosition(item_position).toString() != null) {
            if (!to.exists()) {
                adb = new AlertDialog.Builder(this);
                adb.setTitle(getString(R.string.warning));
                adb.setMessage(getString(R.string.not_exists));
                adb.setIcon(android.R.drawable.ic_dialog_alert);
                adb.setPositiveButton(getString(R.string.pos_ans),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                startLearning(filename);
                            }
                        }
                );

                adb.setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }
                );
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adb.show();
                    }
                });
            } else {
                sendAction(filename);
            }
        } else {
            adb = new AlertDialog.Builder(this);
            adb.setTitle(getString(R.string.error));
            adb.setMessage(getString(R.string.you_need_to_select));
            adb.setIcon(android.R.drawable.ic_dialog_alert);
            adb.setPositiveButton(getString(R.string.pos_ans),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }
            );
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adb.show();
                }
            });
        }

    }

    public void alert(String msg) {
        final AlertDialog.Builder errorD = new AlertDialog.Builder(this);
        errorD.setTitle(getString(R.string.error));
        errorD.setMessage(msg);
        errorD.setIcon(android.R.drawable.ic_dialog_alert);
        errorD.setPositiveButton(getString(R.string.pos_ans),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }
        );
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                errorD.show();
            }
        });
    }

    public void sendAction(final String filename) {
        new Thread(new Runnable() {
            public void run() {
                state = IRCommon.getInstance().send(filename);
                try {
                    if (state < 0) {
                        do_restart = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                alert(getString(R.string.non_zero));
                            }
                        });
                        throw new NonZeroStatusException();
                    }
                } catch (NonZeroStatusException e) {
                    Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                    EasyTracker easyTracker = EasyTracker.getInstance(IRMain.this);

                    easyTracker.send(MapBuilder
                            .createException(new StandardExceptionParser(IRMain.this, null)
                                    .getDescription(Thread.currentThread().getName(),
                                            e), false).build());
                }
            }
        }).start();
    }

    public void prepIRKeys() {
        File f = new File(IRCommon.getIrPath());
        if (!f.isDirectory()) {
            f.mkdirs();
        }
    }

    public void prepItemBrandArray() {
        ArrayList<String> localArrayList1 = new ArrayList<String>();
        boolean edited = false;
        File f = new File(IRCommon.getIrPath());
        File f2 = new File(IRCommon.getIrPath() + "Example-TV");
        if (!f.exists() && !f2.exists()) {
            f.mkdir();
            f2.mkdir();
        } else if (f.exists() && f.listFiles().length == 0) {
            f2.mkdir();
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        for (File localFile1 : new File(IRCommon.getIrPath()).listFiles()) {
            if (localFile1.isDirectory()) {
                if (!localArrayList1.contains(localFile1.getName())) {
                    localArrayList1.add(localFile1.getName());
                    edited = true;
                }
            }
        }

        if (edited) {
            localArrayList1.add(getString(R.string.add_new_device) + "…");
            // set up the drawer's list view with items and click listener
            mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                    R.layout.drawer_list_item, localArrayList1));
        }

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerList.setOnItemLongClickListener(new DrawerItemLongClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        try {
            item = mDrawerList.getItemAtPosition(0).toString();
        } catch (NullPointerException e) {
            item = "Example-TV";
        }
        getActionBar().setTitle(getString(R.string.app_name) + " - " + item);
        mDrawerList.setItemChecked(0, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ir, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menu_item) {
        int id = menu_item.getItemId();
        if (id == R.id.action_settings) {
            main = false;
            new Thread(new Runnable() {
                public void run() {
                    Intent intent = new Intent(IRMain.this,
                            IRSettings.class);
                    startActivity(intent);
                    IRCommon.getInstance().stop(res);
                }
            }).start();
            run_threads = false;
            finish();
            return true;
        } else if (id == R.id.action_mode) {
            LayoutInflater li = LayoutInflater.from(this);
            final View promptsView = li.inflate(R.layout.modes_menu, null);
            int rb_id = promptsView.getResources().getIdentifier(current_mode,
                    "id", "com.sssemil.sonyirremote.ir");

            final RadioButton radioButton = (RadioButton) promptsView.findViewById(rb_id);
            radioButton.setChecked(true);
            final RadioGroup rg = (RadioGroup) promptsView.findViewById(R.id.radioGroup1);

            rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    current_mode = promptsView.getResources()
                            .getResourceEntryName(rg.getCheckedRadioButtonId());
                    if (current_mode.equals("send")) {
                        alert.setVisibility(View.INVISIBLE);
                    }
                    if (!last_mode.equals(current_mode)) {
                        if (current_mode.equals("send")) {
                            alert.setVisibility(View.INVISIBLE);
                        } else if (current_mode.equals("write")) {
                            View promptsView = LayoutInflater.from(IRMain.this)
                                    .inflate(R.layout.wrt_mode, null);
                            adb = new AlertDialog.Builder(IRMain.this);
                            adb.setTitle(getString(R.string.warning));
                            adb.setView(promptsView);
                            adb
                                    .setCancelable(false)
                                    .setPositiveButton(getString(R.string.start),
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,
                                                                    int id) {
                                                    new Thread(new Runnable() {
                                                        public void run() {
                                                            IRCommon.getInstance().restart(res);
                                                        }
                                                    }).start();
                                                    alert.setText(
                                                            getString(R.string.alert_write_mode));
                                                    alert.setVisibility(View.VISIBLE);
                                                    alert.setTextColor(Color.RED);

                                                    File f = new File(IRCommon.getIrPath() + item);
                                                    if (!f.isDirectory()) {
                                                        f.mkdirs();
                                                    }

                                                    File f2 = new File(IRCommon.getIrPath() + brand);
                                                    if (!f2.isDirectory()) {
                                                        f2.mkdirs();
                                                    }
                                                }
                                            }
                                    )
                                    .setNegativeButton(getString(R.string.cancel),
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,
                                                                    int id) {
                                                    current_mode = last_mode;
                                                    radioButton.setChecked(true);
                                                    dialog.cancel();
                                                }
                                            }
                                    );
                            AlertDialog alertDialog = adb.show();
                            alertDialog.getWindow().setLayout(1350, 1000);
                        } else if (current_mode.equals("rename")) {
                            alert.setText(getString(R.string.alert_rename_mode));
                            alert.setVisibility(View.VISIBLE);
                            alert.setTextColor(Color.GREEN);
                        } else if (current_mode.equals("endis")) {
                            alert.setText(getString(R.string.alert_endis_mode));
                            alert.setVisibility(View.VISIBLE);
                            alert.setTextColor(Color.CYAN);
                        }
                    }
                }
            });

            adb = new AlertDialog.Builder(this);
            adb.setTitle("Select mode");
            adb.setIcon(android.R.drawable.ic_dialog_info);
            adb
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.pos_ans),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //changeMode(promptsView);
                                }
                            }
                    );
            adb.setView(promptsView);
            last_mode = current_mode;
            adb.show();
            return true;
        }
        return super.onOptionsItemSelected(menu_item);
    }

    public boolean prepBISpinner() {
        try {
            item = mDrawerList.getItemAtPosition(item_position).toString();
            result = true;
        } catch (NullPointerException e) {
            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
            adb = new AlertDialog.Builder(this);
            adb.setTitle(getString(R.string.error));
            adb.setMessage(getString(R.string.you_need_to_select));
            adb.setIcon(android.R.drawable.ic_dialog_alert);
            adb.setPositiveButton(getString(R.string.pos_ans),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            result = false;
                        }
                    }
            );
            adb.show();
        }
        return result;
    }

    public void setDefaultString(String btn_name) {
        int id = mView.get(mCurrent).getResources().getIdentifier(btn_name,
                "id", "com.sssemil.sonyirremote.ir");
        Button button = ((Button) mView.get(mCurrent).findViewById(id));
        if (btn_name.equals("button3")) {
            button.setText(getString(R.string.plus));
        } else if (btn_name.equals("button4")) {
            button.setText(getString(R.string.minus));
        } else if (btn_name.equals("button5")) {
            button.setText(getString(R.string.plus));
        } else if (btn_name.equals("button6")) {
            button.setText(getString(R.string.minus));
        } else if (btn_name.equals("button7")) {
            button.setText(getString(R.string.one));
        } else if (btn_name.equals("button8")) {
            button.setText(getString(R.string.two));
        } else if (btn_name.equals("button9")) {
            button.setText(getString(R.string.tree));
        } else if (btn_name.equals("button10")) {
            button.setText(getString(R.string.four));
        } else if (btn_name.equals("button11")) {
            button.setText(getString(R.string.five));
        } else if (btn_name.equals("button12")) {
            button.setText(getString(R.string.six));
        } else if (btn_name.equals("button13")) {
            button.setText(getString(R.string.seven));
        } else if (btn_name.equals("button14")) {
            button.setText(getString(R.string.eight));
        } else if (btn_name.equals("button15")) {
            button.setText(getString(R.string.nine));
        } else if (btn_name.equals("button16")) {
            button.setText(getString(R.string.zero));
        } else if (btn_name.equals("button17")) {
            button.setText(getString(R.string.up));
        } else if (btn_name.equals("button18")) {
            button.setText(getString(R.string.down));
        } else if (btn_name.equals("button19")) {
            button.setText(getString(R.string.ok_btn));
        } else if (btn_name.equals("button20")) {
            button.setText(getString(R.string.right));
        } else if (btn_name.equals("button21")) {
            button.setText(getString(R.string.left));
        } else if (btn_name.equals("button22")) {
            button.setText(getString(R.string.mute));
        } else if (btn_name.equals("button23")) {
            button.setText(getString(R.string.input));
        } else if (btn_name.equals("button24")) {
            button.setText(getString(R.string.home));
        } else if (btn_name.equals("button25")) {
            button.setText(getString(R.string.tstt));
        } else if (btn_name.equals("button26")) {
            button.setText(getString(R.string.returnbtn));
        } else if (btn_name.equals("button27")) {
            button.setText(getString(R.string.options));
        } else if (btn_name.equals("button28")) {
            button.setText(getString(R.string.guide));
        } else if (btn_name.equals("button29")) {
            button.setText(getString(R.string.wb));
        } else if (btn_name.equals("button30")) {
            button.setText(getString(R.string.p));
        } else if (btn_name.equals("button31")) {
            button.setText(getString(R.string.pl));
        } else if (btn_name.equals("button32")) {
            button.setText(getString(R.string.wf));
        } else if (btn_name.equals("button33")) {
            button.setText(getString(R.string.stop));
        } else if (btn_name.equals("button34")) {
            button.setText(getString(R.string.sr));
        } else if (btn_name.equals("button35")) {
            button.setText(getString(R.string.que));
        } else if (btn_name.equals("button36")) {
            button.setText(getString(R.string.exit));
        } else if (btn_name.equals("button37")) {
            button.setText(getString(R.string.tvradio));
        } else if (btn_name.equals("button38")) {
            button.setText(getString(R.string.audio));
        }
    }

    private void onReset(String btn_name) {
        File f = new File(IRCommon.getIrPath() + item + "/text.ini");
        try {
            if (f.exists()) {
                FileInputStream is = new FileInputStream(f);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is));
                String line;
                first.clear();
                total.clear();
                while ((line = reader.readLine()) != null) {
                    first.add(line.split(" ", 2)[0]);
                    total.add(line);
                }
                reader.close();
                is.close();
                if (first.contains(btn_name)) {
                    int index = first.indexOf(btn_name);
                    total.remove(index);
                }

                String out_data = "";

                for (int i = 0; i < total.toArray().length; i++) {
                    if (i < total.toArray().length - 1) {
                        out_data += total.toArray()[i] + "\n";
                    } else {
                        out_data += total.toArray()[i];
                    }
                }
                FileOutputStream fOut = new FileOutputStream(f);
                OutputStreamWriter myOutWriter =
                        new OutputStreamWriter(fOut);
                myOutWriter.append(out_data);
                myOutWriter.close();
                fOut.close();
                setDefaultString(btn_name);
            }
        } catch (IOException e) {
            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
        }
    }

    private void onRename(String new_name, String btn_name) {
        File f = new File(IRCommon.getIrPath() + item + "/text.ini");
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            FileInputStream is = new FileInputStream(f);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is));
            String line;
            first.clear();
            total.clear();
            while ((line = reader.readLine()) != null) {
                first.add(line.split(" ", 2)[0]);
                total.add(line);
            }
            reader.close();
            is.close();
        } catch (IOException e) {
            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
        }
        if (!first.contains(btn_name)) {
            total.add(btn_name + " " + new_name);
        } else {
            int index = first.indexOf(btn_name);
            total.remove(index);
            total.add(btn_name + " " + new_name);
        }

        String out_data = "";

        for (int i = 0; i < total.toArray().length; i++) {
            if (i < total.toArray().length - 1) {
                out_data += total.toArray()[i] + "\n";
            } else {
                out_data += total.toArray()[i];
            }
        }

        try {
            FileOutputStream fOut = new FileOutputStream(f);
            OutputStreamWriter myOutWriter =
                    new OutputStreamWriter(fOut);
            myOutWriter.append(out_data);
            myOutWriter.close();
            fOut.close();
        } catch (IOException e) {
            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
        }
    }

    private void onEndis(String btn_name) {
        File f = new File(IRCommon.getIrPath() + item + "/disable.ini");
        int id = getResources().getIdentifier(btn_name,
                "id", "com.sssemil.sonyirremote.ir");
        Button button = (Button) mView.get(mCurrent).findViewById(id);
        if(button !=null) {
            try {
                if (!f.exists()) {
                    f.createNewFile();
                }
                FileInputStream is = new FileInputStream(f);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is));
                String line;
                first.clear();
                total.clear();
                while ((line = reader.readLine()) != null) {
                    first.add(line.split(" ", 2)[0]);
                    total.add(line);
                }
                reader.close();
                is.close();
            } catch (IOException e) {
                Log.d(TAG, "catch " + e.toString() + " hit in run", e);
            }

            if (!first.contains(btn_name)) {
                total.add(btn_name);
                button.setEnabled(false);
            } else {
                int index = first.indexOf(btn_name);
                total.remove(index);
                button.setEnabled(true);
            }
            String out_data = "";

            for (int i = 0; i < total.toArray().length; i++) {
                if (i < total.toArray().length - 1) {
                    out_data += total.toArray()[i] + "\n";
                } else {
                    out_data += total.toArray()[i];
                }
            }

            try {
                FileOutputStream fOut = new FileOutputStream(f);
                OutputStreamWriter myOutWriter =
                        new OutputStreamWriter(fOut);
                myOutWriter.append(out_data);
                myOutWriter.close();
                fOut.close();
            } catch (IOException e) {
                Log.d(TAG, "catch " + e.toString() + " hit in run", e);
            }
        }
    }

    public String compare(String v1, String v2) {
        String s1 = normalisedVersion(v1);
        String s2 = normalisedVersion(v2);
        int cmp = s1.compareTo(s2);
        return cmp < 0 ? "<" : cmp > 0 ? ">" : "==";
    }

    public void update(final boolean silent) {
        final GetText getLastVer1 = new GetText();
        adb = new AlertDialog.Builder(this);
        if (!silent) {
            mProgressDialog = new ProgressDialog(IRMain.this);
            mProgressDialog.setMessage(getString(R.string.checking));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressDialog.show();
                }
            });
        }
        new Thread(new Runnable() {
            public void run() {
                try {
                    Log.i(TAG, "Update last_ver : " + (last_ver = getLastVer1.execute(http_path_root2
                            + "last.php").get().get(0))
                            + " cur_ver : " + cur_ver);
                } catch (InterruptedException e) {
                    Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                } catch (ExecutionException e) {
                    Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                } catch (NullPointerException e) {
                    Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                }
                if (last_ver.equals("zirt")) {
                    if (!silent) {
                        adb.setTitle(getString(R.string.update));
                        adb.setMessage(getString(R.string.ser3));
                        adb.setIcon(android.R.drawable.ic_dialog_alert);
                        adb.setPositiveButton(getString(R.string.pos_ans),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        update(silent);
                                    }
                                }
                        );

                        adb.setNegativeButton(getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        //finish();
                                    }
                                }
                        );
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialog.cancel();
                                adb.show();
                            }
                        });
                    }
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
                    Log.i(TAG, "Update " + String.valueOf(doUpdate));

                    if (doUpdate) {
                        adb.setTitle(getString(R.string.update));
                        adb.setMessage(getString(R.string.new_version_available));
                        adb.setIcon(android.R.drawable.ic_dialog_alert);
                        adb.setPositiveButton(getString(R.string.pos_ans),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        mProgressDialog = new ProgressDialog(IRMain.this);
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

                                                Download downloadApp1 =
                                                        new Download(http_path_last_download1
                                                                + last_ver + http_path_last_download2,
                                                                Environment
                                                                        .getExternalStorageDirectory()
                                                                        + "/upd.apk", IRMain.this, "apk"
                                                        );
                                                try {
                                                    downloadApp1.execute().get();
                                                } catch (InterruptedException e) {
                                                    Log.d(TAG, "catch " + e.toString()
                                                            + " hit in run", e);
                                                } catch (ExecutionException e) {
                                                    Log.d(TAG, "catch " + e.toString()
                                                            + " hit in run", e);
                                                }
                                                mProgressDialog.cancel();
                                            }
                                        }).start();
                                    }
                                }
                        );

                        adb.setNegativeButton(getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                }
                        );
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!silent) {
                                    mProgressDialog.cancel();
                                }
                                adb.show();
                            }
                        });
                    } else if (!doUpdate) {
                        if (!silent) {
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
            }
        }).start();
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public boolean onLongClick(final View v) {
        final Button btn = (Button) v;
        Log.i(TAG, (String) btn.getContentDescription());
        final String usage = (String) btn.getContentDescription();
        if (prepBISpinner()) {
            result = false;
            if (current_mode.equals("send")) {
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        try {
                            while (btn.isPressed() && run_threads) {
                                sendKeyBool(IRCommon.getIrPath() + item + "/" + usage + ".bin");
                                sleep(400);
                            }
                        } catch (InterruptedException e) {
                            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                        }
                    }
                };
                t.start();
            } else if (current_mode.equals("write")) {
                learnKeyBool(IRCommon.getIrPath() + item + "/" + usage + ".bin");
            }
        }
        return true;
    }

    /**
     * A {@link android.support.v13.app.FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            PlaceholderFragment fragment = new PlaceholderFragment();
            return fragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            return getString(R.string.sr).toUpperCase(l);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    @SuppressLint("ValidFragment")
    public class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Log.i("ARG_SECTION_NUMBER", String.valueOf(getArguments().getInt(ARG_SECTION_NUMBER)));
            int current = getArguments().getInt(ARG_SECTION_NUMBER) - 1;
            mView.clear();
            mView.add(inflater.inflate(R.layout.activity_ir1, container, false));
            mView.add(inflater.inflate(R.layout.activity_ir2, container, false));
            String arr[] = mView.get(current).getContentDescription()
                    .toString().split("-", 2);
            String firstWord = arr[0];
            String theRest = arr[1];
            for (int i = Integer.parseInt(firstWord); i <= Integer.parseInt(theRest); i++) {
                final String btn = "button" + i;
                Button button = (Button) mView.get(current).findViewById(mView.get(current)
                        .getResources().getIdentifier(btn,
                                "id", getPackageName()));
                button.setOnLongClickListener(IRMain.this);
            }
            File f = new File(IRCommon.getIrPath() + item + "/text.ini");
            if (f.exists()) {
                try {
                    FileInputStream is = new FileInputStream(f);
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(is));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String arr2[] = line.split(" ", 2);
                        firstWord = arr2[0];
                        theRest = arr2[1];
                        final Button button = (Button) mView.get(current)
                                .findViewById(mView.get(current).getResources()
                                        .getIdentifier(firstWord, "id", getPackageName()));
                        if (button != null) {
                            final String finalTheRest = theRest;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    button.setText(finalTheRest);
                                }
                            });
                        }
                    }
                    reader.close();
                    is.close();
                } catch (FileNotFoundException e) {
                    Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                } catch (IOException e) {
                    Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                }
            }
            return mView.get(current);
        }
    }

    private class StateChecker extends Handler {

        public StateChecker(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (run_threads) {
                checkState();
                sendEmptyMessageDelayed(0, 500);
            }
        }
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position, false);
        }
    }

    private class DrawerItemLongClickListener implements ListView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position, true);
            return false;
        }
    }

    private class NonZeroStatusException extends Exception {
    }
}

