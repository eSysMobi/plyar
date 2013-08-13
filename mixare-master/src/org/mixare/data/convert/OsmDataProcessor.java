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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONException;
import org.mixare.MixView;
import org.mixare.NavigationMarker;
import org.mixare.data.DataHandler;
import org.mixare.data.DataSource;
import org.mixare.lib.marker.Marker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.util.Log;

public class OsmDataProcessor extends DataHandler implements DataProcessor {

	public static final int MAX_JSON_OBJECTS = 1000;

	@Override
	public String[] getUrlMatch() {
		final String[] str = { "mapquestapi", "osm" };
		return str; // NOPMD by Àðò¸ì on 24.06.13 14:20
	}

	@Override
	public String[] getDataMatch() {
		final String[] str = { "mapquestapi", "osm" };
		return str; // NOPMD by Àðò¸ì on 24.06.13 14:20
	}

	@Override
	public boolean matchesRequiredType(final String type) {
		if (type.equals(DataSource.TYPE.OSM.name())) {
			return true; // NOPMD by Àðò¸ì on 24.06.13 14:19
		}
		return false;
	}

	@Override
	public List<Marker> load(final String rawData, final int taskId,
			final int colour) throws JSONException {
		final Element root = convertToXmlDocument(rawData).getDocumentElement();

		final List<Marker> markers = new ArrayList<Marker>();
		final NodeList nodes = root.getElementsByTagName("node");

		for (int i = 0; i < nodes.getLength(); i++) {
			final Node node = nodes.item(i);
			final NamedNodeMap att = node.getAttributes(); // NOPMD by Àðò¸ì on
															// 24.06.13 14:20
			final NodeList tags = node.getChildNodes();
			for (int j = 0; j < tags.getLength(); j++) {
				final Node tag = tags.item(j);
				if (tag.getNodeType() != Node.TEXT_NODE) {
					final String key = tag.getAttributes().getNamedItem("k")
							.getNodeValue();
					if (("name").equals(key)) { // NOPMD by Àðò¸ì on 24.06.13
												// 14:22

						final String name = tag.getAttributes()
								.getNamedItem("v").getNodeValue();
						final String markerID = att.getNamedItem("id")
								.getNodeValue();
						final double lat = Double.valueOf(att.getNamedItem(
								"lat").getNodeValue());
						final double lon = Double.valueOf(att.getNamedItem(
								"lon").getNodeValue());

						Log.v(MixView.TAG, "OSM Node: " + name + " lat " + lat
								+ " lon " + lon + "\n");

						final Marker marker = new NavigationMarker(markerID, // NOPMD
																				// by
																				// Àðò¸ì
																				// on
																				// 24.06.13
																				// 14:22
								name, lat, // NOPMD by Àðò¸ì on 24.06.13 14:20
								lon, 0, // NOPMD by Àðò¸ì on 24.06.13 14:19
								"http://www.openstreetmap.org/?node="
										+ markerID, taskId, colour);
						markers.add(marker);

						// skip to next node
						continue;
					}
				}
			}
		}
		return markers;
	}

	public Document convertToXmlDocument(final String rawData) {
		Document doc = null; // NOPMD by Àðò¸ì on 24.06.13 14:20
		try {
			final DocumentBuilder builder = DocumentBuilderFactory
					.newInstance().newDocumentBuilder();
			// Document doc = builder.parse(is);d
			doc = builder.parse(new InputSource(new StringReader(rawData)));
		} catch (Exception e) {
			Log.e("Error", "Error in converting to xml");
		}
		return doc;
	}

}
