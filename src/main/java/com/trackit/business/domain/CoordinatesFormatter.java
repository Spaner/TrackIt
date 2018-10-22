/*
 * This file is part of Track It!.
 * Copyright (C) 2016, 2017 J M Brisson Lopes
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

import java.util.List;
import java.util.Locale;

import org.apache.derby.iapi.services.loader.GeneratedMethod;

import com.trackit.TrackIt;
import com.trackit.business.common.Constants;
import com.trackit.business.common.Messages;
import com.trackit.business.common.WGS84;
import com.trackit.presentation.event.Event;
import com.trackit.presentation.event.EventListener;
import com.trackit.presentation.event.EventManager;
import com.trackit.presentation.view.map.MapInfo;

public class CoordinatesFormatter implements EventListener{
	
	private CoordinatesType coordinatesType = CoordinatesType.DEFAULT;
	
	private static CoordinatesFormatter formatter = null;
	
	private static final int    decimalDigitsDecimalDegrees = 6;
	private static final int    decimalDigitsDecimalMinutes = 5;
	private static final int    decimalDigitsDecimalSeconds = 4;
	
//	private static final String decimalDegreesFormat         = "%.6f";
//	private static final String degreesDecimalMinutesFormat  = "%dº %.5f'";
//	private static final String degreesMinutesSecondsFormat  = "%dº %d' %.4f\"";
	private static final String defaultCoordinateValueString = "---";
	
	private Locale locale;										//12335: 2017-04-30
		
	public synchronized static CoordinatesFormatter getInstance() {
		if ( formatter == null )
			formatter = new CoordinatesFormatter();
		return formatter;
	}
	
	public CoordinatesFormatter( ) {
		EventManager.getInstance().register( this);
		setCoordinatesType();
	}
	
	private void setCoordinatesType() {
		coordinatesType = CoordinatesType.DEFAULT;
		int intType = TrackIt.getPreferences().getIntPreference(
				Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.COORDINATES, coordinatesType.valueOf()); 
		coordinatesType = CoordinatesType.lookup( intType);
	}
	
	// 12335: 2017-05-25
	public CoordinatesType getCoordinatesType() {
		return coordinatesType;
	}
	
	public void process( Event event, DocumentItem item) {
		if ( event == Event.COORDINATES_TYPE_CHANGED ) {
			setCoordinatesType();
		}
	}
	
	public void process( Event event, DocumentItem item, List<? extends DocumentItem> itens) {
	}
	
	public String toString( Double latitude, Double longitude, String prefix) {
		locale = Messages.getLocale();
		String localPrefix = prefix;
		if ( localPrefix.length() > 0 )
			localPrefix += " ";
		String coordinates = "";
		if ( coordinatesType != CoordinatesType.UTM && coordinatesType != CoordinatesType.MGRS ) {
			coordinates += localPrefix + Messages.getMessage ( "coordinates.latitude") + " "
		                 + ((latitude != null) ? formatLatitude( latitude, -1): 
		                	   					 defaultCoordinateValueString)
		                 + " " + Messages.getMessage( "coordinates.longitude") + " "
		                 + ((longitude != null) ? formatLongitude( longitude, -1):
		                	                     defaultCoordinateValueString);
		}
		else {
			coordinates = localPrefix;
			if ( latitude != null && longitude != null) {
				UTMLocation utm = (new GeographicLocation( latitude, longitude)).toUTM();
				if ( coordinatesType == CoordinatesType.UTM )
					coordinates += Messages.getMessage( "coordinates.utm") + " " + utm.toString();
				else
					coordinates += Messages.getMessage( "coordinates.mgrs") + " " +utm.toMGRS().toString();
			}
			else
				coordinates += defaultCoordinateValueString;
		}
		return coordinates;
	}
	
	// 2017-06-03: 12335
	public String formatAsDecimalDegrees( double coordinateValue) {
		return toString( coordinateValue, decimalDigitsDecimalDegrees);
	}
	// 2017-06-03
	public String formatLatitude( double latitude, int noDecimalDigits) {
		String text = formatCoordinate( latitude, noDecimalDigits);
		if ( coordinatesType.equals( CoordinatesType.DEGREES_DECIMAL_MINUTES) ||
			 coordinatesType.equals( CoordinatesType.DEGREES_MINUTES_SECONDS) ) 
			text += " " + ( (latitude >= 0.) ? Messages.getMessage( "coordinates.north.letter") :
                                               Messages.getMessage( "coordinates.south.letter") );
		return text;
	}
	
	// 2017-06-03
	public String formatLongitude( double longitude, int noDecimalDigits) {
		String text = formatCoordinate( longitude, noDecimalDigits);
		if ( coordinatesType.equals( CoordinatesType.DEGREES_DECIMAL_MINUTES) ||
			 coordinatesType.equals( CoordinatesType.DEGREES_MINUTES_SECONDS) )
			text += " " + ( (longitude >= 0.) ? Messages.getMessage( "coordinates.east.letter") :
				                                Messages.getMessage( "coordinates.west.letter") );
		return text;
	}
		
	//12335: 2017-07-06
	public String formatCoordinate( double value, int noDecimalDigits, CoordinatesType typeToSet) {
		CoordinatesType temporary = coordinatesType;
		coordinatesType = typeToSet;
		String coordinate = formatCoordinate( value, noDecimalDigits);
		coordinatesType = temporary;
		return coordinate;
	}
	
	public String formatCoordinate( double value, int noDecimalDigits) {
		String text = "";
		switch (coordinatesType) {
		case DECIMAL_DEGREES:
//			text = String.format( decimalDegreesFormat, value);					//12335: 2017-06-03
			text = formatGeographicCoordinate(
						value, (noDecimalDigits<0) ? decimalDigitsDecimalDegrees: noDecimalDigits);
			break;
		case DEGREES_DECIMAL_MINUTES: {
//			text = String.format( degreesDecimalMinutesFormat, deg, absValue);	//12335: 2017-06-03
			text = formatGeographicCoordinate(
						value, (noDecimalDigits<0) ? decimalDigitsDecimalMinutes: noDecimalDigits);
		}
			break;
		case DEGREES_MINUTES_SECONDS: {
//			text = String.format( degreesMinutesSecondsFormat, deg, min, absValue);	//12335: 2017-06-03
			text = formatGeographicCoordinate(
						value, (noDecimalDigits<0) ? decimalDigitsDecimalSeconds: noDecimalDigits);
		}
			break;	
		case UTM:
		case MGRS:
			text = toString(value, noDecimalDigits);
			break;
		default:
			break;
		}
		return text;
	}
	
	// 2017-06-03
	private String formatGeographicCoordinate( double value, int noDecimalDigits) {
		if ( coordinatesType.equals( CoordinatesType.DECIMAL_DEGREES) )
			return toString( value, noDecimalDigits);
		else {
			String text;
			double absValue = Math.abs( value);
// 12335: 2017-07-11
//			text = toString( (int) absValue, 0) + "º ";
//			double minutes = (absValue - (int) absValue ) * 60.;
//			if ( coordinatesType.equals( CoordinatesType.DEGREES_DECIMAL_MINUTES) ) 
//				text += toString( minutes, noDecimalDigits) + "'";
//			else {
//				text += toString( (int) minutes, 0) + "' ";
//				double seconds = ( minutes - (int) minutes) * 60.;
//				text += toString( seconds, noDecimalDigits) + "\"";
//			}
			int degrees = (int) absValue;
			double minutesValue = (absValue - degrees) * 60.;
			if ( coordinatesType.equals( CoordinatesType.DEGREES_DECIMAL_MINUTES) ) 
				text = toString( minutesValue, noDecimalDigits) + "'";
			else {
				int minutes = (int) minutesValue;
				text = toString( (minutesValue - minutes)*60., noDecimalDigits);
				if ( text.startsWith( "60") ) {
					text = toString( 0., noDecimalDigits);
					minutes++;
				}
				if ( minutes == 60 ) {
					minutes = 0;
					degrees++;
				}
				text = toString(minutes, 0) + "' " + text + "\"";
			}
			text = toString( degrees, 0) + "º " + text;
			return text;
		}
	}
	
	// 2016-09-14: 12335
	public String toString() {
		return Messages.getMessage( "coordinatesFormatter.name");
	}
	
	// 2017-06-03: 12335
	private String toString( double value, int noDecimalDigits){
		if ( noDecimalDigits == 0 ) {
			return String.format( "%d", (int) Math.round( value));
		}
		else {
			String format = "%." + noDecimalDigits + "f";
//			System.out.println( coordinatesType.valueOf() + "  " + noDecimalDigits + "  " + format);
			return String.format( locale, format, value);
		}
	}
	
	// 2017-06-03: 12335
	private static int noOfDecimalDigits( double precision) {
		return 0;
	}
	
}
