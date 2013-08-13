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
package org.mixare.data.convert;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mixare.MixView;
import org.mixare.POIMarker;
import org.mixare.data.DataHandler;
import org.mixare.lib.HtmlUnescape;
import org.mixare.lib.marker.Marker;

import android.util.Log;

/**
 * A data processor for custom urls or data, Responsible for converting raw data
 * (to json and then) to marker data.
 * 
 * @author A. Egal
 */
public class MixareDataProcessor extends DataHandler implements DataProcessor { // NOPMD
																				// by
																				// Àðò¸ì
																				// on
																				// 24.06.13
																				// 14:24

	public static final int MAX_JSON_OBJECTS = 1000;

	@Override
	public String[] getUrlMatch() {
		final String[] str = new String[0]; // only use this data source if all
											// the
		// others don't match
		return str;
	}

	@Override
	public String[] getDataMatch() {
		final String[] str = new String[0]; // only use this data source if all
											// the
		// others don't match
		return str;
	}

	@Override
	public boolean matchesRequiredType(final String type) {
		return true; // this datasources has no required type, it will always
						// match.
	}

	@Override
	public List<Marker> load(final String rawData, final int taskId, // NOPMD by
																		// Àðò¸ì
																		// on
																		// 24.06.13
																		// 14:24
			final int colour) throws JSONException {
		final List<Marker> markers = new ArrayList<Marker>();
		final JSONObject root = convertToJSON(rawData);
		final JSONArray dataArray = root.getJSONArray("results");
		final int top = Math.min(MAX_JSON_OBJECTS, dataArray.length());

		for (int i = 0; i < top; i++) {
			final JSONObject jsonObject = dataArray.getJSONObject(i);

			Marker ma = null; // NOPMD by Àðò¸ì on 24.06.13 14:24
			if (jsonObject.has("title") && jsonObject.has("lat")
					&& jsonObject.has("lng") && jsonObject.has("elevation")) {

				String markerID = ""; // NOPMD by Àðò¸ì on 24.06.13 14:24
				if (jsonObject.has("id")) {
					markerID = jsonObject.getString("id");
				}

				Log.v(MixView.TAG, "processing Mixare JSON object");
				String link = null; // NOPMD by Àðò¸ì on 24.06.13 14:24

				if (jsonObject.has("has_detail_page")
						&& jsonObject.getInt("has_detail_page") != 0
						&& jsonObject.has("webpage"))

					link = jsonObject.getString("webpage");

				ma = new POIMarker(markerID, HtmlUnescape.unescapeHTML( // NOPMD
																		// by
						// Àðò¸ì on
						// 24.06.13
						// 14:23
						jsonObject.getString("title"), 0),
						jsonObject.getDouble("lat"),
						jsonObject.getDouble("lng"),
						jsonObject.getDouble("elevation"), link, taskId, colour);
				markers.add(ma);
			}
		}
		return markers;
	}

	private JSONObject convertToJSON(final String rawData) {
		try {
			return new JSONObject(rawData);
		} catch (JSONException e) {
			throw new RuntimeException(e); // NOPMD by Àðò¸ì on 24.06.13 11:47
		}
	}

}
