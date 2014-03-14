package com.sssemil.sonyirremoute.ir;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.*;

public class ir extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ir);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        fixPermissionsForIr();
        prepIRKeys();
        startIR();
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
            //setContentView(R.layout.settings);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    EditText mEdit;

    /*public void onSaveClick(View view)
    {
        SharedPreferences irPrefs = getSharedPreferences("irPrefs", MODE_PRIVATE);
        mEdit   = (EditText)findViewById(R.id.editText);
        SharedPreferences.Editor e = irPrefs.edit();
        e.putString("irpath", mEdit.toString()); // add or overwrite someValue
        e.commit(); // this saves to disk and notifies observers
    }*/

    public String brand;
    public String item;

    public void onPowerClick(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "Power" + brand, Toast.LENGTH_SHORT).show();

        sendKey(irpath + brand + "/" + item + "/power.bin");
    }

    public void onChanelPlClick(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        item = spinner2.getSelectedItem().toString();

        sendKey(irpath + brand + "/" + item + "/chanelPl.bin");
    }

    public void onChanelMnSonClick(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "ChanelMn" + brand, Toast.LENGTH_SHORT).show();
        sendKey(irpath + brand + "/" + item + "/chanelMn.bin");
    }

    public void onVolumePlClick(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "VolumePl" + brand, Toast.LENGTH_SHORT).show();
        sendKey(irpath + brand + "/" + item + "/volPl.bin");
    }

    public void onVolumeMnClick(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "VolumeMn" + brand, Toast.LENGTH_SHORT).show();
        sendKey(irpath + brand + "/" + item + "/volMn.bin");
    }

    public void on1Click(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "1" + brand, Toast.LENGTH_SHORT).show();
        sendKey(irpath + brand + "/" + item + "/1.bin");
    }

    public void on2Click(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "2" + brand, Toast.LENGTH_SHORT).show();
        sendKey(irpath + brand + "/" + item + "/2.bin");
    }

    public void on3Click(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "3" + brand, Toast.LENGTH_SHORT).show();
        sendKey(irpath + brand + "/" + item + "/3.bin");
    }

    public void on4Click(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "4" + brand, Toast.LENGTH_SHORT).show();
        sendKey(irpath + brand + "/" + item + "/4.bin");
    }

    public void on5Click(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "5" + brand, Toast.LENGTH_SHORT).show();
        sendKey(irpath + brand + "/" + item + "/5.bin");
    }

    public void on6Click(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "6" + brand, Toast.LENGTH_SHORT).show();
        sendKey(irpath + brand + "/" + item + "/6.bin");
    }

    public void on7Click(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "7" + brand, Toast.LENGTH_SHORT).show();
        sendKey(irpath + brand + "/" + item + "/7.bin");
    }

    public void on8Click(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "8" + brand, Toast.LENGTH_SHORT).show();
        sendKey(irpath + brand + "/" + item + "/8.bin");
    }

    public void on9Click(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "9" + brand, Toast.LENGTH_SHORT).show();
        sendKey(irpath + brand + "/" + item + "/9.bin");
    }

    public void on0Click(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "0" + brand, Toast.LENGTH_SHORT).show();
        sendKey(irpath + brand + "/" + item + "/0.bin");
    }

    public void onUpClick(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "up" + brand, Toast.LENGTH_SHORT).show();
        sendKey(irpath + brand + "/" + item + "/up.bin");
    }

    public void onDownClick(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "down" + brand, Toast.LENGTH_SHORT).show();
        sendKey(irpath + brand + "/" + item + "/down.bin");
    }

    public void onLeftClick(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "left" + brand, Toast.LENGTH_SHORT).show();
        sendKey(irpath + brand + "/" + item + "/left.bin");
    }

    public void onRightClick(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "right" + brand, Toast.LENGTH_SHORT).show();
        sendKey(irpath + brand + "/" + item + "/right.bin");
    }

    public void onEnterClick(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "enter" + brand, Toast.LENGTH_SHORT).show();
        sendKey(irpath + brand + "/" + item + "/enter.bin");
    }

    public void onDClick(View view)
    {
        DownloadALL();
        UnzipALL();
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
