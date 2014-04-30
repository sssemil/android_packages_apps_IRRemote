package com.sssemil.sonyirremote.ir;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;


/**
 * Copyright (c) 2014 Emil Suleymanov
 * Distributed under the GNU GPL v2. For full terms see the file LICENSE.
 */

public class ir extends Activity {

    public static final String PREFS_NAME = "SIRR";
    public String irpath = "/data/data/com.sssemil.sonyirremote.ir/ir/";//place to store commands
    public String http_path_root2;
    public String http_path_last_download1;
    public String http_path_last_download2;
    public int state = 0;
    public String brand;
    public String item;
    public boolean wrt = false;
    public String last_ver = "zirt";
    public String cur_ver;
    Spinner spinner;
    boolean main = true;
    long lastPress;
    ProgressDialog mProgressDialog;
    SharedPreferences settings;
    boolean result = false;
    private ArrayList localArrayList1;

    public static boolean fixPermissionsForIr() {
        //TODO add all this to ramdisk
        // IR Paths
        String[] irEnable = {"su", "-c", "chown system:system /sys/devices/platform/ir_remote_control/enable /dev/ttyHSL2"};
        String[] enablePermissions = {"su", "-c", "chmod 222 /sys/devices/platform/ir_remote_control/enable"};
        String[] devicePermissions = {"su", "-c", "chmod 666 /dev/ttyHSL2"};
        try {
            // Try to enable Infrared Devices
            Runtime.getRuntime().exec(irEnable);
            Runtime.getRuntime().exec(enablePermissions);
            Runtime.getRuntime().exec(devicePermissions);
        } catch (IOException e) {
            // Elevating failed
            return false;
        } finally {
            return true;
        }
    }

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
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        settings = getSharedPreferences("com.sssemil.sonyirremote.ir_preferences", 0);
        if (settings.contains("theme")) {
            if (settings.getString("theme", null).equals("1")) {
                super.setTheme(R.style.Holo);
            } else if (settings.getString("theme", null).equals("2")) {
                super.setTheme(R.style.Holo_Light_DarkActionBar);
            } else if (settings.getString("theme", null).equals("3")) {
                super.setTheme(R.style.Theme_Holo_Light);
            }
        }
        prepItemBrandArray();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = getSharedPreferences("com.sssemil.sonyirremote.ir_preferences", 0);
        if (settings.contains("theme")) {
            if (settings.getString("theme", null).equals("1")) {
                super.setTheme(R.style.Holo);
            } else if (settings.getString("theme", null).equals("2")) {
                super.setTheme(R.style.Holo_Light_DarkActionBar);
            } else if (settings.getString("theme", null).equals("3")) {
                super.setTheme(R.style.Theme_Holo_Light);
            }
        }
        setContentView(R.layout.activity_ir);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        fixPermissionsForIr();
        spinner = ((Spinner) findViewById(R.id.spinner));
        http_path_root2 = getString(R.string.http_path_root2);
        http_path_last_download1 = getString(R.string.http_path_last_download1);
        http_path_last_download2 = getString(R.string.http_path_last_download2);
        new Thread(new Runnable() {
            public void run() {
                IRCommon.getInstance().start();
            }
        }).start();
        prepIRKeys();
        prepItemBrandArray();
        addUUID();
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            cur_ver = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        cur_ver = pInfo.versionName;
        firstRunChecker();
        Thread thread = new Thread() {
            public void run() {
                File f;
                while (true) {
                    if (main) {
                        try {
                            spinner = (Spinner) findViewById(R.id.spinner);
                            item = spinner.getSelectedItem().toString();
                            f = new File(irpath + item + "/disable.ini");
                            if (f.exists() && !wrt) {
                                try {
                                    FileInputStream is = new FileInputStream(f);
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                                    String line = null;
                                    while ((line = reader.readLine()) != null) {
                                        final String finalLine = line;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                int id = getResources().getIdentifier(finalLine,
                                                        "id", "com.sssemil.sonyirremote.ir");
                                                Button button = ((Button) findViewById(id));
                                                try {
                                                    button.setEnabled(false);
                                                } catch (Exception ex) {
                                                    //ex.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                    reader.close();
                                    is.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                for (int i = 3; i < 25; i++) {
                                    final String btn = "button" + i;
                                    final int finalI = i;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            int id = getResources().getIdentifier(btn,
                                                    "id", "com.sssemil.sonyirremote.ir");
                                            Button button = ((Button) findViewById(id));
                                            try {
                                                button.setEnabled(true);
                                            } catch (Exception ex) {
                                                //ex.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            }
                            try {
                                Thread.sleep(500);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } catch (Exception e) {
                            //e.printStackTrace();
                        }
                    }
                }
            }
        };
        thread.start();

        Thread btn3 = new Thread() {
            @Override
            public void run() {
                final Button button = (Button) findViewById(R.id.button3);
                button.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            button.setPressed(true);
                            v.playSoundEffect(android.view.SoundEffectConstants.CLICK);
                            if (prepBISpinner()) ;
                            {
                                result = false;
                                if (!wrt) {
                                    Thread t = new Thread() {
                                        @Override
                                        public void run() {
                                            try {
                                                while (button.isPressed()) {
                                                    sendKeyBool(irpath + item + "/chanelPl.bin");
                                                    sleep(400);
                                                }
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    };
                                    t.start();
                                } else if (wrt) {
                                    learnKeyBool(irpath + item + "/chanelPl.bin");
                                }
                            }
                        } else if (event.getAction() == MotionEvent.ACTION_UP) {
                            button.setPressed(false);
                        }
                        return true;
                    }
                });
            }
        };
        btn3.start();

        Thread btn4 = new Thread() {
            @Override
            public void run() {
                final Button button = (Button) findViewById(R.id.button4);
                button.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            button.setPressed(true);
                            v.playSoundEffect(android.view.SoundEffectConstants.CLICK);
                            if (prepBISpinner()) ;
                            {
                                result = false;
                                if (!wrt) {
                                    Thread t = new Thread() {
                                        @Override
                                        public void run() {
                                            try {
                                                while (button.isPressed()) {
                                                    sendKeyBool(irpath + item + "/chanelMn.bin");
                                                    sleep(400);
                                                }
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    };
                                    t.start();
                                } else if (wrt) {
                                    learnKeyBool(irpath + item + "/chanelMn.bin");
                                }
                            }
                        } else if (event.getAction() == MotionEvent.ACTION_UP) {
                            button.setPressed(false);
                        }
                        return true;
                    }
                });
            }
        };
        btn4.start();

        Thread btn5 = new Thread() {
            @Override
            public void run() {
                final Button button = (Button) findViewById(R.id.button5);
                button.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            button.setPressed(true);
                            v.playSoundEffect(android.view.SoundEffectConstants.CLICK);
                            if (prepBISpinner()) ;
                            {
                                result = false;
                                if (!wrt) {
                                    Thread t = new Thread() {
                                        @Override
                                        public void run() {
                                            try {
                                                while (button.isPressed()) {
                                                    sendKeyBool(irpath + item + "/volPl.bin");
                                                    sleep(400);
                                                }
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    };
                                    t.start();
                                } else if (wrt) {
                                    learnKeyBool(irpath + item + "/volPl.bin");
                                }
                            }
                        } else if (event.getAction() == MotionEvent.ACTION_UP) {
                            button.setPressed(false);
                        }
                        return true;
                    }
                });
            }
        };
        btn5.start();

        Thread btn6 = new Thread() {
            @Override
            public void run() {
                final Button button = (Button) findViewById(R.id.button6);
                button.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            button.setPressed(true);
                            v.playSoundEffect(android.view.SoundEffectConstants.CLICK);
                            if (prepBISpinner()) ;
                            {
                                result = false;
                                if (!wrt) {
                                    Thread t = new Thread() {
                                        @Override
                                        public void run() {
                                            try {
                                                while (button.isPressed()) {
                                                    sendKeyBool(irpath + item + "/volMn.bin");
                                                    sleep(400);
                                                }
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    };
                                    t.start();
                                } else if (wrt) {
                                    learnKeyBool(irpath + item + "/volMn.bin");
                                }
                            }
                        } else if (event.getAction() == MotionEvent.ACTION_UP) {
                            button.setPressed(false);
                        }
                        return true;
                    }
                });
            }
        };
        btn6.start();

        Thread btn17 = new Thread() {
            @Override
            public void run() {
                final Button button = (Button) findViewById(R.id.button17);
                button.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            button.setPressed(true);
                            v.playSoundEffect(android.view.SoundEffectConstants.CLICK);
                            if (prepBISpinner()) ;
                            {
                                result = false;
                                if (!wrt) {
                                    Thread t = new Thread() {
                                        @Override
                                        public void run() {
                                            try {
                                                while (button.isPressed()) {
                                                    sendKeyBool(irpath + item + "/up.bin");
                                                    sleep(400);
                                                }
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    };
                                    t.start();
                                } else if (wrt) {
                                    learnKeyBool(irpath + item + "/up.bin");
                                }
                            }
                        } else if (event.getAction() == MotionEvent.ACTION_UP) {
                            button.setPressed(false);
                        }
                        return true;
                    }
                });
            }
        };
        btn17.start();

        Thread btn18 = new Thread() {
            @Override
            public void run() {
                final Button button = (Button) findViewById(R.id.button18);
                button.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            button.setPressed(true);
                            v.playSoundEffect(android.view.SoundEffectConstants.CLICK);
                            if (prepBISpinner()) ;
                            {
                                result = false;
                                if (!wrt) {
                                    Thread t = new Thread() {
                                        @Override
                                        public void run() {
                                            try {
                                                while (button.isPressed()) {
                                                    sendKeyBool(irpath + item + "/down.bin");
                                                    sleep(400);
                                                }
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    };
                                    t.start();
                                } else if (wrt) {
                                    learnKeyBool(irpath + item + "/down.bin");
                                }
                            }
                        } else if (event.getAction() == MotionEvent.ACTION_UP) {
                            button.setPressed(false);
                        }
                        return true;
                    }
                });
            }
        };
        btn18.start();

        Thread btn20 = new Thread() {
            @Override
            public void run() {
                final Button button = (Button) findViewById(R.id.button20);
                button.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            button.setPressed(true);
                            v.playSoundEffect(android.view.SoundEffectConstants.CLICK);
                            if (prepBISpinner()) ;
                            {
                                result = false;
                                if (!wrt) {
                                    Thread t = new Thread() {
                                        @Override
                                        public void run() {
                                            try {
                                                while (button.isPressed()) {
                                                    sendKeyBool(irpath + item + "/right.bin");
                                                    sleep(400);
                                                }
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    };
                                    t.start();
                                } else if (wrt) {
                                    learnKeyBool(irpath + item + "/right.bin");
                                }
                            }
                        } else if (event.getAction() == MotionEvent.ACTION_UP) {
                            button.setPressed(false);
                        }
                        return true;
                    }
                });
            }
        };
        btn20.start();

        Thread btn21 = new Thread() {
            @Override
            public void run() {
                final Button button = (Button) findViewById(R.id.button21);
                button.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            button.setPressed(true);
                            v.playSoundEffect(android.view.SoundEffectConstants.CLICK);
                            if (prepBISpinner()) ;
                            {
                                result = false;
                                if (!wrt) {
                                    Thread t = new Thread() {
                                        @Override
                                        public void run() {
                                            try {
                                                while (button.isPressed()) {
                                                    sendKeyBool(irpath + item + "/left.bin");
                                                    sleep(400);
                                                }
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    };
                                    t.start();
                                } else if (wrt) {
                                    learnKeyBool(irpath + item + "/left.bin");
                                }
                            }
                        } else if (event.getAction() == MotionEvent.ACTION_UP) {
                            button.setPressed(false);
                        }
                        return true;
                    }
                });
            }
        };
        btn21.start();

        Thread btn29 = new Thread() {
            @Override
            public void run() {
                final Button button = (Button) findViewById(R.id.button29);
                button.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            button.setPressed(true);
                            v.playSoundEffect(android.view.SoundEffectConstants.CLICK);
                            if (prepBISpinner()) ;
                            {
                                result = false;
                                if (!wrt) {
                                    Thread t = new Thread() {
                                        @Override
                                        public void run() {
                                            try {
                                                while (button.isPressed()) {
                                                    sendKeyBool(irpath + item + "/wBack.bin");
                                                    sleep(400);
                                                }
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    };
                                    t.start();
                                } else if (wrt) {
                                    learnKeyBool(irpath + item + "/wBack.bin");
                                }
                            }
                        } else if (event.getAction() == MotionEvent.ACTION_UP) {
                            button.setPressed(false);
                        }
                        return true;
                    }
                });
            }
        };
        btn29.start();

        Thread btn32 = new Thread() {
            @Override
            public void run() {
                final Button button = (Button) findViewById(R.id.button32);
                button.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            button.setPressed(true);
                            v.playSoundEffect(android.view.SoundEffectConstants.CLICK);
                            if (prepBISpinner()) ;
                            {
                                result = false;
                                if (!wrt) {
                                    Thread t = new Thread() {
                                        @Override
                                        public void run() {
                                            try {
                                                while (button.isPressed()) {
                                                    sendKeyBool(irpath + item + "/wFwrd.bin");
                                                    sleep(400);
                                                }
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    };
                                    t.start();
                                } else if (wrt) {
                                    learnKeyBool(irpath + item + "/wFwrd.bin");
                                }
                            }
                        } else if (event.getAction() == MotionEvent.ACTION_UP) {
                            button.setPressed(false);
                        }
                        return true;
                    }
                });
            }
        };
        btn32.start();
    }

    private void addUUID() {
        boolean empty = true;
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        if (!settings.contains("UUID")) {
            empty = true;
        } else if (settings.contains("UUID")) {
            empty = false;
        }
        if (empty) {
            String id = UUID.randomUUID().toString();
            settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("UUID", id);
            editor.commit();
            setUUID setUUID1 = new setUUID(this);
            setUUID1.execute(id);
        }
    }

    public void firstRunChecker() {
        boolean isFirstRun = true;
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        if (!settings.contains("isFirstRun")) {
            isFirstRun = true;
        } else {
            isFirstRun = settings.getBoolean("isFirstRun", false);
        }
        if (isFirstRun) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.welcome));
            builder.setMessage(getString(R.string.fr));
            builder.setPositiveButton(getString(R.string.pos_ans), null);
            builder.show();
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("isFirstRun", false);
            editor.commit();
        }

        boolean checUpd = false;
        SharedPreferences settings2 = getSharedPreferences("com.sssemil.sonyirremote.ir_preferences", 0);
        if (!settings2.contains("checkUpd")) {
            checUpd = false;
        } else {
            checUpd = settings.getBoolean("checkUpd", true);
        }
        if (checUpd) {
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
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setTitle(getString(R.string.warning));
            adb.setMessage(getString(R.string.alredy_exists));
            adb.setIcon(android.R.drawable.ic_dialog_alert);
            adb.setPositiveButton(getString(R.string.pos_ans), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    learnAction(filename);
                }
            });

            adb.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            adb.show();
        } else {
            learnAction(filename);
        }
    }

    public void learnAction(final String filename) {
        mProgressDialog = new ProgressDialog(ir.this);
        mProgressDialog.setMessage(getString(R.string.waiting_for_signal));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.show();
        new Thread(new Runnable() {
            public void run() {
                mProgressDialog.show();
                IRCommon.getInstance().restart();
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
        spinner = ((Spinner) findViewById(R.id.spinner));
        if (spinner.getSelectedItem() != null) {
            if (!to.exists()) {
                AlertDialog.Builder adb = new AlertDialog.Builder(this);
                adb.setTitle(getString(R.string.warning));
                adb.setMessage(getString(R.string.not_exists));
                adb.setIcon(android.R.drawable.ic_dialog_alert);
                adb.setPositiveButton(getString(R.string.pos_ans), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startLearning(filename);
                    }
                });

                adb.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                adb.show();
            } else {
                sendAction(filename);
            }
        } else {
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

    public void alert(String msg) {
        AlertDialog.Builder errorD = new AlertDialog.Builder(this);
        errorD.setTitle(getString(R.string.error));
        errorD.setMessage(msg);
        errorD.setIcon(android.R.drawable.ic_dialog_alert);
        errorD.setPositiveButton(getString(R.string.pos_ans), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        errorD.show();
    }

    public void sendAction(final String filename) {
        new Thread(new Runnable() {
            public void run() {
                state = IRCommon.getInstance().send(filename);
                if (state < 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            alert(getString(R.string.non_zero));
                        }
                    });
                }
            }
        }).start();
    }

    public void prepIRKeys() {
        File f = new File(irpath);
        if (!f.isDirectory()) {
            f.mkdirs();
        }
    }

    public void prepItemBrandArray() {
        spinner = ((Spinner) findViewById(R.id.spinner));
        localArrayList1 = new ArrayList();
        boolean edited = false;

        for (File localFile1 : new File(this.irpath).listFiles()) {
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ir, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            //setContentView(R.layout.settings_ir);
            main = false;
            Intent intent = new Intent(ir.this,
                    IRSettings.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_exit) {
            IRCommon.getInstance().stop();
            System.exit(0);
            return true;
        } else if (id == R.id.action_update) {
            update(false);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPress > 5000) {
            Toast.makeText(getBaseContext(), getString(R.string.pr_bck_ag), Toast.LENGTH_LONG).show();
            lastPress = currentTime;
        } else {
            IRCommon.getInstance().stop();
            super.onBackPressed();
        }
        setContentView(R.layout.activity_ir);
        main = true;
        prepItemBrandArray();
    }

    public void onWrtClick(View view) {
        if (wrt) {
            wrt = false;
            final Button btntxt = (Button) findViewById(R.id.button2);
            btntxt.setText(getResources().getString(R.string.Write_signal));
            settings = getSharedPreferences("com.sssemil.sonyirremote.ir_preferences", 0);
            if (settings.contains("theme")) {
                if (settings.getString("theme", null).equals("1")) {
                    btntxt.setTextColor(Color.WHITE);
                } else {
                    btntxt.setTextColor(Color.BLACK);
                }
            }
        } else if (!wrt) {
            //TODO add warning
            final AlertDialog.Builder adb = new AlertDialog.Builder(this);
            final View promptsView = LayoutInflater.from(this).inflate(R.layout.wrt_mode, null);
            adb.setTitle(getString(R.string.warning));
            adb.setView(promptsView);
            adb.setPositiveButton(getString(R.string.start), null);
            adb
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.start),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    wrt = true;
                                    final Button btntxt = (Button) findViewById(R.id.button2);
                                    btntxt.setText(getResources().getString(R.string.Send_signal));
                                    btntxt.setTextColor(Color.RED);

                                    File f = new File(irpath + item);
                                    if (!f.isDirectory()) {
                                        f.mkdirs();
                                    }

                                    File f2 = new File(irpath + brand);
                                    if (!f2.isDirectory()) {
                                        f2.mkdirs();
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
            AlertDialog alertDialog = adb.show();
            alertDialog.getWindow().setLayout(1350, 1000);
        }
    }

    public boolean prepBISpinner() {
        try {
            spinner = (Spinner) findViewById(R.id.spinner);
            item = spinner.getSelectedItem().toString();
            result = true;
        } catch (NullPointerException ex) {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setTitle(getString(R.string.error));
            adb.setMessage(getString(R.string.you_need_to_select));
            adb.setIcon(android.R.drawable.ic_dialog_alert);
            adb.setPositiveButton(getString(R.string.pos_ans), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    result = false;
                }
            });
            adb.show();
        }
        return result;
    }

    public void onPowerClick(final View view) {
        if (prepBISpinner()) ;
        {
            result = false;
            if (!wrt) {
                sendKeyBool(irpath + item + "/power.bin");
            } else if (wrt) {
                learnKeyBool(irpath + item + "/power.bin");
            }
        }
    }

    public void onChanelPlClick(View view) {
        if (prepBISpinner()) ;
        {
            result = false;
            if (!wrt) {
                sendKeyBool(irpath + item + "/chanelPl.bin");
            } else if (wrt) {
                learnKeyBool(irpath + item + "/chanelPl.bin");
            }
        }
    }

    public void onChanelMnClick(View view) {
        if (prepBISpinner()) ;
        {
            result = false;
            if (!wrt) {
                sendKeyBool(irpath + item + "/chanelMn.bin");
            } else if (wrt) {
                learnKeyBool(irpath + item + "/chanelMn.bin");
            }
        }
    }

    public void onVolumePlClick(View view) {
        if (prepBISpinner()) ;
        {
            result = false;
            if (!wrt) {
                sendKeyBool(irpath + item + "/volPl.bin");
            } else if (wrt) {
                learnKeyBool(irpath + item + "/volPl.bin");
            }
        }
    }

    public void onVolumeMnClick(View view) {
        if (prepBISpinner()) ;
        {
            result = false;
            if (!wrt) {
                sendKeyBool(irpath + item + "/volMn.bin");
            } else if (wrt) {
                learnKeyBool(irpath + item + "/volMn.bin");
            }
        }
    }

    public void on1Click(View view) {
        if (prepBISpinner()) ;
        {
            result = false;
            if (!wrt) {
                sendKeyBool(irpath + item + "/1.bin");
            } else if (wrt) {
                learnKeyBool(irpath + item + "/1.bin");
            }
        }
    }

    public void on2Click(View view) {
        if (prepBISpinner()) ;
        {
            result = false;
            if (!wrt) {
                sendKeyBool(irpath + item + "/2.bin");
            } else if (wrt) {
                learnKeyBool(irpath + item + "/2.bin");
            }
        }
    }

    public void on3Click(View view) {
        if (prepBISpinner()) ;
        {
            result = false;
            if (!wrt) {
                sendKeyBool(irpath + item + "/3.bin");
            } else if (wrt) {
                learnKeyBool(irpath + item + "/3.bin");
            }
        }
    }

    public void on4Click(View view) {
        if (prepBISpinner()) ;
        {
            result = false;
            if (!wrt) {
                sendKeyBool(irpath + item + "/4.bin");
            } else if (wrt) {
                learnKeyBool(irpath + item + "/4.bin");
            }
        }
    }

    public void on5Click(View view) {
        if (prepBISpinner()) ;
        {
            result = false;
            if (!wrt) {
                sendKeyBool(irpath + item + "/5.bin");
            } else if (wrt) {
                learnKeyBool(irpath + item + "/5.bin");
            }
        }
    }

    public void on6Click(View view) {
        if (prepBISpinner()) ;
        {
            result = false;
            if (!wrt) {
                sendKeyBool(irpath + item + "/6.bin");
            } else if (wrt) {
                learnKeyBool(irpath + item + "/6.bin");
            }
        }
    }

    public void on7Click(View view) {
        if (prepBISpinner()) ;
        {
            result = false;
            if (!wrt) {
                sendKeyBool(irpath + item + "/7.bin");
            } else if (wrt) {
                learnKeyBool(irpath + item + "/7.bin");
            }
        }
    }

    public void on8Click(View view) {
        if (prepBISpinner()) ;
        {
            result = false;
            if (!wrt) {
                sendKeyBool(irpath + item + "/8.bin");
            } else if (wrt) {
                learnKeyBool(irpath + item + "/8.bin");
            }
        }
    }

    public void on9Click(View view) {
        if (prepBISpinner()) ;
        {
            result = false;
            if (!wrt) {
                sendKeyBool(irpath + item + "/9.bin");
            } else if (wrt) {
                learnKeyBool(irpath + item + "/9.bin");
            }
        }
    }

    public void on0Click(View view) {
        if (prepBISpinner()) ;
        {
            result = false;
            if (!wrt) {
                sendKeyBool(irpath + item + "/0.bin");
            } else if (wrt) {
                learnKeyBool(irpath + item + "/0.bin");
            }
        }
    }

    public void onUpClick(View view) {
        if (prepBISpinner()) ;
        {
            result = false;
            if (!wrt) {
                sendKeyBool(irpath + item + "/up.bin");
            } else if (wrt) {
                learnKeyBool(irpath + item + "/up.bin");
            }
        }
    }

    public void onDownClick(View view) {
        if (prepBISpinner()) ;
        {
            result = false;
            if (!wrt) {
                sendKeyBool(irpath + item + "/down.bin");
            } else if (wrt) {
                learnKeyBool(irpath + item + "/down.bin");
            }
        }
    }

    public void onLeftClick(View view) {
        if (prepBISpinner()) ;
        {
            result = false;
            if (!wrt) {
                sendKeyBool(irpath + item + "/left.bin");
            } else if (wrt) {
                learnKeyBool(irpath + item + "/left.bin");
            }
        }
    }

    public void onRightClick(View view) {
        if (prepBISpinner()) ;
        {
            result = false;
            if (!wrt) {
                sendKeyBool(irpath + item + "/right.bin");
            } else if (wrt) {
                learnKeyBool(irpath + item + "/right.bin");
            }
        }
    }

    public void onEnterClick(View view) {
        if (prepBISpinner()) ;
        {
            result = false;
            if (!wrt) {
                sendKeyBool(irpath + item + "/enter.bin");
            } else if (wrt) {
                learnKeyBool(irpath + item + "/enter.bin");
            }
        }
    }

    public void onTsttClick(View view) {
        if (prepBISpinner()) ;
        {
            result = false;
            if (!wrt) {
                sendKeyBool(irpath + item + "/tstt.bin");
            } else if (wrt) {
                learnKeyBool(irpath + item + "/tstt.bin");
            }
        }
    }

    public void onReturnClick(View view) {
        if (prepBISpinner()) ;
        {
            result = false;
            if (!wrt) {
                sendKeyBool(irpath + item + "/return.bin");
            } else if (wrt) {
                learnKeyBool(irpath + item + "/return.bin");
            }
        }
    }

    public void onOptionsClick(View view) {
        if (prepBISpinner()) ;
        {
            result = false;
            if (!wrt) {
                sendKeyBool(irpath + item + "/options.bin");
            } else if (wrt) {
                learnKeyBool(irpath + item + "/options.bin");
            }
        }
    }

    public void onGuideClick(View view) {
        if (prepBISpinner()) ;
        {
            result = false;
            if (!wrt) {
                sendKeyBool(irpath + item + "/guide.bin");
            } else if (wrt) {
                learnKeyBool(irpath + item + "/guide.bin");
            }
        }
    }

    public void onMuteClick(View view) {
        if (prepBISpinner()) ;
        {
            result = false;
            if (!wrt) {
                sendKeyBool(irpath + item + "/mute.bin");
            } else if (wrt) {
                learnKeyBool(irpath + item + "/mute.bin");
            }
        }
    }

    public void onHomeClick(View view) {
        if (prepBISpinner()) ;
        {
            result = false;
            if (!wrt) {
                sendKeyBool(irpath + item + "/home.bin");
            } else if (wrt) {
                learnKeyBool(irpath + item + "/home.bin");
            }
        }
    }

    public void onInputClick(View view) {
        if (prepBISpinner()) ;
        {
            result = false;
            if (!wrt) {
                sendKeyBool(irpath + item + "/input.bin");
            } else if (wrt) {
                learnKeyBool(irpath + item + "/input.bin");
            }
        }
    }

    public void onPauseClick(View view) {
        if (prepBISpinner()) ;
        {
            result = false;
            if (!wrt) {
                sendKeyBool(irpath + item + "/pause.bin");
            } else if (wrt) {
                learnKeyBool(irpath + item + "/pause.bin");
            }
        }
    }

    public void onPlayClick(View view) {
        if (prepBISpinner()) ;
        {
            result = false;
            if (!wrt) {
                sendKeyBool(irpath + item + "/play.bin");
            } else if (wrt) {
                learnKeyBool(irpath + item + "/play.bin");
            }
        }
    }

    public String compare(String v1, String v2) {
        String s1 = normalisedVersion(v1);
        String s2 = normalisedVersion(v2);
        int cmp = s1.compareTo(s2);
        String cmpStr = cmp < 0 ? "<" : cmp > 0 ? ">" : "==";
        return cmpStr;
    }

    public void update(final boolean silent) {
        final GetLastVer getLastVer1 = new GetLastVer(ir.this);
        final AlertDialog.Builder adb = new AlertDialog.Builder(this);
        if (!silent) {
            mProgressDialog = new ProgressDialog(ir.this);
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
                    Log.i("Update", "last_ver : " + getLastVer1.execute().get() + " cur_ver : " + cur_ver);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                if (last_ver == "zirt") {
                    if (!silent) {
                        adb.setTitle(getString(R.string.update));
                        adb.setMessage(getString(R.string.ser3));
                        adb.setIcon(android.R.drawable.ic_dialog_alert);
                        adb.setPositiveButton(getString(R.string.pos_ans), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                update(silent);
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
                    }
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
                                mProgressDialog = new ProgressDialog(ir.this);
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

                                        final DownloadApp downloadApp1 = new DownloadApp(ir.this);
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
                                if (!silent) {
                                    mProgressDialog.cancel();
                                }
                                adb.show();
                            }
                        });
                    } else if (doUpdate == false) {
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

    class setUUID extends AsyncTask<String, Integer, String> {

        DefaultHttpClient httpclient = new DefaultHttpClient();
        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public setUUID(Context context) {
            this.context = context;
        }

        protected String doInBackground(String... UUID) {
            try {
                Log.i("setUUID", UUID.toString());
                HttpGet httppost = new HttpGet("http://sssemil.comli.com/uuid.php?uuid=" + UUID[0]);
                HttpResponse response = httpclient.execute(httppost);
                response.getEntity();
                return "done";
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