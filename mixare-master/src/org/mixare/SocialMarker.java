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

package org.mixare;

import org.mixare.lib.gui.PaintScreen;

import android.location.Location;

/**
 * The SocialMarker class represents a marker, which contains data from sources
 * like twitter etc. Social markers appear at the top of the screen and show a
 * small logo of the source.
 * 
 * @author hannes
 * 
 */
public class SocialMarker extends AbstractMarker {

	public static final int MAX_OBJECTS = 15;

	public SocialMarker(final String markerID, final String title,
			final double latitude, final double longitude,
			final double altitude, final String URL, final int type,
			final int color) {
		super(markerID, title, latitude, longitude, altitude, URL, type, color);
	}

	@Override
	public void update(final Location curGPSFix) {

		// 0.35 radians ~= 20 degree
		// 0.85 radians ~= 45 degree
		// minAltitude = sin(0.35)
		// maxAltitude = sin(0.85)

		// we want the social markers to be on the upper part of
		// your surrounding sphere
		final double altitude = curGPSFix.getAltitude()
				+ Math.sin(0.35)
				* distance
				+ Math.sin(0.4)
				* (distance / (MixView.getDataView().getRadius() * 1000f / distance));
		mGeoLoc.setAltitude(altitude);
		super.update(curGPSFix);

	}

	@Override
	public void draw(final PaintScreen draw) {

		drawTextBlock(draw);

		if (isVisible) {
			final float maxHeight = Math.round(draw.getHeight() / 10f) + 1;
			// Bitmap bitmap =
			// BitmapFactory.decodeResource(MixContext.getResources(),
			// DataSource.getDataSourceIcon());
			// if(bitmap!=null) {
			// dw.paintBitmap(bitmap, cMarker.x - maxHeight/1.5f, cMarker.y -
			// maxHeight/1.5f);
			// }
			// else {
			draw.setStrokeWidth(maxHeight / 10f);
			draw.setFill(false);
			// dw.setColor(DataSource.getColor(type));
			draw.paintCircle(cMarker.x, cMarker.y, maxHeight / 1.5f);
			// }
		}
	}

	@Override
	public int getMaxObjects() {
		return MAX_OBJECTS;
	}

}
