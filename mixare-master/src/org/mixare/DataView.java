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

import static android.view.KeyEvent.KEYCODE_CAMERA;
import static android.view.KeyEvent.KEYCODE_DPAD_CENTER;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import static android.view.KeyEvent.KEYCODE_DPAD_RIGHT;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.mixare.data.DataHandler;
import org.mixare.data.DataSource;
import org.mixare.gui.RadarPoints;
import org.mixare.lib.MixUtils;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.gui.ScreenLine;
import org.mixare.lib.marker.Marker;
import org.mixare.lib.render.Camera;
import org.mixare.mgr.downloader.DownloadManager;
import org.mixare.mgr.downloader.DownloadRequest;
import org.mixare.mgr.downloader.DownloadResult;

import android.graphics.Color;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

/**
 * This class is able to update the markers and the radar. It also handles some
 * user events
 * 
 * @author daniele
 * 
 */
public class DataView {

	private final static float CONST = 10f;
	private final static float PAD_W = 4, PAD_H = 2;
	private static final float RX_COORD = 10, RY_COORD = 20;

	final DownloadResult dRes = null;

	/** current context */
	private transient final MixContext mixContext;
	/** is the view Inited? */
	private transient boolean isInit;

	/** width and height of the view */
	private transient int width, height;

	/**
	 * _NOT_ the android camera, the class that takes care of the transformation
	 */
	private transient Camera cam;

	private transient final MixState state = new MixState();

	/** The view can be "frozen" for debug purposes */
	private transient boolean frozen;

	/** how many times to re-attempt download */
	private transient int retry;

	private transient Location curFix;
	private transient DataHandler dataHandler = new DataHandler();

	/** timer to refresh the browser */
	private transient Timer refresh = null;
	private transient float radius = 20;
	private transient final long refreshDelay = 45 * 1000; // refresh every 45
															// seconds

	private transient boolean isLauncherStarted;

	private transient final List<UIEvent> uiEvents = new ArrayList<UIEvent>();

	private transient final RadarPoints radarPoints = new RadarPoints();
	private transient final ScreenLine lrl = new ScreenLine();
	private transient final ScreenLine rrl = new ScreenLine();

	private transient float addX = 0, addY = 0;

	private transient List<Marker> markers;

	/**
	 * Constructor
	 */
	public DataView(final MixContext ctx) {
		this.mixContext = ctx;
	}

	public MixContext getContext() {
		return mixContext;
	}

	public boolean isLauncherStarted() {
		return isLauncherStarted;
	}

	public boolean isFrozen() {
		return frozen;
	}

	public void setFrozen(final boolean frozen) {
		this.frozen = frozen;
	}

	public float getRadius() {
		return radius;
	}

	public void setRadius(final float radius) {
		this.radius = radius;
	}

	public DataHandler getDataHandler() {
		return dataHandler;
	}

	public boolean isDetailsView() {
		return state.isDetailsView();
	}

	public void setDetailsView(final boolean detailsView) {
		state.setDetailsView(detailsView);
	}

	public void doStart() {
		state.nextLStatus = MixState.notStarted;
		mixContext.getLocationFinder().setLocationAtLastDownload(curFix);
	}

	public boolean isInited() {
		return isInit;
	}

	public void init(final int widthInit, final int heightInit) {
		try {
			width = widthInit;
			height = heightInit;

			cam = new Camera(width, height, true);
			cam.setViewAngle(Camera.DEFAULT_VIEW_ANGLE);

			lrl.set(0, -RadarPoints.RADIUS);
			lrl.rotate(Camera.DEFAULT_VIEW_ANGLE / 2);
			lrl.add(RX_COORD + RadarPoints.RADIUS, RY_COORD
					+ RadarPoints.RADIUS);
			rrl.set(0, -RadarPoints.RADIUS);
			rrl.rotate(-Camera.DEFAULT_VIEW_ANGLE / 2);
			rrl.add(RX_COORD + RadarPoints.RADIUS, RY_COORD
					+ RadarPoints.RADIUS);
		} catch (Exception ex) {

		}
		frozen = false;
		isInit = true;
	}

