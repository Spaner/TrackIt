/*
 * This file is part of Track It!.
 * Copyright (C) 2016 João Brisson Lopes
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

import com.trackit.business.common.WGS84;
import com.trackit.business.domain.UTMLocation;

public class UTMLocation {
	
	private double easting  = Double.NaN;
	private double northing = Double.NaN;
	private double altitude = 0.;
	private int    zone     = 0;
	private char   band     = '\0';
	private static char assumeHemisphere = 'X';
	
	private static String bandLetters = "CDEFGHJKLMNPQRSTUVWX";

	public static void assumeNorthernHemisphere()
	{
		assumeHemisphere = 'N';
	}
	
	public static void assumeSouthernHemisphere() {
		assumeHemisphere = 'C';
	}
	
	public static int getZone( double longitude) {
		return ((int) ((longitude + 180.0) / 6.0) + 1);
	}
	
	public static char getBand( double latitude) {
        int idx = ((int)(latitude + 80.0)) / 8;
        if (idx >= 0 && idx < 20)
            return bandLetters.charAt( idx);
        return '\0';
	}
	
	public static double getCentralLongitude( final double longitude) {
		return getCentralLongitude( getZone( longitude));
	}

	public static double getCentralLongitude( final int zone) {
        return zone * 6.0 - 183.0;
	}
	
	public static double bandBaseNorthing( char band) {
		int idx = bandLetters.indexOf( band);
		if ( idx != -1 )
			return (new UTMLocation( idx * 8. -80., 0., 0.)).northing;
		return 0.;
	}
	
	public UTMLocation( double latitude, double longitude, double altitude) {
		UTMLocation tmp = (new GeographicLocation( latitude, longitude, altitude)).toUTM();
		init( tmp.easting, tmp.northing, tmp.zone, tmp.band, tmp.altitude);
	}

	public UTMLocation( double easting, double northing, int zone, char band,
			            double altitude	) {
		init( easting, northing, zone, band, altitude);
	}
	
	public UTMLocation( double easting, double northing, int zone, double altitude) {
		init( easting, northing, zone, assumeHemisphere, altitude);
		recomputeBand();
	}
	
	public UTMLocation( double easting, double northing, int zone, char band) {
		init( easting, northing, zone, band, 0.);
	}

	public UTMLocation( double easting, double northing, int zone) {
		init( easting, northing, zone, assumeHemisphere, 0.);
		recomputeBand();
	}
	
	public double getEasting() {
		return easting;
	}
	
	public double getNorthing() {
		return northing;
	}
	
	public int getZone() {
		return zone;
	}
	
	public char getBand() {
		return band;
	}
	
	public double getAltitude() {
		return altitude;
	}
	
	public String toString() {
		return String.format( "%d%c %.0f %.0f", zone, band, easting, northing);
	}
	
	public GeographicLocation toGeographic() {
		char localBand = band;
		if ( localBand != '\0') {
			double east  = easting - WGS84.FALSEEASTING;
			double north = northing;
			if ( localBand >= 'N' )
				north -= WGS84.FALSENORTHINGNORTH;
			else
				north -= WGS84.FALSENORTHINGSOUTH;
			
			double eOne    = 1. / (2. * WGS84.INVERSEFLATTENING - 1.);
	        double M       = north / WGS84.K0 + GeographicLocation.centralMeridianDistance( WGS84.CENTRALLATITUDE);
	        double miu     = M / (GeographicLocation.m1() * WGS84.EQUATORIALRADIUS);
	        double j1      = (-27.0 / 32.0 * eOne * eOne + 1.5) * eOne;
	        double j2      = (-55.0 / 32.0 * eOne * eOne + 21.0 / 16) * eOne * eOne;
	        double j3      = 151.0 / 96.0 * eOne * eOne * eOne;
	        double j4      = 1097.0 / 512.0 * eOne * eOne * eOne * eOne;
	        double phi     = Math.sin(8.0 * miu) * j4 + Math.sin(6.0 * miu) * j3 + Math.sin(4.0 * miu) * j2 +
			                 Math.sin(2.0 * miu) * j1 + miu;
	        double tanPhi  = Math.tan( phi);
	        double cosPhi  = 1. / Math.sqrt( tanPhi * tanPhi + 1.);
	        double cos2Phi = cosPhi * cosPhi;
	        double sin2Phi = 1. - cos2Phi;
	        double c1      = cos2Phi * WGS84.SECONDECCENTRICITY2;
	        double t1      = tanPhi * tanPhi;
	        double dd      =  1. - WGS84.FIRSTECCENTRICITY2 * sin2Phi;
	        double r1      = (1.0 - WGS84.FIRSTECCENTRICITY2) * WGS84.EQUATORIALRADIUS / Math.pow( dd, 1.5);
	        double n1 	   = WGS84.EQUATORIALRADIUS / Math.sqrt(dd);
	        double d  		= east / (n1 * WGS84.K0);

	        double q1 = tanPhi * n1 / r1;
	        double q2 = d * d / 2;
	        double q3 = ((-4.0 * c1 + 10.0) * c1 + 3.0 * t1 + 5.0 - 9.0 * WGS84.FIRSTECCENTRICITY2) * d * d * d * d / 24.0;
	        double q4 = ((45.0 * t1 + 90.0) * t1 + 61.0 + (-3.0 * c1 + 298.0) * c1 - 252.0 * WGS84.FIRSTECCENTRICITY2) *
	        		    d * d * d * d * d * d / 720.0;

	        double q5 = d;
	        double q6 = ( 1.0 + 2.0 * t1 + c1) * d * d * d / 6.0;
	        double q7 = ((24.0 * t1 + 28.0) * t1 - (3.0 * c1 + 2.0) * c1 + 5.0 + 8 * WGS84.SECONDECCENTRICITY2) *
	        		    d * d * d * d * d / 120.0;

	        return new GeographicLocation(
	        		Math.toDegrees( phi - (q2 - q3 + q4) * q1),
	        		Math.toDegrees( (q5 - q6 + q7) / cosPhi) + getCentralLongitude( zone),
	        		altitude);
		}

		return null;
	}
	
	public MGRSLocation toMGRS() {
		return new MGRSLocation( this);
	}

	private void init( double easting, double northing, int zone, char band,
			           double altitude) {
		this.easting = easting;
		this.northing = northing;
		this.zone     = zone;
		this.band     = band;
	}
	
	private void recomputeBand()
	{
		GeographicLocation geo = this.toGeographic();
		this.band = getBand( geo.getLatitude());
	}

}
