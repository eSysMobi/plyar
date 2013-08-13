/*
 * Copyright (C) 2012- Peer internet solutions 
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
package org.mixare.mgr;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import org.mixare.MixContext;
import org.mixare.mgr.downloader.DownloadRequest;

import android.content.ContentResolver;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

public final class HttpTools { // NOPMD by Àðò¸ì on 24.06.13 14:10

	private static final String IGNORE2 = "ignore";

	/**
	 * Prefered To use InputStream managed!
	 * 
	 * @param request
	 * @param cr
	 * @return
	 * @throws Exception
	 */
	public static String getPageContent(final DownloadRequest request,
			final ContentResolver cr) throws Exception { // NOPMD by Àðò¸ì on
															// 24.06.13 14:03
		String pageContent;
		InputStream inputStream = null; // NOPMD by Àðò¸ì on 24.06.13 14:10
		if (request.getSource().getUrl().startsWith("file://")) {
			inputStream = HttpTools.getHttpGETInputStream(request.getSource()
					.getUrl(), cr);
		} else {
			inputStream = HttpTools.getHttpGETInputStream(request.getSource()
					.getUrl() + request.getParams(), cr);
		}
		pageContent = HttpTools.getHttpInputString(inputStream);
		HttpTools.returnHttpInputStream(inputStream);
		return pageContent;
	}

	public static String getHttpInputString(final InputStream inputStream) {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream), 8 * 1024);
		final StringBuilder stringBuilder = new StringBuilder();

		try {
			String line;
			while ((line = reader.readLine()) != null) { // NOPMD by Àðò¸ì on 24.06.13 14:12
				stringBuilder.append(line + "\n");
			}
		} catch (IOException e) {
			Log.e("Error", "error in adding string");

		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				Log.e("Error", "error in adding string");
			}
		}
		return stringBuilder.toString();
	}

	/**
	 * Input Stream with unsafe close
	 */
	@Deprecated
	public static InputStream getHttpGETInputStream(final String urlStr, // NOPMD
																			// by
																			// Àðò¸ì
																			// on
																			// 24.06.13
																			// 14:10
			final ContentResolver cr) throws Exception { // NOPMD by Àðò¸ì on
															// 24.06.13 14:03
		InputStream inputStream = null; // NOPMD by Àðò¸ì on 24.06.13 14:09
		URLConnection conn = null; // NOPMD by Àðò¸ì on 24.06.13 14:09

		// HTTP connection reuse which was buggy pre-froyo
		if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
			System.setProperty("http.keepAlive", "false");
		}

		if (urlStr.startsWith("file://")) {
			return new FileInputStream(urlStr.replace("file://", "")); // NOPMD
		} // by
			// Àðò¸ì
			// on
			// 24.06.13
			// 14:03

		if (urlStr.startsWith("content://")) {
			return getContentInputStream(urlStr, null, cr); // NOPMD by Àðò¸ì on
		} // 24.06.13 14:03

		if (urlStr.startsWith("https://")) {
			HttpsURLConnection
					.setDefaultHostnameVerifier(new HostnameVerifier() {
						public boolean verify(final String hostname,
								final SSLSession session) {
							return true;
						}
					});
			final SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, new X509TrustManager[] { new X509TrustManager() {
				public void checkClientTrusted(final X509Certificate[] chain,
						final String authType) throws CertificateException {
					Log.d("method", "checkClientTrusted");
				}

				public void checkServerTrusted(final X509Certificate[] chain,
						final String authType) throws CertificateException {
					Log.d("method", "checkServerTrusted");
				}

				public X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}
			} }, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(context
					.getSocketFactory());
		}

		try {
			final URL url = new URL(urlStr);
			conn = url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(10000);

			inputStream = conn.getInputStream();

			return inputStream;
		} catch (Exception ex) {
			try {
				inputStream.close();
			} catch (Exception ignore) {
				Log.w(MixContext.TAG, "Error on url " + urlStr, ignore);
			}
			try {
				if (conn instanceof HttpURLConnection) {
					((HttpURLConnection) conn).disconnect();
				}

			} catch (Exception ignore) {
				Log.d(IGNORE2, IGNORE2);
			}
			throw ex;
		}
	}

	/**
	 * Input Stream with unsafe close
	 */
	@Deprecated
	public static InputStream getHttpPOSTInputStream(final String urlStr,
			final String params, final ContentResolver cr) throws Exception { // NOPMD
																				// by
																				// Àðò¸ì
																				// on
																				// 24.06.13
																				// 14:03
		InputStream inputStream = null; // NOPMD by Àðò¸ì on 24.06.13 14:10
		OutputStream outputStream = null; // NOPMD by Àðò¸ì on 24.06.13 14:10
		HttpURLConnection conn = null; // NOPMD by Àðò¸ì on 24.06.13 14:09

		if (urlStr.startsWith("content://")) {
			return getContentInputStream(urlStr, params, cr); // NOPMD by Àðò¸ì
		} // on 24.06.13
			// 14:03

		try {
			final URL url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(10000);

			if (params != null) {
				conn.setDoOutput(true);
				outputStream = conn.getOutputStream();
				final OutputStreamWriter writer = new OutputStreamWriter(
						outputStream);
				writer.write(params);
				writer.close();
			}

			inputStream = conn.getInputStream();

			return inputStream; // NOPMD by Àðò¸ì on 24.06.13 14:03
		} catch (Exception ex) {

			try {
				inputStream.close();
			} catch (Exception ignore) {
				Log.d(IGNORE2, IGNORE2);
			}
			try {
				outputStream.close();
			} catch (Exception ignore) {
				Log.d(IGNORE2, IGNORE2);
			}
			try {
				conn.disconnect();
			} catch (Exception ignore) {
				Log.d(IGNORE2, IGNORE2);
			}

			if (conn != null && conn.getResponseCode() == 405) {
				return getHttpGETInputStream(urlStr, cr);
			} else {
				throw ex;
			}
		}
	}

	/**
	 * Input Stream with unsafe close
	 */
	@Deprecated
	public static InputStream getContentInputStream(final String urlStr,
			final String params, final ContentResolver cr) throws Exception { // NOPMD
																				// by
																				// Àðò¸ì
																				// on
																				// 24.06.13
																				// 14:03
		// ContentResolver cr = mixView.getContentResolver();
		final Cursor cur = cr
				.query(Uri.parse(urlStr), null, params, null, null);

		cur.moveToFirst();
		final int mode = cur.getInt(cur.getColumnIndex("MODE"));

		if (mode == 1) {
			final String result = cur.getString(cur.getColumnIndex("RESULT"));
			cur.deactivate();

			return new ByteArrayInputStream(result.getBytes());
		} else {
			cur.deactivate();

			throw new Exception("Invalid content:// mode " + mode);
		}
	}

	/**
	 * Input Stream management not safe
	 */
	@Deprecated
	public static void returnHttpInputStream(final InputStream inputStream)
			throws Exception { // NOPMD by Àðò¸ì on 24.06.13 14:03
		if (inputStream != null) {
			inputStream.close();
		}
	}

	/**
	 * Input Stream management not safe
	 */
	@Deprecated
	public InputStream getResourceInputStream(final String name,
			final AssetManager mgr) throws Exception { // NOPMD by Àðò¸ì on
														// 24.06.13 14:07
		// AssetManager mgr = mixView.getAssets();
		return mgr.open(name);
	}

	/**
	 * Input Stream management not safe
	 */
	@Deprecated
	public static void returnResourceInputStream(final InputStream inputStream)
			throws Exception { // NOPMD by Àðò¸ì on 24.06.13 14:07
		if (inputStream != null) {
			inputStream.close();
		}
	}

}
