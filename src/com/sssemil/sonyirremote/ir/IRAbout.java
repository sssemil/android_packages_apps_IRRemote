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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.regex.Pattern;

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
        TextView tw4 = (TextView) findViewById(R.id.textView4);
        TextView tw5 = (TextView) findViewById(R.id.textView5);
        TextView tw6 = (TextView) findViewById(R.id.textView6);

        Pattern pattern = Pattern.compile("https://github.com/sssemil/android_packages_apps_IRRemote");
        Linkify.addLinks(tw4, pattern, "");
        pattern = Pattern.compile("https://github.com/BuzzBumbleBee/lib_sony_ir");
        Linkify.addLinks(tw5, pattern, "");
        tw6.setText(Html.fromHtml("<a href=\"\">" + getString(R.string.open_source_license) + "</a>"));

        PackageInfo pInfo = null;
        String version = "-.-.-";
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

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

    public void onLicenseClick(View view)
    {
        Intent intent = new Intent(this,
                IRLicense.class);
        startActivity(intent);
    }
}
