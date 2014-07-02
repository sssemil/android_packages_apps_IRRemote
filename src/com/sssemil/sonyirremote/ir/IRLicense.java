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
import android.content.res.Resources;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;

public class IRLicense extends Activity {

    private final WebViewClient mClient = new WebViewClient() {
        public WebResourceResponse shouldInterceptRequest(WebView paramAnonymousWebView, String paramAnonymousString) {
            if ("camera:license".equals(paramAnonymousString))
                return new WebResourceResponse("text/html", "utf8", IRLicense.this.getResources().openRawResource(R.raw.license));
            return null;
        }
    };

    @Override
    protected void onApplyThemeResource(Resources.Theme theme, int resid, boolean first) {
        theme.applyStyle(IRCommon.getInstance().getCurrentThemeId(this, resid), true);
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
        getActionBar().setDisplayHomeAsUpEnabled(true);
        WebView localWebView = (WebView) findViewById(R.id.webView);
        localWebView.setWebViewClient(this.mClient);
        localWebView.loadUrl("camera:license");
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
        easyTracker.set(Fields.TRACKING_ID, "UA-52301928-1");
        easyTracker.activityStart(this);
    }
}
