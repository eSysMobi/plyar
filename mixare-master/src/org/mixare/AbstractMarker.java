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

//Comment for github 2

import java.net.URLDecoder;
import java.text.DecimalFormat;

import org.mixare.lib.MixContextInterface;
import org.mixare.lib.MixStateInterface;
import org.mixare.lib.MixUtils;
import org.mixare.lib.gui.Label;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.gui.ScreenLine;
import org.mixare.lib.gui.TextObj;
import org.mixare.lib.marker.Marker;
import org.mixare.lib.marker.draw.ParcelableProperty;
import org.mixare.lib.marker.draw.PrimitiveProperty;
import org.mixare.lib.reality.PhysicalPlace;
import org.mixare.lib.render.Camera;
import org.mixare.lib.render.MixVector;

import android.graphics.Bitmap;
import android.location.Location;

/**
 * The class represents a marker and contains its information. It draws the
 * marker itself and the corresponding label. All markers are specific markers
 * like SocialMarkers or NavigationMarkers, since this class is abstract
 */

public abstract class AbstractMarker implements Marker { // NOPMD by Àðò¸ì on
															// 25.06.13 14:58
	private transient final MixVector ORIGIN = new MixVector(0, 0, 0);
	private transient final MixVector UP_V = new MixVector(0, 1, 0);
	private transient final int COLOUR;

	private transient String markerID;
	protected transient String title;
	protected transient boolean underline = false;
	private transient String url;
	protected transient PhysicalPlace mGeoLoc;
	// distance from user to mGeoLoc in meters
	protected transient double distance;
	// The marker color

	private transient boolean active;

	// Draw properties
	protected transient boolean isVisible;
	// private boolean isLookingAt;
	// private boolean isNear;
	// private float deltaCenter;
	public transient MixVector cMarker = new MixVector();
	protected transient MixVector signMarker = new MixVector();
	// private MixVector oMarker = new MixVector();

	protected transient MixVector locationVector = new MixVector();

	private transient ScreenLine pPt = new ScreenLine();

	public Label txtLab = new Label();
	protected transient TextObj textBlock;

	public AbstractMarker(final String markerID, final String title,
			final double latitude, final double longitude,
			final double altitude, final String link, final int type,
			final int colour) {
		super();

		this.active = false;
		this.title = title;
		this.mGeoLoc = new PhysicalPlace(latitude, longitude, altitude);
		if (link != null && link.length() > 0) {
			url = "webpage:" + URLDecoder.decode(link);
			this.underline = true;
		}
		this.COLOUR = colour;

		this.markerID = markerID + "##" + type + "##" + title;

	}

	public String getTitle() {
		return title;
	}

	public String getURL() {
		return url;
	}

	public double getLatitude() {
		return mGeoLoc.getLatitude();
	}

	public double getLongitude() {
		return mGeoLoc.getLongitude();
	}

	public double getAltitude() {
		return mGeoLoc.getAltitude();
	}

	public MixVector getLocationVector() {
		return locationVector;
	}

	private void cCMarker(final MixVector originalPoint, final Camera viewCam,
			final float addX, final float addY) {

		// Temp properties
		final MixVector tmpa = new MixVector(originalPoint);
		final MixVector tmpc = new MixVector(UP_V);
		tmpa.add(locationVector); // 3
		tmpc.add(locationVector); // 3
		tmpa.sub(viewCam.lco); // 4
		tmpc.sub(viewCam.lco); // 4
		tmpa.prod(viewCam.transform); // 5
		tmpc.prod(viewCam.transform); // 5

		final MixVector tmpb = new MixVector();
		viewCam.projectPoint(tmpa, tmpb, addX, addY); // 6
		cMarker.set(tmpb); // 7
		viewCam.projectPoint(tmpc, tmpb, addX, addY); // 6
		signMarker.set(tmpb); // 7
	}

	private void calcV() {
		isVisible = false;
		// isLookingAt = false;
		// deltaCenter = Float.MAX_VALUE;

		if (cMarker.z < -1f) {
			isVisible = true;
		}
	}

	public void update(final Location curGPSFix) {
		// An elevation of 0.0 probably means that the elevation of the
		// POI is not known and should be set to the users GPS height
		// Note: this could be improved with calls to
		// http://www.geonames.org/export/web-services.html#astergdem
		// to estimate the correct height with DEM models like SRTM, AGDEM or
		// GTOPO30
		if (mGeoLoc.getAltitude() == 0.0) {
			mGeoLoc.setAltitude(curGPSFix.getAltitude());
		}

		// compute the relative position vector from user position to POI
		// location
		PhysicalPlace.convLocToVec(curGPSFix, mGeoLoc, locationVector);
	}

