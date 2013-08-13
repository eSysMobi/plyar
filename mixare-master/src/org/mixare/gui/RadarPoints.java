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
package org.mixare.gui;

import org.mixare.DataView;
import org.mixare.lib.marker.Marker;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.gui.ScreenObj;
import org.mixare.data.DataHandler;

import android.graphics.Color;

/**
 * Takes care of the small radar in the top left corner and of its points
 * 
 * @author daniele
 * 
 */
public class RadarPoints implements ScreenObj {
	/** The screen */
	public transient DataView view;
	/** The radar's range */
	public transient float range;
	/** Radius in pixel on screen */
	public static float RADIUS = 40; // NOPMD by Àðò¸ì on 24.06.13 11:53
	/** Position on screen */
	public static float originX = 0, originY = 0;
	/** Color */
	public static int radarColor = Color.argb(100, 0, 0, 200);

	public void paint(final PaintScreen draw) {
		/** radius is in KM. */
		range = view.getRadius() * 1000;
		/** Draw the radar */
		draw.setFill(true);
		draw.setColor(radarColor);
		draw.paintCircle(originX + RADIUS, originY + RADIUS, RADIUS);

		/** put the markers in it */
		final float scale = range / RADIUS; // NOPMD by Àðò¸ì on 24.06.13 14:13

		final DataHandler jLayer = view.getDataHandler();

		for (int i = 0; i < jLayer.getMarkerCount(); i++) {
			final Marker placeMarker = jLayer.getMarker(i);
			final float xCoord = placeMarker.getLocationVector().x / scale;
			final float yCoord = placeMarker.getLocationVector().z / scale;

			if (placeMarker.isActive()
					&& (xCoord * xCoord + yCoord * yCoord < RADIUS * RADIUS)) {
				draw.setFill(true);

				// For OpenStreetMap the color is changing based on the URL
				draw.setColor(placeMarker.getColour());

				draw.paintRect(xCoord + RADIUS - 1, yCoord + RADIUS - 1, 2, 2);
			}
		}
	}

	/** Width on screen */
	public float getWidth() {
		return RADIUS * 2;
	}

	/** Height on screen */
	public float getHeight() {
		return RADIUS * 2;
	}
}
