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

import org.mixare.lib.MixContextInterface;
import org.mixare.lib.MixStateInterface;
import org.mixare.lib.gui.Label;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.marker.Marker;
import org.mixare.lib.marker.draw.ClickHandler;
import org.mixare.lib.marker.draw.DrawCommand;
import org.mixare.lib.marker.draw.ParcelableProperty;
import org.mixare.lib.marker.draw.PrimitiveProperty;
import org.mixare.lib.render.Camera;
import org.mixare.lib.render.MixVector;
import org.mixare.lib.service.IMarkerService;
import org.mixare.plugin.PluginNotFoundException;

import android.location.Location;
import android.os.RemoteException;

/**
 * The remote marker sends request to the (remote)plugin that it is connected to
 * through IMarkerService. the remote marker is treated like a normal marker in
 * the core. And it overrides the marker interface.
 * 
 * @author A. Egal
 */
public class RemoteMarker implements Marker {

	private transient String markerName;
	private transient final IMarkerService iMarkerService;

	public RemoteMarker(final IMarkerService iMarkerService) {
		this.iMarkerService = iMarkerService;
	}

	public int getPid() {
		return 0;
	}

	public void buildMarker(final int markerID, final String title,
			final double latitude, final double longitude,
			final double altitude, final String url, final int type,
			final int color) {
		try {
			this.markerName = iMarkerService.buildMarker(markerID, title,
					latitude, longitude, altitude, url, type, color);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	public String getPluginName() {
		try {
			return iMarkerService.getPluginName();
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public void calcPaint(final Camera viewCam, final float addX,
			final float addY) {
		try {
			iMarkerService.calcPaint(markerName, viewCam, addX, addY);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public void draw(final PaintScreen draw) {
		try {
			final DrawCommand[] drawCommands = iMarkerService
					.remoteDraw(markerName);
			for (DrawCommand drawCommand : drawCommands) {
				drawCommand.draw(draw);
				if (drawCommand.getProperty("textlab") != null) {
					setTxtLab((Label) ((ParcelableProperty) drawCommand
							.getProperty("textlab")).getObject());
				}
			}
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		} catch (Exception ne) {
			throw new PluginNotFoundException(ne);
		}
	}

	@Override
	public double getAltitude() {
		try {
			return iMarkerService.getAltitude(markerName);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public int getColour() {
		try {
			return iMarkerService.getColour(markerName);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public double getDistance() {
		try {
			return iMarkerService.getDistance(markerName);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public String getID() {
		try {
			return iMarkerService.getID(markerName);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public double getLatitude() {
		try {
			return iMarkerService.getLatitude(markerName);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public MixVector getLocationVector() {
		try {
			return iMarkerService.getLocationVector(markerName);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public double getLongitude() {
		try {
			return iMarkerService.getLongitude(markerName);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public int getMaxObjects() {
		try {
			return iMarkerService.getMaxObjects(markerName);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public String getTitle() {
		try {
			return iMarkerService.getTitle(markerName);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public Label getTxtLab() {
		try {
			return iMarkerService.getTxtLab(markerName);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	public void setTxtLab(final Label txtLab) {
		try {
			if (txtLab != null) {
				iMarkerService.setTxtLab(markerName, txtLab);
			}
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public String getURL() {
		try {
			return iMarkerService.getURL(markerName);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public boolean isActive() {
		try {
			return iMarkerService.isActive(markerName);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public void setActive(final boolean active) {
		try {
			iMarkerService.setActive(markerName, active);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public void setDistance(final double distance) {
		try {
			iMarkerService.setDistance(markerName, distance);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public void setID(final String markerID) {
		try {
			iMarkerService.setID(markerName, markerID);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public void update(final Location curGPSFix) {
		try {
			iMarkerService.update(markerName, curGPSFix);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	public void setExtras(final String name,
			final ParcelableProperty parceableProp) {
		try {
			iMarkerService.setExtrasParc(markerName, name, parceableProp);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	public void setExtras(final String name,
			final PrimitiveProperty primitiveProperty) {
		try {
			iMarkerService.setExtrasPrim(markerName, name, primitiveProperty);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public boolean fClick(final float xCoord, final float yCoord,
			final MixContextInterface ctx, final MixStateInterface state) {
		ClickHandler clickHandler;
		try {
			clickHandler = iMarkerService.fClick(markerName);
			return clickHandler.handleClick(xCoord, yCoord, ctx, state);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(); // NOPMD by Àðò¸ì on 24.06.13 13:58
		}
	}

	@Override
	public boolean equals(final Object object) {
		if (object instanceof RemoteMarker) {
			final RemoteMarker remoteMarker = (RemoteMarker) object;
			if (remoteMarker.markerName.equals(this.markerName)) {
				return true; // NOPMD by Àðò¸ì on 24.06.13 13:58
			}
		}
		return super.equals(object);
	}

	@Override
	public int hashCode() {
		return markerName.hashCode() + iMarkerService.hashCode();
	}

	@Override
	public int compareTo(final Marker another) {
		final Marker remoteMarker = (Marker) another;
		return this.getID().compareTo(remoteMarker.getID());
	}

}