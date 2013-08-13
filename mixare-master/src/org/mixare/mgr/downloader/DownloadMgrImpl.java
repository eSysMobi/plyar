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
package org.mixare.mgr.downloader;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.mixare.MixContext;
import org.mixare.MixView;
import org.mixare.data.convert.DataConvertor;
import org.mixare.lib.marker.Marker;
import org.mixare.mgr.HttpTools;

import android.util.Log;

class DownloadMgrImpl implements Runnable, DownloadManager { // NOPMD by Àðò¸ì on 24.06.13 13:57

	private transient boolean stop = false;
	private transient final MixContext CTX;
	private transient DownloadManagerState state = DownloadManagerState.Confused;
	private transient final LinkedBlockingQueue<ManagedDownloadRequest> TODO_LIST = new LinkedBlockingQueue<ManagedDownloadRequest>();
	private transient final ConcurrentHashMap<String, DownloadResult> DONE_LIST = new ConcurrentHashMap<String, DownloadResult>();

	private transient final Executor EXECUTOR = Executors
			.newSingleThreadExecutor();

	public DownloadMgrImpl(final MixContext ctx) {
		if (ctx == null) {
			throw new IllegalArgumentException("Mix Context IS NULL");
		}
		this.CTX = ctx;
		state = DownloadManagerState.OffLine;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mixare.mgr.downloader.DownloadManager#run()
	 */
	public void run() {
		ManagedDownloadRequest mRequest;
		DownloadResult result;
		stop = false;
		while (!stop) {
			state = DownloadManagerState.OnLine;
			// Wait for proceed
			while (!stop) {
				try {
					mRequest = TODO_LIST.take();
					state = DownloadManagerState.Downloading;
					result = processRequest(mRequest); // NOPMD by Àðò¸ì on
														// 24.06.13 13:57
				} catch (InterruptedException e) {
					result = new DownloadResult(); // NOPMD by Àðò¸ì on 24.06.13
													// 13:57
					result.setError(e, null);
				}
				DONE_LIST.put(result.getIdOfDownloadRequest(), result);
				state = DownloadManagerState.OnLine;
			}
		}
		state = DownloadManagerState.OffLine;
	}

	private DownloadResult processRequest(final ManagedDownloadRequest mRequest) {
		final DownloadRequest request = mRequest.getOriginalRequest();
		final DownloadResult result = new DownloadResult(); // NOPMD by Àðò¸ì on
															// 24.06.13 13:57
		try {
			if (request == null) {
				throw new Exception("Request is null"); // NOPMD by Àðò¸ì on
														// 24.06.13 11:55
			}

			if (!request.getSource().isWellFormed()) {
				throw new Exception("Datasource in not WellFormed"); // NOPMD by
																		// Àðò¸ì
																		// on
																		// 24.06.13
																		// 11:55
			}

			final String pageContent = HttpTools.getPageContent(request,
					CTX.getContentResolver());

			if (pageContent != null) {
				// try loading Marker data
				final List<Marker> markers = DataConvertor.getInstance().load(
						request.getSource().getUrl(), pageContent,
						request.getSource());
				result.setAccomplish(mRequest.getUniqueKey(), markers,
						request.getSource());
			}
		} catch (Exception ex) {
			result.setError(ex, request);
			Log.w(MixContext.TAG, "ERROR ON DOWNLOAD REQUEST", ex);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mixare.mgr.downloader.DownloadManager#purgeLists()
	 */
	public synchronized void resetActivity() { // NOPMD by Àðò¸ì on 24.06.13
												// 13:57
		TODO_LIST.clear();
		DONE_LIST.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mixare.mgr.downloader.DownloadManager#submitJob(org.mixare.mgr.downloader
	 * .DownloadRequest)
	 */
	public String submitJob(final DownloadRequest job) {
		String jobId = null; // NOPMD by Àðò¸ì on 24.06.13 13:57
		if (job != null && job.getSource().isWellFormed()) {
			ManagedDownloadRequest mJob;
			if (!TODO_LIST.contains(job)) {
				mJob = new ManagedDownloadRequest(job);
				TODO_LIST.add(mJob);
				Log.i(MixView.TAG, "Submitted " + job.toString());
				jobId = mJob.getUniqueKey();
			}
		}
		return jobId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mixare.mgr.downloader.DownloadManager#getReqResult(java.lang.String)
	 */
	public DownloadResult getReqResult(final String jobId) {
		final DownloadResult result = DONE_LIST.get(jobId);
		DONE_LIST.remove(jobId);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mixare.mgr.downloader.DownloadManager#getNextResult()
	 */
	public synchronized DownloadResult getNextResult() { // NOPMD by Àðò¸ì on
															// 24.06.13 13:57
		DownloadResult result = null; // NOPMD by Àðò¸ì on 24.06.13 13:57
		if (!DONE_LIST.isEmpty()) {
			final String nextId = DONE_LIST.keySet().iterator().next();
			result = DONE_LIST.get(nextId);
			DONE_LIST.remove(nextId);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mixare.mgr.downloader.DownloadManager#getResultSize()
	 */
	public int getResultSize() {
		return DONE_LIST.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mixare.mgr.downloader.DownloadManager#isDone()
	 */
	public Boolean isDone() {
		return TODO_LIST.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mixare.mgr.downloader.DownloadManager#goOnline()
	 */
	public void switchOn() {
		if (DownloadManagerState.OffLine.equals(getState())) {
			EXECUTOR.execute(this);
		} else {
			Log.i(MixView.TAG, "DownloadManager already started");
		}
	}

	public void switchOff() {
		stop = true;
	}

	@Override
	public DownloadManagerState getState() {
		return state;
	}

}
