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
import java.util.Locale;

import org.json.JSONException;
import org.mixare.data.DataSource;
import org.mixare.lib.marker.Marker;
import org.mixare.lib.reality.PhysicalPlace;

/**
 * This class is responsible for converting raw data to marker data The class
 * will first check which processor is needed before it handles the data After
 * that it will convert the data to the format the processor wants. I.E. JSON /
 * XML
 * 
 * @author A. Egal
 */
public class DataConvertor {

	private static final List<DataProcessor> DATA_PROCESSORS = new ArrayList<DataProcessor>();

	private static DataConvertor instance;

	public static DataConvertor getInstance() {
		if (instance == null) { // NOPMD by Àðò¸ì on 25.06.13 13:04
			instance = new DataConvertor();
			instance.addDefaultDataProcessors();
		}
		return instance;
	}

	public void clearDataProcessors() {
		DATA_PROCESSORS.clear();
		addDefaultDataProcessors();
	}

	public void addDataProcessor(final DataProcessor dataProcessor) {
		DATA_PROCESSORS.add(dataProcessor);
	}

	public void removeDataProcessor(final DataProcessor dataProcessor) {
		DATA_PROCESSORS.remove(dataProcessor);
	}

	public List<Marker> load(final String url, final String rawResult,
			final DataSource dataSource) {
		DataProcessor dataProcessor = searchForMatchingDataProcessors(url,
				rawResult, dataSource.getType());
		if (dataProcessor == null) {
			dataProcessor = new MixareDataProcessor(); // using this as default
														// if nothing is found.
		}
		try {
			return dataProcessor.load(rawResult, dataSource.getTaskId(), dataSource.getColor()); // NOPMD
																					// by
																					// Àðò¸ì
																					// on
																					// 24.06.13
																					// 14:25
		} catch (JSONException e) {
			/*
			 * Find Other Away to notify Error, for now Hide this error
			 * MixView.CONTEXT.runOnUiThread(new Runnable() {
			 * 
			 * @Override public void run() { Toast.makeText(MixView.CONTEXT,
			 * "Could not process the url data", Toast.LENGTH_LONG).show(); }
			 * });
			 */
		}
		return null;
	}

	private DataProcessor searchForMatchingDataProcessors(final String url,
			final String rawResult, final DataSource.TYPE type) {
		for (DataProcessor dp : DATA_PROCESSORS) {
			if (dp.matchesRequiredType(type.name())) {
				// checking if url matches any dataprocessor identifiers
				for (String urlIdentifier : dp.getUrlMatch()) {
					if (url.toLowerCase(Locale.getDefault()).contains(
							urlIdentifier.toLowerCase(Locale.getDefault()))) {
						return dp; // NOPMD by Àðò¸ì on 24.06.13 14:25
					}
				}
				// checking if data matches any dataprocessor identifiers
				for (String dataIdentifier : dp.getDataMatch()) {
					if (rawResult.contains(dataIdentifier)) {
						return dp; // NOPMD by Àðò¸ì on 24.06.13 14:25
					}
				}
			}
		}
		return null;
	}

	private void addDefaultDataProcessors() {
		DATA_PROCESSORS.add(new WikiDataProcessor());
		DATA_PROCESSORS.add(new TwitterDataProcessor());
		DATA_PROCESSORS.add(new OsmDataProcessor());
		DATA_PROCESSORS.add(new FoursquareDataProcessor());
	}

	public static String getOSMBoundingBox(final double lat, final double lon,
			final double radius) {
		String bbox = "[bbox=";
		final PhysicalPlace lbPlace = new PhysicalPlace(); // left bottom
		final PhysicalPlace rtPlace = new PhysicalPlace(); // right top
		PhysicalPlace.calcDestination(lat, lon, 225, radius * 1414, lbPlace); // 1414:
																				// sqrt(2)*1000
		PhysicalPlace.calcDestination(lat, lon, 45, radius * 1414, rtPlace);
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(lbPlace.getLongitude()).append(",")
				.append(lbPlace.getLatitude()).append(",")
				.append(rtPlace.getLongitude()).append(",")
				.append(rtPlace.getLatitude()).append("]");
		bbox += stringBuilder.toString();
		return bbox;

		// return "[bbox=16.365,48.193,16.374,48.199]";
	}

}
