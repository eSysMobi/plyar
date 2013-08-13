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
import org.mixare.data.DataSource;
import org.mixare.lib.HtmlUnescape;
import org.mixare.lib.marker.Marker;
import android.util.Log;

/**
 * A data processor for wikipedia urls or data, Responsible for converting raw
 * data (to json and then) to marker data.
 * 
 * @author A. Egal
 */
public class WikiDataProcessor extends DataHandler implements DataProcessor {

	public static final int MAX_JSON_OBJECTS = 1000;

	@Override
	public String[] getUrlMatch() {
		final String[] str = { "wiki" };
		return str; // NOPMD by Àðò¸ì on 24.06.13 14:15
	}

	@Override
	public String[] getDataMatch() {
		final String[] str = { "wiki" };
		return str; // NOPMD by Àðò¸ì on 24.06.13 14:15
	}

	@Override
	public boolean matchesRequiredType(final String type) {
		if (type.equals(DataSource.TYPE.WIKIPEDIA.name())) {
			return true; // NOPMD by Àðò¸ì on 24.06.13 14:16
		}
		return false;
	}

	@Override
	public List<Marker> load(final String rawData, final int taskId,
			final int colour) throws JSONException {
		final List<Marker> markers = new ArrayList<Marker>();
		final JSONObject root = convertToJSON(rawData);
		final JSONArray dataArray = root.getJSONArray("geonames");
		final int top = Math.min(MAX_JSON_OBJECTS, dataArray.length());

		for (int i = 0; i < top; i++) {
			final JSONObject jsonObject = dataArray.getJSONObject(i);

			Marker marker = null; // NOPMD by Àðò¸ì on 24.06.13 14:15
			if (jsonObject.has("title") && jsonObject.has("lat")
					&& jsonObject.has("lng") && jsonObject.has("elevation")
					&& jsonObject.has("wikipediaUrl")) {

				Log.v(MixView.TAG, "processing Wikipedia JSON object");

				// no unique ID is provided by the web service according to
				// http://www.geonames.org/export/wikipedia-webservice.html
				marker = new POIMarker("", HtmlUnescape.unescapeHTML( // NOPMD by Àðò¸ì on 24.06.13 14:16
						jsonObject.getString("title"), 0),
						jsonObject.getDouble("lat"),
						jsonObject.getDouble("lng"),
						jsonObject.getDouble("elevation"), "http://"
								+ jsonObject.getString("wikipediaUrl"), taskId,
						colour);
				markers.add(marker);
			}
		}
		return markers;
	}

	private JSONObject convertToJSON(final String rawData) {
		try {
			return new JSONObject(rawData);
		} catch (JSONException e) {
			throw new RuntimeException(e); // NOPMD by Àðò¸ì on 24.06.13 11:51
		}
	}

}
