package com.sssemil.sonyirremoute.ir;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
  *
  * Copyright (c) 2014 Emil Suleymanov
  * Distributed under the GNU GPL v2. For full terms see the file LICENSE.
  *
  */

public class ir extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ir);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        fixPermissionsForIr();
        new Thread(new Runnable() {
            public void run() {
                startIR();
            }
        }).start();
        prepIRKeys();
        prepItemBrandArray();
        //SharedPreferences irPrefs = getSharedPreferences("irPrefs", MODE_PRIVATE);
        //irpath = irPrefs.getString("irpath", "");
        /*new Thread(new Runnable() {
            public void run() {
                IrHelper.getInstance().startIrNative();
            }
        }).start();*/
    }

    static {
        System.loadLibrary("jni_sonyopenir");
    }

    public native int startIR();
    public native int stopIR();
    public native int learnKey(String filename);
    public native int sendKey(String filename);


    public String irpath = "/data/data/com.sssemil.sonyirremoute.ir/ir/";

    public void prepIRKeys()
    {
        File f = new File(irpath);
        if (!f.isDirectory()) {
            f.mkdirs();
            DownloadALL();
            UnzipALL();
        }
    }

    private Spinner spinner;
    private Spinner spinner2;

    public void prepItemBrandArray()
    {
        spinner = ((Spinner)findViewById(R.id.spinner2));
        ArrayList localArrayList1 = new ArrayList();
        spinner2 = ((Spinner)findViewById(R.id.spinner));
        ArrayList localArrayList2 = new ArrayList();
        for (File localFile1 : new File(this.irpath).listFiles())
            if (localFile1.isDirectory())
            {
                localArrayList1.add(localFile1.getName());
                for (File localFile2 : new File(localFile1.getPath() + "/").listFiles())
                    if ((localFile2.isDirectory()) && (localFile2.getName() != null))
                        localArrayList2.add(localFile2.getName());
            }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, localArrayList1);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(dataAdapter);

        ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, localArrayList2);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter2);
    }

    public void onMainClick(View paramView)
    {
        setContentView(R.layout.activity_ir);
    }


    EditText brandN;
    EditText itemN;

    public void onAddDeviceClick(View paramView)
    {
        this.itemN = ((EditText)findViewById(R.id.editText));
        this.brandN = ((EditText)findViewById(R.id.editText2));
        File localFile1 = new File(this.irpath + this.brandN.toString());
        if (!localFile1.isDirectory())
            localFile1.mkdirs();
        File localFile2 = new File(this.irpath + this.brandN.toString() + "/" + this.itemN.toString());
        if (!localFile2.isDirectory())
            localFile2.mkdirs();
        Toast.makeText(this, this.irpath + this.brandN.toString() + "/" + this.itemN.toString(), 0).show();
    }


    public static boolean fixPermissionsForIr()
    {
        // IR Paths
        String[] irEnable = { "su", "-c", "chown system:sdcard_rw /sys/devices/platform/ir_remote_control/enable /dev/ttyHSL2"};
        String[] enablePermissions = { "su", "-c", "chmod 220 /sys/devices/platform/ir_remote_control/enable"};
        String[] devicePermissions = { "su", "-c", "chmod 660 /dev/ttyHSL2"};
        //String[] binPermissions = { "su", "-c", "chmod 660 /data/data/com.sssemil.ir/bin/irtest"};
        try {
            // Try to enable Infrared Devices
            Runtime.getRuntime().exec(irEnable);
            Runtime.getRuntime().exec(enablePermissions);
            Runtime.getRuntime().exec(devicePermissions);
            //Runtime.getRuntime().exec(binPermissions);
        } catch (IOException e) {
            // Elevating failed
            return false;
        } finally {
            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.ir, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            setContentView(R.layout.settings_ir);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public String brand;
    public String item;
    public boolean wrt = false;

    public void onAboutClick(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("About");
        builder.setMessage(getResources().getString(R.string.license1) + "\n" + getResources().getString(R.string.license2) + "\n" + getResources().getString(R.string.license3)+ "\n" + getResources().getString(R.string.license4));
        builder.setPositiveButton("OK", null);
        AlertDialog dialog = builder.show();

// Must call show() prior to fetching text view
        TextView messageView = (TextView)dialog.findViewById(android.R.id.message);
        messageView.setGravity(Gravity.CENTER);
    }


    public void onWrtClick(View view)
    {
        if(wrt) {
            wrt = false;
            final Button btntxt = (Button) findViewById(R.id.button2);
            btntxt.setText(getResources().getString(R.string.Write_signal));
            btntxt.setTextColor(Color.BLACK);
        }
        else if(!wrt) {
            wrt = true;
            final Button btntxt = (Button) findViewById(R.id.button2);
            btntxt.setText(getResources().getString(R.string.Send_signal));
            btntxt.setTextColor(Color.RED);

            File f = new File(irpath + brand + "/" + item);
            if (!f.isDirectory()) {
                f.mkdirs();
            }

            File f2 = new File(irpath + brand);
            if (!f2.isDirectory()) {
                f2.mkdirs();
            }
        }
    }

    public void onPowerClick(View view)
    {
        if(!wrt) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "Power" + brand, Toast.LENGTH_SHORT).show();

            sendKey(irpath + brand + "/" + item + "/power.bin");
        }
        else if(wrt) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "Power" + brand + " LEARNING!", Toast.LENGTH_SHORT).show();

            learnKey(irpath + brand + "/" + item + "/power.bin");
        }
    }

    public void onChanelPlClick(View view)
    {
        if(!wrt) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "ChanelPl" + brand, Toast.LENGTH_SHORT).show();

            sendKey(irpath + brand + "/" + item + "/chanelPl.bin");
        }
        else if(wrt) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "ChanelPl" + brand + " LEARNING!", Toast.LENGTH_SHORT).show();

            learnKey(irpath + brand + "/" + item + "/chanelPl.bin");
        }
    }

    public void onChanelMnSonClick(View view)
    {
        if(!wrt) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "ChanelMn" + brand, Toast.LENGTH_SHORT).show();
            sendKey(irpath + brand + "/" + item + "/chanelMn.bin");
        }
        else if(wrt) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "ChanelMn" + brand + " LEARNING!", Toast.LENGTH_SHORT).show();
            learnKey(irpath + brand + "/" + item + "/chanelMn.bin");
        }
    }

    public void onVolumePlClick(View view)
    {
        if (!wrt) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "VolumePl" + brand, Toast.LENGTH_SHORT).show();
            sendKey(irpath + brand + "/" + item + "/volPl.bin");
        }
        else if(wrt) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "VolumePl" + brand + " LEARNING!", Toast.LENGTH_SHORT).show();
            learnKey(irpath + brand + "/" + item + "/volPl.bin");
        }
    }

    public void onVolumeMnClick(View view)
    {
        if(!wrt) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "VolumeMn" + brand, Toast.LENGTH_SHORT).show();
            sendKey(irpath + brand + "/" + item + "/volMn.bin");
        }
        else if(wrt) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "VolumeMn" + brand + " LEARNING!", Toast.LENGTH_SHORT).show();
            learnKey(irpath + brand + "/" + item + "/volMn.bin");
        }
    }

    public void on1Click(View view)
    {
        if(!wrt) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "1" + brand, Toast.LENGTH_SHORT).show();
            sendKey(irpath + brand + "/" + item + "/1.bin");
        }
        else if(wrt) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "1" + brand + " LEARNING!", Toast.LENGTH_SHORT).show();
            learnKey(irpath + brand + "/" + item + "/1.bin");
        }
    }

    public void on2Click(View view)
    {
        if(!wrt) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "2" + brand, Toast.LENGTH_SHORT).show();
            sendKey(irpath + brand + "/" + item + "/2.bin");
        }
        else if(wrt) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "2" + brand + " LEARNING!", Toast.LENGTH_SHORT).show();
            learnKey(irpath + brand + "/" + item + "/2.bin");
        }
    }

    public void on3Click(View view)
    {
        if(!wrt) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "3" + brand, Toast.LENGTH_SHORT).show();
            sendKey(irpath + brand + "/" + item + "/3.bin");
        }
        else if(wrt) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "3" + brand + " LEARNING!", Toast.LENGTH_SHORT).show();
            learnKey(irpath + brand + "/" + item + "/3.bin");
        }
    }

    public void on4Click(View view)
    {
        if(!wrt) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "4" + brand, Toast.LENGTH_SHORT).show();
            sendKey(irpath + brand + "/" + item + "/4.bin");
        }
        else if(wrt) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "4" + brand + " LEARNING!", Toast.LENGTH_SHORT).show();
            learnKey(irpath + brand + "/" + item + "/4.bin");
        }
    }

    public void on5Click(View view)
    {
        if(!wrt) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "5" + brand, Toast.LENGTH_SHORT).show();
            sendKey(irpath + brand + "/" + item + "/5.bin");
        }
        else if(wrt) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "5" + brand + " LEARNING!", Toast.LENGTH_SHORT).show();
            learnKey(irpath + brand + "/" + item + "/5.bin");
        }
    }

    public void on6Click(View view)
    {
        if(!wrt) {
            Spinner spinner = (Spinner)findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "6" + brand, Toast.LENGTH_SHORT).show();
            sendKey(irpath + brand + "/" + item + "/6.bin");
        }
        else if(wrt) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "6" + brand + " LEARNING!", Toast.LENGTH_SHORT).show();
            learnKey(irpath + brand + "/" + item + "/6.bin");
        }
    }

    public void on7Click(View view)
    {
        if(!wrt) {
            Spinner spinner = (Spinner)findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "7" + brand, Toast.LENGTH_SHORT).show();
            sendKey(irpath + brand + "/" + item + "/7.bin");
        }
        else if(wrt) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "7" + brand + " LEARNING!", Toast.LENGTH_SHORT).show();
            learnKey(irpath + brand + "/" + item + "/7.bin");
        }
    }

    public void on8Click(View view)
    {
        if(!wrt) {
            Spinner spinner = (Spinner)findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "8" + brand, Toast.LENGTH_SHORT).show();
            sendKey(irpath + brand + "/" + item + "/8.bin");
        }
        else if(wrt) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "8" + brand + " LEARNING!", Toast.LENGTH_SHORT).show();
            learnKey(irpath + brand + "/" + item + "/8.bin");
        }
    }

    public void on9Click(View view)
    {
        if(!wrt) {
            Spinner spinner = (Spinner)findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "9" + brand, Toast.LENGTH_SHORT).show();
            sendKey(irpath + brand + "/" + item + "/9.bin");
        }
        else if(wrt) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "9" + brand + " LEARNING!", Toast.LENGTH_SHORT).show();
            learnKey(irpath + brand + "/" + item + "/9.bin");
        }
    }

    public void on0Click(View view)
    {
        if(!wrt) {
            Spinner spinner = (Spinner)findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "0" + brand, Toast.LENGTH_SHORT).show();
            sendKey(irpath + brand + "/" + item + "/0.bin");
        }
        else if(wrt) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "0" + brand + " LEARNING!", Toast.LENGTH_SHORT).show();
            learnKey(irpath + brand + "/" + item + "/0.bin");
        }
    }

    public void onUpClick(View view)
    {
        if(!wrt) {
            Spinner spinner = (Spinner)findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "up" + brand, Toast.LENGTH_SHORT).show();
            sendKey(irpath + brand + "/" + item + "/up.bin");
        }
        else if(wrt) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "up" + brand + " LEARNING!", Toast.LENGTH_SHORT).show();
            learnKey(irpath + brand + "/" + item + "/up.bin");
        }
    }

    public void onDownClick(View view)
    {
        if(!wrt) {
            Spinner spinner = (Spinner)findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "down" + brand, Toast.LENGTH_SHORT).show();
            sendKey(irpath + brand + "/" + item + "/down.bin");
        }
        else if(wrt) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "down" + brand + " LEARNING!", Toast.LENGTH_SHORT).show();
            learnKey(irpath + brand + "/" + item + "/down.bin");
        }
    }

    public void onLeftClick(View view)
    {
        if(!wrt) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "left" + brand, Toast.LENGTH_SHORT).show();
            sendKey(irpath + brand + "/" + item + "/left.bin");
        }
        else if(wrt) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "left" + brand + " LEARNING!", Toast.LENGTH_SHORT).show();
            learnKey(irpath + brand + "/" + item + "/left.bin");
        }
    }

    public void onRightClick(View view)
    {
        if(!wrt) {
            Spinner spinner = (Spinner)findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "right" + brand, Toast.LENGTH_SHORT).show();
            sendKey(irpath + brand + "/" + item + "/right.bin");
        }
        else if(wrt) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "right" + brand + " LEARNING!", Toast.LENGTH_SHORT).show();
            learnKey(irpath + brand + "/" + item + "/right.bin");
        }
    }

    public void onEnterClick(View view)
    {
        if(!wrt) {
            Spinner spinner = (Spinner)findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "enter" + brand, Toast.LENGTH_SHORT).show();
            sendKey(irpath + brand + "/" + item + "/enter.bin");
        }
        else if(wrt) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            brand = spinner.getSelectedItem().toString();
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            item = spinner2.getSelectedItem().toString();

            Toast.makeText(this, "enter" + brand + " LEARNING!", Toast.LENGTH_SHORT).show();
            learnKey(irpath + brand + "/" + item + "/enter.bin");
        }
    }

    public void UnzipALL()
    {
        String zipFile = Environment.getExternalStorageDirectory() + "/ir.zip";
        String unzipLocation = irpath + "/";

        Decompress d = new Decompress(zipFile, unzipLocation);
        d.unzip();
    }

    public static void unpack(String path, String dir_to) throws IOException {
        ZipFile zip = new ZipFile(path);
        Enumeration entries = zip.entries();
        LinkedList<ZipEntry> zfiles = new LinkedList<ZipEntry>();
        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            if (entry.isDirectory()) {
                new File(dir_to+"/"+entry.getName()).mkdir();
            } else {
                zfiles.add(entry);
            }
        }
        for (ZipEntry entry : zfiles) {
            InputStream in = zip.getInputStream(entry);
            OutputStream out = new FileOutputStream(dir_to+"/"+entry.getName());
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) >= 0)
                out.write(buffer, 0, len);
            in.close();
            out.close();
        }
        zip.close();
    }

    public void DownloadALL()
    {
        /*final ProgressDialog mProgressDialog = ProgressDialog.show(ir.this, "Please wait ...", "Downloading keys ...", true);

        final DownloadTask downloadTask = new DownloadTask(ir.this);
        downloadTask.execute("http://sssemil.or.gs/sony.zip");*/
        ProgressDialog mProgressDialog;

// instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(ir.this);
        mProgressDialog.setMessage("Downloading keys...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);

// execute this when the downloader must be fired
        final DownloadTask downloadTask = new DownloadTask(ir.this);
        downloadTask.execute("http://sssemil.or.gs/sonyirremoute/sony.zip");

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                downloadTask.cancel(true);
            }
        });
    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {

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
                output = new FileOutputStream(irpath + "/sony.zip");

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
            } catch (Exception e) {
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
}

