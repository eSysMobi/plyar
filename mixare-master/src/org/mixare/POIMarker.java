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

import java.text.DecimalFormat;

import org.mixare.lib.MixUtils;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.gui.TextObj;

import android.graphics.Color;
import android.graphics.Path;

/**
 * This markers represent the points of interest. On the screen they appear as
 * circles, since this class inherits the draw method of the Marker.
 * 
 * @author hannes
 * 
 */
public class POIMarker extends AbstractMarker {

	public static final int MAX_OBJECTS = 20;
	public static final int URL_MAX_OBJECTS = 5;
	private static final float X_COORD = 0;
	private static final float Y_COORD = 0;

	public POIMarker(final String markerID, final String title,
			final double latitude, final double longitude,
			final double altitude, final String URL, final int type,
			final int color) {
		super(markerID, title, latitude, longitude, altitude, URL, type, color);

	}

	@Override
	public int getMaxObjects() {
		return MAX_OBJECTS;
	}

	@Override
	public void drawCircle(final PaintScreen draw) {
		if (isVisible) {
			final float maxHeight = draw.getHeight();
			draw.setStrokeWidth(maxHeight / 100f);
			draw.setFill(false);

			draw.setColor(getColour());

			// draw circle with radius depending on distance
			// 0.44 is approx. vertical fov in radians
			final double angle = 2.0 * Math.atan2(10, distance);
			final double radius = Math.max(
					// NOPMD by Àðò¸ì on 25.06.13 13:40
					Math.min(angle / 0.44 * maxHeight, maxHeight),
					maxHeight / 25f);

			/*
			 * distance 100 is the threshold to convert from circle to another
			 * shape
			 */
			if (distance < 100.0) {
				otherShape(draw);
			} else {
				draw.paintCircle(cMarker.x, cMarker.y, (float) radius);
			}

		}
	}

	@Override
	public void drawTextBlock(final PaintScreen draw) {
		final float maxHeight = Math.round(draw.getHeight() / 10f) + 1;
		// TODO: change textblock only when distance changes

		String textStr = ""; // NOPMD by Àðò¸ì on 25.06.13 13:40

		double dist = distance;
		final DecimalFormat decimalFormat = new DecimalFormat("@#");
		if (dist < 1000.0) {
			textStr = title + " (" + decimalFormat.format(dist) + "m)";
		} else {
			dist = dist / 1000.0;
			textStr = title + " (" + decimalFormat.format(dist) + "km)";
		}

		textBlock = new TextObj(textStr, Math.round(maxHeight / 2f) + 1, 250,
				draw, underline);

		if (isVisible) {
			// based on the distance set the colour
			if (distance < 100.0) {
				textBlock.setBgColor(Color.argb(128, 52, 52, 52));
				textBlock.setBorderColor(Color.rgb(255, 104, 91));
			} else {
				textBlock.setBgColor(Color.argb(128, 0, 0, 0));
				textBlock.setBorderColor(Color.rgb(255, 255, 255));
			}
			// dw.setColor(DataSource.getColor(type));

			final float currentAngle = MixUtils.getAngle(cMarker.x, cMarker.y,
					signMarker.x, signMarker.y);
			txtLab.prepare(textBlock);
			draw.setStrokeWidth(1f);
			draw.setFill(true);
			draw.paintObj(txtLab, signMarker.x - txtLab.getWidth() / 2,
					signMarker.y + maxHeight, currentAngle + 90, 1);

		}
	}

	public void otherShape(final PaintScreen draw) {
		// This is to draw new shape, triangle
		final float currentAngle = MixUtils.getAngle(cMarker.x, cMarker.y,
				signMarker.x, signMarker.y);
		final float maxHeight = Math.round(draw.getHeight() / 10f) + 1;

		draw.setColor(getColour());
		final float radius = maxHeight / 1.5f;
		draw.setStrokeWidth(draw.getHeight() / 100f);
		draw.setFill(false);

		final Path tri = new Path();

		tri.moveTo(X_COORD, Y_COORD);
		tri.lineTo(X_COORD - radius, Y_COORD - radius);
		tri.lineTo(X_COORD + radius, Y_COORD - radius);

		tri.close();
		draw.paintPath(tri, cMarker.x, cMarker.y, radius * 2, radius * 2,
				currentAngle + 90, 1);
	}

}
