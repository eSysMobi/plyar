package org.mixare.data.convert;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mixare.POIMarker;
import org.mixare.data.DataHandler;
import org.mixare.data.DataSource;
import org.mixare.lib.marker.Marker;

import android.util.Log;

public class FoursquareDataProcessor extends DataHandler implements
		DataProcessor {
	private static final String LOCATION = "location";
	private static final String VENUE = "venue";

	@Override
	public String[] getUrlMatch() {
		final String[] str = { "foursquare" };
		return str; // NOPMD by Àðò¸ì on 24.06.13 14:24
	}

	@Override
	public String[] getDataMatch() {
		final String[] str = { "foursquare" };
		return str; // NOPMD by Àðò¸ì on 24.06.13 14:24
	}

	@Override
	public boolean matchesRequiredType(final String type) {
		if (type.equals(DataSource.TYPE.FOURSQUARE.name())) {
			return true; // NOPMD by Àðò¸ì on 24.06.13 14:24
		}
		return false;
	}

	@Override
	public List<Marker> load(final String rawData, final int taskId,
			final int colour) throws JSONException {
		final List<Marker> markers = new ArrayList<Marker>();
		final List<PlacePos> placePos = new ArrayList<PlacePos>();
		final JSONObject root = convertToJSON(rawData);
		try {
			final JSONArray array = root.getJSONObject("response")
					.getJSONArray("groups");
			for (int i = 0; i < array.length(); i++) {
				final JSONArray items = array.getJSONObject(i).getJSONArray(
						"items");
				for (int j = 0; j < items.length(); j++) {
					final JSONObject venueObject = items.getJSONObject(j)
							.getJSONObject(VENUE);
					final JSONArray categoriesArray = venueObject
							.getJSONArray("categories");

					final JSONObject locationObject = venueObject
							.getJSONObject(LOCATION);

					placePos.add(new PlacePos(locationObject.getDouble("lat"), // NOPMD
																				// by
																				// Àðò¸ì
																				// on
																				// 24.06.13
																				// 14:24
							locationObject.getDouble("lng"), venueObject
									.getString("name"),

							locationObject.getString("address"),
							categoriesArray.getJSONObject(0)
									.getJSONObject("icon").getString("prefix")
									+ "bg_32.png"));

				}

			}
			Marker marker = null; // NOPMD by Àðò¸ì on 24.06.13 14:24
			for (int i = 0; i < array.length(); i++) {

				marker = new POIMarker("", placePos.get(i).getName(), placePos // NOPMD by Àðò¸ì on 24.06.13 14:24
						.get( // NOPMD by Àðò¸ì on 24.06.13 14:24
						i).getLat(), placePos.get(i).getLon(), 0.0, "", taskId,
						colour);
				markers.add(marker);
			}
		} catch (JSONException e) {
			Log.d(getClass().getName(), placePos.toString());
			Log.d(getClass().getName(), "Error parsing foursquare");
		}

		return markers;
	}

	private JSONObject convertToJSON(final String rawData) {
		try {
			return new JSONObject(rawData);
		} catch (JSONException e) {
			throw new RuntimeException(e); // NOPMD by Àðò¸ì on 24.06.13 11:45
		}
	}

}
