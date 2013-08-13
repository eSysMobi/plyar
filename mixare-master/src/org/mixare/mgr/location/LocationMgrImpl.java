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

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.mixare.MixContext;
import org.mixare.MixView;
import org.mixare.R;
import org.mixare.mgr.downloader.DownloadManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.GeomagneticField;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.widget.Toast;

/**
 * This class is repsonsible for finding the location, and sending it back to
 * the mixcontext.
 * 
 * @author A. Egal
 */
class LocationMgrImpl implements LocationFinder {

	private transient LocationManager locationManager;
	private transient String locationProvider;
	private transient final MixContext mixContext;
	private transient Location curLoc;
	private transient Location locationLast;
	private transient LocationFinderState state;
	private transient final LocationObserver lob;
	private transient final List<LocationResolver> locationResolvers;

	// frequency and minimum distance for update
	// this values will only be used after there's a good GPS fix
	// see back-off pattern discussion
	// http://stackoverflow.com/questions/3433875/how-to-force-gps-provider-to-get-speed-in-android
	// thanks Reto Meier for his presentation at gddde 2010
	private transient static final long FREQ = 5000; // 5 seconds
	private transient static final float DIST = 20; // 20 meters

	public LocationMgrImpl(final MixContext mixContext) {
		this.mixContext = mixContext;
		this.lob = new LocationObserver(this);
		this.state = LocationFinderState.Inactive;
		this.locationResolvers = new ArrayList<LocationResolver>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mixare.mgr.location.LocationFinder#findLocation(android.content.Context
	 * )
	 */
	public void findLocation() {

		// fallback for the case where GPS and network providers are disabled
		final Location hardFix = new Location("reverseGeocoded");

		// Frangart, Eppan, Bozen, Italy
		hardFix.setLatitude(46.480302);
		hardFix.setLongitude(11.296005);
		hardFix.setAltitude(300);

		try {
			requestBestLocationUpdates();
			// temporary set the current location, until a good provider is
			// found
			curLoc = locationManager.getLastKnownLocation(locationManager
					.getBestProvider(new Criteria(), true));
		} catch (Exception ex2) {
			// ex2.printStackTrace();
			curLoc = hardFix;
			mixContext.doPopUp(R.string.connection_GPS_dialog_text);

		}
	}

	private void requestBestLocationUpdates() {
		final Timer timer = new Timer();
		for (String p : locationManager.getAllProviders()) {
			if (locationManager.isProviderEnabled(p)) {
				final LocationResolver locationResolver = new LocationResolver( // NOPMD
																				// by
																				// Àðò¸ì
																				// on
																				// 24.06.13
																				// 14:00
						locationManager, p, this);
				locationResolvers.add(locationResolver);
				locationManager.requestLocationUpdates(p, 0, 0,
						locationResolver);
			}
		}
		timer.schedule(new LocationTimerTask(), 20 * 1000); // wait 20 seconds
															// for the location
															// updates to find
															// the location
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mixare.mgr.location.LocationFinder#locationCallback(android.content
	 * .Context)
	 */
	public void locationCallback(final String provider) {
		final Location foundLocation = locationManager
				.getLastKnownLocation(provider);
		if (locationProvider == null) {
			curLoc = foundLocation;
			locationProvider = provider;
		} else {
			final Location bestLocation = locationManager
					.getLastKnownLocation(locationProvider);
			if (foundLocation.getAccuracy() < bestLocation.getAccuracy()) {
				curLoc = foundLocation;
				locationProvider = provider;
			}
		}
		setLocationAtLastDownload(curLoc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mixare.mgr.location.LocationFinder#getCurrentLocation()
	 */
	public Location getCurrentLocation() {
		if (curLoc == null) {
			final MixView mixView = mixContext.getActualMixView();
			Toast.makeText(
					mixView,
					mixView.getResources().getString(
							R.string.location_not_found), Toast.LENGTH_LONG)
					.show();
			throw new RuntimeException("No GPS Found"); // NOPMD by Àðò¸ì on
														// 24.06.13 12:03
		}
		synchronized (curLoc) {
			return curLoc;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mixare.mgr.location.LocationFinder#getLocationAtLastDownload()
	 */
	public Location getLocationAtLastDownload() {
		return locationLast;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mixare.mgr.location.LocationFinder#setLocationAtLastDownload(android
	 * .location.Location)
	 */
	public void setLocationAtLastDownload(final Location locationLast) {
		this.locationLast = locationLast;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mixare.mgr.location.LocationFinder#setDownloadManager(org.mixare.
	 * mgr.downloader.DownloadManager)
	 */
	public void setDownloadManager(final DownloadManager downloadManager) {
		getObserver().setDownloadManager(downloadManager);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mixare.mgr.location.LocationFinder#getGeomagneticField()
	 */
	public GeomagneticField getGeomagneticField() {
		final Location location = getCurrentLocation();
		final GeomagneticField gmf = new GeomagneticField(
				(float) location.getLatitude(),
				(float) location.getLongitude(),
				(float) location.getAltitude(), System.currentTimeMillis());
		return gmf;
	}

	public void setPosition(final Location location) {
		synchronized (curLoc) {
			curLoc = location;
		}
		mixContext.getActualMixView().refresh();
		final Location lastLoc = getLocationAtLastDownload();
		if (lastLoc == null) {
			setLocationAtLastDownload(location);
		}
	}

	@Override
	public void switchOn() {
		if (!LocationFinderState.Active.equals(state)) {
			locationManager = (LocationManager) mixContext
					.getSystemService(Context.LOCATION_SERVICE);
			state = LocationFinderState.Confused;
		}
	}

	@Override
	public void switchOff() {
		if (locationManager != null) {
			locationManager.removeUpdates(getObserver());
			state = LocationFinderState.Inactive;
		}
	}

	@Override
	public LocationFinderState getStatus() {
		return state;
	}

	private synchronized LocationObserver getObserver() { // NOPMD by Àðò¸ì on
															// 24.06.13 14:00
		return lob;
	}

	class LocationTimerTask extends TimerTask {

		@Override
		public void run() {
			// remove all location updates
			for (LocationResolver locationResolver : locationResolvers) {
				locationManager.removeUpdates(locationResolver);
			}
			if (locationProvider == null) { // no location found
				mixContext.getActualMixView().runOnUiThread(new Runnable() { // NOPMD
																				// by
																				// Àðò¸ì
																				// on
																				// 24.06.13
																				// 14:00
							@SuppressLint("ShowToast")
							@Override
							public void run() {
								Toast.makeText(
										mixContext.getActualMixView(),
										mixContext
												.getActualMixView()
												.getResources()
												.getString(
														R.string.location_not_found),
										Toast.LENGTH_LONG);
							}
						});

			} else {
				locationManager.removeUpdates(getObserver());
				state = LocationFinderState.Confused;
				mixContext.getActualMixView().runOnUiThread(new Runnable() { // NOPMD
																				// by
																				// Àðò¸ì
																				// on
																				// 24.06.13
																				// 14:00
							@Override
							public void run() {
								locationManager.requestLocationUpdates(
										locationProvider, FREQ, DIST,
										getObserver());
							}
						});
				state = LocationFinderState.Active;
			}

		}

	}

}