	public void requestData(final String url) {
		final DownloadRequest request = new DownloadRequest(new DataSource(
				"LAUNCHER", url, DataSource.TYPE.MIXARE,
				DataSource.DISPLAY.CIRCLE_MARKER, true));
		mixContext.getDataSourceManager().setAllDataSourcesforLauncher(
				request.getSource());
		mixContext.getDownloadManager().submitJob(request);
		state.nextLStatus = MixState.proccessing;
	}

	// public void requestData(DataSource datasource, double lat, double lon,
	// double alt, float radius, String locale) {
	// DownloadRequest request = new DownloadRequest();
	// request.params = datasource.createRequestParams(lat, lon, alt, radius,
	// locale);
	// request.source = datasource;
	//
	// mixContext.getDownloadManager().submitJob(request);
	// state.nextLStatus = MixState.PROCESSING;
	// }

	public void draw(final PaintScreen draw) {
		mixContext.getRM(cam.transform);
		curFix = mixContext.getLocationFinder().getCurrentLocation();

		state.calcPitchBearing(cam.transform);

		// Load Layer
		if (state.nextLStatus == MixState.notStarted && !frozen) {
			loadDrawLayer();
			markers = new ArrayList<Marker>();
		} else if (state.nextLStatus == MixState.proccessing) {
			final DownloadManager downloadManager = mixContext
					.getDownloadManager();

			markers.addAll(downloadDrawResults(downloadManager, dRes));

			if (downloadManager.isDone()) {
				retry = 0;
				state.nextLStatus = MixState.done;

				dataHandler = new DataHandler();
				dataHandler.addMarkers(markers);
				dataHandler.onLocationChanged(curFix);

				if (refresh == null) { // start the refresh timer if it is null
					refresh = new Timer(false);
					final Date date = new Date(System.currentTimeMillis()
							+ refreshDelay);
					refresh.schedule(new TimerTask() {

						@Override
						public void run() {
							callRefreshToast();
							refresh();
						}
					}, date, refreshDelay);
				}
			}
		}

		// Update markers
		dataHandler.updateActivationStatus(mixContext);
		for (int i = dataHandler.getMarkerCount() - 1; i >= 0; i--) {
			final Marker maker = dataHandler.getMarker(i);
			// if (ma.isActive() && (ma.getDistance() / 1000f < radius || ma
			// instanceof NavigationMarker || ma instanceof SocialMarker)) {
			if (maker.isActive() && (maker.getDistance() / 1000f < radius)) {

				// To increase performance don't recalculate position vector
				// for every marker on every draw call, instead do this only
				// after onLocationChanged and after downloading new marker
				// if (!frozen)
				// ma.update(curFix);
				if (!frozen) {
					maker.calcPaint(cam, addX, addY);
				}
				maker.draw(draw);
			}
		}

		// Draw Radar
		drawRadar(draw);

		// Get next event
		UIEvent evt = null; // NOPMD by Àðò¸ì on 25.06.13 15:58
		synchronized (uiEvents) {
			if (uiEvents.size() > 0) {
				evt = uiEvents.get(0);
				uiEvents.remove(0);
			}
		}
		if (evt != null) {
			if (evt.type == UIEvent.KEY) {
				handleKeyEvent((KeyEvent) evt);
			} else if (evt.type == UIEvent.CLICK) {
				handleClickEvent((ClickEvent) evt);
			}
		}
		state.nextLStatus = MixState.proccessing;
	}