	public void calcPaint(final Camera viewCam, final float addX,
			final float addY) {
		cCMarker(ORIGIN, viewCam, addX, addY);
		calcV();
	}

	// private void calcPaint(Camera viewCam) {
	// cCMarker(origin, viewCam, 0, 0);
	// }

	private boolean isClickValid(final float xCoord, final float yCoord) {

		// if the marker is not active (i.e. not shown in AR view) we don't have
		// to check it for clicks
		if (!isActive() && !this.isVisible) {
			return false; // NOPMD by Àðò¸ì on 25.06.13 14:57
		}

		final float currentAngle = MixUtils.getAngle(cMarker.x, cMarker.y,
				signMarker.x, signMarker.y);
		// TODO adapt the following to the variable radius!
		pPt.x = xCoord - signMarker.x;
		pPt.y = yCoord - signMarker.y;
		pPt.rotate((float) Math.toRadians(-(currentAngle + 90)));
		pPt.x += txtLab.getX();
		pPt.y += txtLab.getY();

		final float objX = txtLab.getX() - txtLab.getWidth() / 2;
		final float objY = txtLab.getY() - txtLab.getHeight() / 2;
		final float objW = txtLab.getWidth();
		final float objH = txtLab.getHeight();

		return pPt.x > objX && pPt.x < objX + objW && pPt.y > objY
				&& pPt.y < objY + objH ? true : false;
	}

	public void draw(final PaintScreen draw) {
		drawCircle(draw);
		drawTextBlock(draw);
	}

	public void drawCircle(final PaintScreen draw) {

		if (isVisible) {
			// float maxHeight = Math.round(dw.getHeight() / 10f) + 1;
			final float maxHeight = draw.getHeight();
			draw.setStrokeWidth(maxHeight / 100f);
			draw.setFill(false);
			// dw.setColor(DataSource.getColor(type));

			// draw circle with radius depending on distance
			// 0.44 is approx. vertical fov in radians
			final double angle = 2.0 * Math.atan2(10, distance);
			final double radius = Math.max(
					Math.min(angle / 0.44 * maxHeight, maxHeight),
					maxHeight / 25f);
			// double radius = angle/0.44d * (double)maxHeight;

			draw.paintCircle(cMarker.x, cMarker.y, (float) radius);
		}
	}

	public void drawTextBlock(final PaintScreen draw) {
		// TODO: grandezza cerchi e trasparenza
		final float maxHeight = Math.round(draw.getHeight() / 10f) + 1;

		// TODO: change textblock only when distance changes
		String textStr = ""; // NOPMD by Àðò¸ì on 25.06.13 14:58

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

	public boolean fClick(final float xCoord, final float yCoord,
			final MixContextInterface ctx, final MixStateInterface state) {
		boolean evtHandled = false; // NOPMD by Àðò¸ì on 25.06.13 14:58

		if (isClickValid(xCoord, yCoord)) {
			evtHandled = state.handleEvent(ctx, url);
		}
		return evtHandled;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(final double distance) {
		this.distance = distance;
	}

	public String getID() {
		return markerID;
	}

	public void setID(final String markerID) {
		this.markerID = markerID;
	}

	public int compareTo(final Marker another) {

		final Marker leftPm = this;
		final Marker rightPm = another;

		return Double.compare(leftPm.getDistance(), rightPm.getDistance());

	}

	@Override
	public boolean equals(final Object marker) {
		return this.markerID.equals(((Marker) marker).getID());
	}

	@Override
	public int hashCode() {
		return this.markerID.hashCode();
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(final boolean active) {
		this.active = active;
	}

	abstract public int getMaxObjects();

	public void setImage(Bitmap image) { // NOPMD by Àðò¸ì on 24.06.13 10:22
	}

	public Bitmap getImage() { // NOPMD by Àðò¸ì on 24.06.13 10:22
		return null;
	}

	// get Colour for OpenStreetMap based on the URL number
	public int getColour() {
		return COLOUR;
	}

	@Override
	public void setTxtLab(final Label txtLab) {
		this.txtLab = txtLab;
	}

	@Override
	public Label getTxtLab() {
		return txtLab;
	}

	public void setExtras(String name, PrimitiveProperty primitiveProperty) { // NOPMD
																				// by
																				// Àðò¸ì
																				// on
																				// 24.06.13
																				// 10:22
		// nothing to add
	}

	public void setExtras(String name, ParcelableProperty parcelableProperty) { // NOPMD
																				// by
																				// Àðò¸ì
																				// on
																				// 24.06.13
																				// 10:23
		// nothing to add
	}
}
