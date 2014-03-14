package com.sssemil.sonyirremoute.ir;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;


public class ir extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ir);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    static {
        System.loadLibrary("jni_sonyopenir");
    }

    public native int startIR();
    public native int stopIR();
    public native int learnKey(String filename);
    public native int sendKey(String filename);


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
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onPowerClick(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        String brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        String item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "Power" + brand, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "/sdcard/ir/" + item + "/" + brand + "/power.bin" + brand, Toast.LENGTH_SHORT).show();
        startIR();
        sendKey("/sdcard/ir/" + item + "/" + brand + "/power.bin");
        /*String[] cmd = { "su", "-c", Environment.getDataDirectory() + "/data/com.sssemil.ir/bin/irtest -s /data/data/com.sssemil.ir/ir/" + item + "/sony/power" + brand + ".bin"};
        try {
            // Try to enable Infrared Devices
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }*/
    }

    public void onChanelPlClick(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        String brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        String item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "ChanelPl" + brand, Toast.LENGTH_SHORT).show();
        String[] cmd = { "su", "-c", Environment.getDataDirectory() + "/data/com.sssemil.ir/bin/irtest -s /data/data/com.sssemil.ir/ir/" + item + "/sony/chanelPl" + brand + ".bin"};
        try {
            // Try to enable Infrared Devices
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void onChanelMnSonClick(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        String brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        String item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "ChanelMn" + brand, Toast.LENGTH_SHORT).show();
        String[] cmd = { "su", "-c", Environment.getDataDirectory() + "/data/com.sssemil.ir/bin/irtest -s /data/data/com.sssemil.ir/ir/" + item + "/sony/chanelMn" + brand + ".bin"};
        try {
            // Try to enable Infrared Devices
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void onVolumePlClick(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        String brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        String item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "VolumePl" + brand, Toast.LENGTH_SHORT).show();
        String[] cmd = { "su", "-c", Environment.getDataDirectory() + "/data/com.sssemil.ir/bin/irtest -s /data/data/com.sssemil.ir/ir/" + item + "/sony/volumePl" + brand + ".bin"};
        try {
            // Try to enable Infrared Devices
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void onVolumeMnClick(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        String brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        String item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "VolumeMn" + brand, Toast.LENGTH_SHORT).show();
        String[] cmd = { "su", "-c", Environment.getDataDirectory() + "/data/com.sssemil.ir/bin/irtest -s /data/data/com.sssemil.ir/ir/" + item + "/sony/volumeMn" + brand + ".bin"};
        try {
            // Try to enable Infrared Devices
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void on1Click(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        String brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        String item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "1" + brand, Toast.LENGTH_SHORT).show();
        String[] cmd = { "su", "-c", Environment.getDataDirectory() + "/data/com.sssemil.ir/bin/irtest -s /data/data/com.sssemil.ir/ir/" + item + "/sony/1" + brand + ".bin"};
        try {
            // Try to enable Infrared Devices
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void on2Click(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        String brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        String item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "2" + brand, Toast.LENGTH_SHORT).show();
        String[] cmd = { "su", "-c", Environment.getDataDirectory() + "/data/com.sssemil.ir/bin/irtest -s /data/data/com.sssemil.ir/ir/" + item + "/sony/2" + brand + ".bin"};
        try {
            // Try to enable Infrared Devices
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void on3Click(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        String brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        String item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "3" + brand, Toast.LENGTH_SHORT).show();
        String[] cmd = { "su", "-c", Environment.getDataDirectory() + "/data/com.sssemil.ir/bin/irtest -s /data/data/com.sssemil.ir/ir/" + item + "/sony/3" + brand + ".bin"};
        try {
            // Try to enable Infrared Devices
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void on4Click(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        String brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        String item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "4" + brand, Toast.LENGTH_SHORT).show();
        String[] cmd = { "su", "-c", Environment.getDataDirectory() + "/data/com.sssemil.ir/bin/irtest -s /data/data/com.sssemil.ir/ir/" + item + "/sony/4" + brand + ".bin"};
        try {
            // Try to enable Infrared Devices
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void on5Click(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        String brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        String item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "5" + brand, Toast.LENGTH_SHORT).show();
        String[] cmd = { "su", "-c", Environment.getDataDirectory() + "/data/com.sssemil.ir/bin/irtest -s /data/data/com.sssemil.ir/ir/" + item + "/sony/5" + brand + ".bin"};
        try {
            // Try to enable Infrared Devices
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void on6Click(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        String brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        String item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "6" + brand, Toast.LENGTH_SHORT).show();
        String[] cmd = { "su", "-c", Environment.getDataDirectory() + "/data/com.sssemil.ir/bin/irtest -s /data/data/com.sssemil.ir/ir/" + item + "/sony/6" + brand + ".bin"};
        try {
            // Try to enable Infrared Devices
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void on7Click(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        String brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        String item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "7" + brand, Toast.LENGTH_SHORT).show();
        String[] cmd = { "su", "-c", Environment.getDataDirectory() + "/data/com.sssemil.ir/bin/irtest -s /data/data/com.sssemil.ir/ir/" + item + "/sony/7" + brand + ".bin"};
        try {
            // Try to enable Infrared Devices
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void on8Click(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        String brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        String item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "8" + brand, Toast.LENGTH_SHORT).show();
        String[] cmd = { "su", "-c", Environment.getDataDirectory() + "/data/com.sssemil.ir/bin/irtest -s /data/data/com.sssemil.ir/ir/" + item + "/sony/8" + brand + ".bin"};
        try {
            // Try to enable Infrared Devices
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void on9Click(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        String brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        String item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "9" + brand, Toast.LENGTH_SHORT).show();
        String[] cmd = { "su", "-c", Environment.getDataDirectory() + "/data/com.sssemil.ir/bin/irtest -s /data/data/com.sssemil.ir/ir/" + item + "/sony/9" + brand + ".bin"};
        try {
            // Try to enable Infrared Devices
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void on0Click(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        String brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        String item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "0" + brand, Toast.LENGTH_SHORT).show();
        String[] cmd = { "su", "-c", Environment.getDataDirectory() + "/data/com.sssemil.ir/bin/irtest -s /data/data/com.sssemil.ir/ir/" + item + "/sony/0" + brand + ".bin"};
        try {
            // Try to enable Infrared Devices
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void onUpClick(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        String brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        String item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "up" + brand, Toast.LENGTH_SHORT).show();
        String[] cmd = { "su", "-c", Environment.getDataDirectory() + "/data/com.sssemil.ir/bin/irtest -s /data/data/com.sssemil.ir/ir/" + item + "/sony/up" + brand + ".bin"};
        try {
            // Try to enable Infrared Devices
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void onDownClick(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        String brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        String item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "down" + brand, Toast.LENGTH_SHORT).show();
        String[] cmd = { "su", "-c", Environment.getDataDirectory() + "/data/com.sssemil.ir/bin/irtest -s /data/data/com.sssemil.ir/ir/" + item + "/sony/down" + brand + ".bin"};
        try {
            // Try to enable Infrared Devices
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void onLeftClick(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        String brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        String item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "left" + brand, Toast.LENGTH_SHORT).show();
        String[] cmd = { "su", "-c", Environment.getDataDirectory() + "/data/com.sssemil.ir/bin/irtest -s /data/data/com.sssemil.ir/ir/" + item + "/sony/left" + brand + ".bin"};
        try {
            // Try to enable Infrared Devices
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void onRightClick(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        String brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        String item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "right" + brand, Toast.LENGTH_SHORT).show();
        String[] cmd = { "su", "-c", Environment.getDataDirectory() + "/data/com.sssemil.ir/bin/irtest -s /data/data/com.sssemil.ir/ir/" + item + "/sony/right" + brand + ".bin"};
        try {
            // Try to enable Infrared Devices
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void onEnterClick(View view)
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        String brand = spinner.getSelectedItem().toString();
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        String item = spinner2.getSelectedItem().toString();

        Toast.makeText(this, "enter" + brand, Toast.LENGTH_SHORT).show();
        String[] cmd = { "su", "-c", Environment.getDataDirectory() + "/data/com.sssemil.ir/bin/irtest -s /data/data/com.sssemil.ir/ir/" + item + "/sony/enter" + brand + ".bin"};
        try {
            // Try to enable Infrared Devices
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}
