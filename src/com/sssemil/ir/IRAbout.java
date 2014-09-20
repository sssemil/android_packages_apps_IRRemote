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

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;

import java.util.regex.Pattern;

public class IRAbout extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        if (getActionBar() != null) getActionBar().setDisplayHomeAsUpEnabled(true);

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

        String version = "-.-.-";
        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        tw.setText(getString(R.string.ver) + " " + version);

        tw2.setText(getString(R.string.license1));
    }

    @Override
    protected void onApplyThemeResource(Resources.Theme theme, int resid, boolean first) {
        theme.applyStyle(IRCommon.getCurrentThemeId(this, resid), true);
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

    public void onLicenseClick(View view) {
        Intent intent = new Intent(this,
                IRLicense.class);
        startActivity(intent);
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker easyTracker = EasyTracker.getInstance(this);
        easyTracker.set(Fields.TRACKING_ID, IRCommon.getID());
        easyTracker.activityStart(this);
    }
}
