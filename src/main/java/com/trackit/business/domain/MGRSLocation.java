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

import org.apache.derby.tools.sysinfo;

import com.trackit.business.domain.UTMLocation;

public class MGRSLocation {

	private double easting  = Double.NaN;
	private double northing = Double.NaN;
	private double altitude = 0.;
	private int    zone     = 0;
	private char   band     = '\0';
	private char   column   = '\0';
	private char   row      = '\0';
	
	private static String cLetters   = "ABCDEFGHJKLMNPQRSTUVWXYZ";
	private static String rLetters[] = { "ABCDEFGHJKLMNPQRSTUV", "FGHJKLMNPQRSTUVABCDE"};
	private static double sqSide     = 100000.;
	
	public MGRSLocation( int zone, char band, char column, char row, double easting, double northing) {
		init( zone, band, column, row, easting, northing, northing);
	}
	
	public MGRSLocation( UTMLocation utmLocation) {
		init( utmLocation);
	}
	
	public MGRSLocation( double latitude, double longitude, double altitude) {
		init( (new GeographicLocation(latitude, longitude, altitude)).toUTM());
	}
	
	public int getZone() {
		return zone;
	}
	
	public char getBand() {
		return band;
	}
	
	public char getColumn() {
		return column;
	}
	
	public char getRow() {
		return row;
	}
	
	public double getEasting() {
		return easting;
	}
	
	public double getNorting() {
		return northing;
	}
	
	public double getAltitude() {
		return altitude;
	}

	public String toString() {
		return String.format( "%d%c%c%c%05.0f%05.0f", zone, band, column, row, easting, northing);
	}
	
	public UTMLocation toUTM() {

		int offset = ((zone - 1) % 3) * 8;
		double nEasting = (cLetters.indexOf( column, offset) + 1 - offset) * sqSide + easting;

		double baseNorthing = UTMLocation.bandBaseNorthing( band);
		double nNorthing    = rLetters[ 1 - zone % 2].indexOf( row) * sqSide + northing;
		while ( nNorthing < baseNorthing )
			nNorthing += (20 * sqSide);
		
		return new UTMLocation( nEasting, nNorthing, zone, band);
	}
	
	public GeographicLocation toGeographic() {
		return toUTM().toGeographic();
	}
	
	private void init( UTMLocation utmLocation) {
		int utmZone = utmLocation.getZone();
		
		int    tmpColIdx  = (int) ( utmLocation.getEasting() / sqSide);
		char   tmpColumn  = cLetters.charAt( tmpColIdx - 1 + ((utmZone - 1) % 3) * 8);
		
		int    tmpRowIdx   = (int) ( (utmLocation.getNorthing() % (20 * sqSide)) / sqSide);
		char   tmpRow      = rLetters[ 1 - utmZone % 2].charAt( tmpRowIdx );
		
		init( utmLocation.getZone(), utmLocation.getBand(), tmpColumn, tmpRow, 
			  utmLocation.getEasting() % sqSide, utmLocation.getNorthing() % sqSide, utmLocation.getAltitude());
	}
	
	private void init( int zone, char band, char column, char row, double easting, double northing,
            double altitude) {
		this.easting = easting;
		this.northing = northing;
		this.zone     = zone;
		this.band     = band;
		this.column   = column;
		this.row	  = row;
		this.altitude = altitude;
	}
	
}
