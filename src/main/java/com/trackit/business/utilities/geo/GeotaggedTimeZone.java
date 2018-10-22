/*
 * This file is part of Track It!.
 * Copyright (C) 2016 J M Brisson Lopes
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

package com.trackit.business.utilities.geo;

import java.io.InputStream;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import com.trackit.business.utilities.Connection;
import com.trackit.presentation.view.map.provider.google.TimeZoneResponse;
import com.trackit.presentation.view.map.provider.google.routes.Routes;

public class GeotaggedTimeZone extends TimeZone {

	private boolean valid = false;
	private double  latitude  = 0.;
	private double  longitude = 0.;
	private int     rawOffset = 0;
	private int     dstOffset = 0;
	
	private static final String SERVER = "https://maps.googleapis.com/";
	private static final String URL_TIMEZONE = "%smaps/api/timezone/xml?location=%f,%f&timestamp=%d";
	
	private static Logger logger = Logger.getLogger(Routes.class.getName());
	
	public  GeotaggedTimeZone( double latitude, double longitude) {
		setLocation( latitude, longitude);
	}
	
	public GeotaggedTimeZone( double latitude, double longitude, Date date) {
		setLocation( latitude, longitude, date);
	}
	
	public boolean setLocation( double latitude, double longitude) {
		return setLocation( latitude, longitude, null);
	}
	
	public boolean setLocation( double latitude, double longitude, Date date) {
		geotagAs( latitude, longitude, date);
		if ( valid ) {
			this.latitude  = latitude;
			this.longitude = longitude;
		}
		return isValid();
	}
	
	public boolean isValid() {
		return valid;
	}
	
	public String toString() {
		String res = "Zone ID: " + getID() + "  " + getDisplayName() + "  raw offset: "
				    + rawOffset + "  dst offset: " + dstOffset;
		res += String.format( "   Lat: %.6f   Lon: %.6f", latitude, longitude);
		return res;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}

	@Override
	public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds) {
		return 0;
	}

	@Override
	public void setRawOffset(int offsetMillis) {
		rawOffset = offsetMillis;
	}

	@Override
	public int getRawOffset() {
		return rawOffset;
	}

	@Override
	public boolean useDaylightTime() {
		return dstOffset > 0;
	}

	@Override
	public boolean inDaylightTime(Date date) {
		geotagAs( latitude, longitude, date);
		return useDaylightTime();
	}
	
	private boolean isValid( double latitude, double longitude) {
		valid = false;
		if ( latitude >= -90. && latitude <= 90. && longitude >= -180. && longitude <= 180. )
			return true;
		return false;
	}
	
	private void geotagAs( double latitude, double longitude, Date date) {
		if ( isValid( latitude, longitude) ) {
			try {
				Date localDate = date;
				if ( localDate == null )
					localDate = new Date();
				
				String url = String.format( Locale.ENGLISH, URL_TIMEZONE,
						                    SERVER, latitude, longitude, localDate.getTime()/1000);
//				logger.debug( "\n" + url);
				
		        JAXBContext context = JAXBContext.newInstance( TimeZoneResponse.class);
		        Unmarshaller unmarshaller = context.createUnmarshaller();
		        
		        InputStream inputStream = Connection.getInstance().getResource(url);
		        TimeZoneResponse response = (TimeZoneResponse) unmarshaller.unmarshal(inputStream);
		        inputStream.close();
		        
		        if ( response != null && response.getTimezoneID() != null) {
		        	this.setID( response.getTimezoneID());
		        	int tmp = (int) Double.parseDouble( response.getRawOffset());
		        	this.setRawOffset( tmp * 1000);
		        	tmp = (int) Double.parseDouble( response.getDSTOffset());
		        	this.dstOffset = tmp * 1000;
		        	valid = true;
		        }
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
