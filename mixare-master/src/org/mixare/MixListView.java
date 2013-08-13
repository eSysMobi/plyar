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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.mixare.data.DataHandler;
import org.mixare.data.DataSourceList;
import org.mixare.lib.MixUtils;
import org.mixare.lib.marker.Marker;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This class holds vectors with informaction about sources, their description
 * and whether they have been selected.
 */
public class MixListView extends ListActivity {

	private transient List<SpannableString> listViewMenu;
	private transient List<String> selectedItemURL;
	private transient List<String> dataSourceMenu;
	private transient List<String> sourceDescription;
	private transient List<Boolean> dataSourceChecked;
	private transient List<Integer> dataSourceIcon;
	private transient DataView dataView;

	/*
	 * private MixContext mixContext; private ListItemAdapter adapter; private
	 * static Context ctx;
	 */
	private transient static String searchQuery = "";
	private transient static SpannableString underlinedTitle;
	public transient static List<Marker> resultMarkers;
	public transient static List<Marker> markerList;

	public List<String> getDataSourceMenu() {
		return dataSourceMenu;
	}

	public List<String> getDataSourceDescription() {
		return sourceDescription;
	}

	public List<Boolean> getDataSourceChecked() {
		return dataSourceChecked;
	}

	public List<Integer> getDataSourceIcon() {
		return dataSourceIcon;
	}

	@Override
	public void onCreate(final Bundle state) {
		super.onCreate(state);
		dataView = MixView.getDataView();

		selectedItemURL = new ArrayList<String>();
		listViewMenu = new ArrayList<SpannableString>();
		final DataHandler jLayer = dataView.getDataHandler();
		if (dataView.isFrozen() && jLayer.getMarkerCount() > 0) {
			selectedItemURL.add("search");
		}
		/* add all marker items to a title and a URL Vector */
		for (int i = 0; i < jLayer.getMarkerCount(); i++) {
			final Marker marker = jLayer.getMarker(i);
			if (marker.isActive()) {
				if (marker.getURL() == null) {
					listViewMenu.add(new SpannableString(marker.getTitle())); // NOPMD
																				// by
																				// Àðò¸ì
																				// on
																				// 25.06.13
																				// 14:47
				} else {
					/* Underline the title if website is available */
					underlinedTitle = new SpannableString(marker.getTitle()); // NOPMD
																				// by
																				// Àðò¸ì
																				// on
																				// 25.06.13
																				// 14:47
					underlinedTitle.setSpan(new UnderlineSpan(), 0, // NOPMD by
																	// Àðò¸ì on
																	// 25.06.13
																	// 14:47
							underlinedTitle.length(), 0);
					listViewMenu.add(underlinedTitle);
				}
				/* the website for the corresponding title */
				if (marker.getURL() == null) {
					selectedItemURL.add("");
				} else {
					selectedItemURL.add(marker.getURL());
				}
			}

			if (dataView.isFrozen()) {

				final TextView notificationTxt = new TextView(this); // NOPMD by
																		// Àðò¸ì
																		// on
																		// 25.06.13
																		// 14:47
				notificationTxt.setVisibility(View.VISIBLE);
				notificationTxt.setText(getString(R.string.search_active_1)
						+ " " + DataSourceList.getDataSourcesStringList()
						+ getString(R.string.search_active_2));
				notificationTxt.setWidth(MixView.getdWindow().getWidth());

				notificationTxt.setPadding(10, 2, 0, 0);
				notificationTxt.setBackgroundColor(Color.DKGRAY);
				notificationTxt.setTextColor(Color.WHITE);

				getListView().addHeaderView(notificationTxt);

			}

			setListAdapter(new ArrayAdapter<SpannableString>(this, // NOPMD by
																	// Àðò¸ì on
																	// 25.06.13
																	// 14:47
					android.R.layout.simple_list_item_1, listViewMenu));
			getListView().setTextFilterEnabled(true);
			break;

		}
	}

