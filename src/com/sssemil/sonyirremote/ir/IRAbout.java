package com.sssemil.sonyirremote.ir;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * Copyright (c) 2014 Emil Suleymanov
 * Distributed under the GNU GPL v2. For full terms see the file LICENSE.
 */

public class IRAbout extends Activity {

    SharedPreferences settings;

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
        setContentView(R.layout.about);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        TextView tw = (TextView) findViewById(R.id.textView);
        TextView tw2 = (TextView) findViewById(R.id.textView2);
        PackageInfo pInfo = null;
        String version = "-.-.-";
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        version = pInfo.versionName;

        tw.setText(getString(R.string.ver) + " " + version);

        tw2.setText(getString(R.string.license1));
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
}
