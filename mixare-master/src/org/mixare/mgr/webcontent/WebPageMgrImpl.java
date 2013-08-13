/*
 * Copyright (C) 2012- Peer internet solutions 
 * 
 * This file is part of mixare.
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package org.mixare.mgr.webcontent;

import java.util.List;

import org.mixare.MixContext;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

class WebPageMgrImpl implements WebContentManager {

	protected transient MixContext mixContext;

	/**
	 * Shows a webpage with the given url when clicked on a marker.
	 */
	public void loadMixViewWebPage(final String url) throws Exception { // NOPMD by Àðò¸ì on 24.06.13 13:59
		loadWebPage(url, mixContext.getActualMixView());
	}

	public WebPageMgrImpl(final MixContext mixContext) {
		this.mixContext = mixContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mixare.mgr.webcontent.WebContentManager#loadWebPage(java.lang.String,
	 * android.content.Context)
	 */
	@SuppressLint("SetJavaScriptEnabled")
	public void loadWebPage(final String url, final Context context)
			throws Exception { // NOPMD by Àðò¸ì on 24.06.13 13:59
		final WebView webview = new WebView(context);
		webview.getSettings().setJavaScriptEnabled(true);

		final Dialog dialog = new Dialog(context) { // NOPMD by Àðò¸ì on 24.06.13 13:44
			public boolean onKeyDown(final int keyCode, final KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					this.dismiss();
				}
				return true;
			}
		};

		webview.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(final WebView view,
					final String url) {
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageFinished(final WebView view, final String url) {
				if (url.endsWith("return")) {
					dialog.dismiss();
					mixContext.getActualMixView().repaint();
				} else {
					super.onPageFinished(view, url);
				}
			}

		});

		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setGravity(Gravity.BOTTOM);
		dialog.addContentView(webview, new FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
				Gravity.BOTTOM));

		if (!processUrl(url, mixContext.getActualMixView())) { // if the url
																// could not be
																// processed by
			// another intent
			dialog.show();
			webview.loadUrl(url);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mixare.mgr.webcontent.WebContentManager#processUrl(java.lang.String,
	 * android.content.Context)
	 */
	public boolean processUrl(final String url, final Context ctx) {
		// get available packages from the given url
		final List<ResolveInfo> resolveInfos = getAvailablePackagesForUrl(url,
				ctx);
		// filter the webbrowser > because the webview will replace it, using
		// google as simple url
		final List<ResolveInfo> webBrowsers = getAvailablePackagesForUrl( // NOPMD by Àðò¸ì on 24.06.13 13:44
				"http://www.google.com", ctx);
		for (ResolveInfo resolveInfo : resolveInfos) {
			for (ResolveInfo webBrowser : webBrowsers) { // check if the found
															// intent is not a
															// webbrowser
				if (!resolveInfo.activityInfo.packageName
						.equals(webBrowser.activityInfo.packageName)) {
					final Intent intent = new Intent(Intent.ACTION_VIEW); // NOPMD by Àðò¸ì on 24.06.13 14:00
					intent.setData(Uri.parse(url));
					intent.setClassName(resolveInfo.activityInfo.packageName,
							resolveInfo.activityInfo.name);
					ctx.startActivity(intent);
					return true; // NOPMD by Àðò¸ì on 24.06.13 13:59
				}
			}
		}
		return false;
	}

	private List<ResolveInfo> getAvailablePackagesForUrl(final String url,
			final Context ctx) {
		final PackageManager packageManager = ctx.getPackageManager();
		final Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		return packageManager.queryIntentActivities(intent,
				PackageManager.GET_RESOLVED_FILTER);
	}

}
