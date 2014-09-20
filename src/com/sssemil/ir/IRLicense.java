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
import android.content.res.Resources;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;

public class IRLicense extends Activity {

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

    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.license_menu);
        if (getActionBar() != null) getActionBar().setDisplayHomeAsUpEnabled(true);
        WebView localWebView = (WebView) findViewById(R.id.webView);
        localWebView.loadUrl("file:///android_res/raw/license.html");
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
