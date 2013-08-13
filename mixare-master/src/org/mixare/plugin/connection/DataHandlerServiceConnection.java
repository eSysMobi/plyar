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
package org.mixare.plugin.connection;

import org.mixare.data.convert.DataConvertor;
import org.mixare.lib.service.IDataHandlerService;
import org.mixare.plugin.AbstractConn;
import org.mixare.plugin.remoteobjects.RemoteDataHandler;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public class DataHandlerServiceConnection extends AbstractConn implements
		ServiceConnection {

	@Override
	public void onServiceConnected(final ComponentName name,
			final IBinder service) {
		// get instance of the aidl binder
		final IDataHandlerService iDataHandler = IDataHandlerService.Stub
				.asInterface(service);
		final RemoteDataHandler remoteDataHandler = new RemoteDataHandler(
				iDataHandler);
		remoteDataHandler.buildDataHandler();
		DataConvertor.getInstance().addDataProcessor(remoteDataHandler);
		storeFoundPlugin();
	}

	@Override
	public void onServiceDisconnected(final ComponentName name) {
		DataConvertor.getInstance().clearDataProcessors();
	}

}
