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
package org.mixare.data;

import org.mixare.R;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Singleton slass that manages the storage of datasources You can add, edit or
 * delete a datasource through this class.
 */
public class DataSourceStorage {

	private static final String DATA_SOURCE = "DataSource";

	private transient final SharedPreferences settings;

	private transient final Context ctx;

	public static DataSourceStorage instance;

	private transient boolean customDataSource = false;

	public DataSourceStorage(final Context ctx) {
		this.ctx = ctx;
		settings = ctx.getSharedPreferences(DataSourceList.SHARED_PREFS, 0);
	}

	public static void init(final Context ctx) {
		instance = new DataSourceStorage(ctx);
	}

	public static DataSourceStorage getInstance() {
		return instance;
	}

	public static DataSourceStorage getInstance(final Context ctx) {
		if (instance == null) { // NOPMD by Àðò¸ì on 25.06.13 13:11
			instance = new DataSourceStorage(ctx);
		}
		return instance;
	}

	public void add(final String name, final String url, final String type,
			final String display, final boolean visible) {
		final SharedPreferences.Editor dataSourceEditor = settings.edit();
		dataSourceEditor.putString(DATA_SOURCE + getSize(), name + "|" + url
				+ "|" + type + "|" + display + "|" + visible);
		dataSourceEditor.commit();
	}

	public void add(final String dataID, final String serialized) {
		final SharedPreferences.Editor dataSourceEditor = settings.edit();
		dataSourceEditor.putString(dataID, serialized);
		dataSourceEditor.commit();
	}

	public void clear() {
		final SharedPreferences.Editor dataSourceEditor = settings.edit();
		dataSourceEditor.clear();
		dataSourceEditor.commit();
	}

	public void editVisibility(final int location, final boolean visible) {
		String[] fields = getFields(location);
		if (fields.length == 5) {
			fields[4] = String.valueOf(visible);

			final SharedPreferences.Editor dataSourceEditor = settings.edit();
			dataSourceEditor.putString(DATA_SOURCE + location, fields[0] + "|"
					+ fields[1] + "|" + fields[2] + "|" + fields[3] + "|"
					+ fields[4]);
			dataSourceEditor.commit();
		}
	}

	public void fillDefaultDataSources() {
		final String[] datasources = ctx.getResources().getStringArray(
				R.array.defaultdatasources);
		if (datasources.length > getSize()) {
			for (int i = 0; i < datasources.length; i++) {
				final int dataID = getSize();
				add(DATA_SOURCE + dataID, datasources[i]);
				onCustomDataSourceSelected(dataID);
			}
		}
	}

	public String[] getFields(final int location) {
		return settings.getString(DATA_SOURCE + location, "").split("\\|", -1);
	}

	public int getSize() {
		return settings.getAll().size();
	}

	public void setCustomDataSourceSelected(final boolean customDataSource) {
		this.customDataSource = customDataSource;
	}

	private void onCustomDataSourceSelected(final int dataID) {
		// if a custom data source is selected, then hide the datasources
		editVisibility(dataID, !customDataSource);
	}
}
