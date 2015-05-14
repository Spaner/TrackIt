/*
 * This file is part of Track It!.
 * Copyright (C) 2013 Henrique Malheiro
 * Copyright (C) 2015 Pedro Gomes
 * 
 * TrackIt! is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Track It! is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Track It!. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.henriquemalheiro.trackit.business.utility;

import static com.henriquemalheiro.trackit.business.common.Messages.getMessage;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.params.AuthPNames;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.common.Constants;

public class Connection {
	private static final Logger logger = Logger.getLogger(Connection.class.getName());
	private static Connection connection;
	
	private Connection() {
	}
	
	public static Connection getInstance() {
		if (connection == null) {
			connection = new Connection();
		}
		
		return connection;
	}
	
	public BufferedImage getImageFromURL(String url) {
		try {
			InputStream resource = getResource(url);
			
			if (resource == null) {
				logger.debug(getMessage("connection.errorReadingResource"));
				return null;
			}
			
			BufferedImage image = ImageIO.read(resource);
			resource.close();
			
			return image;
		} catch (IOException e) {
			logger.debug(getMessage("connection.errorReadingResource"), e);
		}

		return null;
	}
	
	public InputStream getResource(String url) {
		ConnectionProperties connectionProperties = getConnectionProperties();

		String urlx = url;
		boolean secureConnection = false;

		if (urlx.startsWith("http://")) {
			urlx = urlx.replace("http://", "");
		} else if (urlx.startsWith("https://")) {
			urlx = urlx.replace("https://", "");
			secureConnection = true;
		}
		
		int p = urlx.indexOf("/");
		String host = null;
		String urls = null;

		if (p == -1) {
			host = urlx;
			urls = "/";
		} else {
			host = urlx.substring(0, p);
			urls = urlx.substring(p);
		}

		try {
			DefaultHttpClient httpclient = new DefaultHttpClient();
			
			if (connectionProperties.useProxy() == true) {
				setProxyConfiguration(httpclient, connectionProperties);
			}

			HttpHost target;
			if (secureConnection) {
				target = new HttpHost(host, 443, "https");
			} else {
				target = new HttpHost(host, 80, "http");
			}
			
			HttpContext localContext = new BasicHttpContext();

			HttpGet httpget = new HttpGet(urls);
			HttpResponse response1 = httpclient.execute(target, httpget, localContext);

			HttpEntity entity1 = response1.getEntity();

			InputStream is = entity1.getContent();

			return is;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public static ConnectionProperties getConnectionProperties() {
		TrackItPreferences preferences = TrackIt.getPreferences();
		
		String host = preferences.getPreference(Constants.PrefsCategories.CONNECTION,
				null, Constants.ConnectionPreferences.HOST, "");
		int port = preferences.getIntPreference(Constants.PrefsCategories.CONNECTION,
				null, Constants.ConnectionPreferences.PORT, 0);
		String domain = preferences.getPreference(Constants.PrefsCategories.CONNECTION,
				null, Constants.ConnectionPreferences.DOMAIN, "");
		String user = preferences.getPreference(Constants.PrefsCategories.CONNECTION,
				null, Constants.ConnectionPreferences.USER, "");
		String pass = preferences.getPreference(Constants.PrefsCategories.CONNECTION,
				null, Constants.ConnectionPreferences.PASS, "");
		boolean useProxy = preferences.getBooleanPreference(Constants.PrefsCategories.CONNECTION,
				null, Constants.ConnectionPreferences.USE_PROXY, false);
		
		return new ConnectionProperties(host, port, domain, user, pass, useProxy);
	}

	private void setProxyConfiguration(DefaultHttpClient httpclient, ConnectionProperties properties) {
		HttpHost proxy = new HttpHost(properties.getHost(), properties.getPort());
		httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		NTCredentials creds = new NTCredentials(properties.getUser(), properties.getPass(), "", properties.getDomain());
		List<String> authpref = new ArrayList<String>();
		authpref.add(AuthPolicy.NTLM);
		httpclient.getParams().setParameter(AuthPNames.PROXY_AUTH_PREF, authpref);
		httpclient.getCredentialsProvider().setCredentials(AuthScope.ANY, creds);
	}
}
