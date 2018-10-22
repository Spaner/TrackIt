/*
 * This file is part of Track It!.
  * Copyright (C) 2016, 2017 João Brisson Lopes
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
import com.trackit.business.common.WGS84;
import com.trackit.business.domain.UTMLocation;

public class GeographicLocation {

	private double latitude  = Double.NaN;
	private double longitude = Double.NaN;
	private double altitude  = 0.;
	
	private static boolean acceptOutOfBounds = false;					//12335: 2017-07-03
	
	public GeographicLocation() {}
	
	public GeographicLocation( double latitude, double longitude) {
		init( latitude, longitude, 0.);
	}
	
	//12335: 2017-05-04
	public GeographicLocation( Location location) {
		init( location.getLatitude(), location.getLongitude(), location.getAltitude());
	}
	
	public GeographicLocation( double latitude, double longitude, double altitude) {
		init( latitude, longitude, altitude);
	}
	
	public GeographicLocation( double latitude, double longitude, boolean radians) {
		if ( radians )
			initRadians(latitude, longitude, 0.);
		else
			init( latitude, longitude, 0.);
	}
	
	public GeographicLocation( double latitude, double longitude, double altitude, boolean radians) {
		if ( radians )
			initRadians(latitude, longitude, altitude);
		else
			init( latitude, longitude, altitude);
	}
	
	//12335: 2017-07-03
	public static boolean acceptOutofBoundsCoordinates( boolean accept) {
		acceptOutOfBounds = accept;
		return acceptOutOfBounds;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public double getAltitude() {
		return altitude;
	}
	
	public String toString() {
		return String.format( "%10.6f %11.6f", latitude, longitude);
	}
	
	public UTMLocation toUTM() {
		int zone                = UTMLocation.getZone( longitude);
		double centralLongitude = UTMLocation.getCentralLongitude( longitude);
		char band               = UTMLocation.getBand( latitude);
		double phi     = Math.toRadians( latitude);
		double cosPhi  = Math.cos( phi);
		double tanPhi  = Math.tan( phi);
		double lambda  = Math.toRadians( longitude);
		double lambda0 = Math.toRadians( centralLongitude);
		double N       = WGS84.EQUATORIALRADIUS / Math.sqrt(1.0 - (1. - cosPhi * cosPhi)
												* WGS84.FIRSTECCENTRICITY2);
        double T       = tanPhi * tanPhi;
		double C       = cosPhi * cosPhi * WGS84.SECONDECCENTRICITY2;
        double A       = cosPhi * (lambda - lambda0);
		double M       = centralMeridianDistance( phi);
       double easting  = ( (((T - 18.0) * T + 5.0 + 72.0 * C - 58.0 * WGS84.SECONDECCENTRICITY2)
    		   							/ 120.0 * A * A + 
    		               (1.0 - T + C) / 6.0) * A * A + 1.0) * A * N * WGS84.K0;
       double northing = ( ((((T - 58.0) * T + 61.0 + 600.0 * C - 330.0 * WGS84.SECONDECCENTRICITY2)
    		                / 720 * A * A +
    		   				((4.0 * C + 9.0) * C + 5.0 - T) / 24.0) * A * A + 0.5) * A * A * N * tanPhi + M - M0)
    		             * WGS84.K0;
       
       easting += WGS84.FALSEEASTING;
       if (latitude >= 0.0)
           northing += WGS84.FALSENORTHINGNORTH;
       else
           northing += WGS84.FALSENORTHINGSOUTH;

       
 	return new UTMLocation( easting, northing, zone, band);
	}
	
	public static double centralMeridianDistance( double phi) {
		if ( Double.isNaN( m1) ) {
			setVariables();
			M0 = computeCentralMeridianDistance( Math.toRadians(WGS84.CENTRALLATITUDE));
		}
		return computeCentralMeridianDistance( phi);
	}

	private static double computeCentralMeridianDistance( double phi) {
		return (-m6 * Math.sin(6.0 * phi) + m4 * Math.sin(4.0 * phi) - m2 * Math.sin(2.0 * phi) + m1 * phi)
				   * WGS84.EQUATORIALRADIUS;
	}

	private void initRadians( double latitude, double longitude, double altitude) {
		init( latitude * 180. / Math.PI, longitude * 180. / Math.PI, altitude);
	}
	
	//12335: 2017-07-03 - accept out of bounds latitude and longitude handling
	private void init( double latitude, double longitude, double altitude) {
		if ( acceptOutOfBounds ) {
			this.latitude  = latitude;
			this.longitude = longitude;
		}
		else {
			if ( Math.abs( latitude) > 90 || Math.abs( longitude) > 180. )
				System.out.println( "WARNING: latitude and/or longitude out of bounds "
			                        + latitude + " " + longitude);
			if ( latitude >= -90.0 && latitude <= 90.0 )
				this.latitude = latitude;
			if ( longitude >= -180.0 && longitude <= 180.0 )	//12335: 2017-07-03
				this.longitude = longitude;
		}
		this.altitude = altitude;
	}
	
	private static double m1 = Double.NaN, m2, m4, m6, M0;
	
	/**
	 * variables setup
	 */
	
	public static double m1() {
		if ( Double.isNaN( m1))
			setVariables();
		return m1;
	}

	private static void setVariables() {
		m1 = ((-5.0 / 256.0 * WGS84.FIRSTECCENTRICITY2 - 3.0 / 64.0) * WGS84.FIRSTECCENTRICITY2 - 0.25)
									* WGS84.FIRSTECCENTRICITY2 + 1.0;
		m2 = ((45.0 / 1024.0 * WGS84.FIRSTECCENTRICITY2 + 3.0 / 32.0) * WGS84.FIRSTECCENTRICITY2 + 3.0 / 8.0)
									* WGS84.FIRSTECCENTRICITY2;
		m4 = (45.0 / 1024.0 * WGS84.FIRSTECCENTRICITY2 + 15.0 / 256.0)
									* WGS84.FIRSTECCENTRICITY2 * WGS84.FIRSTECCENTRICITY2;
		m6 = 35.0 / 3072.0 * WGS84.FIRSTECCENTRICITY2 * WGS84.FIRSTECCENTRICITY2 * WGS84.FIRSTECCENTRICITY2;
	}

}
