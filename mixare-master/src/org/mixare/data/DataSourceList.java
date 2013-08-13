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
package org.mixare.data;

import java.util.ArrayList;
import java.util.List;

import org.mixare.R;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DataSourceList extends ListActivity {

	public static final String SHARED_PREFS = "DataSourcesPrefs";
	private static DataSourceAdapter dataSourceAdapter;

	private static final int MENU_CREATE_ID = Menu.FIRST;
	private static final int MENU_EDIT_ID = Menu.FIRST + 1;
	private static final int MENU_DELETE_ID = Menu.FIRST + 2;

	@Override
	protected void onResume() {
		super.onResume();

		DataSourceStorage.getInstance().fillDefaultDataSources();

		final int size = DataSourceStorage.getInstance().getSize();

		// copy the value from shared preference to adapter
		dataSourceAdapter = new DataSourceAdapter();
		for (int i = 0; i < size; i++) {
			final String fields[] = DataSourceStorage.getInstance()
					.getFields(i);
			dataSourceAdapter.addItem(new DataSource(fields[0], fields[1], // NOPMD
																			// by
																			// Àðò¸ì
																			// on
																			// 25.06.13
																			// 13:13
					fields[2], fields[3], fields[4]));
		}
		setListAdapter(dataSourceAdapter);
		final ListView listView = getListView();
		registerForContextMenu(listView);
	}

	@Override
	protected void onPause() {
		super.onPause();
		DataSourceStorage.getInstance().clear();
		// every URL in Adapter
		// put the URL link and status inside the Shared Preference
		for (int k = 0; k < dataSourceAdapter.getCount(); k++) {
			DataSourceStorage.getInstance().add("DataSource" + k,
					dataSourceAdapter.serialize(k));
		}
	}

	// TODO: check if it's really needed
	public static String getDataSourcesStringList() {

		final StringBuilder builder = new StringBuilder();
		boolean first = true; // NOPMD by Àðò¸ì on 25.06.13 13:19

		for (int i = 0; i < dataSourceAdapter.getCount(); i++) {
			if (dataSourceAdapter.getItemEnabled(i)) {
				if (!first) {
					builder.append(", ");

				}
				builder.append(dataSourceAdapter.getItemName(i));
				first = false; // NOPMD by Àðò¸ì on 25.06.13 13:19
			}
		}

		return builder.toString();
	}

	private class DataSourceAdapter extends BaseAdapter implements
			OnCheckedChangeListener {

		private transient final List<DataSource> DATA_SOURCES = new ArrayList<DataSource>();
		private transient final LayoutInflater INFLATER;

		public DataSourceAdapter() {
			super();
			INFLATER = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public boolean getItemEnabled(final int key) {
			return DATA_SOURCES.get(key).isEnabled();
		}

		public String getItemName(final int key) {
			return DATA_SOURCES.get(key).getName();

		}

		public String serialize(final int key) {
			return DATA_SOURCES.get(key).serialize();
		}

		public void addItem(final DataSource item) {
			DATA_SOURCES.add(item);
			notifyDataSetChanged();
		}

		public void deleteItem(final int dataID) {
			if (DATA_SOURCES.get(dataID).isEnabled()) {
				DATA_SOURCES.get(dataID).setEnabled(false);
				notifyDataSetChanged();
			}
			DATA_SOURCES.remove(dataID);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return DATA_SOURCES.size();
		}

		@Override
		public long getItemId(final int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) { // NOPMD
																				// by
																				// Àðò¸ì
																				// on
																				// 24.06.13
																				// 11:37
			ViewHolder holder = null; // NOPMD by Àðò¸ì on 25.06.13 13:19

			if (convertView == null) {
				convertView = INFLATER.inflate(R.layout.datasourcelist, null);
				holder = new ViewHolder();
				holder.text = (TextView) convertView
						.findViewById(R.id.list_text);
				holder.description = (TextView) convertView
						.findViewById(R.id.description_text);
				holder.checkbox = (CheckBox) convertView
						.findViewById(R.id.list_checkbox);
				holder.checkbox.setTag(position);
				holder.checkbox.setOnCheckedChangeListener(this);
				holder.datasourceIcon = (ImageView) convertView
						.findViewById(R.id.datasource_icon);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.text.setText(DATA_SOURCES.get(position).getName());
			holder.description.setText(DATA_SOURCES.get(position).getUrl());

			holder.datasourceIcon.setImageResource(DATA_SOURCES.get(position)
					.getDataSourceIcon());
			holder.checkbox.setChecked(DATA_SOURCES.get(position).isEnabled());

			return convertView;
		}

		@Override
		public void onCheckedChanged(final CompoundButton buttonView,
				final boolean isChecked) {
			final int position = (Integer) buttonView.getTag();
			if (isChecked) {
				buttonView.setChecked(true);
			} else {
				buttonView.setChecked(false);
			}
			DATA_SOURCES.get(position).setEnabled(isChecked);
		}

		@Override
		public Object getItem(final int arg0) {
			return null;
		}

		private class ViewHolder {

			public ViewHolder() {
				super();
				Log.d("constructor", "Call ViewHolder");
			}

			private TextView text;
			private TextView description;
			private CheckBox checkbox;
			private ImageView datasourceIcon;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		menu.add(MENU_CREATE_ID, MENU_CREATE_ID, MENU_CREATE_ID,
				R.string.data_source_add);
		return super.onCreateOptionsMenu(menu);

	}

	@Override
	public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
		final int itemId = item.getItemId();
		if (itemId == MENU_CREATE_ID) {
			final Intent addDataSource = new Intent(this, DataSource.class);
			startActivity(addDataSource);
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View view,
			final ContextMenuInfo menuInfo) {
		menu.add(MENU_EDIT_ID, MENU_EDIT_ID, MENU_EDIT_ID,
				R.string.data_source_edit);
		menu.add(MENU_DELETE_ID, MENU_DELETE_ID, MENU_DELETE_ID,
				R.string.data_source_delete);
		super.onCreateContextMenu(menu, view, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo(); // NOPMD
																			// by
																			// Àðò¸ì
																			// on
																			// 25.06.13
																			// 13:19
		} catch (ClassCastException e) {
			return false; // NOPMD by Àðò¸ì on 25.06.13 13:12
		}
		final long idOfMenu = getListAdapter().getItemId(info.position);
		final int itemId = item.getItemId();
		if (itemId == MENU_EDIT_ID) {
			if (idOfMenu <= 3) {
				Toast.makeText(this, getString(R.string.data_source_edit_err),
						Toast.LENGTH_SHORT).show();
			} else {
				final Intent editDataSource = new Intent(this, DataSource.class);
				editDataSource.putExtra("DataSourceId", (int) idOfMenu);
				startActivity(editDataSource);
			}
		} else if (itemId == MENU_DELETE_ID) {
			if (idOfMenu <= 3) {
				Toast.makeText(this,
						getString(R.string.data_source_delete_err),
						Toast.LENGTH_SHORT).show();
			} else {
				dataSourceAdapter.deleteItem((int) idOfMenu);
			}
		}
		return super.onContextItemSelected(item);
	}

}