	/**
	 * Part of draw function, loads the layer.
	 */
	private void loadDrawLayer() {
		if (mixContext.getStartUrl().length() > 0) {
			requestData(mixContext.getStartUrl());
			isLauncherStarted = true;
		}

		else {
			final double lat = curFix.getLatitude(), lon = curFix
					.getLongitude(), alt = curFix.getAltitude();
			state.nextLStatus = MixState.proccessing;
			mixContext.getDataSourceManager()
					.requestDataFromAllActiveDataSource(lat, lon, alt, radius);
		}

		// if no datasources are activated
		if (state.nextLStatus == MixState.notStarted) {
			state.nextLStatus = MixState.done;
		}
	}

	private List<Marker> downloadDrawResults(
			final DownloadManager downloadManager, DownloadResult dRes) { // NOPMD
		// by
		// Àðò¸ì
		// on
		// 24.06.13
		// 11:32
		final List<Marker> markers = new ArrayList<Marker>();
		while ((dRes = downloadManager.getNextResult()) != null) {
			if (dRes.isError() && retry < 3) {
				retry++;
				mixContext.getDownloadManager().submitJob(
						dRes.getErrorRequest());
				// Notification
				// Toast.makeText(mixContext, dRes.errorMsg,
				// Toast.LENGTH_SHORT).show();
			}

			if (!dRes.isError() && dRes.getMarkers() != null) {
				// jLayer = (DataHandler) dRes.obj;
				Log.i(MixView.TAG, "Adding Markers");
				markers.addAll(dRes.getMarkers());

				// Notification
				Toast.makeText(
						mixContext,
						mixContext.getResources().getString(
								R.string.download_received)
								+ " " + dRes.getDataSource().getName(),
						Toast.LENGTH_SHORT).show();
			}
		}
		return markers;
	}

	/**
	 * Handles drawing radar and direction.
	 * 
	 * @param PaintScreen
	 *            screen that radar will be drawn to
	 */
	private void drawRadar(final PaintScreen draw) {
		String dirTxt = ""; // NOPMD by Àðò¸ì on 25.06.13 15:58
		final int bearing = (int) state.getCurBearing();
		final int range = (int) (state.getCurBearing() / (360f / 16f));
		// TODO: get strings from the values xml file
		if (range == 15 || range == 0) {
			dirTxt = getContext().getString(R.string.N);
		} else if (range == 1 || range == 2) {
			dirTxt = getContext().getString(R.string.NE);
		} else if (range == 3 || range == 4) {
			dirTxt = getContext().getString(R.string.E);
		} else if (range == 5 || range == 6) {
			dirTxt = getContext().getString(R.string.SE);
		} else if (range == 7 || range == 8) {
			dirTxt = getContext().getString(R.string.S);
		} else if (range == 9 || range == 10) {
			dirTxt = getContext().getString(R.string.SW);
		} else if (range == 11 || range == 12) {
			dirTxt = getContext().getString(R.string.W);
		} else if (range == 13 || range == 14) {
			dirTxt = getContext().getString(R.string.NW);
		}

		radarPoints.view = this;
		draw.paintObj(radarPoints, RX_COORD, RY_COORD, -state.getCurBearing(),
				1);
		draw.setFill(false);
		draw.setColor(Color.argb(150, 0, 0, 220));
		draw.paintLine(lrl.x, lrl.y, RX_COORD + RadarPoints.RADIUS, RY_COORD
				+ RadarPoints.RADIUS);
		draw.paintLine(rrl.x, rrl.y, RX_COORD + RadarPoints.RADIUS, RY_COORD
				+ RadarPoints.RADIUS);
		draw.setColor(Color.rgb(255, 255, 255));
		draw.setFontSize(12);

		radarText(draw, MixUtils.formatDist(radius * 1000), RX_COORD
				+ RadarPoints.RADIUS, RY_COORD + RadarPoints.RADIUS * 2 - 10,
				false);
		radarText(draw, " " + bearing + ((char) 176) + " " + dirTxt, RX_COORD
				+ RadarPoints.RADIUS, RY_COORD - 5, true);
	}

