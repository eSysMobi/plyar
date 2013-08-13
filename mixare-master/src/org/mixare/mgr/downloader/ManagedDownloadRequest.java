/*
 * Copyleft 2012 - Alessandro Staniscia
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

class ManagedDownloadRequest {
	private static final int PRIME = 31;

	private transient final DownloadRequest CONTENT;

	private transient final String UNIQUE_KEY;

	public ManagedDownloadRequest(final DownloadRequest content) {
		this.CONTENT = content;
		this.UNIQUE_KEY = "" + System.currentTimeMillis() + "_" + hashCode(); // NOPMD
																				// by
																				// Àðò¸ì
																				// on
																				// 24.06.13
																				// 12:00
	}

	public DownloadRequest getOriginalRequest() {
		return CONTENT;
	}

	public String getUniqueKey() {
		return UNIQUE_KEY;
	}

	@Override
	public int hashCode() {

		int result = 1; // NOPMD by Àðò¸ì on 24.06.13 13:53
		result = PRIME * result + ((CONTENT == null) ? 0 : CONTENT.hashCode());
		result = PRIME * result
				+ ((UNIQUE_KEY == null) ? 0 : UNIQUE_KEY.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true; // NOPMD by Àðò¸ì on 24.06.13 13:54
		}

		if (obj == null) {
			return false; // NOPMD by Àðò¸ì on 24.06.13 13:54
		}
		if (getClass() != obj.getClass()) {
			return false; // NOPMD by Àðò¸ì on 24.06.13 13:54
		}
		final ManagedDownloadRequest other = (ManagedDownloadRequest) obj;
		return getOriginalRequest().getSource().getName()
				.equals(other.getOriginalRequest().getSource().getName());
	}

}
