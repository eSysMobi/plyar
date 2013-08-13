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
package org.mixare.mgr.downloader;

import java.util.ArrayList;
import java.util.List;

import org.mixare.data.DataSource;
import org.mixare.lib.marker.Marker;

public class DownloadResult {

	private DataSource dataSource;
	private String params;
	private List<Marker> markers;

	private boolean error;
	private String errorMsg = "";
	private DownloadRequest errorRequest;
	private transient String idDownload;

	public DownloadResult() {
		super();
		this.dataSource = null; // NOPMD by Àðò¸ì on 24.06.13 13:57
		this.params = "";
		this.markers = new ArrayList<Marker>();
		this.error = true;
		this.errorMsg = "DUMMY OBJECT";
		this.errorRequest = null; // NOPMD by Àðò¸ì on 24.06.13 13:57
		this.idDownload = "";
	}

	public String getIdOfDownloadRequest() {
		return idDownload;
	}

	public void setIdOfDownloadRequest(final String idRequest) {
		idDownload = idRequest;
	}

	public List<Marker> getMarkers() {
		return markers;
	}

	public void setMarkers(final List<Marker> markers) {
		this.markers = markers;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(final DataSource source) {
		this.dataSource = source;
	}

	public String getParams() {
		return params;
	}

	public void setParams(final String params) {
		this.params = params;
	}

	public boolean isError() {
		return error;
	}

	public void setError(final boolean error) {
		this.error = error;
		if (!error) {
			errorMsg = "";
		}
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(final String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public DownloadRequest getErrorRequest() {
		return errorRequest;
	}

	public void setErrorRequest(final DownloadRequest errorRequest) {
		this.errorRequest = errorRequest;
	}

	public void setError(final Exception exception,
			final DownloadRequest request) {
		error = true;
		errorMsg = exception.getMessage();
		errorRequest = request;
	}

	public void setAccomplish(final String idDownload,
			final List<Marker> markers, final DataSource dataSource) {
		setIdOfDownloadRequest(idDownload);
		setMarkers(markers);
		setDataSource(dataSource);
		setError(false);
		errorMsg = "NO ERROR";
		errorRequest = null; // NOPMD by Àðò¸ì on 24.06.13 13:57
	}

}
