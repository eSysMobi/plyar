/*
 * Copyleft 2012 - Peer internet solutions 
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
package org.mixare.mgr.datasource;

import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.mixare.MixContext;
import org.mixare.data.DataSource;
import org.mixare.data.DataSourceStorage;
import org.mixare.mgr.downloader.DownloadRequest;

class DataSourceMgrImpl implements DataSourceManager {

	private transient final ConcurrentLinkedQueue<DataSource> allDataSources = new ConcurrentLinkedQueue<DataSource>();

	private transient final MixContext ctx;

	public DataSourceMgrImpl(final MixContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public boolean isAtLeastOneDatasourceSelected() {
		boolean atLeastOneDatasourceSelected = false; // NOPMD by Àðò¸ì on
														// 24.06.13 14:03
		for (DataSource ds : this.allDataSources) {
			if (ds.isEnabled()) {
				atLeastOneDatasourceSelected = true; // NOPMD by Àðò¸ì on
														// 24.06.13 14:03
			}
		}
		return atLeastOneDatasourceSelected;
	}

	public void setAllDataSourcesforLauncher(final DataSource datasource) {
		this.allDataSources.clear(); // TODO WHY? CLEAN ALL
		this.allDataSources.add(datasource);
	}

	public void refreshDataSources() {
		this.allDataSources.clear();

		DataSourceStorage.getInstance(ctx).fillDefaultDataSources();

		final int size = DataSourceStorage.getInstance().getSize();

		// copy the value from shared preference to adapter
		for (int i = 0; i < size; i++) {
			final String fields[] = DataSourceStorage.getInstance()
					.getFields(i);
			this.allDataSources.add(new DataSource(fields[0], fields[1], // NOPMD by Àðò¸ì on 24.06.13 14:03
					fields[2], fields[3], fields[4]));
		}
	}

	public void requestDataFromAllActiveDataSource(final double lat,
			final double lon, final double alt, final float radius) {
		for (DataSource ds : allDataSources) {
			/*
			 * when type is OpenStreetMap iterate the URL list and for selected
			 * URL send data request
			 */
			if (ds.isEnabled()) {
				requestData(ds, lat, lon, alt, radius, Locale.getDefault()
						.getLanguage());
			}
		}

	}

	private void requestData(final DataSource datasource, final double lat,
			final double lon, final double alt, final float radius,
			final String locale) {
		final DownloadRequest request = new DownloadRequest(datasource,
				datasource.createRequestParams(lat, lon, alt, radius, locale));
		ctx.getDownloadManager().submitJob(request);

	}

}
