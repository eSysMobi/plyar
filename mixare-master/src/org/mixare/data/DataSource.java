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

package org.mixare.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.mixare.R;
import org.mixare.data.convert.DataConvertor;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * The DataSource class is able to create the URL where the information about a
 * place can be found.
 * 
 * @author hannes
 * 
 */
public class DataSource extends Activity {

	private static final String DATA_SOURCE_ID = "DataSourceId";
	private transient final String NAME;
	private transient final String URL;

	public enum TYPE {
		WIKIPEDIA, FOURSQUARE, OSM, MIXARE, ARENA
	};

	public enum DISPLAY {
		CIRCLE_MARKER, NAVIGATION_MARKER, IMAGE_MARKER
	};

	private transient boolean enabled;
	private transient final TYPE type;
	private transient final DISPLAY display;

	@Override
	public void onCreate(final Bundle state) {
		super.onCreate(state);
		setContentView(R.layout.datasourcedetails);
		final EditText nameField = (EditText) findViewById(R.id.name); // NOPMD by Àðò¸ì on 25.06.13 13:30
		final EditText urlField = (EditText) findViewById(R.id.url); // NOPMD by Àðò¸ì on 25.06.13 13:31
		final Spinner typeSpinner = (Spinner) findViewById(R.id.type); // NOPMD by Àðò¸ì on 25.06.13 13:30
		final Spinner displaySpinner = (Spinner) findViewById(R.id.displaytype); // NOPMD by Àðò¸ì on 25.06.13 13:30
		final Bundle extras = getIntent().getExtras();
		if (extras != null && extras.containsKey(DATA_SOURCE_ID)) {
			final String fields[] = DataSourceStorage.getInstance().getFields(
					extras.getInt(DATA_SOURCE_ID));
			nameField.setText(fields[0], TextView.BufferType.EDITABLE);
			urlField.setText(fields[1], TextView.BufferType.EDITABLE);
			typeSpinner.setSelection(Integer.parseInt(fields[2]) - 3);
			displaySpinner.setSelection(Integer.parseInt(fields[3]));
		}

	}

	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			final EditText nameField = (EditText) findViewById(R.id.name);
			final String name = nameField.getText().toString();
			final EditText urlField = (EditText) findViewById(R.id.url);
			final String url = urlField.getText().toString();
			final Spinner typeSpinner = (Spinner) findViewById(R.id.type);
			final int typeId = (int) typeSpinner
					.getItemIdAtPosition(typeSpinner.getSelectedItemPosition());
			final Spinner displaySpinner = (Spinner) findViewById(R.id.displaytype);
			final int displayId = (int) displaySpinner
					.getItemIdAtPosition(displaySpinner
							.getSelectedItemPosition());

			// TODO: fix the weird hack for type!
			final DataSource newDS = new DataSource(name, url, typeId + 3,
					displayId, true);