	private void handleIntent(final Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			final String query = intent.getStringExtra(SearchManager.QUERY);
			doMixSearch(query);
		}
	}

	@Override
	protected void onNewIntent(final Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}

	@SuppressLint("DefaultLocale")
	@SuppressWarnings("deprecation")
	private void doMixSearch(final String query) {
		final DataHandler jLayer = dataView.getDataHandler();
		if (!dataView.isFrozen()) {
			markerList = jLayer.getMarkerList();
			MixMap.originalMarkerList = jLayer.getMarkerList();
		}
		markerList = jLayer.getMarkerList();
		resultMarkers = new ArrayList<Marker>();
		Log.d("SEARCH-------------------0", " " + query);
		setSearchQuery(query);

		selectedItemURL = new ArrayList<String>();
		listViewMenu = new ArrayList<SpannableString>();
		for (int i = 0; i < jLayer.getMarkerCount(); i++) {
			final Marker marker = jLayer.getMarker(i);

			if (marker.getTitle().toLowerCase(Locale.getDefault())
					.indexOf(searchQuery.toLowerCase(Locale.getDefault())) != -1) {

				resultMarkers.add(marker);
				listViewMenu.add(new SpannableString(marker.getTitle())); // NOPMD
																			// by
																			// Àðò¸ì
																			// on
																			// 25.06.13
																			// 14:48
				/* the website for the corresponding title */
				if (marker.getURL() == null) {
					selectedItemURL.add("");
				} else {
					selectedItemURL.add(marker.getURL());
				}
			}
		}
		if (listViewMenu.size() == 0) { // NOPMD by Àðò¸ì on 25.06.13 14:49
			Toast.makeText(this,
					getString(R.string.search_failed_notification),
					Toast.LENGTH_LONG).show();
		} else {
			jLayer.setMarkerList(resultMarkers);
			dataView.setFrozen(true);
			finish();
			final Intent intent1 = new Intent(this, MixListView.class);
			startActivityForResult(intent1, 42);
		}
	}

	@Override
	protected void onListItemClick(final ListView listView, final View view,
			final int position, final long itemID) {
		super.onListItemClick(listView, view, position, itemID);
		clickOnListView(position);
	}

	@SuppressWarnings("deprecation")
	public void clickOnListView(final int position) {
		/* if no website is available for this item */
		final String selectedURL = position < selectedItemURL.size() ? selectedItemURL
				.get(position) : null; // NOPMD by Àðò¸ì on 25.06.13 14:47
		if (selectedURL == null || selectedURL.length() <= 0) {
			Toast.makeText(this, getString(R.string.no_website_available),
					Toast.LENGTH_LONG).show();
		} else if ("search".equals(selectedURL)) {
			dataView.setFrozen(false);
			dataView.getDataHandler().setMarkerList(markerList);
			finish();
			final Intent intent1 = new Intent(this, MixListView.class);
			startActivityForResult(intent1, 42);
		} else {
			try {
				if (selectedURL.startsWith("webpage")) {
					final String newUrl = MixUtils.parseAction(selectedURL);
					dataView.getContext().getWebContentManager()
							.loadWebPage(newUrl, this);
				}
			} catch (Exception e) {
				Log.e("error", "lisView item click error");
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		final int base = Menu.FIRST;

		/* define menu items */
		final MenuItem item1 = menu.add(base, base, base,
				getString(R.string.menu_item_3));
		final MenuItem item2 = menu.add(base, base + 1, base + 1,
				getString(R.string.map_menu_cam_mode));
		/* assign icons to the menu items */
		item1.setIcon(android.R.drawable.ic_menu_mapmode);
		item2.setIcon(android.R.drawable.ic_menu_camera);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		final int itemId = item.getItemId();
		if (itemId == 1) {
			createMixMap();
			finish();
		} else if (itemId == 2) {
			finish();
		}
		return true;
	}

	public void createMixMap() {
		final Intent intent2 = new Intent(MixListView.this, MixMap.class);
		startActivityForResult(intent2, 20);
	}

	public static String getSearchQuery() {
		return searchQuery;
	}

	public static void setSearchQuery(final String query) {
		searchQuery = query;
	}
}

/**
 * The ListItemAdapter is can store properties of list items, like background or
 * text color
 */
class ListItemAdapter extends BaseAdapter {

	private transient final MixListView mixListView;

	private transient final LayoutInflater myInflater;
	private static ViewHolder holder;
	private transient int[] bgcolors = new int[] { 0, 0, 0, 0, 0 };
	private transient int[] textcolors = new int[] { Color.WHITE, Color.WHITE,
			Color.WHITE, Color.WHITE, Color.WHITE };
	private transient final int[] DESC_COLORS = new int[] { Color.GRAY,
			Color.GRAY, Color.GRAY, Color.GRAY, Color.GRAY };

	public static int itemPosition = 0;

	public ListItemAdapter(final MixListView mixListView) {
		super();
		this.mixListView = mixListView;
		myInflater = LayoutInflater.from(mixListView);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) { // NOPMD
																					// by
																					// Àðò¸ì
																					// on
																					// 24.06.13
																					// 10:50
		itemPosition = position;
		if (convertView == null) {
			convertView = myInflater.inflate(R.layout.main, null);

			holder = new ViewHolder();
			holder.text = (TextView) convertView.findViewById(R.id.list_text);
			holder.description = (TextView) convertView
					.findViewById(R.id.description_text);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.text.setPadding(20, 8, 0, 0);
		holder.description.setPadding(20, 40, 0, 0);

		holder.text.setText(mixListView.getDataSourceMenu().get(position));
		holder.description.setText(mixListView.getDataSourceDescription().get(
				position));

		final int colorPos = position % bgcolors.length;
		convertView.setBackgroundColor(bgcolors[colorPos]);
		holder.text.setTextColor(textcolors[colorPos]);
		holder.description.setTextColor(DESC_COLORS[colorPos]);

		return convertView;
	}

	public void changeColor(final int index, final int bgcolor,
			final int textcolor) {
		if (index < bgcolors.length) {
			bgcolors[index] = bgcolor;
			textcolors[index] = textcolor;
		} else {
			Log.d("Color Error", "too large index");
		}
	}

	public void colorSource(final String source) {
		for (int i = 0; i < bgcolors.length; i++) {
			bgcolors[i] = 0;
			textcolors[i] = Color.WHITE;
		}

		if ("Wikipedia".equals(source)) {
			changeColor(0, Color.WHITE, Color.DKGRAY);
		} else if ("Twitter".equals(source)) {
			changeColor(1, Color.WHITE, Color.DKGRAY);
		} else if ("Buzz".equals(source)) {
			changeColor(2, Color.WHITE, Color.DKGRAY);
		} else if ("OpenStreetMap".equals(source)) {
			changeColor(3, Color.WHITE, Color.DKGRAY);
		} else if ("OwnURL".equals(source)) {
			changeColor(4, Color.WHITE, Color.DKGRAY);
		} else if ("ARENA".equals(source)) {
			changeColor(5, Color.WHITE, Color.DKGRAY);
		}
	}

	@Override
	public int getCount() {
		return mixListView.getDataSourceMenu().size();
	}

	@Override
	public Object getItem(final int position) {
		return this;
	}

	@Override
	public long getItemId(final int position) {
		return position;
	}

	private class ViewHolder {
		public ViewHolder() {
			super();
			Log.d("constructor ", "Call ViewHolder constructor");
		}

		private TextView text;
		private TextView description;
	}
}
