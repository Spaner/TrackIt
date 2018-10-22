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
package com.trackit.business.utilities;

public class ConnectionProperties {
	private boolean useProxy;
	private String host;
	private int port;
	private String domain;
	private String user;
	private String pass;
	
	public ConnectionProperties(String host, int port, String domain,
			String user, String pass, boolean useProxy) {
		this.useProxy = useProxy;
		this.host = host;
		this.port = port;
		this.domain = domain;
		this.user = user;
		this.pass = pass;
	}
	
	public boolean useProxy() {
		return useProxy;
	}
	
	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}
	
	public String getDomain() {
		return domain;
	}
	
	public String getUser() {
		return user;
	}
	
	public String getPass() {
		return pass;
	}
}