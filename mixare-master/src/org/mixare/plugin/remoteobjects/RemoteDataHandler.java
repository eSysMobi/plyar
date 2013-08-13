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
package org.mixare.plugin.remoteobjects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.mixare.data.DataHandler;
import org.mixare.data.convert.DataProcessor;
import org.mixare.lib.marker.InitialMarkerData;
import org.mixare.lib.marker.Marker;
import org.mixare.lib.marker.draw.ParcelableProperty;
import org.mixare.lib.marker.draw.PrimitiveProperty;
import org.mixare.lib.service.IDataHandlerService;
import org.mixare.plugin.PluginLoader;
import org.mixare.plugin.PluginNotFoundException;

import android.os.RemoteException;

public class RemoteDataHandler extends DataHandler implements DataProcessor {

	private transient String dataHandlerName;
	private transient final IDataHandlerService handlerService;

	public String getDataHandlerName() {
		return dataHandlerName;
	}

	public RemoteDataHandler(final IDataHandlerService handlerService) {
		super();
		this.handlerService = handlerService;
	}

	public void buildDataHandler() {
		try {
			this.dataHandlerName = handlerService.build();
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	public String[] getUrlMatch() {
		try {
			return handlerService.getUrlMatch(dataHandlerName);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	public String[] getDataMatch() {
		try {
			return handlerService.getDataMatch(dataHandlerName);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public boolean matchesRequiredType(final String type) {
		// TODO: change the datasource so that it can have more types,
		// so that plugins can also have a required type
		return true;
	}

	public List<Marker> load(final String rawData, final int taskId,
			final int colour) throws JSONException {
		try {
			final List<InitialMarkerData> initialMarkerData = handlerService
					.load(dataHandlerName, rawData, taskId, colour);
			return initializeMarkerData(initialMarkerData);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	private List<Marker> initializeMarkerData(
			final List<InitialMarkerData> initialMarkerData)
			throws PluginNotFoundException, RemoteException {
		final List<Marker> markers = new ArrayList<Marker>();
		for (InitialMarkerData i : initialMarkerData) {
			final Marker marker = PluginLoader.getInstance().getMarkerInstance(
					i.getMarkerName(), (Integer) i.getConstr()[0],
					(String) i.getConstr()[1], (Double) i.getConstr()[2],
					(Double) i.getConstr()[3], (Double) i.getConstr()[4],
					(String) i.getConstr()[5], (Integer) i.getConstr()[6],
					(Integer) i.getConstr()[7]);
			fillExtraMarkerParcelableProperties(marker, i.getExtraParcelables());
			fillExtraMarkerPrimitiveProperties(marker, i.getExtraPrimitives());

			markers.add(marker);
		}
		return markers;
	}

	private Marker fillExtraMarkerParcelableProperties(final Marker marker,
			final Map<String, ParcelableProperty> properties) {
		final Iterator<Entry<String, ParcelableProperty>> iterator = properties
				.entrySet().iterator();
		while (iterator.hasNext()) {
			final Entry<String, ParcelableProperty> pairs = (Entry<String, ParcelableProperty>) iterator
					.next();
			marker.setExtras(pairs.getKey(), pairs.getValue());
		}
		return marker;
	}

	private Marker fillExtraMarkerPrimitiveProperties(final Marker marker,
			final Map<String, PrimitiveProperty> properties) {
		final Iterator<Entry<String, PrimitiveProperty>> iterator = properties
				.entrySet().iterator();
		while (iterator.hasNext()) {
			final Entry<String, PrimitiveProperty> pairs = (Entry<String, PrimitiveProperty>) iterator
					.next();
			marker.setExtras(pairs.getKey(), pairs.getValue());
		}
		return marker;
	}

}
