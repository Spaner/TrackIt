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
package com.trackit.business.domain;

import com.trackit.business.common.Location;
import com.trackit.business.exception.TrackItException;
import com.trackit.business.utilities.Utilities;
import com.trackit.business.utilities.geo.GeolocationService;
import com.trackit.presentation.event.EventPublisher;

public class GeographicBoundingBox extends TrackItBaseType {
	
	private static double MIN_LONGITUDE = -180.;
	private static double MAX_LONGITUDE =  180.;
	private static double MIN_LATITUDE  =  -90.;
	private static double MAX_LATITUDE  =   90.;
	private static double DEFAULT_RANGE = 1000.;
	
	private double SWLatitude;
	private double SWLongitude;
	private double NELatitude;
	private double NELongitude;
	private String centralLocationName = "";
	private Double locationRange = null;
	private boolean isBox;
	
	public GeographicBoundingBox() {
		defaultInit();
	}
	
	public GeographicBoundingBox( GeographicBoundingBox srcBox) {
		this.SWLatitude  = srcBox.SWLatitude;
		this.SWLongitude = srcBox.SWLongitude;
		this.NELatitude  = srcBox.NELatitude;
		this.NELongitude = srcBox.NELongitude;
		this.isBox       = srcBox.isBox;
	}
	
	public GeographicBoundingBox( Double minLatitude, Double minLongitude, 
								  Double maxLatitude, Double maxLongitude) {
		if ( minLatitude <= maxLatitude && minLongitude <= maxLongitude  && 
			 checkLocationCoordinates( minLatitude, minLongitude)      &&
			 checkLocationCoordinates( maxLatitude, maxLongitude)          ) {
			SWLatitude  = minLatitude;
			SWLongitude = minLongitude;
			NELatitude  = maxLatitude;
			NELongitude = maxLongitude;
			checkIfBox();
		} else
			defaultInit();
			checkIfBox();
	}
	
	public GeographicBoundingBox( Double latitude, Double longitude, Double expectedRange) {
		Double range = expectedRange;
		if ( range == null || range <= 0. )
			range = DEFAULT_RANGE;
		if ( checkLocationCoordinates( latitude, longitude) && range > 0. ) {
			double halfLatRange  = halfLatitudeRange(  range);
			double halfLonRange  = halfLongitudeRange( range, latitude);
			SWLatitude  = Math.max( latitude  - halfLatRange, MIN_LATITUDE);
			SWLongitude = Math.max( longitude - halfLonRange, MIN_LONGITUDE);
			NELatitude  = Math.min( latitude  + halfLatRange, MAX_LATITUDE);
			NELongitude = Math.min( longitude + halfLonRange, MAX_LONGITUDE);	
			checkIfBox();
		} else
			defaultInit();
	}
	
	public GeographicBoundingBox( double [] latitude, double [] longitude) {
		if ( latitude != null                    && longitude != null    &&
			 latitude.length == longitude.length && latitude.length > 1      ) {
			SWLatitude  = latitude [0];
			SWLongitude = longitude[0];
			NELatitude  = latitude [0];
			NELongitude = longitude[0];
			for( int i=1; i<latitude.length; i++) {
				SWLatitude  = Math.min( SWLatitude,  latitude[i]);
				NELatitude  = Math.max( NELatitude,  latitude[i]);
				SWLongitude = Math.min( SWLongitude, longitude[i]);
				NELongitude = Math.max( NELongitude, longitude[i]);
			}
			checkIfBox();
		} else
			defaultInit();
	}
	
	public GeographicBoundingBox( GeolocationService service, String locationName, Double expectedRange) {
		
	}
	
	public boolean isAreaBox() {
		return isBox;
	}
	
	public double minLatitude() {
		return SWLatitude;
	}
	
	public double maxLatitude() {
		return NELatitude;
	}
	
	public double minLongitude() {
		return SWLongitude;
	}
	
	public double maxLongitude() {
		return NELongitude;
	}
	
	public Location getSWCorner() {
		return new Location( SWLatitude, SWLongitude);
	}
	
	public Location getNECorner() {
		return new Location( NELatitude, NELongitude);
	}
	
	public double getWidth() {
		return Utilities.getGreatCircleDistance( SWLatitude, SWLongitude, SWLatitude, NELongitude) * 1000.;
	}
	
