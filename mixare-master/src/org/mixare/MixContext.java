/*
 * Copyright (C) 2010- Peer internet solutions
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
package org.mixare;

import org.mixare.lib.MixContextInterface;
import org.mixare.lib.render.Matrix;
import org.mixare.mgr.datasource.DataSourceManager;
import org.mixare.mgr.datasource.DataSourceManagerFactory;
import org.mixare.mgr.downloader.DownloadManager;
import org.mixare.mgr.downloader.DownloadManagerFactory;
import org.mixare.mgr.location.LocationFinder;
import org.mixare.mgr.location.LocationFinderFactory;
import org.mixare.mgr.webcontent.WebContentManager;
import org.mixare.mgr.webcontent.WebContentManagerFactory;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContextWrapper;
import android.content.Intent;
import android.widget.Toast;

/**
 * Cares about location management and about the data (source, inputstream)
 */
public class MixContext extends ContextWrapper implements MixContextInterface {

	// TAG for logging
	public static final String TAG = "Mixare";

	private transient MixView mixView;

	private final static Matrix ROTATION_M = new Matrix();

	/** Responsible for all download */
	private transient DownloadManager downloadManager;

	/** Responsible for all location tasks */
	private transient LocationFinder locationFinder;

	/** Responsible for data Source Management */
	private transient DataSourceManager dataSourceManager;

	/** Responsible for Web Content */
	private transient WebContentManager webContentManager;

	public MixContext(final MixView appCtx) {
		super(appCtx);
		mixView = appCtx;

		// TODO: RE-ORDER THIS SEQUENCE... IS NECESSARY?
		getDataSourceManager().refreshDataSources();

		if (!getDataSourceManager().isAtLeastOneDatasourceSelected()) {
			ROTATION_M.toIdentity();
		}
		getLocationFinder().switchOn();
		getLocationFinder().findLocation();
	}

	public String getStartUrl() {
		final Intent intent = ((Activity) getActualMixView()).getIntent();
		if (intent.getAction() != null
				&& intent.getAction().equals(Intent.ACTION_VIEW)) {
			return intent.getData().toString(); // NOPMD by Àðò¸ì on 25.06.13 14:53
		} else {
			return "";
		}
	}

	public void getRM(final Matrix dest) {
		synchronized (ROTATION_M) {
			dest.set(ROTATION_M);
		}
	}

	/**
	 * Shows a webpage with the given url when clicked on a marker.
	 */
	public void loadMixViewWebPage(final String url) throws Exception { // NOPMD by Àðò¸ì on 25.06.13 14:53
		// TODO: CHECK INTERFACE METHOD
		getWebContentManager().loadWebPage(url, getActualMixView());
	}

	public void doResume(final MixView mixView) {
		setActualMixView(mixView);
	}

	public void updateSmoothRotation(final Matrix smoothR) {
		synchronized (ROTATION_M) {
			ROTATION_M.set(smoothR);
		}
	}

	public DataSourceManager getDataSourceManager() {
		if (this.dataSourceManager == null) {
			dataSourceManager = DataSourceManagerFactory
					.makeDataSourceManager(this);
		}
		return dataSourceManager;
	}

	public LocationFinder getLocationFinder() {
		if (this.locationFinder == null) {
			locationFinder = LocationFinderFactory.makeLocationFinder(this);
		}
		return locationFinder;
	}

	public DownloadManager getDownloadManager() {
		if (this.downloadManager == null) {
			downloadManager = DownloadManagerFactory.makeDownloadManager(this);
			getLocationFinder().setDownloadManager(downloadManager);
		}
		return downloadManager;
	}

	public WebContentManager getWebContentManager() {
		if (this.webContentManager == null) {
			webContentManager = WebContentManagerFactory
					.makeWebContentManager(this);
		}
		return webContentManager;
	}

	public MixView getActualMixView() {
		synchronized (mixView) {
			return this.mixView;
		}
	}

	private void setActualMixView(final MixView mixView) {
		synchronized (mixView) {
			this.mixView = mixView;
		}
	}

	public ContentResolver getContentResolver() {
		ContentResolver out = super.getContentResolver(); // NOPMD by Àðò¸ì on 25.06.13 14:53
		if (super.getContentResolver() == null) {
			out = getActualMixView().getContentResolver();
		}
		return out;
	}

	/**
	 * Toast POPUP notification
	 * 
	 * @param string
	 *            message
	 */
	public void doPopUp(final String string) {
		Toast.makeText(this, string, Toast.LENGTH_LONG).show();
	}

	/**
	 * Toast POPUP notification
	 * 
	 * @param connectionGpsDialogText
	 */
	public void doPopUp(final int RidOfString) {
		doPopUp(this.getString(RidOfString));
	}
}