			int index = DataSourceStorage.getInstance().getSize(); // NOPMD by Àðò¸ì on 25.06.13 13:30
			final Bundle extras = getIntent().getExtras();
			if (extras != null && extras.containsKey(DATA_SOURCE_ID)) {
				index = extras.getInt(DATA_SOURCE_ID);
			}
			DataSourceStorage.getInstance().add("DataSource" + index,
					newDS.serialize());
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		final int base = Menu.FIRST;
		menu.add(base, base, base, R.string.cancel);
		return super.onCreateOptionsMenu(menu);

	}

	@Override
	public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
		final int itemId = item.getItemId();
		if (itemId == Menu.FIRST) {
			finish();
		}
		return super.onMenuItemSelected(featureId, item);
	}

	public DataSource(final String name, final String url, final TYPE type,
			final DISPLAY display, final boolean enabled) {
		super();
		this.NAME = name;
		this.URL = url;
		this.type = type;
		this.display = display;
		this.enabled = enabled;
		Log.d("mixare", "New Datasource!" + name + " " + url + " " + type + " "
				+ display + " " + enabled);
	}

	public DataSource(final String name, final String url, final int typeInt,
			final int displayInt, final boolean enabled) {
		super();
		final TYPE typeEnum = TYPE.values()[typeInt];
		final DISPLAY displayEnum = DISPLAY.values()[displayInt];
		this.NAME = name;
		this.URL = url;
		this.type = typeEnum;
		this.display = displayEnum;
		this.enabled = enabled;
	}

	public DataSource(final String name, final String url,
			final String typeString, final String displayString,
			final String enabledString) {
		super();
		final TYPE typeEnum = TYPE.values()[Integer.parseInt(typeString)];
		final DISPLAY displayEnum = DISPLAY.values()[Integer
				.parseInt(displayString)];
		final Boolean enabledBool = Boolean.parseBoolean(enabledString);
		this.NAME = name;
		this.URL = url;
		this.type = typeEnum;
		this.display = displayEnum;
		this.enabled = enabledBool;
	}

	public String createRequestParams(final double lat, final double lon,
			final double alt, final float radius, final String locale) {

		final StringBuilder builder = new StringBuilder();
		if (!builder.toString().startsWith("file://")) {
			switch (this.type) {

			case WIKIPEDIA:
				final float geoNamesRadius = radius > 20 ? 20 : radius; // Free
				// service
				// limited
				// to 20km
				builder.append("?lat=").append(lat).append("&lng=").append(lon)
						.append("&radius=").append(geoNamesRadius)
						.append("&maxRows=50").append("&lang=").append(locale)
						.append("&username=mixare");
				break;

			// case TWITTER:
			// ret += "?geocode=" + lat + "%2C" + lon + "%2C"
			// + Math.max(radius, 1.0) + "km";
			// break;

			case MIXARE:

				builder.append("?latitude=").append(Double.toString(lat))
						.append("&longitude=").append(Double.toString(lon))
						.append("&altitude=").append(Double.toString(alt))
						.append("&radius=").append(Double.toString(radius));

				break;

			case ARENA:

				builder.append("&lat=").append(Double.toString(lat))
						.append("&lng=").append(Double.toString(lon));

				break;

			case OSM:
				builder.append(DataConvertor
						.getOSMBoundingBox(lat, lon, radius));
				break;

			case FOURSQUARE:
				final SimpleDateFormat dateFormat = new SimpleDateFormat(
						"yyyyMMdd", Locale.getDefault());
				final CharSequence date = dateFormat.format(new Date());

				builder.append(
						"https://api.foursquare.com/v2/venues/explore?ll=")
						.append(Double.toString(lon))
						.append(",")
						.append(Double.toString(lat))
						.append("&llAcc=30&radius=1000&section=food,drinks,cofee,shops,specials,sights,outdoors,trdending,arts")
						.append("&limit=100&venuePhotos=1&client_id=DPQGGAUMTZNJQTEHFIR2VTLH55X3JVLPDUB1VOW0RRIAK0XQ")
						.append("&client_secret=GBSOAKFFSXWAAKY1EUCVPHUH52FVRZ1OKEOHOU2M15ONWL54&v=")
						.append(date);

				break;
			default:
				final float geoNamesRadius2 = radius > 20 ? 20 : radius; // Free

				builder.append("?lat=").append(lat).append("&lng=").append(lon)
						.append("&radius=").append(geoNamesRadius2)
						.append("&maxRows=50").append("&lang=").append(locale)
						.append("&username=mixare");

				break;
			}

		}

		return builder.toString();
	}

	public int getColor() {
		int ret;
		switch (this.type) {

		// case TWITTER:
		// ret = Color.rgb(50, 204, 255);
		// break;
		case WIKIPEDIA:
			ret = Color.RED;
			break;
		case ARENA:
			ret = Color.RED;
			break;
		case FOURSQUARE:
			ret = Color.BLUE;
			break;
		default:
			ret = Color.WHITE;
			break;
		}
		return ret;
	}

	public int getDataSourceIcon() {
		int ret;
		switch (this.type) {

		// case TWITTER:
		// ret = R.drawable.twitter;
		// break;
		case OSM:
			ret = R.drawable.osm;
			break;
		case WIKIPEDIA:
			ret = R.drawable.wikipedia;
			break;
		case ARENA:
			ret = R.drawable.arena;
			break;
		case FOURSQUARE:
			ret = R.drawable.wikipedia;
			break;
		default:
			ret = R.drawable.ic_launcher;
			break;
		}
		return ret;
	}

	public int getDisplayId() {
		return this.display.ordinal();
	}

	public int getTypeId() {
		return this.type.ordinal();
	}

	public DISPLAY getDisplay() {
		return this.display;
	}

	public TYPE getType() {
		return this.type;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public String getName() {
		return this.NAME;
	}

	public String getUrl() {
		return this.URL;
	}

	public String serialize() {
		return this.getName() + "|" + this.getUrl() + "|" + this.getTypeId()
				+ "|" + this.getDisplayId() + "|" + this.isEnabled();
	}

	public void setEnabled(final boolean isChecked) {
		this.enabled = isChecked;
	}

	@Override
	public String toString() {
		return "DataSource [name=" + NAME + ", url=" + URL + ", enabled="
				+ enabled + ", type=" + type + ", display=" + display + "]";
	}

	/**
	 * Check the minimum required data
	 * 
	 * @return boolean
	 */
	public boolean isWellFormed() {
		boolean out = false; // NOPMD by Àðò¸ì on 25.06.13 13:30
		if (isUrlWellFormed() || getName() != null || !getName().isEmpty()) { // NOPMD
																				// by
																				// Àðò¸ì
																				// on
																				// 24.06.13
																				// 10:38
			out = true;
		}
		return out;
	}

	public boolean isUrlWellFormed() {
		return getUrl() != null || !getUrl().isEmpty()
				|| "http://".equalsIgnoreCase(getUrl());
	}

}
