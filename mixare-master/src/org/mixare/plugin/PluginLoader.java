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
package org.mixare.plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mixare.lib.marker.Marker;
import org.mixare.lib.service.IMarkerService;
import org.mixare.plugin.connection.ActivityConnection;
import org.mixare.plugin.connection.MarkerServiceConnection;
import org.mixare.plugin.remoteobjects.RemoteMarker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.RemoteException;

/**
 * Searches, loads and executes available plugins that are installed on the
 * device.
 * 
 * @author A.Egal
 */
public class PluginLoader {

	private static PluginLoader instance;

	private transient Activity activity;

	private static final Map<String, AbstractConn> PLUGIN_MAP = new HashMap<String, AbstractConn>();

	private transient int pendingActivity = 0;

	public static PluginLoader getInstance() {
		if (instance == null) { // NOPMD by Àðò¸ì on 24.06.13 13:59
			instance = new PluginLoader();
		}
		return instance;
	}

	public void setActivity(final Activity activity) {
		this.activity = activity;
	}

	/**
	 * loads all plugins from a plugin type.
	 */
	public void loadPlugin(final PluginType pluginType) {
		final PackageManager packageManager = activity.getPackageManager();
		final Intent baseIntent = new Intent(pluginType.getActionName());
		baseIntent.setFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
		final List<ResolveInfo> list = packageManager.queryIntentServices(
				baseIntent, PackageManager.GET_RESOLVED_FILTER);

		initService(list, activity, pluginType);
	}

	/**
	 * Initializes the services from the loaded plugins and stores them in the
	 * pluginmap
	 */
	private void initService(final List<ResolveInfo> list,
			final Activity activity, final PluginType pluginType) {
		for (int i = 0; i < list.size(); ++i) {
			final ResolveInfo info = list.get(i);
			final ServiceInfo sinfo = info.serviceInfo;
			if (sinfo != null) {
				final Intent serviceIntent = new Intent(); // NOPMD by Àðò¸ì on 24.06.13 13:59
				serviceIntent.setClassName(sinfo.packageName, sinfo.name);
				activity.startService(serviceIntent);
				activity.bindService(serviceIntent,
						(ServiceConnection) pluginType.getPluginConnection(),
						Context.BIND_AUTO_CREATE);
				checkForPendingActivity(pluginType);
			}
		}
	}

	/**
	 * Unbinds all plugins from the activity
	 */
	public void unBindServices() {
		for (AbstractConn abstractConn : PLUGIN_MAP.values()) {
			if (abstractConn instanceof ServiceConnection) {
				activity.unbindService((ServiceConnection) abstractConn);
			}
		}
	}

	/**
	 * Starts an activity plugin
	 */
	public void startPlugin(final PluginType pluginType, final String pluginName) {
		if (pluginType.getLoader() == Loader.Activity) {
			final ActivityConnection activityConn = (ActivityConnection) PLUGIN_MAP
					.get(pluginName);
			activityConn.startActivityForResult(activity);
		} else {
			throw new PluginNotFoundException(
					"Cannot directly start a non-activity plugin,"
							+ " you must call a instance for it");
		}
	}

	protected void addFoundPluginToMap(final String pluginName,
			final AbstractConn abstractConn) {
		PLUGIN_MAP.put(pluginName, abstractConn);
	}

	public Marker getMarkerInstance(final String markername,
			final int markerID, final String title, final double latitude,
			final double longitude, final double altitude, final String link,
			final int type, final int color) throws PluginNotFoundException,
			RemoteException {

		final MarkerServiceConnection msc = (MarkerServiceConnection) PLUGIN_MAP
				.get(PluginType.MARKER.toString());
		final IMarkerService iMarkerService = msc.getMarkerServices().get(
				markername);

		if (iMarkerService == null) {
			throw new PluginNotFoundException();
		}
		final RemoteMarker remoteMarker = new RemoteMarker(iMarkerService);
		remoteMarker.buildMarker(markerID, title, latitude, longitude,
				altitude, link, type, color);
		return remoteMarker;
	}

	public AbstractConn getPluginConnection(final String name) {
		return PLUGIN_MAP.get(name);
	}

	public int getPendingActivitiesOnResult() {
		return pendingActivity;
	}

	public void increasePendingActivitiesOnResult() {
		pendingActivity++;
	}

	public void decreasePendingActivitiesOnResult() {
		pendingActivity--;
	}

	private void checkForPendingActivity(final PluginType pluginType) {
		if (pluginType.getLoader() == Loader.Activity) {
			increasePendingActivitiesOnResult();
		}
	}

}