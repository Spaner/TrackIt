/*
 * This file is part of Track It!.
 * Copyright (C) 2018 João Brisson
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

import java.util.Date;

import com.trackit.business.utilities.Connection;

public class MagneticDeclinationService {
	
	protected Connection connection = null;	
	protected Double declination = null, uncertainty = null, changeRate = null;
	protected Double latitude    = null, longitude   = null, elevation  = null;
	protected Date   date        = null;
	
	public MagneticDeclinationService() {
		connection = Connection.getInstance();
	}
	
	public Double getDeclination( double latitude, double longitude) {
		return getDeclination(latitude, longitude, null);
	}
	
	public Double getDeclination( double latitude, double longitude, Date date) {
		return null;
	}
	
	public Double getDeclinationUncertainty() {
		return uncertainty;
	}
	
	public Double getDeclinationChangeRate() {
		return changeRate;
	}
	
	public Double getLatitude() {
		return latitude;
	}
	
	public Double getLongitude() {
		return longitude;
	}
	
	public Double getElevation() {
		return elevation;
	}

}
