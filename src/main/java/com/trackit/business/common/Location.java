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
package com.trackit.business.common;

import com.trackit.business.utilities.geo.Datum;
import com.trackit.business.utilities.geo.DatumFactory;
import com.trackit.business.utilities.geo.Datums;
import com.trackit.business.utilities.geo.Geo;

public class Location {
	public static final int GEOGRAPHIC = 0;
	public static final int CARTESIAN = 1;
	
	private double longitude;
	private double latitude;
	private double altitude;
	private Datum datum;
	private int type;
	
	public Location(double longitude, double latitude, double altitude, Datum datum, int type) {
		this.longitude = longitude;
		this.latitude = latitude;
		this.altitude = altitude;
		this.datum = datum;
		this.type = type;
	}
	
	public Location(double longitude, double latitude, Datum datum, int type) {
		this.longitude = longitude;
		this.latitude = latitude;
		this.altitude = 0.0;
		this.datum = datum;
		this.type = type;
	}
	
	public Location(double longitude, double latitude) {
		this.longitude = longitude;
		this.latitude = latitude;
		this.altitude = 0.0;
		this.datum = DatumFactory.getInstance().getDatum(Datums.WGS84);
		this.type = GEOGRAPHIC;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}
	
	public Datum getDatum() {
		return datum;
	}
	
	public boolean isGeographic() {
		return (type == GEOGRAPHIC);
	}
	
	public boolean isCartesian() {
		return (type == CARTESIAN);
	}
	
	public Location toCartesian() {
		if (type == CARTESIAN) {
			return this;
		}
		
		return datum.geographicToCartesian(this);
	}
	
	public Location toGeographic() {
		if (type == GEOGRAPHIC) {
			return this;
		}
		
		return datum.cartesianToGeographic(this, false, Geo.computeZone(this.longitude));
	}
	
	@Override
	public String toString() {
		return "Location [longitude=" + longitude + ", latitude=" + latitude + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(latitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(longitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(altitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Location other = (Location) obj;
		if (Math.abs(latitude - other.latitude) > 0.01
				|| Math.abs(longitude - other.longitude) >= 0.01
				|| Math.abs(altitude - other.altitude) >= 0.01) {
			return false;
		}
		return true;
	}
}
