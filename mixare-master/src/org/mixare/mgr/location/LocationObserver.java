/*
 * Copyright (C) 2012- Peer internet solutions & Finalist IT Group
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
package org.mixare.mgr.location;

import org.mixare.MixContext;
import org.mixare.MixMap;
import org.mixare.mgr.downloader.DownloadManager;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.google.android.maps.GeoPoint;

class LocationObserver implements LocationListener {

	private DownloadManager downloadManager;
	private transient final LocationMgrImpl myController;

	public LocationObserver(final LocationMgrImpl myController) {
		super();
		this.myController = myController;
	}

	public DownloadManager getDownloadManager() {
		return downloadManager;
	}

	public void setDownloadManager(final DownloadManager downloadManager) {
		this.downloadManager = downloadManager;
	}

	public void onLocationChanged(final Location location) {
		Log.d(MixContext.TAG,
				"Normal Location Changed: " + location.getProvider() + " lat: "
						+ location.getLatitude() + " lon: "
						+ location.getLongitude() + " alt: "
						+ location.getAltitude() + " acc: "
						+ location.getAccuracy());
		try {
			addWalkingPathPosition(location);
			deleteAllDownloadActivity();
			Log.v(MixContext.TAG,
					"Location Changed: " + location.getProvider() + " lat: "
							+ location.getLatitude() + " lon: "
							+ location.getLongitude() + " alt: "
							+ location.getAltitude() + " acc: "
							+ location.getAccuracy());
			myController.setPosition(location);
		} catch (Exception ex) {
			Log.d("Loc changed", "Location changing exception");
		}
	}

	private void deleteAllDownloadActivity() {
		if (downloadManager != null) {
			downloadManager.resetActivity();
		}
	}

	private void addWalkingPathPosition(final Location location) {
		MixMap.addWalkingPathPosition(new GeoPoint((int) (location
				.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6)));
	}

	public void onProviderDisabled(final String provider) {
		Log.d("method", "onProviderDisabled");
	}

	public void onProviderEnabled(final String provider) {
		Log.d("method", "onProviderEnabled");
	}

	public void onStatusChanged(final String provider, final int status,
			final Bundle extras) {
		Log.d("method", "onStatusChanged");
	}

}