	private void handleKeyEvent(final KeyEvent evt) {
		/** Adjust marker position with keypad */

		switch (evt.keyCode) {
		case KEYCODE_DPAD_LEFT:
			addX -= CONST;
			break;
		case KEYCODE_DPAD_RIGHT:
			addX += CONST;
			break;
		case KEYCODE_DPAD_DOWN:
			addY += CONST;
			break;
		case KEYCODE_DPAD_UP:
			addY -= CONST;
			break;
		case KEYCODE_DPAD_CENTER:
			setFrozen(!frozen);
			break;
		case KEYCODE_CAMERA:
			setFrozen(!frozen);
			break; // freeze the overlay with the camera button
		default: // if key is set, then ignore event
			break;
		}
	}

	public boolean handleClickEvent(final ClickEvent evt) {
		boolean evtHandled = false;

		// Handle event
		if (state.nextLStatus == MixState.done) {
			// the following will traverse the markers in ascending order (by
			// distance) the first marker that
			// matches triggers the event.
			// TODO handle collection of markers. (what if user wants the one at
			// the back)
			for (int i = 0; i < dataHandler.getMarkerCount() && !evtHandled; i++) {
				final Marker pMarker = dataHandler.getMarker(i);

				evtHandled = pMarker.fClick(evt.xCoord, evt.yCoord, mixContext,
						state);
			}
		}
		return evtHandled;
	}

	private void radarText(final PaintScreen draw, final String txt,
			final float xCoord, final float yCoord, final boolean isbackGrnd) {

		final float width = draw.getTextWidth(txt) + PAD_W * 2;
		final float height = draw.getTextAsc() + draw.getTextDesc() + PAD_H * 2;
		if (isbackGrnd) {
			draw.setColor(Color.rgb(0, 0, 0));
			draw.setFill(true);
			draw.paintRect(xCoord - width / 2, yCoord - height / 2, width,
					height);
			draw.setColor(Color.rgb(255, 255, 255));
			draw.setFill(false);
			draw.paintRect(xCoord - width / 2, yCoord - height / 2, width,
					height);
		}
		draw.paintText(PAD_W + xCoord - width / 2, PAD_H + draw.getTextAsc()
				+ yCoord - height / 2, txt, false);
	}

	public void clickEvent(final float xCoord, final float yCoord) {
		synchronized (uiEvents) {
			uiEvents.add(new ClickEvent(xCoord, yCoord));
		}
	}

	public void keyEvent(final int keyCode) {
		synchronized (uiEvents) {
			uiEvents.add(new KeyEvent(keyCode));
		}
	}

	public void clearEvents() {
		synchronized (uiEvents) {
			uiEvents.clear();
		}
	}

	public void cancelRefreshTimer() {
		if (refresh != null) {
			refresh.cancel();
		}
	}

	/**
	 * Re-downloads the markers, and draw them on the map.
	 */
	public void refresh() {
		state.nextLStatus = MixState.notStarted;
	}

	private void callRefreshToast() {
		mixContext.getActualMixView().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(
						mixContext,
						mixContext.getResources()
								.getString(R.string.refreshing),
						Toast.LENGTH_SHORT).show();
			}
		});
	}

}

class UIEvent {
	public static final int CLICK = 0;
	public static final int KEY = 1;

	public int type;
}

class ClickEvent extends UIEvent {
	public ClickEvent() {
		super();
		Log.d("constructor", "Call Click event constructor");
	}

	public transient float xCoord, yCoord;

	public ClickEvent(final float xCoord, final float yCoord) {
		super();
		this.type = CLICK;
		this.xCoord = xCoord;
		this.yCoord = yCoord;
	}

	@Override
	public String toString() {
		return "(" + xCoord + "," + yCoord + ")";
	}
}

class KeyEvent extends UIEvent {
	public transient int keyCode;

	public KeyEvent(final int keyCode) {
		super();
		this.type = KEY;
		this.keyCode = keyCode;
	}

	@Override
	public String toString() {
		return "(" + keyCode + ")";
	}
}