	public double getHeight() {
		return Utilities.getGreatCircleDistance( SWLatitude, SWLongitude, NELatitude, SWLongitude) * 1000.;
	}
	
	public double diagonalLength() {
		return Utilities.getGreatCircleDistance( SWLatitude, SWLongitude, NELatitude, NELongitude) * 1000.;
	}
	
	public boolean checkBoxRange( Double expectedRange) {
		double range = expectedRange != null ? expectedRange: DEFAULT_RANGE;
		return checkBoxRange( range+range, range+range, false);
	}
	
	public boolean checkBoxRange( Double expectedRange, boolean correct) {
		double range = expectedRange != null ? expectedRange: DEFAULT_RANGE;
		return checkBoxRange( range+range, range+range, correct);
	}
	
	// checks if box satisfies the specified horizontal and vertical ranges
	// corrects box if so specified
	public boolean checkBoxRange( double horizontal, double vertical, boolean correct) {
		if ( horizontal <= 0. )
			horizontal = DEFAULT_RANGE * 2.;
		if ( vertical <= 0. )
			vertical = DEFAULT_RANGE * 2.;
		boolean isOK = true;
		// check vertical range
		double centralLatitude = (NELatitude + SWLatitude) * .5;
		if ( getBoxNSRange( NELatitude - SWLatitude) < vertical ) {
			isOK = false;
			if ( correct) {
				double halfRange = halfLatitudeRange( vertical);
				SWLatitude  = Math.max( centralLatitude  - halfRange, MIN_LATITUDE);
				NELatitude  = Math.min( centralLatitude  + halfRange, MAX_LATITUDE);
			}
		}
		// check horizontal range
		if ( getBoxEWRange( NELongitude - SWLongitude, centralLatitude) < horizontal ) {
			isOK = false;
			if ( correct ) {
				double halfRange = halfLongitudeRange( horizontal, centralLatitude);
				double centralLongitude = (SWLongitude + NELongitude) * .5;
				SWLongitude = Math.max( centralLongitude - halfRange, MIN_LONGITUDE);
				NELongitude = Math.min( centralLongitude + halfRange, MAX_LONGITUDE);	
			}
		}
		
		return isOK;
	}
	
	public String toString() {
		return String.format( "%f %f %f %f", SWLatitude, NELatitude, SWLongitude, NELongitude);
	}
	
	private void defaultInit() {
		SWLatitude  = MIN_LATITUDE;
		SWLongitude = MIN_LONGITUDE;
		NELatitude  = MAX_LATITUDE;
		NELongitude = MAX_LONGITUDE;
		isBox       = true;
	}
	
	protected boolean checkLocationCoordinates( double latitude, double longitude ) {
		if ( latitude  >= MIN_LATITUDE  && latitude  <= MAX_LATITUDE &&
			 longitude >= MIN_LONGITUDE && longitude <= MAX_LONGITUDE  )
			return true;
		return false;
	}
	
	// returns degrees (Earth radius in km)
	public static double halfLatitudeRange( double NSRange) {
		return Utilities.radToDegrees( NSRange * .0005 / Utilities.EarthRadius());
	}
	
	// returns degrees, takes latitude in degrees (Earth radius in km)
	public static double halfLongitudeRange( double EWRange, double latitude) {
		return Utilities.radToDegrees( 
				Math.asin(  EWRange * .0005 / Utilities.EarthRadius()
								/ Math.cos( Utilities.degreesToRad(latitude))));
	}
	
	// returns box vertical range (in m), takes latitude range in degrees
	public static double getBoxNSRange( double latitudeRange) {
		return Utilities.degreesToRad( latitudeRange) * 1000. * Utilities.EarthRadius();
	}
	
	// returns box horizontal range (in m) from longitude range and latitude in degrees
	public static double getBoxEWRange( double longitudeRange, double latitude) {
		return Math.sin( Utilities.degreesToRad( longitudeRange) *
			   Math.cos( Utilities.degreesToRad( latitude))) * 1000. * Utilities.EarthRadius();
	}
	
	protected void checkIfBox() {
		isBox = SWLatitude < NELatitude && SWLongitude < NELongitude;
	}

	@Override
	public void publishSelectionEvent(EventPublisher publisher) {
	}

	@Override
	public void accept(Visitor visitor) throws TrackItException {
	}

}
