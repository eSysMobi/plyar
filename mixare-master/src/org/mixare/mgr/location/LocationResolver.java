package org.mixare.mgr.location;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

/**
 * This class will be used to start each location provider for 20 seconds and
 * they will then listen for locations. This class will check for updates for
 * the observer. Using this method: http://stackoverflow.com/questions/3145089/
 * 
 * @author A. Egal
 */
public class LocationResolver implements LocationListener {

	private final transient String provider;
	private final transient LocationMgrImpl locationMgrImpl;
	private final transient LocationManager locationManager;

	public LocationResolver(final LocationManager locationManager,
			final String provider, final LocationMgrImpl locationMgrImpl) {
		this.locationManager = locationManager;
		this.provider = provider;
		this.locationMgrImpl = locationMgrImpl;
	}

	@Override
	public void onLocationChanged(final Location location) {
		locationManager.removeUpdates(this);
		locationMgrImpl.locationCallback(provider);
	}

	@Override
	public void onProviderDisabled(final String provider) {
		Log.d("Method", "onProviderDisabled");
	}

	@Override
	public void onProviderEnabled(final String provider) {
		Log.d("Method", "onProviderEnabled");
	}

	@Override
	public void onStatusChanged(final String provider, final int status,
			final Bundle extras) {
		Log.d("Method", "onStatusChanged");
	}

}
